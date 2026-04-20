package reyga.starter.foundation.core.service;

import org.springframework.http.HttpStatus;
import reyga.starter.foundation.common.enumeration.ServiceCodeEnum;
import reyga.starter.foundation.common.logging.CommonLogger;
import reyga.starter.foundation.common.logging.InjectLogger;
import reyga.starter.foundation.common.model.dto.request.BaseRequest;
import reyga.starter.foundation.core.exception.AppFaultContent;
import reyga.starter.foundation.core.exception.AppFaultException;

public abstract class BaseService<T extends BaseRequest, R> implements FoundationService<T, R> {

    @InjectLogger
    protected CommonLogger logger;

    @Override
    public R execute(T request) {
        if (request.getServletRequest() == null || request.getServletResponse() == null) {
            throw new AppFaultException(AppFaultContent.builder()
                    .errorCode(ServiceCodeEnum.SERVLET_CONTEXT_NOT_FOUND.getCode())
                    .errorMessage(ServiceCodeEnum.SERVLET_CONTEXT_NOT_FOUND.getMessage())
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build());
        }
        
        logInformation(request);
        
        validateRequest(request);
        
        return processFlow(request);
    }

    protected abstract R processFlow(T request);

    protected abstract void validateRequest(T request);

    protected void logInformation(T request) {
        if (logger != null) {
            logger.info("Executing Service...");
            logger.info("Request : ", String.valueOf(request));
        }
    }

}
