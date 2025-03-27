package io.dayfit.github.backgroundServices.services;

import io.dayfit.github.backgroundServices.managers.PathManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static org.mockito.Mockito.*;

@SpringBootTest
class ShutdownServiceTest {

    @Autowired
    private PathManager pathManager;

    private ShutdownService shutdownService;

    @BeforeEach
    void setUp() {
        pathManager = Mockito.mock(PathManager.class);
        shutdownService = new ShutdownService(pathManager);
    }

    @Test
    void onShutdownSavesProtectedPaths() throws IOException {
        shutdownService.onShutdown();
        verify(pathManager, times(1)).saveProtectedPaths();
    }

    @Test
    void onShutdownHandlesIOException() throws IOException {
        doThrow(new IOException("Test Exception")).when(pathManager).saveProtectedPaths();
        shutdownService.onShutdown();
        verify(pathManager, times(1)).saveProtectedPaths();
    }
}