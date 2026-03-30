package reyga.starter.foundation.core.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import reyga.starter.foundation.common.enumeration.HeaderEnum;
import reyga.starter.foundation.common.logging.BaseLogging;
import reyga.starter.foundation.common.model.dto.response.FieldErrorDetail;
import reyga.starter.foundation.common.util.DateUtil;
import reyga.starter.foundation.core.exception.AppFaultException;

import java.time.LocalDateTime;
import java.util.List;

public abstract class BaseExceptionHandler<T> extends BaseLogging {

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

    @ExceptionHandler(AppFaultException.class)
    public ResponseEntity<T> handleAppFaultException(AppFaultException ex, HttpServletRequest request) {
        request.setAttribute(HeaderEnum.EXCEPTION.getValue(), ex);
        return processAppFaultErrorHandler(ex, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<T> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        request.setAttribute(HeaderEnum.EXCEPTION.getValue(), ex);
        List<FieldErrorDetail> fieldErrorDetails = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> FieldErrorDetail.builder()
                        .field(error.getField())
                        .message(error.getDefaultMessage())
                        .timestamp(DateUtil.getTimestamp(LocalDateTime.now()))
                        .build())
                .toList();
        return processMethodArgumentNotValidException(ex, request, fieldErrorDetails);
    }

    protected abstract ResponseEntity<T> processGlobalErrorHandler(Exception exception, HttpServletRequest servletRequest);

    protected abstract ResponseEntity<T> processDatabaseErrorHandler(Exception exception, HttpServletRequest servletRequest);

    protected abstract ResponseEntity<T> processAppFaultErrorHandler(AppFaultException appFaultException, HttpServletRequest servletRequest);

    protected abstract ResponseEntity<T> processMethodArgumentNotValidException(MethodArgumentNotValidException methodArgumentNotValidException, HttpServletRequest servletRequest, List<FieldErrorDetail> fieldErrorDetails);
}
