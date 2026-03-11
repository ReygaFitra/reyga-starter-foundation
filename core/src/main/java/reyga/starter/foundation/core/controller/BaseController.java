package reyga.starter.foundation.core.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reyga.starter.foundation.common.enumeration.ServiceStatusResponseEnum;
import reyga.starter.foundation.core.dto.request.BaseRequest;
import reyga.starter.foundation.core.dto.response.ResponseData;

@RequiredArgsConstructor
public abstract class BaseController extends ResponseErrorTemplate {

    protected <T> ResponseEntity<T> createResponse(T data, HttpStatus status) {
        return new ResponseEntity<>(data, status);
    }

    protected <T> ResponseEntity<ResponseData<T>> createResponse(
            T data, HttpStatus status, String code, String message
    ) {
        ResponseData<T> responseData = buildResponseData(data, ServiceStatusResponseEnum.SUCCESS.getValue(), code, message);
        return new ResponseEntity<>(responseData, status);
    }

    protected <T extends BaseRequest> T setServletRequestResponse(
            T request, HttpServletRequest servletRequest, HttpServletResponse servletResponse
    ) {
        request.setServletRequest(servletRequest);
        request.setServletResponse(servletResponse);
        return request;
    }

    protected <T extends BaseRequest> HttpServletRequest getServletRequest(T request) {
        return request.getServletRequest();
    }

    protected <T extends BaseRequest> HttpServletResponse getServletResponse(T request) {
        return request.getServletResponse();
    }

}
