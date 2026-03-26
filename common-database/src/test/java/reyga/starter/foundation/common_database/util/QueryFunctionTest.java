package reyga.starter.foundation.common_database.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QueryFunctionTest {

    @Test
    void count_handlesNullOrStar() {
        assertEquals("COUNT(*)", QueryFunction.count(null));
        assertEquals("COUNT(*)", QueryFunction.count("*"));
    }

    @Test
    void count_handlesColumn() {
        assertEquals("COUNT(col)", QueryFunction.count("col"));
    }

    @Test
    void coalesceAndNvl_formatValues() {
        assertEquals("COALESCE(col, 'x')", QueryFunction.coalesce("col", "x"));
        assertEquals("NVL(col, 1)", QueryFunction.nvl("col", 1));
    }

    @Test
    void toDateAndTimestamp_escapeQuotes() {
        assertEquals("TO_DATE('a''b', 'x')", QueryFunction.toDate("a'b", "x"));
        assertEquals("TO_TIMESTAMP('a''b', 'x')", QueryFunction.toTimestamp("a'b", "x"));
    }
}
