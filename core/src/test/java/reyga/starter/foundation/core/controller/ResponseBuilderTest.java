package reyga.starter.foundation.core.controller;

import org.junit.jupiter.api.Test;
import reyga.starter.foundation.common.model.dto.response.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResponseBuilderTest {

    @Test
    void should_ReturnResponseData_When_AllValuesAreProvided() {
        // Given
        String data = "payload";

        // When
        ResponseData<String> result = TestResponseBuilder.responseData(data, "SUCCESS", "00", "ok");

        // Then
        assertEquals("SUCCESS", result.getStatus());
        assertEquals("00", result.getCode());
        assertEquals("ok", result.getMessage());
        assertEquals(data, result.getData());
    }

    @Test
    void should_ReturnBasicResponseError_When_DetailValuesAreNotProvided() {
        // Given
        String status = "FAILED";

        // When
        ResponseError result = TestResponseBuilder.responseError(status, "01", "invalid");

        // Then
        assertEquals(status, result.getStatus());
        assertEquals("01", result.getCode());
        assertEquals("invalid", result.getMessage());
        assertNull(result.getDetails());
    }

    @Test
    void should_ReturnDetailedResponseError_When_BusinessDetailsAreProvided() {
        // Given
        String business = "payments";

        // When
        ResponseError result = TestResponseBuilder.responseError(
                "FAILED", "02", "declined", business, "insufficient balance"
        );

        // Then
        assertBaseError(result, "02", "declined");
        assertNotNull(result.getDetails());
        assertEquals(business, result.getDetails().getBusiness());
        assertEquals("insufficient balance", result.getDetails().getAdditionalInfo());
        assertNotNull(result.getDetails().getTimestamp());
        assertNull(result.getDetails().getRequestFieldDetails());
        assertNull(result.getDetails().getFileDetails());
    }

    @Test
    void should_ReturnFieldErrorDetails_When_FieldErrorsAreProvided() {
        // Given
        List<FieldErrorDetail> fieldErrors = List.of(FieldErrorDetail.builder().field("email").message("invalid").build());

        // When
        ResponseError result = TestResponseBuilder.responseError(
                "FAILED", "03", "validation failed", "customer", "invalid request", fieldErrors
        );

        // Then
        assertBaseError(result, "03", "validation failed");
        assertNotNull(result.getDetails());
        assertSame(fieldErrors, result.getDetails().getRequestFieldDetails());
        assertNull(result.getDetails().getFileDetails());
        assertNotNull(result.getDetails().getTimestamp());
    }

    @Test
    void should_ReturnFieldAndFileErrorDetails_When_AllErrorDetailsAreProvided() {
        // Given
        List<FieldErrorDetail> fieldErrors = List.of(FieldErrorDetail.builder().field("name").message("required").build());
        List<FileErrorDetail> fileErrors = List.of(FileErrorDetail.builder().fileName("data.csv").operation("upload").build());

        // When
        ResponseError result = TestResponseBuilder.responseError(
                "FAILED", "04", "request failed", "imports", "invalid data", fieldErrors, fileErrors
        );

        // Then
        assertBaseError(result, "04", "request failed");
        assertNotNull(result.getDetails());
        assertEquals("imports", result.getDetails().getBusiness());
        assertEquals("invalid data", result.getDetails().getAdditionalInfo());
        assertSame(fieldErrors, result.getDetails().getRequestFieldDetails());
        assertSame(fileErrors, result.getDetails().getFileDetails());
        assertNotNull(result.getDetails().getTimestamp());
    }

    private static void assertBaseError(ResponseError result, String code, String message) {
        assertEquals("FAILED", result.getStatus());
        assertEquals(code, result.getCode());
        assertEquals(message, result.getMessage());
    }

    private static final class TestResponseBuilder extends ResponseBuilder {
        static <T> ResponseData<T> responseData(T data, String status, String code, String message) {
            return buildResponseData(data, status, code, message);
        }

        static ResponseError responseError(String status, String code, String message) {
            return buildResponseError(status, code, message);
        }

        static ResponseError responseError(String status, String code, String message, String business, String info) {
            return buildResponseError(status, code, message, business, info);
        }

        static ResponseError responseError(String status, String code, String message, String business, String info,
                                           List<FieldErrorDetail> fields) {
            return buildResponseError(status, code, message, business, info, fields);
        }

        static ResponseError responseError(String status, String code, String message, String business, String info,
                                           List<FieldErrorDetail> fields, List<FileErrorDetail> files) {
            return buildResponseError(status, code, message, business, info, fields, files);
        }
    }
}
