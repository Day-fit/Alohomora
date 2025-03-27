package io.dayfit.github.backgroundServices.utils;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class EncryptorTest {

    @Test
    void testEncryptWithValidInput() throws Exception {
        // Arrange
        File inputFile = File.createTempFile("testInput", ".txt");
        inputFile.deleteOnExit();
        File outputFile = File.createTempFile("testOutput", ".enc");
        outputFile.deleteOnExit();
        String password = "securePassword123";

        // Write data to the input file
        String inputData = "This is a test string.";
        Files.writeString(inputFile.toPath(), inputData);

        // Act
        Encryptor.encrypt(inputFile, outputFile, password);

        // Assert
        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0); // Check that the file is not empty
        assertNotEquals(inputFile.length(), outputFile.length());
    }

    @Test
    void testEncryptWithInvalidInputSingleFile() throws Exception
    {
        File inputFile = File.createTempFile("testInput", ".txt");
        inputFile.deleteOnExit();

        String password = "securePassword123";

        String inputData = "This is a test string.";
        Files.writeString(inputFile.toPath(), inputData);

        Encryptor.encrypt(inputFile, password);

        assertTrue(inputFile.exists());
        assertTrue(inputFile.length() > 0);
        assertNotEquals(inputData.length(), inputFile.length()); //Encrypted file should not have same length
    }

    @Test
    void testEncryptEmptyFile() throws Exception {
        // Arrange
        File inputFile = File.createTempFile("testEmptyInput", ".txt");
        inputFile.deleteOnExit();
        File outputFile = File.createTempFile("testEmptyOutput", ".enc");
        outputFile.deleteOnExit();
        String password = "securePassword123";

        // Act
        Encryptor.encrypt(inputFile, outputFile, password);

        // Assert
        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0); // Encrypted output should not be empty (AES Block)
    }

    @Test
    void testEncryptNonexistentInputFile() {
        // Arrange
        File inputFile = new File("nonexistentFile.txt");
        File outputFile = new File("output.enc");
        String password = "securePassword123";

        // Act & Assert
        assertThrows(FileNotFoundException.class, () -> Encryptor.encrypt(inputFile, outputFile, password));
    }

    @Test
    void testEncryptWithInvalidPassword() throws Exception {
        // Arrange
        File inputFile = File.createTempFile("testInput", ".txt");
        inputFile.deleteOnExit();
        File outputFile = File.createTempFile("testOutput", ".enc");
        outputFile.deleteOnExit();
        String password = "short";

        // Write data to the input file
        String inputData = "This is a test string.";
        Files.writeString(inputFile.toPath(), inputData);

        // Act
        Encryptor.encrypt(inputFile, outputFile, password);

        // Assert
        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0); // Output file should be created even with short password
    }

    @Test
    void testEncryptInvalidOutputFile() throws IOException {
        // Arrange
        File inputFile = File.createTempFile("testInput", ".txt");
        inputFile.deleteOnExit();
        File outputFile = new File("/invalidPath/output.enc");
        String password = "securePassword123";

        // Write data to the input file
        String inputData = "This is a test string.";
        try {
            Files.writeString(inputFile.toPath(), inputData);
        } catch (IOException e) {
            fail("Failed to write to test input file");
        }

        // Act & Assert
        assertThrows(IOException.class, () -> Encryptor.encrypt(inputFile, outputFile, password));
    }


    @Test
    void testEncryptDirectory() throws Exception
    {
        File inputDirectory = new File(System.getProperty("java.io.tmpdir"), "testEncryptDirectory");
        inputDirectory.deleteOnExit();

        if (!inputDirectory.exists())
        {
            if (!inputDirectory.mkdir())
            {
                fail("Failed to create test directory");
            }
        }

        File tempFile1 = new File(inputDirectory,"file.txt");
        tempFile1.deleteOnExit();

        if(!tempFile1.createNewFile())
        {
            fail("Failed to create temp file");
        }

        File tempFile2 = new File(inputDirectory,"file1.txt");
        tempFile2.deleteOnExit();

        if(!tempFile2.createNewFile())
        {
            fail("Failed to create temp file");
        }

        String password = "securePassword123";

        String inputData1 = "Here is first test text";
        String inputData2 = "And here is second test text";

        Files.writeString(tempFile1.toPath(), inputData1);
        Files.writeString(tempFile2.toPath(), inputData2);

        Encryptor.encryptDirectory(inputDirectory, password);

        assertTrue(tempFile1.exists());
        assertTrue(tempFile2.exists());
        assertFalse(Arrays.equals(inputData1.getBytes(), Files.readAllBytes(tempFile1.toPath())));
        assertFalse(Arrays.equals(inputData2.getBytes(), Files.readAllBytes(tempFile2.toPath())));
    }

    @Test
    void testDecryptWithValidInputFile() throws Exception {
        File inputFile = File.createTempFile("testInput", ".txt");
        inputFile.deleteOnExit();

        File outputFile = File.createTempFile("testOutput", ".enc");
        outputFile.deleteOnExit();

        File decryptedFile = File.createTempFile("testDecrypted", ".txt");
        decryptedFile.deleteOnExit();

        String password = "securePassword123";
        String inputData = "This is a test string.";

        Files.writeString(inputFile.toPath(), inputData);

        Encryptor.encrypt(inputFile, outputFile, password);
        Encryptor.decrypt(outputFile, decryptedFile, password);

        assertEquals(inputData, Files.readString(decryptedFile.toPath()));
    }

    @Test
    void testDecryptWithValidInputFileSingleFile() throws Exception {
        File inputFile = File.createTempFile("testInput", ".txt");
        inputFile.deleteOnExit();

        String password = "securePassword123";
        String inputData = "This is a test string.";

        Files.writeString(inputFile.toPath(), inputData);

        Encryptor.encrypt(inputFile, password);
        Encryptor.decrypt(inputFile, password);

        assertEquals(inputData, Files.readString(inputFile.toPath()));
    }

    @Test
    void testDecryptDirectory() throws Exception
    {
        File inputDirectory = new File(System.getProperty("java.io.tmpdir"), "testDecryptDirectory");
        inputDirectory.deleteOnExit();

        if (!inputDirectory.exists())
        {
            if (!inputDirectory.mkdir())
            {
                fail("Failed to create test directory");
            }
        }

        File tempFile1 = new File(inputDirectory,"file.txt");
        tempFile1.deleteOnExit();

        if(!tempFile1.createNewFile())
        {
            fail("Failed to create temp file");
        }

        File tempFile2 = new File(inputDirectory,"file1.txt");
        tempFile2.deleteOnExit();

        if(!tempFile2.createNewFile())
        {
            fail("Failed to create temp file");
        }

        String password = "securePassword123";

        String inputData1 = "Here is first test text";
        String inputData2 = "And here is second test text";

        Files.writeString(tempFile1.toPath(), inputData1);
        Files.writeString(tempFile2.toPath(), inputData2);

        Encryptor.encryptDirectory(inputDirectory, password);
        Encryptor.decryptDirectory(inputDirectory, password);

        assertTrue(tempFile1.exists());
        assertTrue(tempFile2.exists());
        assertEquals(inputData1, Files.readString(tempFile1.toPath()));
        assertEquals(inputData2, Files.readString(tempFile2.toPath()));
    }
}