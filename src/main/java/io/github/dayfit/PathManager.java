package io.github.dayfit;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class PathManager {
    private Set<String> protectedPaths = new HashSet<>();
    final String PATH_MANAGER_FILE = "protectedPaths.json";

    PathManager(){
    }

    public Set<String> getProtectedPaths() {
        return protectedPaths;
    }

    public void setProtectedPaths(HashSet<String> protectedPaths) {
        this.protectedPaths = protectedPaths;
    }

    public void addProtectedPath(String protectedPath) {
        this.protectedPaths.add(protectedPath);
    }

    public void removeProtectedPath(String protectedPath) {
        this.protectedPaths.remove(protectedPath);
    }

    public void saveProtectedPaths() throws IOException {
        JSON.saveJSON(JSON.toJSON(this.protectedPaths), Path.of(PATH_MANAGER_FILE));
    }
}