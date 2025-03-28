package io.dayfit.github.clientApp;

import io.dayfit.github.backgroundServices.POJOs.ServerMessage;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Main client application class for Alohomora that handles interaction
 * with background services via REST API.
 * <p>
 * This application is responsible for starting background services,
 * sending commands and receiving responses.
 * </p>
 */
public class Application {
    final static String SERVER_PING_RESPONSE = getProperty("server.ping.response");
    final static String APPLICATION_VERSION = getProperty("application.version");
    final static String SERVER_PORT = getProperty("server.port");
    final static int MAX_TIME_WAIT = Integer.parseInt(getProperty("max.wait.time.backgroundServices"));

    final static PasswordManager PASSWORD_MANAGER = new PasswordManager();

    /**
     * Main application method that processes command line arguments
     * and communicates with the background service.
     * 
     * @param args Command line arguments to process
     */
    public static void main(String[] args) {

        if (args.length <= 0) {
            System.out.println("No arguments provided. Exiting...");
            System.exit(0);
        }

        try {
            handleStartingBackgroundServices();
        } catch (InterruptedException | IOException e) {
            System.err.println("Error during starting background services " + e.getMessage());
        }

        for (String arg : args) {
            boolean waitForResponse = !arg.contains("-c");
            sendCliToBackgroundService(arg, waitForResponse);

            if (arg.contains("-c")) {
                System.out.println("Exiting...");
                System.exit(0);
            }
        }
    }

    /**
     * Sends a command to the background service and optionally waits for a response.
     * Handles authentication for secure commands (-d, -e, -c, -p).
     * 
     * @param command The command to send to the background service
     * @param waitForResponse If true, waits for and displays the service response
     */
    private static void sendCliToBackgroundService(String command, boolean waitForResponse) {
        RestTemplate restTemplate = createRestTemplateWithTimeout();
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();

        if (command.contains("-d") || command.contains("-e") || command.contains("-c") || command.contains("-p"))
        {
            requestBody.add("password", PASSWORD_MANAGER.getPassword());
        }

        requestBody.add("command", command);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(requestBody, headers);

        if (waitForResponse) {
            ServerMessage response = restTemplate.postForObject("http://localhost:" + SERVER_PORT + "/cli", request, ServerMessage.class);
            if (response != null) {
                System.out.println(response.getPrefix() + " " + response.getMessage());
            }
        } else {
            restTemplate.postForObject("http://localhost:" + SERVER_PORT + "/cli", request, Void.class);
        }
    }

    /**
     * Handles the initialization of background services.
     * Checks if the service is running and starts it if necessary.
     * 
     * @throws InterruptedException If the thread is interrupted while waiting for services
     * @throws IOException If there's an error starting the background service process
     */
    private static void handleStartingBackgroundServices() throws InterruptedException, IOException {
        String alohomoraServicesPath = "Alohomora-" + APPLICATION_VERSION + "-background.jar";

        if (isIncorrectPingResponse()) {
            System.out.println("Background services are down, trying to start them up...");

            if (!Path.of(alohomoraServicesPath).toFile().exists()) {
                System.err.println("Background services " + alohomoraServicesPath + " not found. Ensure that background service file is in same directory.");
                System.exit(1);
            }

            ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", alohomoraServicesPath);
            processBuilder.inheritIO();
            processBuilder.start();

            awaitForBackgroundServices();
        } else {
            System.out.println("Background services are up and running");
        }
    }

    /**
     * Waits for background services to become available.
     * Periodically checks if the service is responding correctly.
     * Times out after MAX_TIME_WAIT milliseconds.
     * 
     * @throws InterruptedException If the thread is interrupted while waiting
     */
    private static void awaitForBackgroundServices() throws InterruptedException {
        int timeWaited = 0;
        while (isIncorrectPingResponse()) {
            if (timeWaited > MAX_TIME_WAIT) {
                System.err.println("Could not connect to background services. Tried to connect longer than time waiting was.");
                System.exit(1);
            }

            Thread.sleep(100);
            timeWaited += 100;
        }

        System.out.println("Background services are up and running...");
    }

    /**
     * Checks if the ping response from background services is incorrect.
     * 
     * @return true if ping response is incorrect or an exception occurs,
     *         false if ping response matches expected response
     */
    private static boolean isIncorrectPingResponse() {
        try {
            RestTemplate restTemplate = createRestTemplateWithTimeout();
            String pingResponse = restTemplate.getForObject("http://localhost:" + SERVER_PORT + "/ping", String.class);

            return pingResponse == null || !pingResponse.equals(SERVER_PING_RESPONSE);
        } catch (ResourceAccessException ex) {
            return true;
        }
    }

    /**
     * Creates a RestTemplate with connection and read timeouts set.
     * 
     * @return Configured RestTemplate instance
     */
    private static RestTemplate createRestTemplateWithTimeout() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(5000);
        return new RestTemplate(factory);
    }

    /**
     * Retrieves a property value from the application.properties file.
     * 
     * @param key The property key to look up
     * @return The property value
     * @throws RuntimeException If the properties file cannot be loaded or the key isn't found
     * @throws NoSuchFileException If the properties file is not found
     */
    private static String getProperty(String key) {
        Properties props = new Properties();
        try (InputStream inputStream = Application.class.getResourceAsStream("/application.properties")) {
            if (inputStream != null) {
                props.load(inputStream);
                return props.getProperty(key);
            } else {
                throw new NoSuchFileException("Version file not found");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error loading version file", e);
        }
    }
}
