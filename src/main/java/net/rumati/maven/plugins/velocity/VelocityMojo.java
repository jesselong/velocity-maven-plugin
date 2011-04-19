package net.rumati.maven.plugins.velocity;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Map;
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
     * Template file
     * @parameter
     * @required
     */
    private File template;

    /**
     * Output file
     * @parameter
     * @required
     */
    private File outputFile;

    /**
     * Properties to populate in the template
     * @parameter
     * @required
     */
    private Map<Object, Object> properties;

    public void execute()
        throws MojoExecutionException
    {
        File parentDirectory = outputFile.getParentFile();
        if (!parentDirectory.isDirectory() && !parentDirectory.mkdirs()){
            throw new MojoExecutionException("Error creating output directory: " + parentDirectory.getAbsolutePath());
        }
        
        try {
            Reader reader = new InputStreamReader(new FileInputStream(template), Charset.forName("UTF-8"));
            try {
                Writer writer = new OutputStreamWriter(new FileOutputStream(outputFile), Charset.forName("UTF-8"));
                try {
                    VelocityEngine engine = new VelocityEngine();
                    engine.setProperty(VelocityEngine.RUNTIME_LOG, this);
                    engine.init();
                    VelocityContext ctx = new VelocityContext();
                    for (Map.Entry<Object, Object> e : properties.entrySet()){
                        ctx.put(e.getKey().toString(), e.getValue());
                    }
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
