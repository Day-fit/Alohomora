package io.dayfit.github.backgroundServices.utils;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.DosFileAttributes;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * The Encryptor class provides methods for encrypting and decrypting files and directories using the AES algorithm.
 */
public class Encryptor {
    final static String ALGORITHM = "AES";

    final static String FILE_DECRYPTED_SUCCESSFULLY = "File has been decrypted successfully: ";
    final static String FILE_ENCRYPTED_SUCCESSFULLY = "File has been encrypted successfully: ";

    private Encryptor() {
    }

    /**
     * Encrypts a file using the specified password.
     *
     * @param inputFile the file to be encrypted
     * @param password  the password used for encryption
     * @throws IllegalBlockSizeException if the provided data is not a multiple of the block size
     * @throws NoSuchAlgorithmException if the specified algorithm is not available
     * @throws IOException if an I/O error occurs
     * @throws InvalidKeyException if the given key is invalid
     */
    public static void encrypt(File inputFile, String password) throws IllegalBlockSizeException, NoSuchAlgorithmException, IOException, InvalidKeyException, NoSuchPaddingException, BadPaddingException {
        encrypt(inputFile, inputFile, password);
    }

    /**
      * Encrypts a file and writes the encrypted data to the specified output file.
      * This method uses the AES algorithm to encrypt the contents of the input file
      * and writes the encrypted data to the output file. The encryption is performed
      * using the specified password, which is converted into a SecretKey.
      *
      * @param inputFile  the file to be encrypted
      * @param outputFile the file to write the encrypted data to
      * @param password   the password used for encryption
     *
     * @throws BadPaddingException if the specified password is invalid or file is corrupted
      * @throws NoSuchAlgorithmException if the specified algorithm is not available
      * @throws InvalidKeyException if the given key is invalid
      * @throws IOException if an I/O error occurs
      * @throws IllegalBlockSizeException if the provided data is not a multiple of the block size
      */
    public static void encrypt(File inputFile, File outputFile, String password) throws NoSuchAlgorithmException, InvalidKeyException, IOException, IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException {
        handleEncryptionDecryption(inputFile, outputFile, password, true);
    }

