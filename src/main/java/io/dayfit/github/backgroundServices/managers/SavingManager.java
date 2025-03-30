package io.dayfit.github.backgroundServices.managers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

public class SavingManager implements Runnable {

    private final Path path;
    private final String text;

    public SavingManager(Path path, String text)
    {
        this.path = path;
        this.text = text;
    }

    @Override
    public void run()
    {
        try {
            saveFile();
        } catch (IOException e) {
            System.err.println("An error occurred "+e.getMessage());
        }
    }

    private void saveFile() throws IOException {
        if (path.toFile().exists())
        {
            throw new NoSuchFileException("No such file has been found: "+path.toAbsolutePath());
        }

        Files.writeString(path, text);
    }
}
