package reyga.starter.foundation.common.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.*;

class DateUtilityTest {

    @Test
    void now_returnsNonNull() {
        assertNotNull(DateUtility.now());
    }

    @Test
    void formatAndParse_roundTrip() {
        LocalDate date = LocalDate.of(2024, 2, 29);
        String formatted = DateUtility.format(date, "yyyy-MM-dd");
        LocalDate parsed = DateUtility.parseLocalDate(formatted, "yyyy-MM-dd");
        assertEquals(date, parsed);
    }

    @Test
    void startAndEndOfMonth() {
        LocalDateTime date = LocalDateTime.of(2024, 2, 15, 10, 0);
        assertEquals(1, DateUtility.getStartOfMonth(date).getDayOfMonth());
        assertEquals(29, DateUtility.getEndOfMonth(date).getDayOfMonth());
    }

    @Test
    void parseYearMonth_andEndOfMonth() {
        YearMonth ym = DateUtility.parseYearMonth("2024-02", "yyyy-MM");
        assertEquals(LocalDate.of(2024, 2, 29), DateUtility.getEndOfMonth(ym));
    }

    @Test
    void isValidDate_returnsFalseForInvalid() {
        assertTrue(DateUtility.isValidDate("2024-02-29", "yyyy-MM-dd"));
        assertFalse(DateUtility.isValidDate("2024-02-30", "yyyy-MM-dd"));
    }
}
