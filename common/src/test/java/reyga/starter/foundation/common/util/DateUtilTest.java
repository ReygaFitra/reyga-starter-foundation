package reyga.starter.foundation.common.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.*;

class DateUtilTest {

    @Test
    void now_returnsNonNull() {
        assertNotNull(DateUtil.now());
    }

    @Test
    void formatAndParse_roundTrip() {
        LocalDate date = LocalDate.of(2024, 2, 29);
        String formatted = DateUtil.format(date, "yyyy-MM-dd");
        LocalDate parsed = DateUtil.parseLocalDate(formatted, "yyyy-MM-dd");
        assertEquals(date, parsed);
    }

    @Test
    void startAndEndOfMonth() {
        LocalDateTime date = LocalDateTime.of(2024, 2, 15, 10, 0);
        assertEquals(1, DateUtil.getStartOfMonth(date).getDayOfMonth());
        assertEquals(29, DateUtil.getEndOfMonth(date).getDayOfMonth());
    }

    @Test
    void parseYearMonth_andEndOfMonth() {
        YearMonth ym = DateUtil.parseYearMonth("2024-02", "yyyy-MM");
        assertEquals(LocalDate.of(2024, 2, 29), DateUtil.getEndOfMonth(ym));
    }

    @Test
    void isValidDate_returnsFalseForInvalid() {
        assertTrue(DateUtil.isValidDate("2024-02-29", "yyyy-MM-dd"));
        assertFalse(DateUtil.isValidDate("2024-02-30", "yyyy-MM-dd"));
    }
}
