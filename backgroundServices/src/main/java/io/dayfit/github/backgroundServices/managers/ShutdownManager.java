package io.dayfit.github.backgroundServices.managers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Component
public class ShutdownManager {
    private final ApplicationContext context;

    @Autowired
    public ShutdownManager(ApplicationContext context) {
        this.context = context;
    }

    @Async
    public void shutdown() {
        CompletableFuture.runAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(1);
                int exitCode = SpringApplication.exit(context, () -> 0);
                System.exit(exitCode);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }
}