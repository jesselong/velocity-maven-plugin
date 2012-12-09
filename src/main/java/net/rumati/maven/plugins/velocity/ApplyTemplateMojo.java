package net.rumati.maven.plugins.velocity;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.mappers.MapperException;
import org.apache.maven.shared.model.fileset.util.FileSetManager;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.PathTool;

import java.io.*;
import java.nio.charset.Charset;
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
     * @parameter expression="${velocity-maven-plugin.template}"
     * @required
     */
    private File template;

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

        String templateFile = readFile(template, characterSet);

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
            templateProperties.put("inputFile", inputFilename);
            templateProperties.put("inputRoot", PathTool.getRelativeFilePath(inputDirectory.getAbsolutePath(), fileSet.getDirectory()));
            templateProperties.put("outputFile", outputFilename);
            templateProperties.put("outputRoot", PathTool.getRelativeFilePath(outputDirectory.getAbsolutePath(), fileSet.getOutputDirectory()));

            applyTemplate(outputFile, templateFile, templateProperties, characterSet);
        }
    }

    @SuppressWarnings("unchecked")
    protected Map<String, String> getInputOutputMap(FileSetManager fileSetManager, FileSet fileSet) throws MojoExecutionException {
        try {
            return fileSetManager.mapIncludedFiles(fileSet);
        } catch (MapperException e) {
            throw new MojoExecutionException("Failed to map input to output", e);
        }
    }

    private String readFile(File file, Charset encoding) throws MojoExecutionException {
        try {
            return FileUtils.fileRead(file, encoding.name());
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to read " + file.getAbsolutePath(), e);
        }
    }

    protected void applyTemplate(File outputFile, String template, Properties templateProperties, Charset characterSet) throws MojoExecutionException {
        try {
            Writer writer = new OutputStreamWriter(new FileOutputStream(outputFile), characterSet);
            try {
                VelocityEngine engine = new VelocityEngine();
                engine.setProperty(VelocityEngine.RUNTIME_LOG_LOGSYSTEM, new MavenLogChute(getLog()));
                engine.init();
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
