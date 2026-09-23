package reyga.starter.foundation.core.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reyga.starter.foundation.common.enumeration.ServiceStatusResponseEnum;
import reyga.starter.foundation.common.model.dto.response.ResponseData;

import static org.junit.jupiter.api.Assertions.*;

class BaseControllerTest {

    private final TestController controller = new TestController();

    @Test
    void should_ReturnRawResponse_When_DataAndStatusAreProvided() {
        // Given
        String data = "payload";
        HttpStatus status = HttpStatus.CREATED;

        // When
        ResponseEntity<String> result = controller.raw(data, status);

        // Then
        assertEquals(status, result.getStatusCode());
        assertEquals(data, result.getBody());
        assertTrue(result.getHeaders().isEmpty());
    }

    @Test
    void should_ReturnResponseWithNullBody_When_DataIsNull() {
        // Given
        String data = null;

        // When
        ResponseEntity<String> result = controller.raw(data, HttpStatus.NO_CONTENT);

        // Then
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        assertNull(result.getBody());
        assertTrue(result.getHeaders().isEmpty());
    }

    @Test
    void should_ReturnWrappedResponse_When_ResponseMetadataIsProvided() {
        // Given
        String data = "payload";

        // When
        ResponseEntity<ResponseData<String>> result = controller.wrapped(
                data, HttpStatus.ACCEPTED, "00", "accepted"
        );

        // Then
        assertEquals(HttpStatus.ACCEPTED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(ServiceStatusResponseEnum.SUCCESS.getLabel(), result.getBody().getStatus());
        assertEquals("00", result.getBody().getCode());
        assertEquals("accepted", result.getBody().getMessage());
        assertEquals(data, result.getBody().getData());
        assertTrue(result.getHeaders().isEmpty());
    }

    private static final class TestController extends BaseController {
        <T> ResponseEntity<T> raw(T data, HttpStatus status) {
            return createResponse(data, status);
        }

        <T> ResponseEntity<ResponseData<T>> wrapped(T data, HttpStatus status, String code, String message) {
            return createResponse(data, status, code, message);
        }
    }
}
