package io.github.dayfit;

import javax.crypto.IllegalBlockSizeException;
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
import java.util.Scanner;

/**
 * Handles command-line interface (CLI) arguments and operations.
 */
public class CLIHandler {
    private final PathManager pathManager;

    final String PROVIDE_A_PASSWORD_TEXT = "Please enter your password: ";
    final String FILE_NOT_FOUND_TEXT = "No such a file or directory have been found";
    final String ERROR_TEXT = "Something went wrong!";
    final String NO_SUCH_ARGUMENT = "No such argument, please try -h argument for help";
    final String EMPTY_PATH = "Path cannot be empty";
    final String HELP_TEXT = """
            Usage: java -jar alohomora.jar [argument=(value)]\s
            \t-h - provide a help message.
            \t-e=[path] - encrypt a directory or file
            \t-d=[path] - decrypt a directory or file
            \t-a=[path] - add a path to the protected paths list
            \t-r=[path] - remove a path from the protected paths list
            \t-p - decrypt the protected paths list
            \t-c - encrypt the protected paths list
            \t-vp - view the protected paths list""";

    private String protectedPathsPassword;
    boolean isTested = false;

    /**
     * Constructs a CLIHandler with the specified PathManager.
     *
     * @param pathManager the PathManager to use for managing protected paths
     */
    public CLIHandler(PathManager pathManager) {
        this.pathManager = pathManager;
    }

    /**
     * Constructs a CLIHandler with the specified arguments and PathManager.
     *
     * @param args the command-line arguments
     * @param pathManager the PathManager to use for managing protected paths
     */
    public CLIHandler(String[] args, PathManager pathManager) {
        this(pathManager);
        processArguments(args);
    }

    /**
     * Constructs a CLIHandler with the specified arguments, PathManager, and test mode flag.
     *
     * @param args the command-line arguments
     * @param pathManager the PathManager to use for managing protected paths
     * @param isTested a boolean flag indicating if the handler is in test mode
     */
    public CLIHandler(String[] args, PathManager pathManager, boolean isTested) {
        this(pathManager);
        this.isTested = isTested;
        processArguments(args);
    }

    /**
     * Processes the provided command-line arguments.
     *
     * @param args the command-line arguments
     */
    public void processArguments(String[] args) {
        List<String> arguments = new ArrayList<>(Arrays.asList(args));

        for (String rawArgument : arguments) {
            String arg = rawArgument.substring(0, Math.min(rawArgument.length(), 3));

            switch (arg) {
                case "-h", "":
                    System.out.println(HELP_TEXT);
                    break;

                case "-p":
                    try {
                        handleProtectedPaths(false);
                    } catch (FileNotFoundException e) {
                        System.out.println(FILE_NOT_FOUND_TEXT + e.getMessage());
                    } catch (Exception e) {
                        System.out.println(ERROR_TEXT + "\n" + e.getMessage());
                    }
                    break;

                case "-c":
                    if (!this.isTested)
                    {
                        System.exit(0);
                    } else
                    {
                        try {
                            handleProtectedPaths(true);
                        } catch (Exception e)
                        {
                            System.out.println(ERROR_TEXT + "\n" + e.getMessage());
                        }
                    }
                    break;

                case "-d=":
                    String decryptPath = rawArgument.substring(3);

                    try {
                        handleEncryptionDecryption(decryptPath, false);
                    } catch (FileNotFoundException e) {
                        System.out.println(FILE_NOT_FOUND_TEXT);
                    }
                    break;

                case "-e=":
                    String encryptPath = rawArgument.substring(3);

                    try {
                        handleEncryptionDecryption(encryptPath, true);
                    } catch (FileNotFoundException e) {
                        System.out.println(FILE_NOT_FOUND_TEXT);
                    }
                    break;

                case "-a=":
                    handleAddingAndRemovingProtectedPaths(true, rawArgument);
                    break;

                case "-r=":
                    handleAddingAndRemovingProtectedPaths(false, rawArgument);
                    break;

                case "-vp":
                    System.out.println(pathManager.getProtectedPaths().toString());
                    break;

                default:
                    System.out.println(NO_SUCH_ARGUMENT + " [argument=" + rawArgument + "]");
                    break;
            }
        }
    }

