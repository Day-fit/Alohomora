package io.dayfit.github.backgroundServices.managers;
import io.dayfit.github.backgroundServices.utils.Encryptor;
import io.dayfit.github.shared.JSON;
import lombok.Getter;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class PathManager {
    @Getter
    private Set<String> protectedPaths = new HashSet<>();
    private final String PATH_MANAGER_FILE = "protectedPaths.json";

    /**
     * Constructor for the PathManager class.
     * Initializes a new instance of the PathManager class.
     */
    public PathManager(){
        loadProtectedPaths();
    }

    public PathManager(boolean loadProtectedPaths)
    {
        if (loadProtectedPaths)
        {
            loadProtectedPaths();
        }
    }

    /**
     * Adds a protected path to the set.
     *
     * @param protectedPath a string representing the path to be added
     */
    public void addProtectedPath(String protectedPath) throws IOException {
        this.protectedPaths.add(protectedPath);
        saveProtectedPaths();
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
        Path savePath = Path.of(PATH_MANAGER_FILE);
        if (!savePath.toFile().exists())
        {
            Files.createFile(savePath);
        }

        Runnable saving = new SavingManager(savePath, JSON.toJSON(this.protectedPaths));
        Thread thread = new Thread(saving);
        thread.start();
    }

    /**
     * Encrypts the protected paths using the specified password.
     *
     * @param password the password used for encryption
     *
     * @throws BadPaddingException if given password is wrong or file is corrupted
     * @throws IOException if an I/O error occurs
     * @throws NoSuchPaddingException if the specified padding mechanism is not available
     * @throws IllegalBlockSizeException if the provided block size is invalid
     * @throws NoSuchAlgorithmException if the specified algorithm is not available
     * @throws InvalidKeyException if the given key is invalid
     */
    public void encryptProtectedPaths(String password) throws IOException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException {
        handleProtectedPaths(true, password);
    }

    /**
     * Decrypts the protected paths using the specified password.
     *
     * @param password the password used for decryption
     *
     * @throws BadPaddingException if given password is wrong or file is corrupted
     * @throws IOException if an I/O error occurs
     * @throws IllegalBlockSizeException if the provided block size is invalid
     * @throws NoSuchPaddingException if the specified padding mechanism is not available
     * @throws NoSuchAlgorithmException if the specified algorithm is not available
     * @throws InvalidKeyException if the given key is invalid
     */
    public void decryptProtectedPaths(String password) throws IOException, IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException {
        handleProtectedPaths(false, password);
    }

    /**
     * Handles the encryption or decryption of protected paths.
     *
     * @param encryption a boolean indicating whether to encrypt (true) or decrypt (false) the protected paths
     * @param password the password used for encryption or decryption
     *
     * @throws BadPaddingException if given password is wrong or file is corrupted
     * @throws NoSuchAlgorithmException if the specified algorithm is not available
     * @throws IOException if an I/O error occurs
     * @throws InvalidKeyException if the given key is invalid
     * @throws NoSuchPaddingException if the specified padding mechanism is not available
     * @throws IllegalBlockSizeException if the provided block size is invalid
     */
    private void handleProtectedPaths(boolean encryption, String password) throws NoSuchAlgorithmException, IOException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        for (String path : protectedPaths) {
            File protectedFile = new File(path);

            if (!protectedFile.exists())
            {
                throw new FileNotFoundException("No such file or directory: " + protectedFile.getAbsolutePath());
            }

            if (protectedFile.isDirectory())
            {
                if (encryption) {
                    Encryptor.encryptDirectory(protectedFile, password);
                }
                else{
                    Encryptor.decryptDirectory(protectedFile, password);
                }
            }

            else
            {
                if (encryption) {
                    Encryptor.encrypt(protectedFile, password);
                }

                else{
                    Encryptor.decrypt(protectedFile, password);
                }
            }
        }
    }

    private void loadProtectedPaths()
    {
        try
        {
            String protectedPathsJSON = Files.readString(Path.of(PATH_MANAGER_FILE));
            Set<?> tempSet = JSON.fromJSON(protectedPathsJSON, Set.class);
            this.protectedPaths = tempSet.stream().filter(element -> element instanceof String).map(element -> (String)element).collect(Collectors.toSet());
        }catch (IOException e)
        {
            System.err.println("WARNING: Could not load protectedPaths.json. Using empty protectedPaths File.");
        }
    }
}