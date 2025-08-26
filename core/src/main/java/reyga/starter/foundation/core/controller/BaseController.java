package reyga.starter.foundation.core.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reyga.starter.foundation.common.enumeration.ServiceStatusResponseEnum;
import reyga.starter.foundation.core.dto.response.ResponseData;

@RequiredArgsConstructor
public abstract class BaseController extends BaseResponseError {

    protected <T> ResponseEntity<T> createResponse(T data, HttpStatus status) {
        setMDCResponse(data);
        return new ResponseEntity<>(data, status);
    }

    protected <T> ResponseEntity<ResponseData<T>> createResponse(
            T data, HttpStatus status, String code, String message
    ) {
        ResponseData<T> responseData = buildResponseData(data, ServiceStatusResponseEnum.SUCCESS.getValue(), code, message);
        setMDCResponse(responseData);
        return new ResponseEntity<>(responseData, status);
    }

}
