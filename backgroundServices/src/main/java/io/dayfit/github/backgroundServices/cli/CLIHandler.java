package io.dayfit.github.backgroundServices.cli;

import io.dayfit.github.backgroundServices.managers.ShutdownManager;
import io.dayfit.github.backgroundServices.utils.Encryptor;
import io.dayfit.github.backgroundServices.managers.PathManager;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Handles command-line interface (CLI) arguments and operations.
 */
@Component
public class CLIHandler {
    @Setter
    private PathManager pathManager;
    private final ShutdownManager shutdownManager;

    public final String FILE_NOT_FOUND_TEXT = "No such a file or directory have been found";
    public final String ERROR_TEXT = "Something went wrong!";
    public final String NO_SUCH_ARGUMENT = "No such argument, please try -h argument for help";
    public final String EMPTY_PATH = "Path cannot be empty";
    public final String HELP_TEXT = """
            Usage: java -jar alohomora.jar [argument=(value)]\s
            \t-h - provide a help message.
            \t-e=[path] - encrypt a directory or file
            \t-d=[path] - decrypt a directory or file
            \t-a=[path] - add a path to the protected paths list
            \t-r=[path] - remove a path from the protected paths list
            \t-p - decrypt the protected paths list
            \t-c - encrypt the protected paths list
            \t-vp - view the protected paths list""";

    boolean isTested = false;

    /**
     * Primary constructor with all required dependencies.
     *
     * @param pathManager the PathManager to use for managing protected paths
     * @param shutdownManager the ShutdownManager to use for shutting down the application
     */
    @Autowired
    public CLIHandler(PathManager pathManager, ShutdownManager shutdownManager) {
        this.pathManager = pathManager;
        this.shutdownManager = shutdownManager;
    }

    /**
     * Constructs a CLIHandler with the specified arguments and dependencies.
     */
    public CLIHandler(String[] args, PathManager pathManager, ShutdownManager shutdownManager, String password) {
        this(pathManager, shutdownManager);
        processArguments(args, password);
    }

    /**
     * Constructs a CLIHandler with test mode flag.
     */
    public CLIHandler(String[] args, PathManager pathManager, ShutdownManager shutdownManager, String password, boolean isTested) {
        this(pathManager, shutdownManager);

        this.isTested = isTested;
        processArguments(args, password);
    }

    /**
     * Processes the provided command-line arguments.
     *
     * @param args the command-line arguments
     * @param password the password to use for encryption and decryption
     */
    public void processArguments(String[] args, String password) {
        List<String> arguments = new ArrayList<>(Arrays.asList(args));

        if (arguments.isEmpty() || arguments.contains("-h")) {
            System.out.println("[Success]: " + HELP_TEXT);
            return;
        }

        for (String rawArgument : arguments) {
            String arg = rawArgument.length() >= 2 ?
                    (rawArgument.contains("=") ? rawArgument.substring(0, rawArgument.indexOf("=") + 1) : rawArgument) :
                    rawArgument;

            try {
                switch (arg) {
                    case "-h":
                        System.out.println("[Success]: " + HELP_TEXT);
                        break;

                    case "-p":
                        handleProtectedPaths(false, password);
                        break;

                    case "-c":
                        handleProtectedPaths(true, password);
                        if (!this.isTested) {
                            try {
                                System.out.println("[Success]: Shutting down application...");
                                shutdownManager.shutdown();
                            } catch (Exception e) {
                                System.err.println("[Error]: Failed to shut down the application: " + e.getMessage());
                            }
                        }
                        break;
                    case "-d=":
                        String decryptPath = rawArgument.substring(3);
                        handleEncryptionDecryption(decryptPath, false, password);
                        break;

                    case "-e=":
                        String encryptPath = rawArgument.substring(3);
                        handleEncryptionDecryption(encryptPath, true, password);
                        break;

                    case "-a=":
                        handleAddingAndRemovingProtectedPaths(true, rawArgument);
                        break;

                    case "-r=":
                        handleAddingAndRemovingProtectedPaths(false, rawArgument);
                        break;

                    case "-vp":
                        System.out.println("[Success]: " + pathManager.getProtectedPaths().toString());
                        break;

                    default:
                        System.err.println("[Error]: " + NO_SUCH_ARGUMENT + " [argument=" + rawArgument + "]");
                        break;
                }
            } catch (Exception e) {
                System.err.println("[Error]: " + ERROR_TEXT + "\n" + e.getMessage());
            }
        }
    }