    /**
     * Retrieves the password for protected paths.
     * If the password is not already set, prompts the user to enter a password.
     *
     * @return the password for protected paths
     */
    public String getProtectedPathsPassword() {
        this.protectedPathsPassword = this.protectedPathsPassword != null ? this.protectedPathsPassword : askAPassword();
        return this.protectedPathsPassword;
    }

    /**
     * Handles encryption or decryption of a file or directory.
     *
     * @param path the path to the file or directory
     * @param isEncryption true if encryption is to be performed, false for decryption
     * @throws FileNotFoundException if the file or directory does not exist
     */
    private void handleEncryptionDecryption(String path, boolean isEncryption) throws FileNotFoundException {
        File targetFile = new File(path);

        if (!targetFile.exists()) {
            throw new FileNotFoundException("No such a file found");
        }

        try {
            if (!targetFile.isDirectory()) {
                if (isEncryption) {
                    Encryptor.encrypt(targetFile, askAPassword());
                } else {
                    Encryptor.decrypt(targetFile, askAPassword());
                }
            } else {
                if (isEncryption) {
                    Encryptor.encryptDirectory(targetFile, askAPassword());
                } else {
                    Encryptor.decryptDirectory(targetFile, askAPassword());
                }
            }
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException(e.getMessage());
        } catch (Exception e) {
            System.err.println(ERROR_TEXT + "\n" + e.getMessage());
        }
    }

    /**
     * Prompts the user to enter a password.
     *
     * @return the entered password
     */
    private String askAPassword() {
        System.out.print(PROVIDE_A_PASSWORD_TEXT);
        Scanner scanner = new Scanner(System.in);

        return scanner.nextLine();
    }

    /**
     * Handles adding or removing protected paths based on the provided argument.
     *
     * @param addProtectedPaths true to add the path, false to remove the path
     * @param rawArgument the raw argument containing the path to add or remove
     */
    private void handleAddingAndRemovingProtectedPaths(boolean addProtectedPaths, String rawArgument)
    {
        String path = rawArgument.substring(3).replaceAll("\"", "").trim();
        String action = addProtectedPaths ? "adding" : "removing";

        if (path.isEmpty()) {
            System.out.println(EMPTY_PATH);
            return;
        }

        try {
            if (!Files.exists(Path.of(path))) {
                System.out.println(FILE_NOT_FOUND_TEXT + ": " + path);
            } else {
                if (addProtectedPaths) {
                    pathManager.addProtectedPath(path);
                }
                else {
                    if (pathManager.getProtectedPaths().contains(path))
                    {
                        pathManager.removeProtectedPath(path);
                    }
                    else
                    {
                        System.out.println("Path is not protected path" + ": " + path);
                    }
                }

                System.out.println("Successfully ended "+action+" protected path: " + path);
            }
        } catch (Exception e) {
            System.out.println("Error "+action+" path: " + e.getMessage());
        }
    }

    /**
     * Handles encryption or decryption of the protected paths list.
     *
     * @param encryption true if encryption is to be performed, false for decryption
     * @throws NoSuchAlgorithmException if the algorithm is not available
     * @throws IOException if an I/O error occurs
     * @throws InvalidKeyException if the key is invalid
     */
    private void handleProtectedPaths(boolean encryption) throws NoSuchAlgorithmException, IOException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException {
        if (this.protectedPathsPassword == null) {
            this.protectedPathsPassword = askAPassword();
        }

        if (encryption) {
            pathManager.encryptProtectedPaths(this.protectedPathsPassword);
        } else
        {
            pathManager.decryptProtectedPaths(this.protectedPathsPassword);
        }
    }
}