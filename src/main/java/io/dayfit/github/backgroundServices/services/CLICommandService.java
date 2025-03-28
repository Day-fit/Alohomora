package io.dayfit.github.backgroundServices.services;

import io.dayfit.github.backgroundServices.cli.CLIHandler;
import io.dayfit.github.backgroundServices.managers.PathManager;
import org.springframework.stereotype.Service;

/**
 * Service class for executing CLI commands.
 */
@Service
public class CLICommandService
{
    private final CLIHandler handler;

    public CLICommandService(CLIHandler handler, PathManager pathManager)
    {
        this.handler = handler;
        this.handler.setPathManager(pathManager);
    }

    /**
     * Executes the given CLI command.
     *
     * @param command the CLI command to execute
     * @throws IllegalStateException if the pathManager is not set
     */
    public void executeCommand(String command, String password) throws IllegalStateException {
        if(handler == null) {
            throw new IllegalStateException("pathManager is not set");
        }

        handler.processArguments(command.split(" "), password);
    }

    /**
     * Executes the given CLI command.
     *
     * @param command the CLI command to execute
     * @throws IllegalStateException if the pathManager is not set
     */
    public void executeCommand(String command) throws IllegalStateException {
        if(handler == null) {
            throw new IllegalStateException("pathManager is not set");
        }

        handler.processArguments(command.split(" "), "");
    }
}