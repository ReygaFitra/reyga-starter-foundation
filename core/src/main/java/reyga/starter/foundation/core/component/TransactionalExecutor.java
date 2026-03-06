package reyga.starter.foundation.core.component;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface TransactionalExecutor {

    <T> T runInTransaction(Supplier<T> supplier, Function<Throwable, T> fallback);

    void runInTransaction(Runnable runnable, Consumer<Throwable> fallback);
}
