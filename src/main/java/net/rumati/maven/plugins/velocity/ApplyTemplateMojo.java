package net.rumati.maven.plugins.velocity;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.PathTool;

import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Apply a template to a set of files.
 *
 * @goal apply-template
 * @phase process-resources
 */
public class ApplyTemplateMojo extends AbstractMojo {

    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    /**
     * Template path
     * @parameter expression="${velocity-maven-plugin.templateDir}"
     */
    private File templateDirectory;

    /**
     * Template path
     * @parameter expression="${velocity-maven-plugin.template}"
     * @required
     */
    private String template;

    /**
     * File set
     * @parameter expression="${velocity-maven-plugin.fileSet}"
     * @required
     */
    private FileSet fileSet;

    /**
     * Properties passed to Velocity context.
     * @parameter expression="${velocity-maven-plugin.properties}"
     */
    private Properties properties;

    /**
     * The character set encoding to be used when reading and writing files.
     * If this is not set, then {@code project.build.sourceEncoding} is used.
     * If {@code project.build.sourceEncoding} is also not set, then the default
     * character set encoding is used.
     * @parameter expression="${velocity-maven-plugin.encoding}"
     */
    private String encoding;

    public void execute() throws MojoExecutionException, MojoFailureException {

        if (encoding == null) {
            encoding = project.getProperties().getProperty("project.build.sourceEncoding");
        }

        Charset characterSet;
        if (encoding == null) {
            getLog().warn("Using default character set encoding");
            characterSet = Charset.defaultCharset();
        } else {
            characterSet = Charset.forName(encoding);
        }

        getLog().debug("Using character set: " + characterSet.displayName());

        VelocityEngineBuilder engineBuilder = new VelocityEngineBuilder().
                withLogChute(new MavenLogChute(getLog()));

        String velocityMacro;
        try {
            InputStream templateStream = getClass().getResourceAsStream(template);
            if (templateStream == null) {
                getLog().debug("Could not find a resource called " + template + ", trying as a file name");
                templateStream = new FileInputStream(template);
            } else {
                engineBuilder.withClasspathLoader();
                getLog().debug("Using resource called " + template);
            }
            velocityMacro = IOUtil.toString(templateStream, characterSet.name());
        } catch (IOException ioe) {
            throw new MojoExecutionException("Failed to read " + template, ioe);
        }

        engineBuilder.withFileLoader(fileSet.getDirectory());
        if (templateDirectory != null) {
            engineBuilder.withFileLoader(templateDirectory.getAbsolutePath());
        }
        engineBuilder.withFallbackLoader();

        VelocityEngine engine = engineBuilder.build();
        FileSetManager fileSetManager = new FileSetManager(getLog());
        for (Map.Entry<String, String> inputOutput : getInputOutputMap(fileSetManager, fileSet).entrySet()) {
            String inputFilename = inputOutput.getKey();
            File inputFile = new File(fileSet.getDirectory(), inputFilename);
            File inputDirectory = inputFile.getParentFile();

            String outputFilename = inputOutput.getValue();
            File outputFile = new File(fileSet.getOutputDirectory(), outputFilename);

            // Create output directory for outputFile, if required
            File outputDirectory = outputFile.getParentFile();
            if (!outputDirectory.isDirectory() && !outputDirectory.mkdirs()) {
                throw new MojoExecutionException("Failed to create " + outputDirectory.getAbsolutePath());
            }

            Properties templateProperties = new Properties(properties);
            templateProperties.put("content", readFile(inputFile, characterSet));
            templateProperties.put("inputPath", getRelativePath(fileSet.getDirectory(), inputDirectory.getAbsolutePath()));
            templateProperties.put("inputFile", inputFilename);
            templateProperties.put("outputPath", getRelativePath(fileSet.getOutputDirectory(), outputDirectory.getAbsolutePath()));
            templateProperties.put("outputFile", outputFilename);
            templateProperties.put("relativePath", getRelativePath(outputDirectory.getAbsolutePath(), fileSet.getOutputDirectory()));

            templateProperties.put("project", project);
            templateProperties.put("system", System.getProperties());
            templateProperties.put("env", System.getenv());

            applyTemplate(engine, outputFile, velocityMacro, templateProperties, characterSet);
        }
    }

    @SuppressWarnings("unchecked")
    protected Map<String, String> getInputOutputMap(FileSetManager fileSetManager, FileSet fileSet) throws MojoExecutionException {
        Map<String, String> mappedFiles = new HashMap<String, String>();

        String[] includedFiles = fileSetManager.getIncludedFiles(fileSet);
        for (String includedFile : includedFiles) {
            // Use simple identity mapping
            mappedFiles.put(includedFile, includedFile);
        }

        return mappedFiles;
    }

    protected String getRelativePath(String oldPath, String newPath) {
        String path = PathTool.getRelativeFilePath(oldPath, newPath);
        if (path.isEmpty()) {
            // Ensure path is never empty
            return ".";
        }
        return path;
    }

    private String readFile(File file, Charset encoding) throws MojoExecutionException {
        try {
            return FileUtils.fileRead(file, encoding.name());
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to read " + file.getAbsolutePath(), e);
        }
    }

    protected void applyTemplate(VelocityEngine engine, File outputFile, String template, Properties templateProperties, Charset characterSet) throws MojoExecutionException {
        try {
            Writer writer = new OutputStreamWriter(new FileOutputStream(outputFile), characterSet);
            try {
                VelocityContext ctx = new VelocityContext(templateProperties);
                if (!engine.evaluate(ctx, writer, "velocity-maven-plugin", template)) {
                    throw new MojoExecutionException("Failed to apply template");
                }
            } finally {
                writer.close();
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to apply template", e);
        }
    }

}
