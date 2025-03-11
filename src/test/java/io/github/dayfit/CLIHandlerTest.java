package io.github.dayfit;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class CLIHandlerTest {

    final PathManager pathManager = new PathManager();

    @Test
    void CLIHandlerWithValidEncryptFileArgumentEncryptsFile() throws Exception {
        String testData = "this is a test data";
        String testPassword = "password";

        Path tempFile = Files.createTempFile("testFile", ".txt");
        Files.writeString(tempFile, testData);
        assertArrayEquals(testData.getBytes(), Files.readAllBytes(tempFile));

        String[] args = {"-e=" + tempFile};

        InputStream inputStream = new ByteArrayInputStream(testPassword.getBytes());
        InputStream originalInputStream = System.in;

        System.setIn(inputStream);

        new CLIHandler(args, pathManager);

        System.setIn(originalInputStream);

        assertFalse(Arrays.equals(testData.getBytes(), Files.readAllBytes(tempFile)));
        Files.deleteIfExists(tempFile);
    }

    @Test
    void CLIHandlerWithValidDecryptFileArgumentDecryptsFile() throws Exception {
        String testData = "this is a test data";
        String testPassword = "password";

        Path tempFile = Files.createTempFile("testFile", ".txt");
        Files.writeString(tempFile, testData);
        assertArrayEquals(testData.getBytes(), Files.readAllBytes(tempFile));

        String[] args = {"-e=" + tempFile, "-d=" + tempFile};

        String passwordInput = testPassword + "\n" + testPassword + "\n";

        InputStream passwordInputStream = new ByteArrayInputStream(passwordInput.getBytes()); //We need second because this one will be consumed

        InputStream originalInputStream = System.in;

        Encryptor.encrypt(tempFile.toFile(), testPassword);

        System.setIn(originalInputStream);

        System.setIn(passwordInputStream);
        new CLIHandler(new String[]{args[1]}, pathManager);
        assertArrayEquals(testData.getBytes(), Files.readAllBytes(tempFile));

        Files.deleteIfExists(tempFile);
    }


    @Test
    void CLIHandlerWithValidEncryptDirectoryArgumentEncryptsDirectory() throws Exception {
        Path tempDir = Files.createTempDirectory("testDir");
        tempDir.toFile().deleteOnExit();

        String[] args = {"-e=" + tempDir};

        File tempFile1 = new File(String.valueOf(tempDir),"file.txt");
        tempFile1.deleteOnExit();

        if(!tempFile1.createNewFile())
        {
            fail("Failed to create temp file");
        }

        File tempFile2 = new File(String.valueOf(tempDir),"file1.txt");
        tempFile2.deleteOnExit();

        if(!tempFile2.createNewFile())
        {
            fail("Failed to create temp file");
        }

        String password = "securePassword123";
        InputStream passwordInputStream = new ByteArrayInputStream(password.getBytes());
        InputStream originalInputStream = System.in;


        String inputData1 = "Here is first test text";
        String inputData2 = "And here is second test text";

        Files.writeString(tempFile1.toPath(), inputData1);
        Files.writeString(tempFile2.toPath(), inputData2);

        System.setIn(passwordInputStream);
        new CLIHandler(args, pathManager);
        System.setIn(originalInputStream);

        assertTrue(tempFile1.exists());
        assertTrue(tempFile2.exists());

        assertFalse(Arrays.equals(inputData1.getBytes(), Files.readAllBytes(tempFile1.toPath())));
        assertFalse(Arrays.equals(inputData2.getBytes(), Files.readAllBytes(tempFile2.toPath())));
    }

    @Test
    void CLIHandlerWithValidDecryptDirectoryArgumentDecryptsDirectory() throws Exception {
        Path tempDir = Files.createTempDirectory("testDir");
        tempDir.toFile().deleteOnExit();

        File tempFile1 = new File(String.valueOf(tempDir),"file.txt");
        File tempFile2 = new File(String.valueOf(tempDir),"file1.txt");

        tempFile1.deleteOnExit();
        tempFile2.deleteOnExit();

        if(!tempFile1.createNewFile()) {
            fail("Failed to create temp file");
        }

        if(!tempFile2.createNewFile()) {
            fail("Failed to create temp file");
        }

        String password = "securePassword123";
        String inputData1 = "Here is first test text";
        String inputData2 = "And here is second test text";

        InputStream passwordInputStream = new ByteArrayInputStream(password.getBytes());
        InputStream originalInputStream = System.in;

        Files.writeString(tempFile1.toPath(), inputData1);
        Files.writeString(tempFile2.toPath(), inputData2);

        Encryptor.encryptDirectory(tempDir.toFile(), password);

        String[] args = {"-d=" + tempDir};

        System.setIn(passwordInputStream);
        new CLIHandler(args, pathManager);
        System.setIn(originalInputStream);

        assertTrue(tempFile1.exists());
        assertTrue(tempFile2.exists());

        assertArrayEquals(inputData1.getBytes(), Files.readAllBytes(tempFile1.toPath()));
        assertArrayEquals(inputData2.getBytes(), Files.readAllBytes(tempFile2.toPath()));
    }

    @Test
    void CLIHandlerWithHelpArgumentDisplaysHelpMessage() {
        String[] args = {"-h"};

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outputStream);
        PrintStream old = System.out;

        System.setOut(printStream);

        CLIHandler cliHandler = new CLIHandler(args, pathManager);

        System.setOut(old);

        assertEquals(cliHandler.HELP_TEXT, outputStream.toString().trim());
    }

    @Test
    void CLIHandlerWithInvalidArgumentDisplaysErrorMessage() {
        String[] args = {"-x=invalid"};

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outputStream);
        PrintStream old = System.out;

        System.setOut(printStream);

        CLIHandler cliHandler = new CLIHandler(args, pathManager);

        System.setOut(old);

        assertEquals(cliHandler.NO_SUCH_ARGUMENT, outputStream.toString().trim());
    }

    @Test
    void CLIHandlerWithNonExistentFileThrowsFileNotFoundException() {
        String[] args = {"-e=nonExistentFile.txt"};

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outputStream);
        PrintStream old = System.out;


        System.setOut(printStream);
        CLIHandler cliHandler = new CLIHandler(args, pathManager);
        System.setOut(old);

        assertEquals(cliHandler.FILE_NOT_FOUND_TEXT, outputStream.toString().trim());
    }

    @Test
    void CLIHandlerWithAddProtectedPathArgumentAddsPath() {
        String[] args = {"-a=protectedPath"};

        new CLIHandler(args, pathManager);

        assertTrue(pathManager.getProtectedPaths().contains(Path.of("protectedPath")));
    }

    @Test
    void CLIHandlerWithRemoveProtectedPathArgumentRemovesPath()
    {
        String[] args = {"-a=protectedPath", "-r=protectedPath"};
        new CLIHandler(args, pathManager);
        assertTrue(pathManager.getProtectedPaths().isEmpty());
    }

    @Test
    void CLIHandlerWithShowProtectedPathArgumentShowsPath()
    {
        String[] args = {"-vp"};
        pathManager.addProtectedPath(Path.of("protectedPathTest"));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outputStream);
        PrintStream originalPrintStream = System.out;

        System.setOut(printStream);
        new CLIHandler(args, pathManager);
        System.setOut(originalPrintStream);

        assertEquals(pathManager.getProtectedPaths().toString(), outputStream.toString().trim());
    }

    @Test
    void CLIHandlerWithValidProtectedPathsArgumentDecryptsPaths() throws Exception {
        File file1 = Files.createTempFile("file1", ".txt").toFile();
        File file2 = Files.createTempFile("file2", ".txt").toFile();

        pathManager.addProtectedPath(file1.toPath());
        pathManager.addProtectedPath(file2.toPath());

        String[] args = {"-p"};
        String password = "securePassword123";
        String inputData1 = "Here is first test text";
        String inputData2 = "And here is second test text";

        Files.writeString(file1.toPath(), inputData1);
        Files.writeString(file2.toPath(), inputData2);

        Encryptor.encrypt(file1, password);
        Encryptor.encrypt(file2, password);

        InputStream passwordInputStream = new ByteArrayInputStream(password.getBytes());
        InputStream originalInputStream = System.in;

        System.setIn(passwordInputStream);
        new CLIHandler(args, pathManager);
        System.setIn(originalInputStream);

        assertTrue(file1.exists());
        assertTrue(file2.exists());

        assertEquals(inputData1, Files.readString(file1.toPath()));
        assertEquals(inputData2, Files.readString(file2.toPath()));
    }

    @Test
    void CLIHandlerWithValidProtectedPathsArgumentEncryptsPaths() throws Exception {
        File tempFile1 = Files.createTempFile("file1", ".txt").toFile();
        File tempFile2 = Files.createTempFile("file2", ".txt").toFile();

        pathManager.addProtectedPath(tempFile1.toPath());
        pathManager.addProtectedPath(tempFile2.toPath());

        String[] args = {"-o"};
        String password = "securePassword123";
        String inputData1 = "Here is first test text";
        String inputData2 = "And here is second test text";

        Files.writeString(tempFile1.toPath(), inputData1);
        Files.writeString(tempFile2.toPath(), inputData2);

        InputStream passwordInputStream = new ByteArrayInputStream(password.getBytes());
        InputStream originalInputStream = System.in;

        System.setIn(passwordInputStream);
        new CLIHandler(args, pathManager);
        System.setIn(originalInputStream);

        assertFalse(Arrays.equals(inputData1.getBytes(), Files.readAllBytes(tempFile1.toPath())));
        assertFalse(Arrays.equals(inputData2.getBytes(), Files.readAllBytes(tempFile2.toPath())));
    }
}