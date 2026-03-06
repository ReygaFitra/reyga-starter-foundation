package reyga.starter.foundation.core.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import reyga.starter.foundation.common.enumeration.HeaderEnum;
import reyga.starter.foundation.common.logging.BaseLogging;
import reyga.starter.foundation.core.exception.AppFaultException;

public abstract class BaseExceptionHandler<ER> extends BaseLogging {

    @ExceptionHandler({Exception.class})
    protected ResponseEntity<ER> handleGlobalErrorException(Exception e, HttpServletRequest request) {
        request.setAttribute(HeaderEnum.EXCEPTION.getValue(), e);
        log.exception("EXCEPTION", null,e);
        return processGlobalErrorHandler(e, request);
    }

    @ExceptionHandler({JpaSystemException.class, DataAccessException.class})
    public ResponseEntity<ER> handleDatabaseErrorException(Exception e, HttpServletRequest request) {
        request.setAttribute(HeaderEnum.EXCEPTION.getValue(), e);
        log.exception("DATABASE EXCEPTION", null,e);
        return processDatabaseErrorHandler(e, request);
    }

    @ExceptionHandler(AppFaultException.class)
    public ResponseEntity<ER> handleAppFaultException(AppFaultException appFaultException, HttpServletRequest request) {
        request.setAttribute(HeaderEnum.EXCEPTION.getValue(), appFaultException);
        log.exception("APP_FAULT_EXCEPTION", appFaultException.getFaultInfo(), appFaultException);
        return processAppFaultErrorHandler(appFaultException, request);
    }

    protected abstract ResponseEntity<ER> processGlobalErrorHandler(Exception exception, HttpServletRequest servletRequest);

    protected abstract ResponseEntity<ER> processDatabaseErrorHandler(Exception exception, HttpServletRequest servletRequest);

    protected abstract ResponseEntity<ER> processAppFaultErrorHandler(AppFaultException appFaultException, HttpServletRequest servletRequest);
}
