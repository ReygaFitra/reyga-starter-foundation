package reyga.starter.foundation.common.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.MappedSuperclass;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import reyga.starter.foundation.common.model.dto.content.BaseContent;
import reyga.starter.foundation.common.model.dto.content.EmptyContent;
import reyga.starter.foundation.common.model.dto.request.BaseRequest;
import reyga.starter.foundation.common.model.dto.request.EmptyRequest;
import reyga.starter.foundation.common.model.dto.request.RequestLogging;
import reyga.starter.foundation.common.model.dto.response.*;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class CommonDtoTest {

    @Test
    void should_StoreNodeIds_When_ContentAccessorsAreUsed() {
        // Given
        EmptyContent content = new EmptyContent();
        Map<String, UUID> nodeIds = Map.of("node", UUID.randomUUID());

        // When
        content.setNodeId(nodeIds);

        // Then
        assertSame(nodeIds, content.getNodeId());
        assertInstanceOf(BaseContent.class, content);
        assertInstanceOf(Serializable.class, content);
        assertTrue(BaseContent.class.isAnnotationPresent(MappedSuperclass.class));
    }

    @Test
    void should_StoreServletObjects_When_RequestAccessorsAreUsed() {
        // Given
        EmptyRequest request = new EmptyRequest();
        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        HttpServletResponse servletResponse = mock(HttpServletResponse.class);

        // When
        request.setServletRequest(servletRequest);
        request.setServletResponse(servletResponse);

        // Then
        assertSame(servletRequest, request.getServletRequest());
        assertSame(servletResponse, request.getServletResponse());
        assertInstanceOf(BaseRequest.class, request);
        assertInstanceOf(Serializable.class, request);
        assertTrue(BaseRequest.class.isAnnotationPresent(MappedSuperclass.class));
    }

    @Test
    void should_ReturnAllLoggingFields_When_RequestLoggingIsConstructed() {
        // Given
        Map<String, Object> params = Map.of("query", "value");
        Map<String, Object> multipart = Map.of("file", "name.txt");
        Map<String, Object> paths = Map.of("id", 1);
        Object body = Map.of("name", "value");

        // When
        RequestLogging logging = new RequestLogging(params, multipart, paths, body);
        String result = logging.toString();

        // Then
        assertSame(params, logging.getRequestParams());
        assertSame(multipart, logging.getRequestMultiPart());
        assertSame(paths, logging.getRequestPathVariable());
        assertSame(body, logging.getRequestBody());
        assertEquals("{requestParams={query=value}, requestMultiPart={file=name.txt}, requestPathVariable={id=1}, requestBody={name=value}}", result);
    }

    @Test
    void should_UpdateLoggingFields_When_SettersAreUsed() {
        // Given
        RequestLogging logging = new RequestLogging();
        Map<String, Object> value = Map.of("key", "value");

        // When
        logging.setRequestParams(value);
        logging.setRequestMultiPart(value);
        logging.setRequestPathVariable(value);
        logging.setRequestBody("body");

        // Then
        assertSame(value, logging.getRequestParams());
        assertSame(value, logging.getRequestMultiPart());
        assertSame(value, logging.getRequestPathVariable());
        assertEquals("body", logging.getRequestBody());
    }

    @Test
    void should_ReturnAllFieldErrorValues_When_BuilderAndToBuilderAreUsed() {
        // Given
        Timestamp timestamp = Timestamp.valueOf("2024-02-29 10:15:30");

        // When
        FieldErrorDetail original = FieldErrorDetail.builder().field("name").message("required").timestamp(timestamp).build();
        FieldErrorDetail copy = original.toBuilder().message("invalid").build();

        // Then
        assertEquals("name", original.getField());
        assertEquals("required", original.getMessage());
        assertSame(timestamp, original.getTimestamp());
        assertEquals("name", copy.getField());
        assertEquals("invalid", copy.getMessage());
        assertSame(timestamp, copy.getTimestamp());
    }

    @Test
    void should_ReturnAllFileErrorValues_When_AllArgumentsConstructorIsUsed() {
        // Given
        String fileName = "data.csv";

        // When
        FileErrorDetail detail = new FileErrorDetail(fileName, "READ", "text/csv", "UTF-8",
                false, 100L, 200L, 10L, 20L);

        // Then
        assertEquals("data.csv", detail.getFileName());
        assertEquals("READ", detail.getOperation());
        assertEquals("text/csv", detail.getMimeType());
        assertEquals("UTF-8", detail.getCharset());
        assertFalse(detail.isDirectory());
        assertEquals(100L, detail.getSizeBytes());
        assertEquals(200L, detail.getLastModifiedEpochMillis());
        assertEquals(10L, detail.getOffset());
        assertEquals(20L, detail.getLength());
        assertEquals("data.csv", detail.toBuilder().build().getFileName());
    }

    @Test
    void should_ReturnAllResponseErrorDetailValues_When_BuilderIsUsed() {
        // Given
        Timestamp timestamp = Timestamp.valueOf("2024-02-29 10:15:30");
        List<FieldErrorDetail> fields = List.of(new FieldErrorDetail());
        List<FileErrorDetail> files = List.of(new FileErrorDetail());

        // When
        ResponseErrorDetail detail = ResponseErrorDetail.builder().business("business")
                .additionalInfo("info").timestamp(timestamp).requestFieldDetails(fields).fileDetails(files).build();

        // Then
        assertEquals("business", detail.getBusiness());
        assertEquals("info", detail.getAdditionalInfo());
        assertSame(timestamp, detail.getTimestamp());
        assertSame(fields, detail.getRequestFieldDetails());
        assertSame(files, detail.getFileDetails());
        assertEquals("business", detail.toBuilder().build().getBusiness());
    }

    @Test
    void should_ReturnCompleteResponseData_When_ConstructorAndBuilderAreUsed() {
        // Given
        Map<String, Integer> data = Map.of("total", 1);

        // When
        ResponseData<Map<String, Integer>> response = new ResponseData<>("SUCCESS", "200000", "OK", data);
        ResponseData<Map<String, Integer>> copy = response.toBuilder().message("Updated").build();

        // Then
        assertEquals("SUCCESS", response.getStatus());
        assertEquals("200000", response.getCode());
        assertEquals("OK", response.getMessage());
        assertSame(data, response.getData());
        assertEquals("{status='SUCCESS', code='200000', message='OK', data={total=1}}", response.toString());
        assertEquals("Updated", copy.getMessage());
        assertSame(data, copy.getData());
    }

    @Test
    void should_SupportResponseDataConstructorVariants_When_OptionalValuesAreUsed() {
        // Given
        String data = "data";

        // When
        ResponseData<String> empty = new ResponseData<>();
        ResponseData<String> statusOnly = new ResponseData<>("SUCCESS", "200000", "OK");
        ResponseData<String> dataOnly = new ResponseData<>(data);

        // Then
        assertNull(empty.getData());
        assertEquals("SUCCESS", statusOnly.getStatus());
        assertNull(statusOnly.getData());
        assertEquals("data", dataOnly.getData());
        assertNull(dataOnly.getStatus());
    }

    @Test
    void should_ReturnCompleteResponseError_When_ConstructorAndBuilderAreUsed() {
        // Given
        ResponseErrorDetail detail = new ResponseErrorDetail();

        // When
        ResponseError response = new ResponseError("FAILED", "400441", "Invalid", detail);
        ResponseError copy = response.toBuilder().message("Updated").build();

        // Then
        assertEquals("FAILED", response.getStatus());
        assertEquals("400441", response.getCode());
        assertEquals("Invalid", response.getMessage());
        assertSame(detail, response.getDetails());
        assertEquals("Updated", copy.getMessage());
        assertSame(detail, copy.getDetails());
        assertEquals(JsonInclude.Include.NON_NULL, ResponseError.class.getAnnotation(JsonInclude.class).value());
    }

    @Test
    void should_SupportResponseErrorConstructorVariants_When_OptionalValuesAreUsed() {
        // Given
        ResponseErrorDetail detail = new ResponseErrorDetail();

        // When
        ResponseError empty = new ResponseError();
        ResponseError statusOnly = new ResponseError("FAILED", "500599", "Error");
        ResponseError detailOnly = new ResponseError(detail);

        // Then
        assertNull(empty.getDetails());
        assertEquals("FAILED", statusOnly.getStatus());
        assertNull(statusOnly.getDetails());
        assertSame(detail, detailOnly.getDetails());
        assertNull(detailOnly.getStatus());
    }

    @Test
    void should_CreateStatusAndEmptyResponses_When_ConstructorsAndBuildersAreUsed() {
        // Given
        String statusValue = "SUCCESS";

        // When
        ResponseStatusOnly status = ResponseStatusOnly.builder().status(statusValue).code("200000").message("OK").build();
        EmptyResponse empty = new EmptyResponse();

        // Then
        assertEquals("SUCCESS", status.getStatus());
        assertEquals("200000", status.getCode());
        assertEquals("OK", status.getMessage());
        assertNotNull(empty);
        assertInstanceOf(Serializable.class, empty);
        assertTrue(BaseResponse.class.isAnnotationPresent(MappedSuperclass.class));
    }
}
