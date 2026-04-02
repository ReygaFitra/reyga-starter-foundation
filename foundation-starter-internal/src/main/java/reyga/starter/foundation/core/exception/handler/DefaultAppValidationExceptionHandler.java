package reyga.starter.foundation.core.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import reyga.starter.foundation.common.enumeration.HeaderEnum;
import reyga.starter.foundation.common.enumeration.ServiceCodeEnum;
import reyga.starter.foundation.common.logging.BaseLogging;
import reyga.starter.foundation.common.model.dto.response.FieldErrorDetail;
import reyga.starter.foundation.common.model.dto.response.FileErrorDetail;
import reyga.starter.foundation.common.model.dto.response.ResponseError;
import reyga.starter.foundation.common.util.DateUtility;
import reyga.starter.foundation.common.util.FileUtility;
import reyga.starter.foundation.common_io.exception.IOFaultException;
import reyga.starter.foundation.common_io.exception.IOFaultMetadata;
import reyga.starter.foundation.core.controller.ResponseErrorTemplate;
import reyga.starter.foundation.core.exception.AppFaultException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class DefaultAppValidationExceptionHandler extends BaseLogging {

    @ExceptionHandler(AppFaultException.class)
    public ResponseEntity<ResponseError> handleAppFaultException(AppFaultException ex, HttpServletRequest request) {
        request.setAttribute(HeaderEnum.EXCEPTION.getValue(), ex);

        log.exception("AppFaultException".toUpperCase(), ex.getFaultInfo(), ex);
        return ResponseErrorTemplate.createErrorResponse(ex.getStatusCode(), ex.getErrorCode(), ex.getErrorMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseError> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        request.setAttribute(HeaderEnum.EXCEPTION.getValue(), ex);

        List<FieldErrorDetail> fieldErrorDetails = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> FieldErrorDetail.builder()
                        .field(error.getField())
                        .message(error.getDefaultMessage())
                        .timestamp(DateUtility.getTimestamp(LocalDateTime.now()))
                        .build())
                .toList();
        log.exception("MethodArgumentNotValidException".toUpperCase(), fieldErrorDetails, ex);

        return ResponseErrorTemplate.createErrorResponse(
                HttpStatus.BAD_REQUEST, ServiceCodeEnum.VALIDATION_ERROR.getCode(), ServiceCodeEnum.VALIDATION_ERROR.getMessage(),
                null, null, fieldErrorDetails
        );
    }

    @ExceptionHandler(IOFaultException.class)
    public ResponseEntity<ResponseError> handleIOFaultException(IOFaultException fex, HttpServletRequest request) {
        request.setAttribute(HeaderEnum.EXCEPTION.getValue(), fex);
        String message = fex.getMessage() != null ? fex.getMessage() : ServiceCodeEnum.FILE_ERROR.getMessage();

        List<FileErrorDetail> fileErrorDetailList = new ArrayList<>();
        if (fex.getMetadata() != null) {
            fileErrorDetailList.add(buildFileErrorDetail(fex.getMetadata()));
        }
        if (fex.getMetadataList() != null && !fex.getMetadataList().isEmpty()) {
            fileErrorDetailList.addAll(fex.getMetadataList().stream()
                    .map(this::buildFileErrorDetail)
                    .toList());
        }

        log.exception("IOFaultException".toUpperCase(), fileErrorDetailList, fex);

        return ResponseErrorTemplate.createErrorResponse(
                HttpStatus.BAD_REQUEST, ServiceCodeEnum.FILE_ERROR.getCode(), message,
                null, null, null, fileErrorDetailList.isEmpty() ? null : fileErrorDetailList
        );
    }

    private FileErrorDetail buildFileErrorDetail(IOFaultMetadata metadata) {
        String maskedFileName = metadata.getFileName() != null ? FileUtility.maskFileName(metadata.getFileName()) : null;
        long offset = metadata.getOffset() != null ? metadata.getOffset() : 0L;
        long length = metadata.getLength() != null ? metadata.getLength() : 0L;
        return FileErrorDetail.builder()
                .fileName(maskedFileName)
                .operation(String.valueOf(metadata.getOperation()))
                .mimeType(String.valueOf(metadata.getMimeType()))
                .charset(metadata.getCharset())
                .directory(metadata.isDirectory())
                .sizeBytes(metadata.getSizeBytes())
                .lastModifiedEpochMillis(metadata.getLastModifiedEpochMillis())
                .offset(offset)
                .length(length)
                .build();
    }
}
