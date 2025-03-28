package io.dayfit.github.backgroundServices.configurations;

import io.dayfit.github.backgroundServices.POJOs.ServerMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for the background application.
 */
@Configuration
public class BackgroundApplicationConfiguration {

    /**
     * Creates a new ServerMessage bean.
     *
     * @return a new instance of ServerMessage
     */
    @Bean
    ServerMessage serverMessage() {
        return new ServerMessage();
    }
}