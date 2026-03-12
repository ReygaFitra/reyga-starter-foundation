package reyga.starter.foundation.core.service;

import reyga.starter.foundation.core.component.BaseTransactionalExecutor;
import reyga.starter.foundation.common.model.dto.content.BaseContent;
import reyga.starter.foundation.common.model.dto.request.BaseRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class BaseServiceBuilder<T extends BaseServiceBuilder<T, Q, R, C>, Q extends BaseRequest, R, C extends BaseContent> extends BaseTransactionalExecutor implements FoundationBuilderService<Q, R, C> {

    private final List<Supplier<?>> processes = new ArrayList<>();
    private Q request;
    private C content;
    private Supplier<R> endProcess;
    private boolean asyncMode = false;
    private boolean transactionalMode = false;
    private Function<Throwable, R> transactionalFallback;

    @Override
    public R execute(Q req, C content) {
        logInformation(req);
        return processFlow(req, content);
    }

    protected abstract R processFlow(Q req, C content);

    protected void logInformation(Q req) {
        log.info("Executing Service...");
        log.info("Request : {}", req.toString());
    }

    @SuppressWarnings("unchecked")
    public T service(Q request, C content) {
        this.request = request;
        this.content = content;
        processes.clear();
        registerProcesses(request, content);
        this.asyncMode = false;
        this.transactionalMode = false;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withAsync() {
        this.asyncMode = true;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withTransactional() {
        this.transactionalMode = true;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T onTransactionalFallback(Function<Throwable, R> fallback) {
        this.transactionalFallback = fallback;
        return (T) this;
    }

    protected abstract void registerProcesses(Q request, C content);

    protected void addProcess(Runnable process) {
        processes.add(() -> {
            process.run();
            return null;
        });
    }

    protected <X> void addProcess(Supplier<X> process) {
        processes.add(process);
    }

    protected void endProcess(Runnable process) {
        this.endProcess = () -> {
            process.run();
            return null;
        };
    }

    protected void endProcess(Supplier<R> process) {
        this.endProcess = process;
    }

    public R build() {
        if (asyncMode) {
            CompletableFuture<R> future = CompletableFuture.supplyAsync(this::executeProcesses);
            return future.join();
        }

        if (transactionalMode) {
            if (transactionalExecutor == null) {
                throw new IllegalStateException("TransactionalExecutor bean is not available. Ensure default starter module is enabled.");
            }
            return transactionalExecutor.runInTransaction(
                    this::executeProcesses,
                    ex -> {
                        log.error("Transaction fallback process message: {}", ex.getMessage(), ex);
                        if (transactionalFallback != null) {
                            return transactionalFallback.apply(ex);
                        }
                        throw new RuntimeException(ex);
                    }
            );
        }

        return executeProcesses();
    }

    protected Q getRequest() {
        return request;
    }

    protected C getContent() {
        return content;
    }

    private R executeProcesses() {
        for (Supplier<?> process : processes) {
            process.get();
        }

        if (endProcess == null) {
            throw new IllegalStateException("endProcess must be defined before build()");
        }

        return endProcess.get();
    }
}
