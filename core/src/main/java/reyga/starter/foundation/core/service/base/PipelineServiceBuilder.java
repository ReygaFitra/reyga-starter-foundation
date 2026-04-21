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
        this.transactionalFallback = null;
        this.asyncMode = false;
        this.transactionalMode = false;
        this.initialized = true;
        return (T) this;
    }

    public <O> ChainedPipeline<O> startPipe(Function<InitialState<Q, C>, O> firstStep) {
        ensureInitialized();
        return new ChainedPipeline<>(firstStep);
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

    protected Q getRequest() {
        return this.request;
    }

    private void ensureInitialized() {
        if (!initialized) {
            throw new IllegalStateException("service(request, content) must be called before using the pipeline.");
        }
    }

    public class ChainedPipeline<CURRENT_RESULT> {
        private final Function<InitialState<Q, C>, CURRENT_RESULT> composedFunction;

        private ChainedPipeline(Function<InitialState<Q, C>, CURRENT_RESULT> func) {
            this.composedFunction = func;
        }

        public ChainedPipeline<CURRENT_RESULT> doValidationRequest() {
            return andThen(result -> {
                ValidationUtility.chain().validateRequest(request);
                return result;
            });
        }

        public <G> ChainedPipeline<CURRENT_RESULT> doValidationRequest(List<Class<G>> groupList) {
            return andThen(result -> {
                ValidationUtility.chain().validateRequest(request, groupList);
                return result;
            });
        }

        public <NEXT_RESULT> ChainedPipeline<NEXT_RESULT> andThen(Function<CURRENT_RESULT, NEXT_RESULT> nextStep) {
            Function<InitialState<Q, C>, NEXT_RESULT> newComposed = composedFunction.andThen(nextStep);
            return new ChainedPipeline<>(newComposed);
        }

        public ChainedPipeline<CURRENT_RESULT> andThenAsync(Consumer<CURRENT_RESULT> asyncTask) {
            return andThen(result -> {
                if (asyncExecutor == null) {
                    throw new IllegalStateException("AsyncPipelineExecutor bean is not available. Ensure @EnableAsync is configured.");
                }
                asyncExecutor.execute(() -> asyncTask.accept(result));
                return result;
            });
        }

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
