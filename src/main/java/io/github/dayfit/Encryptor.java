package io.github.dayfit;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
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
    final static Cipher cipher;

    static {
        try {
            cipher = Cipher.getInstance(ALGORITHM);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    final static String FILE_DECRYPTED_SUCCESSFULLY = "File has been decrypted successfully: ";
    final static String FILE_ENCRYPTED_SUCCESSFULLY = "File has been encrypted successfully: ";

    static final String BAD_PASSWORD_TEXT = "Given password might be incorrect or corrupted. \n" + "Please check if file is encrypted or not";

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
    public static void encrypt(File inputFile, String password) throws IllegalBlockSizeException, NoSuchAlgorithmException, IOException, InvalidKeyException {
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
      * @throws NoSuchAlgorithmException if the specified algorithm is not available
      * @throws InvalidKeyException if the given key is invalid
      * @throws IOException if an I/O error occurs
      * @throws IllegalBlockSizeException if the provided data is not a multiple of the block size
      */
    public static void encrypt(File inputFile, File outputFile, String password) throws NoSuchAlgorithmException, InvalidKeyException, IOException, IllegalBlockSizeException {
        cipher.init(Cipher.ENCRYPT_MODE, getKeyFromPassword(password));

        if (!inputFile.exists())
        {
            throw new FileNotFoundException(inputFile.getAbsolutePath() + " does not exist");
        }

        if (!outputFile.exists()) {
            if (!outputFile.createNewFile()) {
                System.out.println("fatal error");
                System.exit(1);
            }
        }

        List<byte[]> encryptedData = new ArrayList<>();

        try (FileInputStream fileInputStream = new FileInputStream(inputFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                byte[] encryptedBytes = cipher.update(buffer, 0, bytesRead);
                if (encryptedBytes != null && encryptedBytes.length > 0) {
                    encryptedData.add(encryptedBytes);
                }
            }

            byte[] finalBytes = cipher.doFinal();

            if (finalBytes != null) {
                encryptedData.add(finalBytes);
            }

        try (FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
            for (byte[] encryptedBytes : encryptedData) {
                fileOutputStream.write(encryptedBytes);
            }

            System.out.println(FILE_ENCRYPTED_SUCCESSFULLY + inputFile.getAbsolutePath());
        }

        } catch (BadPaddingException e) {
            System.out.println("Could not decrypt file: " + inputFile.getAbsolutePath() + "\n" + BAD_PASSWORD_TEXT);
        }
    }

    /**
     * Encrypts all files in a directory recursively using the specified password.
     *
     * @param directory the directory containing files to be encrypted
     * @param password  the password used for encryption
     */
    public static void encryptDirectory(File directory, String password) {
        List<File> queue = new ArrayList<>(Arrays.asList(Objects.requireNonNull(directory.listFiles())));

        while (!queue.isEmpty()) {
            File file = queue.removeFirst();

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
     * @throws NoSuchPaddingException if the specified padding mechanism is not available
     * @throws NoSuchAlgorithmException if the specified algorithm is not available
     * @throws InvalidKeyException if the given key is invalid
     * @throws IOException if an I/O error occurs
     * @throws IllegalBlockSizeException if the provided data is not a multiple of the block size
     */
    public static void decrypt(File inputFile, String password) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IOException, IllegalBlockSizeException {
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
     * @throws NoSuchPaddingException if the specified padding mechanism is not available
     * @throws NoSuchAlgorithmException if the specified algorithm is not available
     * @throws InvalidKeyException if the given key is invalid
     * @throws IllegalBlockSizeException if the provided data is not a multiple of the block size
     * @throws IOException if an I/O error occurs
     */
    public static void decrypt(File inputFile, File outputFile, String password) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, IOException {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, getKeyFromPassword(password));

        if (!inputFile.exists())
        {
            throw new FileNotFoundException(inputFile.getAbsolutePath() + " does not exist");
        }

        if (!outputFile.exists()) {
            if (!outputFile.createNewFile()) {
                System.out.println("fatal error");
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

                System.out.println(FILE_DECRYPTED_SUCCESSFULLY + inputFile.getAbsolutePath());
            }
        } catch (BadPaddingException e) {
            System.out.println("[ERROR]: Could not decrypt file: " + inputFile.getAbsolutePath() + "\n" + BAD_PASSWORD_TEXT);
        }
    }

    /**
     * Decrypts all files in a directory recursively using the specified password.
     *
     * @param directory the directory containing files to be decrypted
     * @param password  the password used for decryption
     */
    public static void decryptDirectory(File directory, String password) {
        List<File> queue = new ArrayList<>(Arrays.asList(Objects.requireNonNull(directory.listFiles())));

        while (!queue.isEmpty()) {
            File file = queue.removeFirst();

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
}