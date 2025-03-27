package io.dayfit.github.backgroundServices.controllers;

import io.dayfit.github.backgroundServices.POJOs.ServerMessage;
import io.dayfit.github.backgroundServices.components.ConfigurationReader;
import io.dayfit.github.backgroundServices.services.CLICommandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller for handling CLI commands and ping requests.
 */
@Controller
public class CLIController {
    private final CLICommandService cliCommandService;
    private final ConfigurationReader configurationReader;
    private final ServerMessage serverMessage;

    /**
     * Constructor for CLIController.
     *
     * @param cliCommandService the service to execute CLI commands
     */
    @Autowired
    public CLIController(CLICommandService cliCommandService, ConfigurationReader configurationReader, ServerMessage serverMessage) {
        this.serverMessage = serverMessage;
        this.cliCommandService = cliCommandService;
        this.configurationReader = configurationReader;
    }

    /**
     * Endpoint to execute a CLI command.
     *
     * @param command the CLI command to execute
     *
     * @return a message indicating the result of the command execution
     */
    @PostMapping("/cli")
    @ResponseBody
    public ServerMessage cli(@RequestParam String command) {
        try {
            cliCommandService.executeCommand(command);
            serverMessage.setPrefix("[Success]: ");

            return serverMessage;
        } catch (Exception ex) {
            serverMessage.setPrefix("[Error]: ");
            serverMessage.setMessage("Exception occurred while executing command: " + ex.getMessage());

            return serverMessage;
        }
    }

    /**
     * Endpoint to check the server status.
     *
     * @return a string indicating the server status
     */
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return new ResponseEntity<>(configurationReader.getServerPingResponse(), HttpStatus.OK);
    }
}