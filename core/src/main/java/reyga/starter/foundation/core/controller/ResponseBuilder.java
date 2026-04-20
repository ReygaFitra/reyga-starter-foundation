package reyga.starter.foundation.core.controller;

import lombok.NoArgsConstructor;
import reyga.starter.foundation.common.logging.CommonLogger;
import reyga.starter.foundation.common.logging.InjectLogger;
import reyga.starter.foundation.common.model.dto.response.*;
import reyga.starter.foundation.common.util.DateUtility;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
public class ResponseBuilder {

    @InjectLogger
    protected CommonLogger logger;

    protected static <T> ResponseData<T> buildResponseData(T data, String status, String code, String message) {
        return ResponseData.<T>builder()
                .status(status)
                .code(code)
                .message(message)
                .data(data)
                .build();
    }

    protected static ResponseError buildResponseError(String status, String code, String message) {
        return ResponseError.builder()
                .status(status)
                .code(code)
                .message(message)
                .build();
    }

    protected static ResponseError buildResponseError(String status, String code, String message, String business, String additionalInfo) {
        return buildResponseError(status, code, message).toBuilder()
                .details(ResponseErrorDetail.builder()
                        .business(business)
                        .additionalInfo(additionalInfo)
                        .timestamp(DateUtility.getTimestamp(LocalDateTime.now()))
                        .build())
                .build();
    }

    protected static ResponseError buildResponseError(String status, String code, String message, String business, String additionalInfo, List<FieldErrorDetail> fieldErrorDetails) {
        return buildResponseError(status, code, message).toBuilder()
                .details(ResponseErrorDetail.builder()
                        .business(business)
                        .additionalInfo(additionalInfo)
                        .timestamp(DateUtility.getTimestamp(LocalDateTime.now()))
                        .requestFieldDetails(fieldErrorDetails)
                        .build())
                .build();
    }

    protected static ResponseError buildResponseError(String status, String code, String message, String business, String additionalInfo, List<FieldErrorDetail> requestFieldsErrorDetail, List<FileErrorDetail> fileErrorDetails) {
        return buildResponseError(status, code, message).toBuilder()
                .details(ResponseErrorDetail.builder()
                        .business(business)
                        .additionalInfo(additionalInfo)
                        .timestamp(DateUtility.getTimestamp(LocalDateTime.now()))
                        .requestFieldDetails(requestFieldsErrorDetail)
                        .fileDetails(fileErrorDetails)
                        .build())
                .build();
    }
}
