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
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

/**
 * Processes a Velocity template
 *
 * @goal velocity
 * @phase process-resources
 */
public class VelocityMojo
    extends AbstractMojo
{
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
    private String template;

    /**
     * Output file
     * @parameter expression="${velocity-maven-plugin.outputFile}"
     * @required
     */
    private File outputFile;
    
    /**
     * The character set encoding to be used when reading and writing files.
     * If this is not set, then {@code project.build.sourceEncoding} is used.
     * If {@code project.build.sourceEncoding} is also not set, then the default
     * character set encoding is used.
     * @parameter expression="${velocity-maven-plugin.encoding}"
     */
    private String encoding;

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
        
        File parentDirectory = outputFile.getParentFile();
        if (!parentDirectory.isDirectory() && !parentDirectory.mkdirs()){
            throw new MojoExecutionException("Error creating output directory: " + parentDirectory.getAbsolutePath());
        }
        
        try {
            InputStream templateStream;
            templateStream = this.getClass().getResourceAsStream(template);
            if (templateStream == null) {
                getLog().debug("Could not find a resource called " + template + ", trying as a file name");
                templateStream = new FileInputStream(template);
            } else {
                getLog().debug("Using resource called " + template);
            }
            Reader reader = new InputStreamReader(templateStream, characterSet);
            try {
                Writer writer = new OutputStreamWriter(new FileOutputStream(outputFile), characterSet);
                try {
                    VelocityEngine engine = new VelocityEngine();
                    engine.setProperty(VelocityEngine.RUNTIME_LOG_LOGSYSTEM, new MavenLogChute(getLog()));
                    engine.init();
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
