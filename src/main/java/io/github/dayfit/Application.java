package io.github.dayfit;

public class Application {
    public static void main(String[] args) {

        try
        {
            CLIHandler cliHandler = new CLIHandler(args);
        }

        catch (Exception e)
        {
            e.printStackTrace();
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() ->
        {
            //TODO: Add decryption for every selected file on turning off
        }
        ));
    }
}
