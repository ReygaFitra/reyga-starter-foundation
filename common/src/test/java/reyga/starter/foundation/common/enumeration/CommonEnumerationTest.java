package reyga.starter.foundation.common.enumeration;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class CommonEnumerationTest {

    @Test
    void should_ReturnConfiguredHeaderValues_When_AllHeadersAreRead() {
        // Given
        Map<HeaderEnum, String> expected = Map.ofEntries(
                Map.entry(HeaderEnum.ACCESS_TOKEN, "x-starter-access-token"),
                Map.entry(HeaderEnum.USERNAME, "x-starter-user-name"),
                Map.entry(HeaderEnum.REQUEST_ID, "x-starter-request-id"),
                Map.entry(HeaderEnum.METHOD, "x-starter-request-method"),
                Map.entry(HeaderEnum.STATUS_CODE, "x-starter-status-code"),
                Map.entry(HeaderEnum.REQUEST_ENDPOINT, "x-starter-request-uri"),
                Map.entry(HeaderEnum.FORWARDED_FOR, "x-starter-forwarded-for"),
                Map.entry(HeaderEnum.PACKAGE_INFO, "x-starter-package-name"),
                Map.entry(HeaderEnum.EXCEPTION, "x-starter-request-exception"),
                Map.entry(HeaderEnum.REQUEST, "x-starter-request"),
                Map.entry(HeaderEnum.RESPONSE, "x-starter-response"),
                Map.entry(HeaderEnum.USER_AGENT, "x-starter-user-agent"),
                Map.entry(HeaderEnum.RESPONSE_TIME, "x-starter-response-time"),
                Map.entry(HeaderEnum.SUMMARY_LOG, "x-starter-summary-log")
        );

        // When
        HeaderEnum[] values = HeaderEnum.values();

        // Then
        assertEquals(expected.size(), values.length);
        expected.forEach((key, value) -> assertEquals(value, key.getValue()));
    }

    @Test
    void should_ReturnConfiguredServiceCodes_When_AllServiceCodesAreRead() {
        // Given
        Map<ServiceCodeEnum, String> expectedCodes = Map.of(
                ServiceCodeEnum.RATE_LIMIT_EXCEEDED, "400429",
                ServiceCodeEnum.VALIDATION_ERROR, "400441",
                ServiceCodeEnum.FILE_ERROR, "400442",
                ServiceCodeEnum.GLOBAL_ERROR, "500599",
                ServiceCodeEnum.DATABASE_ERROR, "500598",
                ServiceCodeEnum.DATABASE_QUERY_FAULT, "500597",
                ServiceCodeEnum.DATABASE_CONSTRAINT_FAULT, "500596",
                ServiceCodeEnum.SERVLET_CONTEXT_NOT_FOUND, "500501"
        );

        // When
        ServiceCodeEnum[] values = ServiceCodeEnum.values();

        // Then
        assertEquals(expectedCodes.size(), values.length);
        expectedCodes.forEach((key, code) -> {
            assertEquals(code, key.getCode());
            assertNotNull(key.getMessage());
            assertFalse(key.getMessage().isBlank());
        });
    }

    @Test
    void should_ReturnConfiguredStatusLabels_When_AllStatusesAreRead() {
        // Given
        ServiceStatusResponseEnum success = ServiceStatusResponseEnum.SUCCESS;
        ServiceStatusResponseEnum failed = ServiceStatusResponseEnum.FAILED;

        // When
        String successLabel = success.getLabel();
        String failedLabel = failed.getLabel();

        // Then
        assertEquals("SUCCESS", successLabel);
        assertEquals("FAILED", failedLabel);
        assertEquals(2, ServiceStatusResponseEnum.values().length);
    }

    @Test
    void should_MatchPositiveSamples_When_FieldFormatRegexIsUsed() {
        // Given
        Map<FieldFormatTypeEnum, String> validSamples = Map.of(
                FieldFormatTypeEnum.EMAIL, "user@example.com",
                FieldFormatTypeEnum.PHONE_NUMBER, "+62 812-3456",
                FieldFormatTypeEnum.IP_ADDRESS, "192.168.1.1",
                FieldFormatTypeEnum.DATE, "2024-02-29",
                FieldFormatTypeEnum.LETTER_ONLY, "Alpha",
                FieldFormatTypeEnum.ALPHANUMERIC, "Alpha123",
                FieldFormatTypeEnum.NUMERIC_ONLY, "12345",
                FieldFormatTypeEnum.UUID, "123e4567-e89b-12d3-a456-426614174000",
                FieldFormatTypeEnum.PASSWORD_STRONG, "Strong1!",
                FieldFormatTypeEnum.ALLOW_ALL, "anything !@#"
        );

        // When
        Map<FieldFormatTypeEnum, Boolean> results = validSamples.entrySet().stream().collect(
                java.util.stream.Collectors.toMap(Map.Entry::getKey,
                        entry -> Pattern.matches(entry.getKey().getRegex(), entry.getValue())));

        // Then
        validSamples.forEach((format, sample) -> {
            assertNotNull(format.getRegex());
            assertTrue(results.get(format), format.name());
        });
    }

    @Test
    void should_NotMatchNegativeSamples_When_RestrictedFieldFormatRegexIsUsed() {
        // Given
        Map<FieldFormatTypeEnum, String> invalidSamples = Map.of(
                FieldFormatTypeEnum.EMAIL, "invalid-email",
                FieldFormatTypeEnum.PHONE_NUMBER, "phone",
                FieldFormatTypeEnum.IP_ADDRESS, "192.168.1",
                FieldFormatTypeEnum.DATE, "29-02-2024",
                FieldFormatTypeEnum.LETTER_ONLY, "Alpha1",
                FieldFormatTypeEnum.ALPHANUMERIC, "Alpha-123",
                FieldFormatTypeEnum.NUMERIC_ONLY, "12A",
                FieldFormatTypeEnum.UUID, "not-a-uuid",
                FieldFormatTypeEnum.PASSWORD_STRONG, "weak"
        );

        // When
        Map<FieldFormatTypeEnum, Boolean> results = invalidSamples.entrySet().stream().collect(
                java.util.stream.Collectors.toMap(Map.Entry::getKey,
                        entry -> Pattern.matches(entry.getKey().getRegex(), entry.getValue())));

        // Then
        invalidSamples.forEach((format, sample) -> assertFalse(results.get(format), format.name()));
    }
}
