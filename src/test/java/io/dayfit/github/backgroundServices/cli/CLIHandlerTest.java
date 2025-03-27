package io.dayfit.github.backgroundServices.cli;

import io.dayfit.github.backgroundServices.POJOs.ServerMessage;
import io.dayfit.github.backgroundServices.managers.PathManager;
import io.dayfit.github.backgroundServices.managers.ShutdownManager;
import io.dayfit.github.backgroundServices.utils.Encryptor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CLIHandlerTest {

    @Configuration
    static class CLIHandlerTestConfiguration {
        @Bean
        public PathManager pathManager() {
            return new PathManager();
        }

        @Bean
        public ShutdownManager shutdownManager(ApplicationContext applicationContext) {
            return new ShutdownManager(applicationContext);
        }
    }

    @Autowired
    private PathManager pathManager;

    @Autowired
    private ShutdownManager shutdownManager;

    private ServerMessage serverMessage;
    private final String TEST_PASSWORD = "testPassword";
    private InputStream originalSystemIn;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        serverMessage = new ServerMessage();
        originalSystemIn = System.in;
    }

    @AfterEach
    void tearDown() {
        System.setIn(originalSystemIn);
    }

    @Test
    void CLIHandlerWithValidEncryptFileArgumentEncryptsFile() throws Exception {
        String testData = "this is a test data";

        Path tempFile = tempDir.resolve("encryptTest.txt");
        Files.writeString(tempFile, testData);
        assertArrayEquals(testData.getBytes(), Files.readAllBytes(tempFile));

        String[] args = {"-e=" + tempFile};
        simulateUserInput(TEST_PASSWORD);

        new CLIHandler(args, pathManager, serverMessage, shutdownManager, TEST_PASSWORD);

        assertFalse(Arrays.equals(testData.getBytes(), Files.readAllBytes(tempFile)),
                "File content should be encrypted");
    }

    @Test
    void CLIHandlerWithValidDecryptFileArgumentDecryptsFile() throws Exception {
        String testData = "this is a test data";

        Path tempFile = tempDir.resolve("decryptTest.txt");
        Files.writeString(tempFile, testData);

        // Encrypt the file first
        Encryptor.encrypt(tempFile.toFile(), TEST_PASSWORD);
        assertFalse(Arrays.equals(testData.getBytes(), Files.readAllBytes(tempFile)),
                "File should be encrypted");

        // Decrypt with CLI
        String[] args = {"-d=" + tempFile};
        simulateUserInput(TEST_PASSWORD);

        new CLIHandler(args, pathManager, serverMessage, shutdownManager, TEST_PASSWORD);

        assertArrayEquals(testData.getBytes(), Files.readAllBytes(tempFile),
                "File should be decrypted back to original content");
    }

    @Test
    void CLIHandlerWithValidEncryptDirectoryArgumentEncryptsDirectory() throws Exception {
        Path testDir = tempDir.resolve("encryptDir");
        Files.createDirectory(testDir);

        Path file1 = testDir.resolve("file1.txt");
        Path file2 = testDir.resolve("file2.txt");

        String inputData1 = "Here is first test text";
        String inputData2 = "And here is second test text";

        Files.writeString(file1, inputData1);
        Files.writeString(file2, inputData2);

        String[] args = {"-e=" + testDir};
        simulateUserInput(TEST_PASSWORD);

        new CLIHandler(args, pathManager, serverMessage, shutdownManager, TEST_PASSWORD);

        assertTrue(Files.exists(file1), "File should still exist");
        assertTrue(Files.exists(file2), "File should still exist");

        assertFalse(Arrays.equals(inputData1.getBytes(), Files.readAllBytes(file1)),
                "File content should be encrypted");
        assertFalse(Arrays.equals(inputData2.getBytes(), Files.readAllBytes(file2)),
                "File content should be encrypted");
    }

    @Test
    void CLIHandlerWithValidDecryptDirectoryArgumentDecryptsDirectory() throws Exception {
        Path testDir = tempDir.resolve("decryptDir");
        Files.createDirectory(testDir);

        Path file1 = testDir.resolve("file1.txt");
        Path file2 = testDir.resolve("file2.txt");

        String inputData1 = "Here is first test text";
        String inputData2 = "And here is second test text";

        Files.writeString(file1, inputData1);
        Files.writeString(file2, inputData2);

        // Encrypt directory first
        Encryptor.encryptDirectory(testDir.toFile(), TEST_PASSWORD);

        assertFalse(Arrays.equals(inputData1.getBytes(), Files.readAllBytes(file1)),
                "File content should be encrypted");

        // Decrypt with CLI
        String[] args = {"-d=" + testDir};
        simulateUserInput(TEST_PASSWORD);

        new CLIHandler(args, pathManager, serverMessage, shutdownManager, TEST_PASSWORD);

        assertArrayEquals(inputData1.getBytes(), Files.readAllBytes(file1),
                "File should be decrypted back to original content");
        assertArrayEquals(inputData2.getBytes(), Files.readAllBytes(file2),
                "File should be decrypted back to original content");
    }

    @Test
    void CLIHandlerWithHelpArgumentDisplaysHelpMessage() {
        String[] args = {"-h"};

        CLIHandler cliHandler = new CLIHandler(args, pathManager, serverMessage, shutdownManager, TEST_PASSWORD);

        assertEquals(cliHandler.HELP_TEXT, serverMessage.getMessage(),
                "Help message should be set in server message");
    }

    @Test
    void CLIHandlerWithInvalidArgumentDisplaysErrorMessage() {
        String[] args = {"-x=invalid"};

        CLIHandler cliHandler = new CLIHandler(args, pathManager, serverMessage, shutdownManager, TEST_PASSWORD);

        assertEquals("[Error]: " + cliHandler.NO_SUCH_ARGUMENT + " [argument=" + args[0] + "]",
                serverMessage.getPrefix() + serverMessage.getMessage(),
                "Error message should indicate invalid argument");
    }

    @Test
    void CLIHandlerWithNonExistentFileThrowsFileNotFoundException() {
        String[] args = {"-e=nonExistentFile.txt"};

        new CLIHandler(args, pathManager, serverMessage, shutdownManager, TEST_PASSWORD);

        assertTrue(serverMessage.getMessage().contains("File not found"),
                "Error message should indicate file not found");
    }

    @Test
    void CLIHandlerWithAddProtectedPathArgumentAddsPath() throws IOException {
        Path testPath = tempDir.resolve("protectedPath.txt");
        Files.createFile(testPath);

        String[] args = {"-a=" + testPath};

        new CLIHandler(args, pathManager, serverMessage, shutdownManager, TEST_PASSWORD);

        assertTrue(pathManager.getProtectedPaths().contains(testPath.toString()),
                "Path should be added to protected paths");
        assertTrue(serverMessage.getMessage().contains("Successfully added protected path"),
                "Success message should be shown");
    }

    @Test
    void CLIHandlerWithRemoveProtectedPathArgumentRemovesPath() throws IOException {
        Path testPath = tempDir.resolve("toRemove.txt");
        Files.createFile(testPath);

        pathManager.addProtectedPath(testPath.toString());
        assertTrue(pathManager.getProtectedPaths().contains(testPath.toString()),
                "Path should be in protected paths");

        String[] args = {"-r=" + testPath};

        new CLIHandler(args, pathManager, serverMessage, shutdownManager, TEST_PASSWORD);

        assertFalse(pathManager.getProtectedPaths().contains(testPath.toString()),
                "Path should be removed from protected paths");
        assertTrue(serverMessage.getMessage().contains("Successfully removed protected path"),
                "Success message should be shown");
    }

    @Test
    void CLIHandlerWithShowProtectedPathArgumentShowsPath() {
        String testPath = "protectedPathTest";
        pathManager.addProtectedPath(testPath);

        String[] args = {"-vp"};

        new CLIHandler(args, pathManager, serverMessage, shutdownManager, TEST_PASSWORD);

        assertEquals(pathManager.getProtectedPaths().toString(), serverMessage.getMessage(),
                "Protected paths should be displayed in server message");
    }

    @Test
    void CLIHandlerWithValidProtectedPathsArgumentDecryptsPaths() throws Exception {
        Path file1 = tempDir.resolve("protectedFile1.txt");
        Path file2 = tempDir.resolve("protectedFile2.txt");

        Files.writeString(file1, "Here is first test text");
        Files.writeString(file2, "And here is second test text");

        pathManager.addProtectedPath(file1.toString());
        pathManager.addProtectedPath(file2.toString());

        // Encrypt files individually
        Encryptor.encrypt(file1.toFile(), TEST_PASSWORD);
        Encryptor.encrypt(file2.toFile(), TEST_PASSWORD);

        // Decrypt protected paths
        String[] args = {"-p"};

        new CLIHandler(args, pathManager, serverMessage, shutdownManager, TEST_PASSWORD);

        assertTrue(serverMessage.getMessage().contains("Protected paths list successfully decrypted"),
                "Success message should be shown");
    }

    @Test
    void CLIHandlerWithValidProtectedPathsArgumentEncryptsPaths() throws Exception {
        Path file1 = tempDir.resolve("toEncryptFile1.txt");
        Path file2 = tempDir.resolve("toEncryptFile2.txt");

        Files.writeString(file1, "Here is first test text");
        Files.writeString(file2, "And here is second test text");

        pathManager.addProtectedPath(file1.toString());
        pathManager.addProtectedPath(file2.toString());

        // Encrypt protected paths
        String[] args = {"-c"};

        new CLIHandler(args, pathManager, serverMessage, shutdownManager, TEST_PASSWORD, true);

        assertTrue(serverMessage.getMessage().contains("Protected paths list successfully encrypted"),
                "Success message should be shown");
    }

    @Test
    void CLIHandlerWithEmptyArgumentsDisplaysHelpMessage() {
        String[] args = {};

        CLIHandler cliHandler = new CLIHandler(args, pathManager, serverMessage, shutdownManager, TEST_PASSWORD);

        assertEquals(cliHandler.HELP_TEXT, serverMessage.getMessage(),
                "Help message should be displayed for empty arguments");
    }

    @Test
    void CLIHandlerWithEmptyPathThrowsIllegalArgumentException() {
        String[] args = {"-e="};

        CLIHandler cliHandler = new CLIHandler(args, pathManager, serverMessage, shutdownManager, TEST_PASSWORD);

        assertTrue(serverMessage.getMessage().contains(cliHandler.EMPTY_PATH),
                "Error message should indicate empty path");
    }

    @Test
    void CLIHandlerWithIncorrectPasswordForDecryption() throws Exception {
        String testData = "this is a test data";
        String correctPassword = "correctPassword";
        String wrongPassword = "wrongPassword";

        Path tempFile = tempDir.resolve("passwordTest.txt");
        Files.writeString(tempFile, testData);

        // Encrypt with correct password
        Encryptor.encrypt(tempFile.toFile(), correctPassword);

        // Try to decrypt with wrong password
        String[] args = {"-d=" + tempFile};
        simulateUserInput(wrongPassword);

        new CLIHandler(args, pathManager, serverMessage, shutdownManager, wrongPassword);

        System.out.println(serverMessage.getMessage());

        assertTrue(serverMessage.getMessage().contains("Invalid password or file is corrupted: "),
                "Error message should indicate invalid password");
    }

    @Test
    void CLIHandlerWithRemoveNonProtectedPathDisplaysWarning() throws IOException {
        Path testPath = tempDir.resolve("nonProtectedPath.txt");
        Files.createFile(testPath);

        // Ensure path is not in protected paths
        pathManager.getProtectedPaths().remove(testPath.toString());

        String[] args = {"-r=" + testPath};

        new CLIHandler(args, pathManager, serverMessage, shutdownManager, TEST_PASSWORD);

        assertEquals("[Warning]: ", serverMessage.getPrefix(),
                "Warning prefix should be set");
        assertTrue(serverMessage.getMessage().contains("Path is not a protected path"),
                "Warning message should indicate path is not protected");
    }

    @Test
    void CLIHandlerWithMultipleArgumentsProcessesAllInOrder() throws IOException {
        Path testPath1 = tempDir.resolve("path1.txt");
        Path testPath2 = tempDir.resolve("path2.txt");
        Files.createFile(testPath1);
        Files.createFile(testPath2);

        String[] args = {"-a=" + testPath1, "-a=" + testPath2, "-vp"};

        new CLIHandler(args, pathManager, serverMessage, shutdownManager, TEST_PASSWORD);

        // The last command (-vp) should determine the final message
        assertTrue(serverMessage.getMessage().contains(testPath1.toString())
                        && serverMessage.getMessage().contains(testPath2.toString()),
                "Final message should show both protected paths");
    }

    /**
     * Helper method to simulate user input via System.in
     */
    private void simulateUserInput(String input) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
    }
}