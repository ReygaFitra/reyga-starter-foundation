package reyga.starter.foundation.core.service.base;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import reyga.starter.foundation.common.enumeration.ServiceCodeEnum;
import reyga.starter.foundation.common.logging.CommonLogger;
import reyga.starter.foundation.common.logging.InjectLogger;
import reyga.starter.foundation.common.model.dto.content.BaseContent;
import reyga.starter.foundation.common.model.dto.request.BaseRequest;
import reyga.starter.foundation.core.annotation.ExperimentalApi;
import reyga.starter.foundation.core.exception.AppFaultContent;
import reyga.starter.foundation.core.exception.AppFaultException;
import reyga.starter.foundation.core.service.executor.AsyncPipelineExecutor;
import reyga.starter.foundation.core.service.executor.TransactionalPipelineExecutor;
import reyga.starter.foundation.core.service.foundation.FoundationBuilderService;
import reyga.starter.foundation.core.validation.ValidationUtility;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Abstract base class for building and executing service pipelines with support for
 * validation, transaction management, and asynchronous execution.
 *
 * @param <T> The concrete type of the builder for fluent API chaining.
 * @param <Q> The type of the request object.
 * @param <R> The type of the response object.
 * @param <C> The type of the content/context object.
 */
public abstract class PipelineServiceBuilder<
        T extends PipelineServiceBuilder<T, Q, R, C>,
        Q extends BaseRequest,
        R,
        C extends BaseContent
        > implements FoundationBuilderService<Q, R, C> {

    @InjectLogger
    protected CommonLogger logger;

    @Autowired(required = false)
    private TransactionalPipelineExecutor transactionalExecutor;

    @Autowired(required = false)
    private AsyncPipelineExecutor asyncExecutor;

    private Q request;
    private C content;
    private boolean asyncMode = false;
    private boolean transactionalMode = false;
    private Function<Throwable, R> transactionalFallback;
    private boolean initialized = false;

    /**
     * Represents the initial state passed to the first step of the pipeline.
     *
     * @param request The service request.
     * @param content The service content/context.
     */
    protected record InitialState<Q extends BaseRequest, C extends BaseContent>(Q request, C content) {}

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
     * Orchestrates the service logic. Implementation should define the pipeline flow.
     *
     * @param req     The request object.
     * @param content The content object.
     * @return The service response.
     */
    protected abstract R orchestrate(Q req, C content);

    /**
     * Determines if the service requires HttpServletRequest and HttpServletResponse
     * to be present in the request object.
     *
     * @return true if servlet parameters are required.
     */
    protected abstract boolean useHttpServletParameter();

    /**
     * Logs basic information about the service execution.
     *
     * @param req The request object.
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
     * @param request The request object.
     * @param content The content object.
     * @return The builder instance.
     */
    @SuppressWarnings("unchecked")
    public T service(Q request, C content) {
        this.request = Objects.requireNonNull(request, "request must not be null");
        this.content = Objects.requireNonNull(content, "content must not be null");
        this.transactionalFallback = null;
        this.asyncMode = false;
        this.transactionalMode = false;
        this.initialized = true;
        return (T) this;
    }

    /**
     * Starts the pipeline with an initial step.
     *
     * @param firstStep A function defining the first operation.
     * @param <O>       The return type of the first step.
     * @return A ChainedPipeline instance to continue building.
     */
    public <O> ChainedPipeline<O> startPipe(Function<InitialState<Q, C>, O> firstStep) {
        ensureInitialized();
        return new ChainedPipeline<>(firstStep);
    }

    /**
     * Enables asynchronous execution for the pipeline.
     *
     * @return The builder instance.
     */
    @SuppressWarnings("unchecked")
    @ExperimentalApi("Async execution mode is experimental and may change.")
    public T withAsync() {
        this.asyncMode = true;
        return (T) this;
    }

    /**
     * Enables transactional execution for the pipeline.
     *
     * @return The builder instance.
     */
    @SuppressWarnings("unchecked")
    @ExperimentalApi("Transactional execution mode is experimental and may change.")
    public T withTransactional() {
        this.transactionalMode = true;
        return (T) this;
    }

    /**
     * Sets a fallback function to handle exceptions during transactional execution.
     *
     * @param fallback Function that takes a Throwable and returns a response.
     * @return The builder instance.
     */
    @SuppressWarnings("unchecked")
    @ExperimentalApi("Transactional fallback is experimental and may change.")
    public T onTransactionalFallback(Function<Throwable, R> fallback) {
        this.transactionalFallback = fallback;
        return (T) this;
    }

    protected Q getRequest() {
        return this.request;
    }

    private void ensureInitialized() {
        if (!initialized) {
            throw new IllegalStateException("service(request, content) must be called before using the pipeline.");
        }
    }

    /**
     * Inner class representing a stage in the service pipeline.
     *
     * @param <CURRENT_RESULT> The result type of the current stage.
     */
    public class ChainedPipeline<CURRENT_RESULT> {
        private final Function<InitialState<Q, C>, CURRENT_RESULT> composedFunction;

        private ChainedPipeline(Function<InitialState<Q, C>, CURRENT_RESULT> func) {
            this.composedFunction = func;
        }

        /**
         * Adds a validation step for the request object using default groups.
         *
         * @return The pipeline instance.
         */
        public ChainedPipeline<CURRENT_RESULT> doValidationRequest() {
            return andThen(result -> {
                ValidationUtility.chain().validateRequest(request);
                return result;
            });
        }

        /**
         * Adds a validation step for the request object using specific validation groups.
         *
         * @param groupList List of validation group classes.
         * @param <G>       The group type.
         * @return The pipeline instance.
         */
        public <G> ChainedPipeline<CURRENT_RESULT> doValidationRequest(List<Class<G>> groupList) {
            return andThen(result -> {
                ValidationUtility.chain().validateRequest(request, groupList);
                return result;
            });
        }

        /**
         * Chains a new synchronous step to the pipeline.
         *
         * @param nextStep    Function defining the next operation.
         * @param <NEXT_RESULT> The result type of the next step.
         * @return A new ChainedPipeline instance.
         */
        public <NEXT_RESULT> ChainedPipeline<NEXT_RESULT> andThen(Function<CURRENT_RESULT, NEXT_RESULT> nextStep) {
            Function<InitialState<Q, C>, NEXT_RESULT> newComposed = composedFunction.andThen(nextStep);
            return new ChainedPipeline<>(newComposed);
        }

        /**
         * Chains an asynchronous task to be executed after the current step.
         * The task does not modify the pipeline result.
         *
         * @param asyncTask Consumer representing the async operation.
         * @return The pipeline instance.
         */
        public ChainedPipeline<CURRENT_RESULT> andThenAsync(Consumer<CURRENT_RESULT> asyncTask) {
            return andThen(result -> {
                if (asyncExecutor == null) {
                    throw new IllegalStateException("AsyncPipelineExecutor bean is not available. Ensure @EnableAsync is configured.");
                }
                asyncExecutor.execute(() -> asyncTask.accept(result));
                return result;
            });
        }

        /**
         * Finalizes the pipeline and executes it based on the configured modes (Async/Transactional).
         *
         * @return The final service response.
         */
        @SuppressWarnings("unchecked")
        public R build() {
            Supplier<R> finalSupplier = () -> (R) composedFunction.apply(new InitialState<>(request, content));

            try {
                if (asyncMode) {
                    if (asyncExecutor == null) {
                        throw new IllegalStateException("AsyncPipelineExecutor bean is not available. Ensure @EnableAsync is configured.");
                    }
                    return asyncExecutor.execute(() -> executeTransactionally(finalSupplier)).join();
                }
                return executeTransactionally(finalSupplier);
            } catch (Exception e) {
                if (transactionalFallback != null) {
                    return transactionalFallback.apply(e);
                }
                throw e;
            }
        }
        
        private R executeTransactionally(Supplier<R> supplier) {
            if (transactionalMode) {
                if (transactionalExecutor == null) {
                    throw new IllegalStateException("TransactionalPipelineExecutor bean is not available. Ensure it is configured in your Spring context.");
                }
                return transactionalExecutor.execute(supplier);
            }
            return supplier.get();
        }
    }
}
