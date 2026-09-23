package reyga.starter.foundation.common.util;

import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtility {
    public static final String DEFAULT_TIMEZONE_ID = "UTC";

    public DateUtility() {
    }

    public static LocalDateTime now() {
        return LocalDateTime.now(Clock.systemUTC());
    }

    public static LocalDateTime nowSystemTimezone() {
        return LocalDateTime.now(TimeZone.getDefault().toZoneId());
    }

    public static LocalDate nowLocalDateSystemTimezone() {
        return LocalDate.now(TimeZone.getDefault().toZoneId());
    }

    public static Timestamp getTimestamp(LocalDateTime localDateTime) {
        return Timestamp.valueOf(localDateTime);
    }

    public static LocalDateTime getLocalDateTime(Date date) {
        return (new Timestamp(date.getTime())).toLocalDateTime();
    }

    public static LocalDateTime getLocalDateTime(LocalDate date) {
        return date.atStartOfDay();
    }

    public static LocalDateTime getLocalDateTime(LocalDate date, LocalTime time) {
        return date.atTime(time);
    }

    public static long nowInMillisUtc() {
        return OffsetDateTime.now(Clock.systemUTC()).toInstant().toEpochMilli();
    }

    public static long nowInMillis() {
        return OffsetDateTime.now(ZoneOffset.systemDefault()).toInstant().toEpochMilli();
    }

    public static LocalDateTime newLocalDateTime(long timestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), TimeZone.getDefault().toZoneId());
    }

    public static LocalDateTime newLocalDateTimeUtc(long timestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), TimeZone.getTimeZone("UTC").toZoneId());
    }

    public static String format(LocalDateTime localDateTime, String pattern) {
        return localDateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    public static String format(LocalDateTime localDateTime, String pattern, Locale locale) {
        return localDateTime.format(DateTimeFormatter.ofPattern(pattern, locale));
    }

    public static String format(LocalDate localDate, String pattern) {
        return localDate.format(DateTimeFormatter.ofPattern(pattern));
    }

    public static String format(LocalDate localDate, String pattern, Locale locale) {
        return localDate.format(DateTimeFormatter.ofPattern(pattern, locale));
    }

    public static LocalDate parseLocalDate(String date, String pattern) {
        return LocalDate.parse(date, DateTimeFormatter.ofPattern(pattern));
    }

    public static LocalDate parseLocalDateWithoutDate(String date, String pattern) {
        return YearMonth.parse(date, DateTimeFormatter.ofPattern(pattern)).atDay(1);
    }

    public static LocalDateTime parseLocalDateTime(String date, String pattern) {
        return LocalDateTime.parse(date, DateTimeFormatter.ofPattern(pattern));
    }

    public static LocalDateTime getEndOfMonth(LocalDateTime localDateTime) {
        return localDateTime.withDayOfMonth(localDateTime.toLocalDate().getMonth().length(localDateTime.toLocalDate().isLeapYear()));
    }

    public static LocalDate getEndOfMonth(LocalDate localDate) {
        return localDate.withDayOfMonth(localDate.getMonth().length(localDate.isLeapYear()));
    }

    public static LocalDateTime getStartOfMonth(LocalDateTime localDateTime) {
        return localDateTime.withDayOfMonth(1);
    }

    public static LocalDate getStartOfMonth(LocalDate localDate) {
        return localDate.withDayOfMonth(1);
    }

    public static LocalDateTime getStartOfWeek(LocalDateTime localDateTime) {
        return localDateTime.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    public static LocalDateTime getEndOfWeek(LocalDateTime localDateTime) {
        return localDateTime.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
    }

    public static LocalDateTime getStartOfDay(LocalDateTime localDateTime) {
        return localDateTime.toLocalDate().atStartOfDay();
    }

    public static LocalDateTime getEndOfDay(LocalDateTime localDateTime) {
        return localDateTime.toLocalDate().atTime(LocalTime.MAX);
    }

    public static Date getDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static YearMonth parseYearMonth(String monthYear, String pattern) {
        return YearMonth.parse(monthYear, DateTimeFormatter.ofPattern(pattern));
    }

    public static LocalDate getEndOfMonth(YearMonth yearMonth) {
        return yearMonth.atEndOfMonth();
    }

    public static boolean isValidDate(String trxDate, String pattern) {
        try {
            LocalDate.parse(trxDate, DateTimeFormatter.ofPattern(pattern));
            return true;
        } catch (DateTimeParseException var3) {
            return false;
        }
    }
}
