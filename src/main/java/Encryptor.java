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

public class Encryptor {
    final static String ALGORITHM = "AES";

    private Encryptor() {
    }

    public static void encrypt(File inputFile, String password) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, IOException, BadPaddingException, InvalidKeyException {
        encrypt(inputFile, inputFile, password);
    }

    public static void encrypt(File inputFile, File outputFile, String password) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IOException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, getKeyFromPassword(password));

        if (!outputFile.exists())
        {
            if(!outputFile.createNewFile())
            {
                System.out.println("fatal error");
                System.exit(1);
            }
        }

        List<byte[]> encryptedData = new ArrayList<>();

        try(FileInputStream fileInputStream = new FileInputStream(inputFile))
        {
            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = fileInputStream.read(buffer)) != -1)
            {
                byte[] encryptedBytes = cipher.update(buffer, 0, bytesRead);
                if (encryptedBytes != null && encryptedBytes.length > 0)
                {
                    encryptedData.add(encryptedBytes);
                }
            }

            byte[] finalBytes = cipher.doFinal();

            if (finalBytes != null)
            {
                encryptedData.add(finalBytes);
            }
        }

        try(FileOutputStream fileOutputStream = new FileOutputStream(outputFile))
        {
            for (byte[] encryptedBytes : encryptedData)
            {
                fileOutputStream.write(encryptedBytes);
            }
        }
    }

    public static void encryptDirectory(File directory, String password)
    {
        //List is better than array cause of adding/deleting elements is O(1)
        List<File> queue = new ArrayList<>(Arrays.asList(Objects.requireNonNull(directory.listFiles())));

        while (!queue.isEmpty())
        {
            File file = queue.removeFirst();

            if (file.isDirectory())
            {
                encryptDirectory(file, password);
            }

            else
            {
                try
                {
                    encrypt(file, password);
                }

                catch (Exception e)
                {
                    System.out.println("Could not encrypt file: " + file.getAbsolutePath());
                }
            }
        }
    }

    public static void decrypt(File inputFile, String password) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IOException, IllegalBlockSizeException, BadPaddingException
    {
        decrypt(inputFile, inputFile, password);
    }

    public static void decrypt(File inputFile, File outputFile, String password) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,BadPaddingException,IllegalBlockSizeException, IOException {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, getKeyFromPassword(password));

        if (!outputFile.exists())
        {
            if(!outputFile.createNewFile())
            {
                System.out.println("fatal error");
                System.exit(1);
            }

            System.out.println("output file created");
        }

        List<byte[]> decryptedData = new ArrayList<>();

        try(FileInputStream fileInputStream = new FileInputStream(inputFile))
        {
            byte[] buffer = new byte[1024];
            int bytesRead;

            while((bytesRead = fileInputStream.read(buffer)) != -1)
            {
                byte[] decryptedBytes = cipher.update(buffer, 0, bytesRead);
                if (decryptedBytes != null)
                {
                    decryptedData.add(decryptedBytes);
                }
            }

            byte[] finalBytes = cipher.doFinal();
            if (finalBytes != null)
            {
                decryptedData.add(finalBytes);
            }

            try (FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
                for (byte[] data : decryptedData) {
                    fileOutputStream.write(data);
                }
            }
        }

        catch (IOException e)
        {
            System.out.println(e.getMessage());
        }
    }

    public static void decryptDirectory(File directory, String password)
    {
        //List is better than array cause of adding/deleting elements is O(1)
        List<File> queue = new ArrayList<>(Arrays.asList(Objects.requireNonNull(directory.listFiles())));

        while (!queue.isEmpty())
        {
            File file = queue.removeFirst();

            if (file.isDirectory())
            {
                decryptDirectory(file, password);
            }

            else
            {
                try
                {
                    decrypt(file, password);
                }

                catch (Exception e)
                {
                    System.out.println("Could not encrypt file: " + file.getAbsolutePath());
                }
            }
        }
    }

    private static SecretKey getKeyFromPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = sha.digest(password.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(keyBytes, 0, 16, ALGORITHM);
    }
}
