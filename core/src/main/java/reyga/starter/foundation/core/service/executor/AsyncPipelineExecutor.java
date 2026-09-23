package reyga.starter.foundation.core.service.executor;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * A Spring component responsible for executing a given process asynchronously
 * using Spring's @Async annotation.
 */
@Component
public class AsyncPipelineExecutor {

    /**
     * Executes a supplier asynchronously and returns a CompletableFuture.
     */
    @Async
    public <R> CompletableFuture<R> execute(Supplier<R> supplier) {
        return CompletableFuture.supplyAsync(supplier);
    }

    /**
     * Executes a runnable task asynchronously (fire-and-forget).
     */
    @Async
    public void execute(Runnable task) {
        task.run();
    }
}
