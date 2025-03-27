package io.dayfit.github.backgroundServices.services;

import io.dayfit.github.backgroundServices.managers.PathManager;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ShutdownService {
    private final PathManager pathManager;

    @Autowired
    public ShutdownService(PathManager pathManager) {
        this.pathManager = pathManager;
    }

    @PreDestroy
    public void onShutdown() {
        try {
            pathManager.saveProtectedPaths();
        } catch (IOException e) {
            System.err.println("Failed to save protected paths " + e.getMessage());
        }
    }
}
