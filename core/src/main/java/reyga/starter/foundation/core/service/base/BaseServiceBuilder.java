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

/**
 * Abstract base class for building and orchestrating service logic using a fluent builder pattern.
 *
 * @param <T> The type of the builder implementation (self-referencing)
 * @param <Q> The type of the request object
 * @param <R> The type of the response/result object
 * @param <C> The type of the content/context object
 */
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

    /**
     * Entry point for service execution. Validates servlet context if required and triggers orchestration.
     *
     * @param req     The request object
     * @param content The content/context object
     * @return The result of the service orchestration
     */
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

    /**
     * Abstract method to define the main orchestration logic of the service.
     */
    protected abstract R orchestrate(Q req, C content);

    /**
     * Determines if the service requires HttpServletRequest/Response to be present in the request object.
     *
     * @return true if servlet parameters are required
     */
    protected abstract boolean useHttpServletParameter();

    /**
     * Logs basic information about the service execution.
     *
     * @param req The request object to log
     */
    protected void logInformation(Q req) {
        if (logger != null) {
            logger.info("Executing Service...");
            logger.info("Request : ", String.valueOf(req));
        }
    }

    /**
     * Initializes the builder with the required request and content.
     *
     * @param request The request object
     * @param content The content object
     * @return The builder instance
     */
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

    /**
     * Adds a side-effect process (Runnable) to the execution chain.
     *
     * @param process The process to run
     * @return The builder instance
     */
    @SuppressWarnings("unchecked")
    public T addProcess(Runnable process) {
        processes.add(() -> {
            process.run();
            return null;
        });
        return (T) this;
    }

    /**
     * Adds a process (Supplier) to the execution chain.
     *
     * @param process The supplier to run
     * @return The builder instance
     */
    @SuppressWarnings("unchecked")
    public <X> T addProcess(Supplier<X> process) {
        processes.add(process);
        return (T) this;
    }

    /**
     * Defines the final process that produces the result of the service.
     *
     * @param process The runnable to execute before returning null
     * @return The builder instance
     */
    @SuppressWarnings("unchecked")
    public T endProcess(Runnable process) {
        this.endProcess = () -> {
            process.run();
            return null;
        };
        return (T) this;
    }

    /**
     * Defines the final process that produces the result of the service.
     *
     * @param process The supplier that returns the final result
     * @return The builder instance
     */
    @SuppressWarnings("unchecked")
    public T endProcess(Supplier<R> process) {
        this.endProcess = process;
        return (T) this;
    }

    /**
     * Adds a request validation step to the process chain.
     *
     * @return The builder instance
     */
    @SuppressWarnings("unchecked")
    public T validateRequest() {
        addProcess(() -> ValidationUtility.chain().validateRequest(this.request));
        return (T) this;
    }

    /**
     * Adds a request validation step with specific validation groups.
     *
     * @param groupList The list of validation groups
     * @return The builder instance
     */
    @SuppressWarnings("unchecked")
    public <G> T validateRequest(List<Class<G>> groupList) {
        addProcess(() -> ValidationUtility.chain().validateRequest(this.request, groupList));
        return (T) this;
    }

    /**
     * Enables asynchronous execution mode for the builder.
     */
    @SuppressWarnings("unchecked")
    @ExperimentalApi("Async execution mode is experimental and may change.")
    public T withAsync() {
        this.asyncMode = true;
        return (T) this;
    }

    /**
     * Enables transactional execution mode for the builder.
     */
    @SuppressWarnings("unchecked")
    @ExperimentalApi("Transactional execution mode is experimental and may change.")
    public T withTransactional() {
        this.transactionalMode = true;
        return (T) this;
    }

    /**
     * Defines a fallback function to handle exceptions during transactional execution.
     *
     * @param fallback The function to handle the error and return a result
     */
    @SuppressWarnings("unchecked")
    @ExperimentalApi("Transactional fallback is experimental and may change.")
    public T onTransactionalFallback(Function<Throwable, R> fallback) {
        this.transactionalFallback = fallback;
        return (T) this;
    }

    /**
     * Executes the built process chain based on the configured modes (async, transactional).
     *
     * @return The result of the execution
     */
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

    private R runTransactional(Supplier<R> action) {
        if (transactionalExecutor == null) {
            throw new IllegalStateException("TransactionalExecutor bean is not available. Ensure default starter module is enabled.");
        }
        return transactionalExecutor.runInTransaction(action, ex -> {
            if (logger != null) logger.error("Transaction fallback process message: {}", ex.getMessage(), ex);
            if (transactionalFallback != null) return transactionalFallback.apply(ex);
            throw new RuntimeException(ex);
        });
    }

    private R transactionalAsyncBuilder(List<Supplier<?>> processSnapshot, Supplier<R> endProcessSnapshot) {
        return CompletableFuture.supplyAsync(() -> runTransactional(() -> executeProcesses(processSnapshot, endProcessSnapshot))).join();
    }

    private R asyncBuilder(List<Supplier<?>> processSnapshot, Supplier<R> endProcessSnapshot) {
        return CompletableFuture.supplyAsync(() -> executeProcesses(processSnapshot, endProcessSnapshot)).join();
    }

    private R transactionalBuilder(List<Supplier<?>> processSnapshot, Supplier<R> endProcessSnapshot) {
        return runTransactional(() -> executeProcesses(processSnapshot, endProcessSnapshot));
    }
}
