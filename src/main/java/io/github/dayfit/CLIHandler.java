package io.github.dayfit;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class CLIHandler {
    private final PathManager pathManager;

    final String PROVIDE_A_PASSWORD_TEXT = "Please enter your password: ";
    final String BAD_PASSWORD_TEXT = "Given password is incorrect or file is broken.";
    final String FILE_NOT_FOUND_TEXT = "No such a file found";
    final String ERROR_TEXT = "Something went wrong!";
    final String NO_SUCH_ARGUMENT = "No such argument, please try -h argument for help";
    final String HELP_TEXT = """
            Usage: java -jar alohomora.jar [argument=(value)]\s
            \t-h - provide a help message.
            \t-e=[path] - encrypt a directory or file
            \t-d=[path] - decrypt a directory or file
            \t-a=[path] - add a path to the protected paths list
            \t-r=[path] - remove a path from the protected paths list
            \t-p - decrypt the protected paths list
            \t-o - encrypt the protected paths list
            \t-vp - view the protected paths list""";

    String protectedPathsPassword;

    public CLIHandler(String[] args, PathManager pathManager) {

        this.pathManager = pathManager;

        List<String> arguments = new ArrayList<>(Arrays.asList(args));



        for (String rawArgument : arguments)
        {
            String arg = rawArgument.substring(0, Math.min(rawArgument.length(), 3));

            switch (arg) {
                case "-h", "":
                    System.out.println(HELP_TEXT);
                    break;

                case "-p":
                    try {
                        handleProtectedPaths(false);
                    } catch (FileNotFoundException e){
                        System.out.println(FILE_NOT_FOUND_TEXT);
                    } catch (BadPaddingException e) {
                        System.out.println(BAD_PASSWORD_TEXT);
                    } catch (Exception e) {
                        System.out.println(ERROR_TEXT + "\n" + e.getMessage());
                    }
                    break;

                case "-o":
                    try {
                        handleProtectedPaths(true);
                    }catch (FileNotFoundException e) {
                        System.out.println(FILE_NOT_FOUND_TEXT);
                    } catch (Exception e) {
                        System.out.println(ERROR_TEXT + "\n" + e.getMessage());
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
                    String addPath = rawArgument.substring(3);
                    pathManager.addProtectedPath(Path.of(addPath));
                    break;

                case "-r=":
                    String removePath = rawArgument.substring(3);
                    pathManager.removeProtectedPath(Path.of(removePath));
                    break;

                case "-vp":
                    System.out.println(pathManager.getProtectedPaths().toString());
                    break;

                default:
                    System.out.println(NO_SUCH_ARGUMENT);
                    break;
            }
        }
    }

    private void handleEncryptionDecryption (String path,boolean isEncryption) throws FileNotFoundException {
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
        } catch (BadPaddingException e) {
            System.out.println(BAD_PASSWORD_TEXT);
        } catch (Exception e) {
            System.out.println(ERROR_TEXT + "\n" + e.getMessage());
        }
    }

    private String askAPassword ()
    {
        System.out.print(PROVIDE_A_PASSWORD_TEXT);
        Scanner scanner = new Scanner(System.in);

        return scanner.nextLine();
    }

    private void handleProtectedPaths(boolean encryption) throws IOException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        if (encryption)
        {
            this.protectedPathsPassword = askAPassword();
        }

        for (Path path : pathManager.getProtectedPaths()) {
            if (encryption) {
                if (path.toFile().isDirectory()) {
                    Encryptor.encryptDirectory(path.toFile(), this.protectedPathsPassword);
                } else {
                    Encryptor.encrypt(path.toFile(), this.protectedPathsPassword);
                }
            }

            else {
                if (path.toFile().isDirectory())
                {
                    Encryptor.decryptDirectory(path.toFile(), this.protectedPathsPassword);
                }
                else {
                    Encryptor.decrypt(path.toFile(), this.protectedPathsPassword);
                }
            }
        }
    }
}