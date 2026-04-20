package reyga.starter.foundation.core.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import reyga.starter.foundation.common.enumeration.HeaderEnum;

public abstract class BaseExceptionHandler<T> {

    @ExceptionHandler({Exception.class})
    protected ResponseEntity<T> handleGlobalErrorException(Exception ex, HttpServletRequest request) {
        request.setAttribute(HeaderEnum.EXCEPTION.getValue(), ex);
        return processGlobalErrorHandler(ex, request);
    }

    @ExceptionHandler({JpaSystemException.class, DataAccessException.class})
    public ResponseEntity<T> handleDatabaseErrorException(Exception ex, HttpServletRequest request) {
        request.setAttribute(HeaderEnum.EXCEPTION.getValue(), ex);
        return processDatabaseErrorHandler(ex, request);
    }

    protected abstract ResponseEntity<T> processGlobalErrorHandler(Exception exception, HttpServletRequest servletRequest);

    protected abstract ResponseEntity<T> processDatabaseErrorHandler(Exception exception, HttpServletRequest servletRequest);
}
