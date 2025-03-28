package io.dayfit.github.backgroundServices.components;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Component for reading configuration properties.
 */
@Component
public class ConfigurationReader {
    private Environment env;

    /**
     * Sets the environment to be used for reading properties.
     *
     * @param env the Environment object to be set
     */
    @Autowired
    public void setEnvironment(Environment env) {
        this.env = env;
    }

    /**
     * Retrieves the server ping response from the configuration properties.
     *
     * @return the server ping response as a String
     */
    public String getServerPingResponse() {
        return env.getProperty("server.ping.response");
    }
}