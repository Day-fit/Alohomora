package io.dayfit.github.backgroundServices.POJOs;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents a server message with a prefix and a message.
 */
@Setter
@Getter
public class ServerMessage {
    private String prefix;
    private String message;

    public ServerMessage() {}
}