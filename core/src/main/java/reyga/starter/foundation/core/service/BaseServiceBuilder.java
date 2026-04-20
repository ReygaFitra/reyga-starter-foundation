package reyga.starter.foundation.core.service;

import reyga.starter.foundation.common.logging.CommonLogger;
import reyga.starter.foundation.common.logging.InjectLogger;
import reyga.starter.foundation.common.model.dto.content.BaseContent;
import reyga.starter.foundation.common.model.dto.request.BaseRequest;
import reyga.starter.foundation.core.annotation.ExperimentalApi;
import reyga.starter.foundation.core.component.BaseTransactionalExecutor;
import reyga.starter.foundation.core.validation.ValidationUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class BaseServiceBuilder<
        T extends BaseServiceBuilder<T, Q, R, C>,
        Q extends BaseRequest,
        R,
        C extends BaseContent
        > extends BaseTransactionalExecutor implements FoundationBuilderService<Q, R, C> {

    @InjectLogger
    protected CommonLogger logger;

    private final List<Supplier<?>> processes = new ArrayList<>();
    private Q request;
    private C content;
    private Supplier<R> endProcess;
    private boolean asyncMode = false;
    private boolean transactionalMode = false;
    private Function<Throwable, R> transactionalFallback;
    private boolean initialized = false;

    @Override
    public R execute(Q req, C content) {
        logInformation(req);
        return processFlow(req, content);
    }

    protected abstract R processFlow(Q req, C content);

    protected abstract void registerProcesses(ProcessContext<Q, C> ctx);

    protected void logInformation(Q req) {
        if (logger != null) {
            logger.info("Executing Service...");
            logger.info("Request : ", String.valueOf(req));
        }
    }

    @SuppressWarnings("unchecked")
    public T service(Q request, C content) {
        this.request = Objects.requireNonNull(request, "request must not be null");
        this.content = Objects.requireNonNull(content, "content must not be null");
        processes.clear();
        this.endProcess = null;
        this.transactionalFallback = null;
        this.asyncMode = false;
        this.transactionalMode = false;
        this.initialized = true;
        registerProcesses(new DefaultProcessContext());
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @ExperimentalApi("Async execution mode is experimental and may change.")
    public T withAsync() {
        this.asyncMode = true;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @ExperimentalApi("Transactional execution mode is experimental and may change.")
    public T withTransactional() {
        this.transactionalMode = true;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @ExperimentalApi("Transactional fallback is experimental and may change.")
    public T onTransactionalFallback(Function<Throwable, R> fallback) {
        this.transactionalFallback = fallback;
        return (T) this;
    }

    public R build() {
        this.ensureInitialized();

        if (endProcess == null) {
            throw new IllegalStateException("endProcess must be defined before build()");
        }

        List<Supplier<?>> processSnapshot = List.copyOf(processes);
        Supplier<R> endProcessSnapshot = endProcess;

        if (asyncMode && transactionalMode)
            return this.transactionalAsyncBuilder(processSnapshot, endProcessSnapshot);

        if (asyncMode)
            return this.asyncBuilder(processSnapshot, endProcessSnapshot);

        if (transactionalMode)
            return this.transactionalBuilder(processSnapshot, endProcessSnapshot);

        return executeProcesses(processSnapshot, endProcessSnapshot);
    }

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

    protected void validateRequest(Q request) {
        addProcess(() -> ValidationUtility.chain().validateRequest(request));
    }

    protected <G> void validateRequest(Q request, List<Class<G>> groupList) {
        addProcess(() -> ValidationUtility.chain().validateRequest(request, groupList));
    }

    protected Q getRequest() {
        return request;
    }

    protected C getContent() {
        return content;
    }

    private R executeProcesses(List<Supplier<?>> processSnapshot, Supplier<R> endProcessSnapshot) {
        for (Supplier<?> process : processSnapshot) {
            process.get();
        }
        return endProcessSnapshot.get();
    }

    private void ensureInitialized() {
        if (!initialized) {
            throw new IllegalStateException("service(request, content) must be called before build()");
        }
    }

    protected interface ProcessContext<Q extends BaseRequest, C extends BaseContent> {
        Q getRequest();

        C getContent();

        void validateRequest(Q request);

        <G> void validateRequest(Q request, List<Class<G>> groupList);

        void addProcess(Runnable process);

        <X> void addProcess(Supplier<X> process);

        void endProcess(Runnable process);

        void endProcess(Supplier<?> process);
    }

    private class DefaultProcessContext implements ProcessContext<Q, C> {
        @Override
        public Q getRequest() {
            return BaseServiceBuilder.this.getRequest();
        }

        @Override
        public C getContent() {
            return BaseServiceBuilder.this.getContent();
        }

        @Override
        public void validateRequest(Q request) {
            BaseServiceBuilder.this.validateRequest(request);
        }

        @Override
        public <G> void validateRequest(Q request, List<Class<G>> groupList) {
            BaseServiceBuilder.this.validateRequest(request, groupList);
        }

        @Override
        public void addProcess(Runnable process) {
            BaseServiceBuilder.this.addProcess(process);
        }

        @Override
        public <X> void addProcess(Supplier<X> process) {
            BaseServiceBuilder.this.addProcess(process);
        }

        @Override
        public void endProcess(Runnable process) {
            BaseServiceBuilder.this.endProcess(process);
        }

        @Override
        @SuppressWarnings("unchecked")
        public void endProcess(Supplier<?> process) {
            BaseServiceBuilder.this.endProcess((Supplier<R>) process);
        }
    }

    private R transactionalAsyncBuilder(List<Supplier<?>> processSnapshot, Supplier<R> endProcessSnapshot) {
        if (transactionalExecutor == null) {
            throw new IllegalStateException("TransactionalExecutor bean is not available. Ensure default starter module is enabled.");
        }
        CompletableFuture<R> future = CompletableFuture.supplyAsync(
                () -> transactionalExecutor.runInTransaction(
                        () -> executeProcesses(processSnapshot, endProcessSnapshot),
                        ex -> {
                            if (logger != null) {
                                logger.error("Transaction fallback process message: {}", ex.getMessage(), ex);
                            }
                            if (transactionalFallback != null) {
                                return transactionalFallback.apply(ex);
                            }
                            throw new RuntimeException(ex);
                        }
                )
        );
        return future.join();
    }

    private R asyncBuilder(List<Supplier<?>> processSnapshot, Supplier<R> endProcessSnapshot) {
        CompletableFuture<R> future = CompletableFuture.supplyAsync(
                () -> executeProcesses(processSnapshot, endProcessSnapshot)
        );
        return future.join();
    }

    private R transactionalBuilder(List<Supplier<?>> processSnapshot, Supplier<R> endProcessSnapshot) {
        if (transactionalExecutor == null) {
            throw new IllegalStateException("TransactionalExecutor bean is not available. Ensure default starter module is enabled.");
        }
        return transactionalExecutor.runInTransaction(
                () -> executeProcesses(processSnapshot, endProcessSnapshot),
                ex -> {
                    if (logger != null) {
                        logger.error("Transaction fallback process message: {}", ex.getMessage(), ex);
                    }
                    if (transactionalFallback != null) {
                        return transactionalFallback.apply(ex);
                    }
                    throw new RuntimeException(ex);
                }
        );
    }
}
