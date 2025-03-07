import javax.crypto.BadPaddingException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class CLIHandler {
    final String PROVIDE_A_PASSWORD_TEXT = "Please enter your password: ";
    final String BAD_PASSWORD_TEXT = "Given password is incorrect or file is broken.";
    final String ERROR_TEXT = "Something went wrong!";
    final String NO_SUCH_ARGUMENT = "No such argument, please try -h argument for help";
    final String HELP_TEXT = "Usage: java -jar alohomora.jar [argument=(value)]" +
                             "\t-h - provide a help message."+
                             "\t-e=[path] - encrypt a directory or file"+
                             "\t-d=[path] - decrypt a directory or file";

    public CLIHandler(String[] args) throws FileNotFoundException {

        for (String arg : args)
        {
            if (arg.startsWith("-d="))
            {
                String path = arg.substring(3);
                File targetFile = new File(path);

                if (!targetFile.exists())
                {
                    throw new FileNotFoundException("No such a file found");
                }

                try
                {
                    Encryptor.decrypt(targetFile, askAPassword());
                }

                catch (BadPaddingException e)
                {
                    System.out.println(BAD_PASSWORD_TEXT);
                }

                catch (Exception e)
                {
                    System.out.println(ERROR_TEXT);
                }
            }

            else if (arg.startsWith("-e=")) {
                String path = arg.substring(3);
                File targetFile = new File(path);

                if (!targetFile.exists())
                {
                    throw new FileNotFoundException("No such a file found");
                }

                try
                {
                    if (!targetFile.isDirectory())
                    {
                        Encryptor.encrypt(targetFile, askAPassword());
                    }

                    else
                    {
                        Encryptor.encryptDirectory(targetFile, askAPassword());
                    }
                }

                catch (Exception e)
                {
                    System.out.println(ERROR_TEXT);
                }
            }

            else if (arg.startsWith("-h"))
            {
                System.out.println(HELP_TEXT);
            }

            else
            {
                System.out.println(NO_SUCH_ARGUMENT);
            }
        }
    }

    private String askAPassword()
    {
        System.out.print(PROVIDE_A_PASSWORD_TEXT);
        Scanner scanner = new Scanner(System.in);
        String password = scanner.nextLine();
        scanner.close();

        return password;
    }
}
