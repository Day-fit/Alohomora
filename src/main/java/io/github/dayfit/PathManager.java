package io.github.dayfit;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

public class PathManager {
    private Set<String> protectedPaths = new HashSet<>();
    final String PATH_MANAGER_FILE = "protectedPaths.json";

    /**
     * Constructor for the PathManager class.
     * Initializes a new instance of the PathManager class.
     */
    PathManager(){
    }

    /**
     * Gets the set of protected paths.
     *
     * @return a set of strings representing the protected paths
     */
    public Set<String> getProtectedPaths() {
        return protectedPaths;
    }

    /**
     * Sets the protected paths.
     *
     * @param protectedPaths a HashSet of strings representing the protected paths
     */
    public void setProtectedPaths(HashSet<String> protectedPaths) {
        this.protectedPaths = protectedPaths;
    }

    /**
     * Adds a protected path to the set.
     *
     * @param protectedPath a string representing the path to be added
     */
    public void addProtectedPath(String protectedPath) {
        this.protectedPaths.add(protectedPath);
    }

    /**
     * Removes a protected path from the set.
     *
     * @param protectedPath a string representing the path to be removed
     */
    public void removeProtectedPath(String protectedPath) {
        this.protectedPaths.remove(protectedPath);
    }

    /**
     * Saves the protected paths to a file.
     *
     * @throws IOException if an I/O error occurs
     */
    public void saveProtectedPaths() throws IOException {
        JSON.saveJSON(JSON.toJSON(this.protectedPaths), Path.of(PATH_MANAGER_FILE));
    }

    /**
     * Encrypts the protected paths using the specified password.
     *
     * @param password the password used for encryption
     * @throws IOException if an I/O error occurs
     * @throws NoSuchPaddingException if the specified padding mechanism is not available
     * @throws IllegalBlockSizeException if the provided block size is invalid
     * @throws NoSuchAlgorithmException if the specified algorithm is not available
     * @throws InvalidKeyException if the given key is invalid
     */
    public void encryptProtectedPaths(String password) throws IOException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeyException {
        handleProtectedPaths(true, password);
    }

    /**
     * Decrypts the protected paths using the specified password.
     *
     * @param password the password used for decryption
     * @throws IOException if an I/O error occurs
     * @throws IllegalBlockSizeException if the provided block size is invalid
     * @throws NoSuchPaddingException if the specified padding mechanism is not available
     * @throws NoSuchAlgorithmException if the specified algorithm is not available
     * @throws InvalidKeyException if the given key is invalid
     */
    public void decryptProtectedPaths(String password) throws IOException, IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        handleProtectedPaths(false, password);
    }

    /**
     * Handles the encryption or decryption of protected paths.
     *
     * @param encryption a boolean indicating whether to encrypt (true) or decrypt (false) the protected paths
     * @param password the password used for encryption or decryption
     * @throws NoSuchAlgorithmException if the specified algorithm is not available
     * @throws IOException if an I/O error occurs
     * @throws InvalidKeyException if the given key is invalid
     * @throws NoSuchPaddingException if the specified padding mechanism is not available
     * @throws IllegalBlockSizeException if the provided block size is invalid
     */
    private void handleProtectedPaths(boolean encryption, String password) throws NoSuchAlgorithmException, IOException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException {
        for (String path : protectedPaths) {
            File protectedFile = new File(path);
            if (encryption) {
                if (protectedFile.isDirectory()) {
                    Encryptor.encryptDirectory(protectedFile, password);
                } else {
                    Encryptor.encrypt(protectedFile, password);
                }
            } else {
                if (protectedFile.isDirectory()) {
                    Encryptor.decryptDirectory(protectedFile, password);
                } else {
                    Encryptor.decrypt(protectedFile, password);
                }
            }
        }
    }
}