package net.rumati.maven.plugins.velocity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Properties;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.apache.velocity.runtime.resource.loader.FileResourceLoader;

/**
 * Processes a Velocity template
 *
 * @goal velocity
 * @requiresDependencyResolution runtime
 * @requiresDependencyResolution test
 */
public class VelocityMojo
        extends AbstractMojo
{
    /**
     * @parameter property="project"
     * @required
     * @readonly
     */
    protected MavenProject project;
    /**
     * The template. This can be either a path to a file or a path to a resource somewhere in the classpath.
     *
     * @parameter property="velocity-maven-plugin.template"
     */
    private String template;
    /**
     * Output file
     *
     * @parameter property="velocity-maven-plugin.outputFile"
     */
    private File outputFile;
    /**
     * The character set encoding to be used when reading and writing files. If this is not set, then
     * {@code project.build.sourceEncoding} is used. If {@code project.build.sourceEncoding} is also not set, then the default
     * character set encoding is used.
     *
     * @parameter property="velocity-maven-plugin.encoding"
     */
    private String encoding;
    /**
     * A list of transformations. Each entry contains a {@code template} and {@code outputFile} to be processed.
     *
     * @parameter
     */
    private Transformation[] transformations;

    public void execute()
            throws MojoExecutionException
    {
        Charset characterSet = null;

        if (encoding == null){
            encoding = project.getProperties().getProperty("project.build.sourceEncoding");
        }

        if (encoding == null){
            getLog().warn("Using default character set encoding");
            characterSet = Charset.defaultCharset();
        }else{
            characterSet = Charset.forName(encoding);
        }

        getLog().debug("Using character set: " + characterSet.displayName());

        if (template != null && outputFile != null){
            Transformation t = new Transformation();
            t.outputFile = outputFile;
            t.template = template;
            transform(t, characterSet);
        }

        if (transformations != null){
            for (Transformation t : transformations){
                if (t.outputFile != null && t.template != null){
                    transform(t, characterSet);
                }
            }
        }
    }

    private void transform(Transformation transformation, Charset characterSet)
            throws MojoExecutionException
    {
        File parentDirectory = transformation.outputFile.getParentFile();
        if (!parentDirectory.isDirectory() && !parentDirectory.mkdirs()){
            throw new MojoExecutionException("Error creating output directory: " + parentDirectory.getAbsolutePath());
        }

        try {
            File fileResourceDirectory = null;
            InputStream templateStream;
            templateStream = this.getClass().getResourceAsStream(transformation.template);
            if (templateStream == null){
                getLog().debug("Could not find a resource called " + transformation.template + ", trying as a file name");
                templateStream = new FileInputStream(transformation.template);
                fileResourceDirectory = new File(transformation.template).getAbsoluteFile().getParentFile();
            }else{
                getLog().debug("Using resource called " + transformation.template);
            }
            Reader reader = new InputStreamReader(templateStream, characterSet);
            try {
                Writer writer = new OutputStreamWriter(new FileOutputStream(transformation.outputFile), characterSet);
                try {
                    VelocityEngine engine = new VelocityEngine();
                    Properties engineConfig = new Properties();
                    engineConfig.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
                    if (fileResourceDirectory == null){
                        engineConfig.setProperty("resource.loader", "classpath");
                    }else{
                        engineConfig.setProperty("resource.loader", "classpath,file");
                        engineConfig.setProperty("file.resource.loader.class", FileResourceLoader.class.getName());
                        engineConfig.setProperty("file.resource.loader.path", fileResourceDirectory.getAbsolutePath());
                    }
                    engine.setProperty(VelocityEngine.RUNTIME_LOG_LOGSYSTEM, new MavenLogChute(getLog()));
                    engine.init(engineConfig);
                    VelocityContext ctx = new VelocityContext();
                    ctx.put("project", project);
                    ctx.put("system", System.getProperties());
                    ctx.put("env", System.getenv());
                    engine.evaluate(ctx, writer, "velocity-maven-plugin", reader);
                }finally{
                    writer.close();
                }
            }finally{
                reader.close();
            }
        }catch (Exception e){
            throw new MojoExecutionException("Error processing template", e);
        }
    }
}
