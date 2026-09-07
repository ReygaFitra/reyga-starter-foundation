package reyga.starter.foundation.core.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reyga.starter.foundation.common.enumeration.ServiceStatusResponseEnum;
import reyga.starter.foundation.common.model.dto.response.FieldErrorDetail;
import reyga.starter.foundation.common.model.dto.response.FileErrorDetail;
import reyga.starter.foundation.common.model.dto.response.ResponseError;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResponseErrorBuilderTest {

    @Test
    void should_ReturnBasicErrorResponse_When_BasicValuesAreProvided() {
        // Given
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;

        // When
        ResponseEntity<ResponseError> result = ResponseErrorBuilder.createErrorResponse(
                httpStatus, "01", "invalid"
        );

        // Then
        assertResponse(result, httpStatus, "01", "invalid");
        assertNull(result.getBody().getDetails());
    }

    @Test
    void should_ReturnDetailedErrorResponse_When_BusinessDetailsAreProvided() {
        // Given
        HttpStatus httpStatus = HttpStatus.CONFLICT;

        // When
        ResponseEntity<ResponseError> result = ResponseErrorBuilder.createErrorResponse(
                httpStatus, "02", "conflict", "orders", "duplicate order"
        );

        // Then
        assertResponse(result, httpStatus, "02", "conflict");
        assertNotNull(result.getBody().getDetails());
        assertEquals("orders", result.getBody().getDetails().getBusiness());
        assertEquals("duplicate order", result.getBody().getDetails().getAdditionalInfo());
        assertNotNull(result.getBody().getDetails().getTimestamp());
    }

    @Test
    void should_ReturnFieldErrorResponse_When_FieldErrorsAreProvided() {
        // Given
        List<FieldErrorDetail> fieldErrors = List.of(FieldErrorDetail.builder().field("email").message("invalid").build());

        // When
        ResponseEntity<ResponseError> result = ResponseErrorBuilder.createErrorResponse(
                HttpStatus.UNPROCESSABLE_ENTITY, "03", "validation failed", "customer", "invalid input", fieldErrors
        );

        // Then
        assertResponse(result, HttpStatus.UNPROCESSABLE_ENTITY, "03", "validation failed");
        assertNotNull(result.getBody().getDetails());
        assertSame(fieldErrors, result.getBody().getDetails().getRequestFieldDetails());
        assertNull(result.getBody().getDetails().getFileDetails());
    }

    @Test
    void should_ReturnFieldAndFileErrorResponse_When_AllErrorDetailsAreProvided() {
        // Given
        List<FieldErrorDetail> fieldErrors = List.of(FieldErrorDetail.builder().field("file").message("required").build());
        List<FileErrorDetail> fileErrors = List.of(FileErrorDetail.builder().fileName("input.csv").operation("upload").build());

        // When
        ResponseEntity<ResponseError> result = ResponseErrorBuilder.createErrorResponse(
                HttpStatus.BAD_REQUEST, "04", "invalid upload", "imports", "bad file", fieldErrors, fileErrors
        );

        // Then
        assertResponse(result, HttpStatus.BAD_REQUEST, "04", "invalid upload");
        assertNotNull(result.getBody().getDetails());
        assertSame(fieldErrors, result.getBody().getDetails().getRequestFieldDetails());
        assertSame(fileErrors, result.getBody().getDetails().getFileDetails());
    }

    private static void assertResponse(ResponseEntity<ResponseError> response, HttpStatus status,
                                       String code, String message) {
        assertEquals(status, response.getStatusCode());
        assertTrue(response.getHeaders().isEmpty());
        assertNotNull(response.getBody());
        assertEquals(ServiceStatusResponseEnum.FAILED.getLabel(), response.getBody().getStatus());
        assertEquals(code, response.getBody().getCode());
        assertEquals(message, response.getBody().getMessage());
    }
}
