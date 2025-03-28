package io.dayfit.github.clientApp;

import java.util.Scanner;

/**
 * Manages the password for the client application.
 */
public class PasswordManager {
    private String password;

    /**
     * Retrieves the password. If the password is not set, prompts the user to enter it.
     *
     * @return the password as a String
     */
    public String getPassword() {
        password = password != null ? password : askPassword();
        return password;
    }

    /**
     * Prompts the user to enter their password via the console.
     *
     * @return the entered password as a String
     */
    private String askPassword() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your password: ");
        String password = scanner.nextLine();
        scanner.close();
        
        return password;
    }
}