    /**
     * Handles encryption or decryption of a file or directory.
     *
     * @param path the path to the file or directory
     * @param isEncryption true if encryption is to be performed, false for decryption
     * @throws FileNotFoundException if the file or directory does not exist
     */
    private void handleEncryptionDecryption(String path, boolean isEncryption, String password) throws Exception {
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException(EMPTY_PATH);
        }

        File targetFile = new File(path);

        if (!targetFile.exists()) {
            throw new FileNotFoundException("File not found: " + path);
        }

        String operation = isEncryption ? "encrypted" : "decrypted";

        try {
            if (!targetFile.isDirectory()) {
                if (isEncryption) {
                    Encryptor.encrypt(targetFile, password);
                } else {
                    Encryptor.decrypt(targetFile, password);
                }
                System.out.println("[Success]: File " + path + " successfully " + operation);
            } else {
                if (isEncryption) {
                    Encryptor.encryptDirectory(targetFile, password);
                } else {
                    Encryptor.decryptDirectory(targetFile, password);
                }
                System.out.println("[Success]: Directory " + path + " successfully " + operation);
            }
        } catch (InvalidKeyException e) {
            throw new InvalidKeyException("Invalid password or encryption key: " + e.getMessage());
        } catch (BadPaddingException e) {
            System.err.println("[Error]: Invalid password or file is corrupted: " + e.getMessage());
        } catch (Exception e) {
            throw new Exception("Failed to " + operation + " " + path + ": " + e.getMessage());
        }
    }

    /**
     * Handles adding or removing protected paths based on the provided argument.
     *
     * @param addProtectedPaths true to add the path, false to remove the path
     * @param rawArgument the raw argument containing the path to add or remove
     */
    private void handleAddingAndRemovingProtectedPaths(boolean addProtectedPaths, String rawArgument) throws Exception {
        String path = rawArgument.substring(3).replaceAll("\"", "").trim();

        if (path.isEmpty()) {
            throw new IllegalArgumentException(EMPTY_PATH);
        }

        if (!Files.exists(Path.of(path))) {
            throw new FileNotFoundException(FILE_NOT_FOUND_TEXT + ": " + path);
        }

        if (addProtectedPaths) {
            System.out.println("[Success]: Successfully added protected path: " + path);
            pathManager.addProtectedPath(path);
        } else {
            if (pathManager.getProtectedPaths().contains(path)) {
                System.out.println("[Success]: Successfully removed protected path: " + path);
                pathManager.removeProtectedPath(path);
            } else {
                System.out.println("[Warning]: Path is not a protected path: " + path);
            }
        }
    }

    /**
     * Handles encryption or decryption of the protected paths list.
     *
     * @param encryption true if encryption is to be performed, false for decryption
     */
    private void handleProtectedPaths(boolean encryption, String password) {
        try {
            if (encryption) {
                System.out.println("[Success]: Protected paths list successfully encrypted");
                pathManager.encryptProtectedPaths(password);
            } else {
                System.out.println("[Success]: Protected paths list successfully decrypted");
                pathManager.decryptProtectedPaths(password);
            }
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            System.err.println("[Error]: Encryption algorithm error: " + e.getMessage());
        } catch (BadPaddingException e) {
            System.err.println("[Error]: Invalid password or file is corrupted: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("[Error]: Error reading/writing protected paths: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("[Error]: Error processing protected paths: " + e.getMessage());
        }
    }
}