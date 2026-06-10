package reyga.starter.foundation.core.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reyga.starter.foundation.common.enumeration.ServiceStatusResponseEnum;
import reyga.starter.foundation.common.model.dto.response.ResponseData;

/**
 * Base controller providing common utility methods for handling HTTP responses.
 * Extends {@link BaseHttpServletBuilder} to leverage servlet-related building capabilities.
 */
@RequiredArgsConstructor
public abstract class BaseController extends BaseHttpServletBuilder {

    /**
     * Creates a standard {@link ResponseEntity} with the provided data and HTTP status.
     *
     * @param data   the body of the response
     * @param status the HTTP status code
     * @param <T>    the type of the data
     * @return a {@link ResponseEntity} containing the data and status
     */
    protected <T> ResponseEntity<T> createResponse(T data, HttpStatus status) {
        return new ResponseEntity<>(data, status);
    }

    /**
     * Creates a {@link ResponseEntity} wrapped in a {@link ResponseData} envelope.
     *
     * @param data    the body of the response
     * @param status  the HTTP status code
     * @param code    the application-specific response code
     * @param message the descriptive message for the response
     * @param <T>     the type of the data
     * @return a {@link ResponseEntity} containing the wrapped {@link ResponseData}
     */
    protected <T> ResponseEntity<ResponseData<T>> createResponse(
            T data, HttpStatus status, String code, String message
    ) {
        ResponseData<T> responseData = buildResponseData(data, ServiceStatusResponseEnum.SUCCESS.getLabel(), code, message);
        return new ResponseEntity<>(responseData, status);
    }

}
