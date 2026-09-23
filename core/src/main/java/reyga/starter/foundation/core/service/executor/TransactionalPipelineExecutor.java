package reyga.starter.foundation.core.service.executor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

/**
 * A Spring component whose sole responsibility is to execute a given process
 * within a Spring-managed transaction. This ensures that the execution is
 * intercepted by Spring's transaction AOP proxy.
 */
@Component
public class TransactionalPipelineExecutor {

    /**
     * Executes the given supplier within a new transaction.
     * The transaction will roll back for any subclass of {@link Exception}.
     *
     * @param supplier The process to execute.
     * @param <R> The return type of the process.
     * @return The result of the supplier's execution.
     */
    @Transactional(rollbackFor = Exception.class)
    public <R> R execute(Supplier<R> supplier) {
        return supplier.get();
    }
}
