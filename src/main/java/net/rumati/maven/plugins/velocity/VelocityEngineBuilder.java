package net.rumati.maven.plugins.velocity;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.log.LogChute;

import java.util.Collections;

/**
 * Builder for VelocityEngine objects.
 */
public class VelocityEngineBuilder {

    private ExtendedProperties configuration = new ExtendedProperties();

    public VelocityEngineBuilder withLogChute(LogChute logChute) {
        configuration.setProperty(VelocityEngine.RUNTIME_LOG_LOGSYSTEM, logChute);
        return this;
    }

    private int fileLoaderCount = 0;

    public VelocityEngineBuilder withFileLoader(String... directories) {
        String loaderName = "file" + fileLoaderCount;
        fileLoaderCount ++;

        configuration.addProperty("resource.loader", loaderName);
        configuration.setProperty(loaderName + ".resource.loader.class", "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
        for (String directory : directories) {
            configuration.addProperty(loaderName + ".resource.loader.path", directory);
        }
        return this;
    }

    public VelocityEngineBuilder withClasspathLoader() {
        if (hasResourceLoader("classpath")) {
            return this;
        }

        configuration.addProperty("resource.loader", "classpath");
        configuration.setProperty("classpath.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        return this;
    }

    public VelocityEngineBuilder withFallbackLoader() {
        if (hasResourceLoader("fallback")) {
            return this;
        }

        configuration.addProperty("resource.loader", "fallback");
        configuration.setProperty("fallback.resource.loader.class", "net.rumati.maven.plugins.velocity.MissingResourceLoader");
        return this;
    }

    private boolean hasResourceLoader(String resourceLoader) {
        return configuration.getList("resource.loader", Collections.emptyList()).contains(resourceLoader);
    }

    public VelocityEngine build() {
        VelocityEngine engine = new VelocityEngine();
        engine.setExtendedProperties(configuration);
        engine.init();
        return engine;
    }

}
