package io.dayfit.github.backgroundServices.managers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PathManagerTest {
    private PathManager pathManager;

    @BeforeEach
    void setUp() {
        pathManager = new PathManager(false);
    }

    @Test
    void addingProtectedPathIncreasesListSize() throws Exception {
        String path ="path/to/protect";
        pathManager.addProtectedPath(path);
        assertEquals(1, pathManager.getProtectedPaths().size());
    }

    @Test
    void removingProtectedPathDecreasesListSize() throws Exception{
        String path = "path/to/protect";
        pathManager.addProtectedPath(path);
        pathManager.removeProtectedPath(path);
        assertEquals(0, pathManager.getProtectedPaths().size());
    }

    @Test
    void getProtectedPathsReturnsCorrectPaths() throws Exception{
        String path1 = "path/to/protect1";
        String path2 = "path/to/protect2";
        pathManager.addProtectedPath(path1);
        pathManager.addProtectedPath(path2);
        Set<String> protectedPaths = pathManager.getProtectedPaths();
        assertTrue(protectedPaths.contains(path1));
        assertTrue(protectedPaths.contains(path2));
    }

    @Test
    void setProtectedPathsReplacesExistingPaths() throws Exception{
        String path1 = "path/to/protect1";
        String path2 = "path/to/protect2";
        pathManager.addProtectedPath(path1);
        pathManager.setProtectedPaths(new HashSet<>(Set.of(path2)));
        Set<String> protectedPaths = pathManager.getProtectedPaths();
        assertFalse(protectedPaths.contains(path1));
        assertTrue(protectedPaths.contains(path2));
    }
}