package net.rumati.maven.plugins.velocity;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Fallback ResourceLoader that logs resource requests and returns all resources as empty (to suppress
 * ResourceNotFoundException exceptions)
 */
public class MissingResourceLoader extends ResourceLoader {

    @Override
    public void init(ExtendedProperties configuration) {
    }

    @Override
    public InputStream getResourceStream(String source) throws ResourceNotFoundException {
        log.warn("Failed to find " + source);
        return new ByteArrayInputStream(new byte[0]);
    }

    @Override
    public boolean isSourceModified(Resource resource) {
        return false;
    }

    @Override
    public long getLastModified(Resource resource) {
        return 0;
    }

}
