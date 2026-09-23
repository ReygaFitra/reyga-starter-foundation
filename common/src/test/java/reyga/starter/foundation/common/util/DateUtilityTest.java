package reyga.starter.foundation.common.util;

import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class DateUtilityTest {

    @Test
    void should_ReturnCurrentValues_When_CurrentTimeMethodsAreCalled() {
        // Given
        long before = System.currentTimeMillis();

        // When
        LocalDateTime utc = DateUtility.now();
        LocalDateTime system = DateUtility.nowSystemTimezone();
        LocalDate systemDate = DateUtility.nowLocalDateSystemTimezone();
        long utcMillis = DateUtility.nowInMillisUtc();
        long systemMillis = DateUtility.nowInMillis();

        // Then
        long after = System.currentTimeMillis();
        assertNotNull(utc);
        assertNotNull(system);
        assertEquals(LocalDate.now(ZoneId.systemDefault()), systemDate);
        assertTrue(utcMillis >= before && utcMillis <= after);
        assertTrue(systemMillis >= before && systemMillis <= after);
    }

    @Test
    void should_ConvertTemporalValues_When_ValidValuesAreProvided() {
        // Given
        LocalDate date = LocalDate.of(2024, 2, 29);
        LocalTime time = LocalTime.of(10, 15, 30);
        LocalDateTime dateTime = date.atTime(time);
        long millis = dateTime.toInstant(ZoneOffset.UTC).toEpochMilli();

        // When
        Timestamp timestamp = DateUtility.getTimestamp(dateTime);
        LocalDateTime fromDate = DateUtility.getLocalDateTime(new Date(timestamp.getTime()));
        LocalDateTime fromLocalDate = DateUtility.getLocalDateTime(date);
        LocalDateTime fromDateAndTime = DateUtility.getLocalDateTime(date, time);
        LocalDateTime fromMillis = DateUtility.newLocalDateTime(millis);
        LocalDateTime fromMillisUtc = DateUtility.newLocalDateTimeUtc(millis);

        // Then
        assertEquals(dateTime, timestamp.toLocalDateTime());
        assertEquals(dateTime, fromDate);
        assertEquals(date.atStartOfDay(), fromLocalDate);
        assertEquals(dateTime, fromDateAndTime);
        assertEquals(LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault()), fromMillis);
        assertEquals(dateTime, fromMillisUtc);
    }

    @Test
    void should_FormatAndParseValues_When_PatternsAndLocalesAreValid() {
        // Given
        LocalDate date = LocalDate.of(2024, 2, 29);
        LocalDateTime dateTime = date.atTime(10, 15, 30);

        // When
        String dateResult = DateUtility.format(date, "dd MMMM yyyy", Locale.ENGLISH);
        String dateTimeResult = DateUtility.format(dateTime, "dd MMMM yyyy HH:mm", Locale.ENGLISH);

        // Then
        assertEquals("29 February 2024", dateResult);
        assertEquals("29 February 2024 10:15", dateTimeResult);
        assertEquals("2024-02-29", DateUtility.format(date, "yyyy-MM-dd"));
        assertEquals("2024-02-29 10:15:30", DateUtility.format(dateTime, "yyyy-MM-dd HH:mm:ss"));
        assertEquals(date, DateUtility.parseLocalDate("2024-02-29", "yyyy-MM-dd"));
        assertEquals(LocalDate.of(2024, 2, 1), DateUtility.parseLocalDateWithoutDate("2024-02", "yyyy-MM"));
        assertEquals(dateTime, DateUtility.parseLocalDateTime("2024-02-29 10:15:30", "yyyy-MM-dd HH:mm:ss"));
    }

    @Test
    void should_ReturnBoundaries_When_LeapYearDateIsProvided() {
        // Given
        LocalDate date = LocalDate.of(2024, 2, 15);
        LocalDateTime dateTime = date.atTime(10, 30);

        // When
        LocalDate startMonth = DateUtility.getStartOfMonth(date);
        LocalDate endMonth = DateUtility.getEndOfMonth(date);

        // Then
        assertEquals(LocalDate.of(2024, 2, 1), startMonth);
        assertEquals(LocalDate.of(2024, 2, 29), endMonth);
        assertEquals(LocalDateTime.of(2024, 2, 1, 10, 30), DateUtility.getStartOfMonth(dateTime));
        assertEquals(LocalDateTime.of(2024, 2, 29, 10, 30), DateUtility.getEndOfMonth(dateTime));
        assertEquals(DayOfWeek.MONDAY, DateUtility.getStartOfWeek(dateTime).getDayOfWeek());
        assertEquals(DayOfWeek.SUNDAY, DateUtility.getEndOfWeek(dateTime).getDayOfWeek());
        assertEquals(date.atStartOfDay(), DateUtility.getStartOfDay(dateTime));
        assertEquals(date.atTime(LocalTime.MAX), DateUtility.getEndOfDay(dateTime));
    }

    @Test
    void should_ConvertDateAndYearMonth_When_ValuesAreValid() {
        // Given
        LocalDateTime dateTime = LocalDateTime.of(2024, 2, 29, 10, 15);

        // When
        Date date = DateUtility.getDate(dateTime);
        YearMonth yearMonth = DateUtility.parseYearMonth("2024-02", "yyyy-MM");

        // Then
        assertEquals(dateTime, LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()));
        assertEquals(YearMonth.of(2024, 2), yearMonth);
        assertEquals(LocalDate.of(2024, 2, 29), DateUtility.getEndOfMonth(yearMonth));
    }

    @Test
    void should_ReturnExpectedValidity_When_DateTextIsChecked() {
        // Given
        String pattern = "yyyy-MM-dd";

        // When
        boolean valid = DateUtility.isValidDate("2024-02-29", pattern);
        boolean invalid = DateUtility.isValidDate("not-a-date", pattern);

        // Then
        assertTrue(valid);
        assertFalse(invalid);
    }

    @Test
    void should_ThrowDateTimeParseException_When_DateTextIsInvalid() {
        // Given
        String invalidDate = "not-a-date";

        // When
        DateTimeParseException exception = assertThrows(DateTimeParseException.class,
                () -> DateUtility.parseLocalDate(invalidDate, "yyyy-MM-dd"));

        // Then
        assertEquals(invalidDate, exception.getParsedString());
    }
}
