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
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class Application {
    final static String SERVER_PING_RESPONSE = getProperty("server.ping.response");
    final static String APPLICATION_VERSION = getProperty("application.version");
    final static String SERVER_PORT = getProperty("server.port");
    final static int MAX_TIME_WAIT = Integer.parseInt(getProperty("max.wait.time.backgroundServices"));

    final static PasswordManager PASSWORD_MANAGER = new PasswordManager();

    public static void main(String[] args) {
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

            Process process = processBuilder.start();
            boolean finished = process.waitFor(10, TimeUnit.SECONDS);
            if (!finished) {
                System.err.println("Background services did not start in time.");
                process.destroy();
                System.exit(1);
            }

            awaitForBackgroundServices();
        } else {
            System.out.println("Background services are up and running");
        }
    }

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

    private static boolean isIncorrectPingResponse() {
        try {
            RestTemplate restTemplate = createRestTemplateWithTimeout();
            String pingResponse = restTemplate.getForObject("http://localhost:" + SERVER_PORT + "/ping", String.class);

            return pingResponse == null || !pingResponse.equals(SERVER_PING_RESPONSE);
        } catch (ResourceAccessException ex) {
            return true;
        }
    }

    private static RestTemplate createRestTemplateWithTimeout() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(5000);
        return new RestTemplate(factory);
    }

    private static String getProperty(String key) {
        Properties props = new Properties();
        try (InputStream inputStream = Application.class.getResourceAsStream("/application.properties")) {
            if (inputStream != null) {
                props.load(inputStream);
                return props.getProperty(key);
            } else {
                throw new RuntimeException("Version file not found");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error loading version file", e);
        }
    }
}
