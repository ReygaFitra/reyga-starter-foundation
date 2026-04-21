package reyga.starter.foundation.core.service.base;

import org.springframework.http.HttpStatus;
import reyga.starter.foundation.common.enumeration.ServiceCodeEnum;
import reyga.starter.foundation.common.logging.CommonLogger;
import reyga.starter.foundation.common.logging.InjectLogger;
import reyga.starter.foundation.common.model.dto.content.BaseContent;
import reyga.starter.foundation.common.model.dto.request.BaseRequest;
import reyga.starter.foundation.core.annotation.ExperimentalApi;
import reyga.starter.foundation.core.component.BaseTransactionalExecutor;
import reyga.starter.foundation.core.exception.AppFaultContent;
import reyga.starter.foundation.core.exception.AppFaultException;
import reyga.starter.foundation.core.service.foundation.FoundationBuilderService;
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
        if (useHttpServletParameter() && (req.getServletRequest() == null || req.getServletResponse() == null)) {
            throw new AppFaultException(AppFaultContent.builder()
                    .errorCode(ServiceCodeEnum.SERVLET_CONTEXT_NOT_FOUND.getCode())
                    .errorMessage(ServiceCodeEnum.SERVLET_CONTEXT_NOT_FOUND.getMessage())
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build());
        }
        logInformation(req);
        return orchestrate(req, content);
    }

    protected abstract R orchestrate(Q req, C content);

    protected abstract boolean useHttpServletParameter();

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
        this.processes.clear();
        this.endProcess = null;
        this.transactionalFallback = null;
        this.asyncMode = false;
        this.transactionalMode = false;
        this.initialized = true;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T addProcess(Runnable process) {
        processes.add(() -> {
            process.run();
            return null;
        });
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public <X> T addProcess(Supplier<X> process) {
        processes.add(process);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T endProcess(Runnable process) {
        this.endProcess = () -> {
            process.run();
            return null;
        };
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T endProcess(Supplier<R> process) {
        this.endProcess = process;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T validateRequest() {
        addProcess(() -> ValidationUtility.chain().validateRequest(this.request));
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public <G> T validateRequest(List<Class<G>> groupList) {
        addProcess(() -> ValidationUtility.chain().validateRequest(this.request, groupList));
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