    /**
     * Encrypts all files in a directory using the specified password.
     *
     * @param directory the directory containing files to be encrypted
     * @param password  the password used for encryption
     */
    public static void encryptDirectory(File directory, String password) {
        List<File> queue = new ArrayList<>(Arrays.asList(Objects.requireNonNull(directory.listFiles())));

        while (!queue.isEmpty()) {
            File file = queue.remove(0);

            if (file.isDirectory()) {
                queue.addAll(Arrays.asList(Objects.requireNonNull(file.listFiles())));
            } else {
                try {
                    encrypt(file, password);
                } catch (Exception e) {
                    System.out.println("Could not encrypt file: " + file.getAbsolutePath() + "\n[ERROR]: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Decrypts a file using the specified password.
     *
     * @param inputFile the file to be decrypted
     * @param password  the password used for decryption
     *
     * @throws BadPaddingException if the specified password is invalid or file is corrupted
     * @throws NoSuchPaddingException if the specified padding mechanism is not available
     * @throws NoSuchAlgorithmException if the specified algorithm is not available
     * @throws InvalidKeyException if the given key is invalid
     * @throws IOException if an I/O error occurs
     * @throws IllegalBlockSizeException if the provided data is not a multiple of the block size
     */
    public static void decrypt(File inputFile, String password) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IOException, IllegalBlockSizeException, BadPaddingException {
        decrypt(inputFile, inputFile, password);
    }

    /**
     * Decrypts a file and writes the decrypted data to the specified output file.
     * This method uses the AES algorithm to decrypt the contents of the input file
     * and writes the decrypted data to the output file. The decryption is performed
     * using the specified password, which is converted into a SecretKey.
     *
     * @param inputFile  the file to be decrypted
     * @param outputFile the file to write the decrypted data to
     * @param password   the password used for decryption
     *
     * @throws BadPaddingException if the specified password is invalid or file is corrupted
     * @throws NoSuchPaddingException if the specified padding mechanism is not available
     * @throws NoSuchAlgorithmException if the specified algorithm is not available
     * @throws InvalidKeyException if the given key is invalid
     * @throws IllegalBlockSizeException if the provided data is not a multiple of the block size
     * @throws IOException if an I/O error occurs
     */
    public static void decrypt(File inputFile, File outputFile, String password) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, IOException, BadPaddingException {
        handleEncryptionDecryption(inputFile, outputFile, password, false);
    }

    /**
     * Decrypts all files in a directory using the specified password.
     *
     * @param directory the directory containing files to be decrypted
     * @param password  the password used for decryption
     */
    public static void decryptDirectory(File directory, String password) {
        List<File> queue = new ArrayList<>(Arrays.asList(Objects.requireNonNull(directory.listFiles())));

        while (!queue.isEmpty()) {
            File file = queue.remove(0);

            if (file.isDirectory()) {
                queue.addAll(Arrays.asList(Objects.requireNonNull(file.listFiles())));
            } else {
                try {
                    decrypt(file, password);
                } catch (Exception e) {
                    System.out.println("[ERROR]: Could not encrypt file: " + file.getAbsolutePath() + "\n[ERROR]: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Handles the encryption and decryption of files.
     *
     * @param inputFile  the file to be encrypted or decrypted
     * @param outputFile the file to write the encrypted or decrypted data to
     * @param password   the password used for encryption or decryption
     * @param isEncryption true if the operation is encryption, false if decryption
     *
     * @throws BadPaddingException if the specified password is invalid or file is corrupted
     * @throws NoSuchPaddingException if the specified padding mechanism is not available
     * @throws NoSuchAlgorithmException if the specified algorithm is not available
     * @throws InvalidKeyException if the given key is invalid
     * @throws IOException if an I/O error occurs
     * @throws IllegalBlockSizeException if the provided data is not a multiple of the block size
     */
    private static void handleEncryptionDecryption(File inputFile, File outputFile, String password, boolean isEncryption) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IOException, IllegalBlockSizeException, BadPaddingException {
        final int CIPHER_MODE = isEncryption? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE;
        final String MESSAGE = isEncryption ? FILE_ENCRYPTED_SUCCESSFULLY : FILE_DECRYPTED_SUCCESSFULLY;

        if (!inputFile.exists())
        {
            throw new FileNotFoundException(inputFile.getAbsolutePath() + " does not exist");
        }

        if (isSystemPath(inputFile)) {
            System.out.println("[ERROR]: Cannot encrypt/decrypt system file: " + inputFile.getAbsolutePath());
            return;
        }

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(CIPHER_MODE, getKeyFromPassword(password));

        if (!outputFile.exists()) {
            if (!outputFile.createNewFile()) {
                System.out.println("Could not create output file: " + outputFile.getAbsolutePath());
                System.exit(1);
            }

            System.out.println("output file created");
        }

        List<byte[]> decryptedData = new ArrayList<>();

        try (FileInputStream fileInputStream = new FileInputStream(inputFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                byte[] decryptedBytes = cipher.update(buffer, 0, bytesRead);
                if (decryptedBytes != null) {
                    decryptedData.add(decryptedBytes);
                }
            }

            byte[] finalBytes = cipher.doFinal();
            if (finalBytes != null) {
                decryptedData.add(finalBytes);
            }

            try (FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
                for (byte[] data : decryptedData) {
                    fileOutputStream.write(data);
                }

                System.out.println(MESSAGE + inputFile.getAbsolutePath());
            }
        }
    }

    /**
     * Generates a SecretKey from the given password using SHA-256.
     *
     * @param password the password used to generate the key
     * @return the generated SecretKey
     * @throws NoSuchAlgorithmException if the specified algorithm is not available
     */
    private static SecretKey getKeyFromPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = sha.digest(password.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(keyBytes, 0, 16, ALGORITHM);
    }

    private static boolean isSystemPath(File file) throws IOException {
        final String OS_NAME = System.getProperty("os.name").toLowerCase();
        final String[] SYSTEM_DIRS = {"/bin", "/sbin", "/etc", "/usr/bin", "/usr/sbin"};

        if (OS_NAME.contains("win"))
        {
            DosFileAttributes dosFileAttributes = Files.readAttributes(file.toPath(), DosFileAttributes.class);
            return dosFileAttributes.isSystem();
        }

        else if (OS_NAME.contains("mac") || OS_NAME.contains("nix") || OS_NAME.contains("nux") || OS_NAME.contains("aix"))
        {
            for (String dir : SYSTEM_DIRS)
            {
                if (file.getAbsolutePath().contains(dir))
                {
                    return true;
                }
            }
        }
        return false;
    }
}