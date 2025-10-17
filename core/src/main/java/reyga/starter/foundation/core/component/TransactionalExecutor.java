package reyga.starter.foundation.core.component;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class TransactionalExecutor {

    private final PlatformTransactionManager transactionManager;

    public <T> T runInTransaction(Supplier<T> supplier, Function<Throwable, T> fallback) {
        return new TransactionTemplate(transactionManager).execute(status -> {
            try {
                return supplier.get();
            } catch (Exception ex) {
                status.setRollbackOnly();
                return fallback.apply(ex);
            }
        });
    }

    public void runInTransaction(Runnable runnable, Consumer<Throwable> fallback) {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            try {
                runnable.run();
            } catch (Exception ex) {
                status.setRollbackOnly();
                fallback.accept(ex);
            }
        });
    }
}
