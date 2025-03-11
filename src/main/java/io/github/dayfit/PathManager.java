package io.github.dayfit;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PathManager {
    private List<Path> protectedPaths = new ArrayList<>();
    final String PATH_MANAGER_FILE = "protectedPaths.json";

    PathManager(){
    }

    public List<Path> getProtectedPaths() {
        return protectedPaths;
    }

    public void setProtectedPaths(List<Path> protectedPaths)
    {
        this.protectedPaths = protectedPaths;
    }

    public void addProtectedPath(Path protectedPaths) {
        this.protectedPaths.add(protectedPaths);
    }

    public void removeProtectedPath(Path protectedPaths)
    {
        this.protectedPaths.remove(protectedPaths);
    }

    public void saveProtectedPaths() throws IOException {
        Path path = Path.of(PATH_MANAGER_FILE);
        JSON.saveJSON(JSON.toJSON(this.protectedPaths), path);
    }
}
