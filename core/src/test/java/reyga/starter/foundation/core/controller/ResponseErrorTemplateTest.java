package reyga.starter.foundation.core.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reyga.starter.foundation.common.enumeration.ServiceStatusResponseEnum;
import reyga.starter.foundation.common.model.dto.response.ResponseError;

import static org.junit.jupiter.api.Assertions.*;

class ResponseErrorTemplateTest {

    @Test
    void createErrorResponse_setsStatusAndBody() {
        ResponseEntity<ResponseError> response = ResponseErrorTemplate.createErrorResponse(
                HttpStatus.BAD_REQUEST, "01", "invalid"
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ServiceStatusResponseEnum.FAILED.getValue(), response.getBody().getStatus());
        assertEquals("01", response.getBody().getCode());
        assertEquals("invalid", response.getBody().getMessage());
    }
}
