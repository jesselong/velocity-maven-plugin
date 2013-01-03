package net.rumati.maven.plugins.velocity;

import org.apache.maven.plugin.logging.Log;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogChute;

public class MavenLogChute
        implements LogChute
{
    private final Log mavenLogger;

    public MavenLogChute(Log mavenLogger)
    {
        this.mavenLogger = mavenLogger;
    }

    public void init(RuntimeServices rs)
            throws Exception
    {
        /*
         * do nothing
         */
    }

    public void log(int i, String string)
    {
        switch (i){
            case LogChute.DEBUG_ID:
                mavenLogger.debug(string);
                break;
            case LogChute.INFO_ID:
                mavenLogger.info(string);
                break;
            case LogChute.WARN_ID:
                mavenLogger.warn(string);
                break;
            case LogChute.ERROR_ID:
                mavenLogger.error(string);
                break;
        }
    }

    public void log(int i, String string, Throwable thrwbl)
    {
        switch (i){
            case LogChute.DEBUG_ID:
                mavenLogger.debug(string, thrwbl);
                break;
            case LogChute.INFO_ID:
                mavenLogger.info(string, thrwbl);
                break;
            case LogChute.WARN_ID:
                mavenLogger.warn(string, thrwbl);
                break;
            case LogChute.ERROR_ID:
                mavenLogger.error(string, thrwbl);
                break;
        }
    }

    public boolean isLevelEnabled(int i)
    {
        switch(i){
            case LogChute.TRACE_ID:
                return false;
            case LogChute.DEBUG_ID:
                return mavenLogger.isDebugEnabled();
            case LogChute.INFO_ID:
                return mavenLogger.isInfoEnabled();
            case LogChute.WARN_ID:
                return mavenLogger.isWarnEnabled();
            case LogChute.ERROR_ID:
                return mavenLogger.isErrorEnabled();
            default:
                throw new IllegalArgumentException("Invalid level id: " + i);
        }
    }
}
