package reyga.starter.foundation.core.service.base;

import org.springframework.http.HttpStatus;
import reyga.starter.foundation.common.enumeration.ServiceCodeEnum;
import reyga.starter.foundation.common.logging.CommonLogger;
import reyga.starter.foundation.common.logging.InjectLogger;
import reyga.starter.foundation.common.model.dto.request.BaseRequest;
import reyga.starter.foundation.core.exception.AppFaultContent;
import reyga.starter.foundation.core.exception.AppFaultException;
import reyga.starter.foundation.core.service.foundation.FoundationService;

/**
 * Abstract base class for all services in the foundation layer.
 * Implements the template method pattern for service execution.
 *
 * @param <T> The type of the request object, must extend {@link BaseRequest}
 * @param <R> The type of the response object
 */
public abstract class BaseService<T extends BaseRequest, R> implements FoundationService<T, R> {

    @InjectLogger
    protected CommonLogger logger;

    /**
     * Executes the service logic following a predefined workflow: validation, logging, and orchestration.
     *
     * @param request the service request
     * @return the service response
     */
    @Override
    public R execute(T request) {
        if (useHttpServletParameter() && (request.getServletRequest() == null || request.getServletResponse() == null)) {
                throw new AppFaultException(AppFaultContent.builder()
                        .errorCode(ServiceCodeEnum.SERVLET_CONTEXT_NOT_FOUND.getCode())
                        .errorMessage(ServiceCodeEnum.SERVLET_CONTEXT_NOT_FOUND.getMessage())
                        .statusCode(HttpStatus.INTERNAL_SERVER_ERROR)
                        .build());
            }


        logInformation(request);
        
        validateRequest(request);
        
        return orchestrate(request);
    }

    /**
     * Contains the core business logic of the service.
     *
     * @param request the service request
     * @return the service response
     */
    protected abstract R orchestrate(T request);

    /**
     * Validates the incoming request.
     *
     * @param request the service request
     * @throws RuntimeException if validation fails
     */
    protected abstract void validateRequest(T request);

    /**
     * Determines if the service requires HttpServletRequest and HttpServletResponse.
     * If true, the execution will fail if they are missing from the request.
     *
     * @return true if servlet parameters are required, false otherwise
     */
    protected abstract boolean useHttpServletParameter();

    /**
     * Logs the execution start and the request details.
     *
     * @param request the service request
     */
    protected void logInformation(T request) {
        if (logger != null) {
            logger.info("Executing Service...");
            logger.info("Request : ", String.valueOf(request));
        }
    }

}
