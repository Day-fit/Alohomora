package io.github.dayfit;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BackgroundServer {
    private final int port;
    private final CLIHandler cliHandler;
    private ServerSocket serverSocket;
    private boolean running = true;
    private final ExecutorService executorService;

    public BackgroundServer(int port, CLIHandler cliHandler) {
        this.port = port;
        this.cliHandler = cliHandler;
        this.executorService = Executors.newCachedThreadPool();
    }

    public void start() {
        executorService.submit(() -> {
            try {
                serverSocket = new ServerSocket(port);

                while (running) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        handleClient(clientSocket);
                    } catch (IOException e) {
                        if (running) {
                            System.err.println("Error accepting client connection: " + e.getMessage());
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Could not start server: " + e.getMessage());
            }
        });
    }

    private void handleClient(Socket clientSocket) {
        executorService.submit(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                String command = reader.readLine();
                if (command != null && !command.isEmpty()) {

                    if (command.equalsIgnoreCase("ping")) {
                        out.println("ALOHOMORA_SERVER");
                    } else {
                        String[] args = command.split(" (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                        cliHandler.processArguments(args);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error handling client: " + e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.err.println("Error closing client socket: " + e.getMessage());
                }
            }
        });
    }

    public void stop() {
        running = false;
        executorService.shutdown();

        if (serverSocket != null && !serverSocket.isClosed())
        {
            try
            {
                serverSocket.close();
            }catch (IOException e)
            {
                System.err.println("Error closing server socket: " + e.getMessage());
            }
        }
    }
}