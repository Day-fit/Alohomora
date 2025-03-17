package io.github.dayfit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Application {
    final static PathManager pathManager = new PathManager();
    final static CLIHandler cliHandler = new CLIHandler(pathManager);
    static BackgroundServer server;
    static boolean isClosed = false;

    public static void main(String[] args) throws IOException {
        final int DEFAULT_PORT = 8765;

        Runtime.getRuntime().addShutdownHook(new Thread(() ->
        {
            isClosed = true;

            try {
                if (!pathManager.getProtectedPaths().isEmpty()) {
                    System.out.println("Saving protected paths...");
                    pathManager.saveProtectedPaths();
                }

                if (server != null) {
                    System.out.println("Stopping background services...");
                    server.stop();
                }

                pathManager.encryptProtectedPaths(cliHandler.getProtectedPathsPassword());

                System.out.println("Alohomora closed successfully");
            } catch (Exception e) {
                System.out.println("An error occured: " + e.getMessage());
            }
        }
        ));

        try {
            final Path PROTECTED_PATHS_PATH = Path.of(pathManager.PATH_MANAGER_FILE);

            //To avoid unchecked warning we use pathsRaw List
            if (Files.size(PROTECTED_PATHS_PATH) > 0) {
                HashSet<?> pathsRaw = JSON.fromJSON(String.join("", Files.readAllLines(PROTECTED_PATHS_PATH)), HashSet.class);
                HashSet<String> protectedPaths = pathsRaw.stream()
                        .map(Object::toString)
                        .collect(Collectors.toCollection(HashSet::new));
                pathManager.setProtectedPaths(protectedPaths);
            }
        }catch (NoSuchFileException e) {
            System.out.println("[WARNING]: No protected paths file has been found");
        }

        if (Arrays.stream(args).noneMatch(arg -> arg.equals("-h"))) {
            System.out.println("Starting interactive mode...");
            server = new BackgroundServer(DEFAULT_PORT, cliHandler);
            server.start();
        }

        //Execute arguments that app was executed with
        cliHandler.processArguments(args);

        Scanner scanner = new Scanner(System.in);
        while(!isClosed)
        {
            System.out.print("> ");
            String line = scanner.nextLine();

            try (Socket socket = new Socket("localhost", DEFAULT_PORT);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                out.println(line);

                String response = in.readLine();

                if (response != null) {
                    System.out.println("Alohomora: " + response);
                }
            } catch (IOException e) {
                System.err.println("Failed to communicate with the server: " + e.getMessage());
                System.exit(1);
            }
        }
    }
}