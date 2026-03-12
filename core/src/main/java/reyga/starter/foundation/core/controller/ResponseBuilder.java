package reyga.starter.foundation.core.controller;

import lombok.NoArgsConstructor;
import reyga.starter.foundation.common.logging.BaseLogging;
import reyga.starter.foundation.common.util.DateUtil;
import reyga.starter.foundation.common.model.dto.response.ResponseData;
import reyga.starter.foundation.common.model.dto.response.ResponseError;
import reyga.starter.foundation.common.model.dto.response.ResponseErrorDetail;

import java.time.LocalDateTime;

@NoArgsConstructor
public class ResponseBuilder extends BaseLogging {

    protected static <T> ResponseData<T> buildResponseData(T data, String status, String code, String message) {
        return ResponseData.<T>builder()
                .status(status)
                .code(code)
                .message(message)
                .data(data)
                .build();
    }

    protected static ResponseError buildResponseError(String status, String code, String message) {
        return ResponseError.Builder.newBuilder()
                .status(status)
                .code(code)
                .message(message)
                .build();
    }

    protected static ResponseError buildResponseError(String status, String code, String message, String business, String additionalInfo) {
        return ResponseError.Builder.newBuilder()
                .status(status)
                .code(code)
                .message(message)
                .details(ResponseErrorDetail.Builder.newBuilder()
                        .business(business)
                        .additionalInfo(additionalInfo)
                        .timestamp(DateUtil.getTimestamp(LocalDateTime.now()))
                        .build())
                .build();
    }
}
