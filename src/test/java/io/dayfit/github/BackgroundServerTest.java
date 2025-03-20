package io.dayfit.github;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BackgroundServerTest {
    private BackgroundServer server;
    private CLIHandler cliHandler;
    private int testPort;

    @BeforeEach
    void setUp() {
        // Use a different port for each test to avoid "address already in use" errors
        testPort = 8760 + (int)(Math.random() * 100);
        cliHandler = mock(CLIHandler.class);
        server = new BackgroundServer(testPort, cliHandler);
        server.start();
        // Small delay to ensure server is started
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop();
            // Give time for resources to be released
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Test
    void serverRespondsWithAlohomoraServerToPing() throws Exception {
        String response = sendCommand("ping");
        assertEquals("ALOHOMORA_SERVER", response);
    }

    @Test
    void nonPingCommandsAreForwardedToCLIHandler() throws Exception {
        sendCommand("add path/to/file");
        verify(cliHandler).processArguments(new String[]{"add", "path/to/file"});
    }

    @Test
    void multipleClientsCanConnectSimultaneously() throws Exception {
        int clientCount = 5;
        CountDownLatch latch = new CountDownLatch(clientCount);
        AtomicReference<Exception> threadException = new AtomicReference<>();

        for (int i = 0; i < clientCount; i++) {
            new Thread(() -> {
                try {
                    String response = sendCommand("ping");
                    assertEquals("ALOHOMORA_SERVER", response);
                    latch.countDown();
                } catch (Exception e) {
                    threadException.set(e);
                    latch.countDown(); // Ensure countdown happens even on exception
                }
            }).start();
            // Small delay to avoid overwhelming the server
            Thread.sleep(50);
        }

        assertTrue(latch.await(10, TimeUnit.SECONDS), "Not all clients completed successfully");
        if (threadException.get() != null) {
            fail("Exception in client thread: " + threadException.get().getMessage());
        }
    }

    @Test
    void serverHandlesEmptyCommand() throws Exception {
        // Empty command should be ignored
        sendCommandWithoutResponse();
        verify(cliHandler, never()).processArguments(any());
    }

    @Test
    void cannotConnectAfterServerStopped() throws Exception {
        server.stop();
        // Ensure server is fully stopped
        Thread.sleep(500);

        try (Socket socket = new Socket("localhost", testPort)) {
            socket.setSoTimeout(500); // Set timeout to avoid test hanging
            fail("Should not be able to connect to stopped server");
        } catch (Exception e) {
            // Expected exception
            assertTrue(true);
        }
    }

    @Test
    @Timeout(5)
    void serverStopsGracefully() {
        server.stop();
    }

    private String sendCommand(String command) throws Exception {
        try (Socket socket = new Socket("localhost", testPort)) {
            socket.setSoTimeout(2000);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.println(command);
            return in.readLine();
        }
    }

    private void sendCommandWithoutResponse() throws Exception {
        try (Socket socket = new Socket("localhost", testPort)) {
            socket.setSoTimeout(2000);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println("");
            // Give server time to process
            Thread.sleep(100);
        }
    }
}