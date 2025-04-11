package io.dayfit.github.clientApp;

import io.dayfit.github.shared.JSON;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.HashMap;
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
    final static String URL_ADDRESS = "http://localhost:"+SERVER_PORT;

    /**
     * Main application method that processes command line arguments
     * and communicates with the background service.
     * 
     * @param args Command line arguments to process
     */
    public static void main(String[] args) {

        if (args.length == 0) {
            System.out.println("No arguments provided. Exiting...");
            System.exit(0);
        }

        try {
            handleStartingBackgroundServices();

            for (String arg : args) {
                sendCliToBackgroundService(arg);

                if (arg.contains("-c")) {
                    System.out.println("Exiting...");
                    System.exit(0);
                }
            }
        } catch (InterruptedException | IOException e) {
            System.err.println("Error during starting background services " + e.getMessage());
        }
    }

    /**
     * Sends a command to the background service and optionally waits for a response.
     * Handles authentication for secure commands (-d, -e, -c, -p).
     * 
     * @param command The command to send to the background service
     */
    private static void sendCliToBackgroundService(String command) throws IOException
    {
        URL url = new URL(URL_ADDRESS+"/cli");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);

        OutputStream os = connection.getOutputStream();
        String requestBody = "command=" + URLEncoder.encode(command, StandardCharsets.UTF_8);

        if (command.contains("-d") || command.contains("-e") || command.contains("-c") || command.contains("-p")) {
            String password = PASSWORD_MANAGER.getPassword();
            requestBody += "&password=" + password;
        }

        os.write(requestBody.getBytes(StandardCharsets.UTF_8));

        os.flush();
        os.close();

        int responseCode = connection.getResponseCode();
        InputStream is = responseCode < 400 ? connection.getInputStream() : connection.getErrorStream();

        String response = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        HashMap<?, ?> responseMessage = JSON.fromJSON(response, HashMap.class);

        if (responseMessage != null) {
            System.out.println(responseMessage.get("status") + " " + responseMessage.get("message"));
        } else {
            System.err.println("Error: Response received is null");
        }
        connection.disconnect();
    }

    /**
     * Handles the initialization of background services.
     * Checks if the service is running and starts it if necessary.
     * 
     * @throws InterruptedException If the thread is interrupted while waiting for services
     * @throws IOException If there's an error starting the background service process
     */
    private static void handleStartingBackgroundServices() throws InterruptedException, IOException {
        String alohomoraServicesPath = "Alohomora-background-"+APPLICATION_VERSION+".jar";

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
            URL pingEndpoint = new URL(URL_ADDRESS + "/ping");
            HttpURLConnection connection = (HttpURLConnection) pingEndpoint.openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);

            connection.connect();
            InputStream is = connection.getInputStream();
            String pingResponse = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            connection.disconnect();

            return !pingResponse.equals(SERVER_PING_RESPONSE);
        } catch (IOException ex) {
            return true;
        }
    }

    /**
     * Retrieves a property value from the application.properties file.
     *
     * @param key The property key to look up
     * @return The property value
     */
    private static String getProperty(String key) throws RuntimeException{
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
