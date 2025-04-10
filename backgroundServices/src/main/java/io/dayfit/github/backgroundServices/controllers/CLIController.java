package io.dayfit.github.backgroundServices.controllers;

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

import java.util.HashMap;

/**
 * Controller for handling CLI commands and ping requests.
 */
@Controller
public class CLIController {
    private final CLICommandService cliCommandService;
    private final ConfigurationReader configurationReader;

    /**
     * Constructor for CLIController.
     *
     * @param cliCommandService the service to execute CLI commands
     */
    @Autowired
    public CLIController(CLICommandService cliCommandService, ConfigurationReader configurationReader) {
        this.cliCommandService = cliCommandService;
        this.configurationReader = configurationReader;
    }

    @PostMapping("/cli")
    @ResponseBody
    public HashMap<String, String> cli(@RequestParam String command, @RequestParam(required = false) String password) {
        try {
            if (password != null && !password.isEmpty()) {
                cliCommandService.executeCommand(command, password);
            } else {
                cliCommandService.executeCommand(command);
            }

            return new HashMap<>() {{
                put("status", "success");
                put("message", "Command executed successfully.");
            }};

        } catch (Exception ex) {
            return new HashMap<>() {{
               put("status", "error");
               put("message", ex.getMessage());
            }};
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