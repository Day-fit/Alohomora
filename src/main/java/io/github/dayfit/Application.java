package io.github.dayfit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Application {
    final static PathManager pathManager = new PathManager();

    public static void main(String[] args) throws IOException {
        try {
            final Path PROTECTED_PATHS_PATH = Path.of(pathManager.PATH_MANAGER_FILE);

            //To avoid unchecked warning we use pathsRaw List
            if (Files.size(PROTECTED_PATHS_PATH) > 0) {
                List<?> pathsRaw = JSON.fromJSON(String.join("", Files.readAllLines(PROTECTED_PATHS_PATH)), List.class);
                List<Path> protectedPaths = pathsRaw.stream()
                        .map(element -> Paths.get(element.toString().replace("file:///", "")))
                        .toList();
                pathManager.setProtectedPaths(protectedPaths);
            }
        }catch (NoSuchFileException e) {
            System.out.println("No protected paths found");
        }

        new CLIHandler(args, pathManager);

        Runtime.getRuntime().addShutdownHook(new Thread(() ->
        {
            try {
                if (!pathManager.getProtectedPaths().isEmpty())
                {
                    pathManager.saveProtectedPaths();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        ));
    }
}
