package reyga.starter.foundation.core.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reyga.starter.foundation.common.enumeration.ServiceStatusResponseEnum;
import reyga.starter.foundation.core.dto.response.ResponseData;
import reyga.starter.foundation.core.dto.response.ResponseStaticTemplate;

@RequiredArgsConstructor
public abstract class BaseController extends ResponseStaticTemplate {

    protected <T> ResponseEntity<T> createResponse(T data, HttpStatus status) {
        setMDCResponse(data);
        return new ResponseEntity<>(data, status);
    }

    protected <T> ResponseEntity<ResponseData<T>> createResponse(
            T data, HttpStatus status, String code, String message
    ) {
        ResponseData<T> responseData = ResponseData.<T>builder()
                .status(ServiceStatusResponseEnum.SUCCESS.getValue())
                .code(code)
                .message(message)
                .data(data)
                .build();
        setMDCResponse(responseData);
        return new ResponseEntity<>(responseData, status);
    }

}
