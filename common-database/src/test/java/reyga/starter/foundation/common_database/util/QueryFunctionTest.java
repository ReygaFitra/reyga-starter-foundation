package reyga.starter.foundation.common_database.util;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

class QueryFunctionTest {

    @Test
    void should_ReturnCountAllExpression_When_ColumnIsNullBlankOrStar() {
        // Given
        String nullColumn = null;

        // When
        String nullResult = QueryFunction.count(nullColumn);
        String blankResult = QueryFunction.count("   ");
        String starResult = QueryFunction.count("*");

        // Then
        assertEquals("COUNT(*)", nullResult);
        assertEquals("COUNT(*)", blankResult);
        assertEquals("COUNT(*)", starResult);
    }

    @Test
    void should_ReturnCountColumnExpression_When_ColumnIsProvided() {
        // Given
        String column = "u.id";

        // When
        String result = QueryFunction.count(column);

        // Then
        assertEquals("COUNT(u.id)", result);
    }

    @Test
    void should_ReturnCaseAndLengthExpressions_When_ColumnIsProvided() {
        // Given
        String column = "username";

        // When
        String upper = QueryFunction.upper(column);
        String lower = QueryFunction.lower(column);
        String length = QueryFunction.length(column);

        // Then
        assertEquals("UPPER(username)", upper);
        assertEquals("LOWER(username)", lower);
        assertEquals("LENGTH(username)", length);
    }

    @Test
    void should_ReturnNullFallbackExpressions_When_DefaultValuesHaveSupportedTypes() {
        // Given
        Object customValue = new Object() {
            @Override
            public String toString() {
                return "DEFAULT_EXPRESSION";
            }
        };

        // When
        String nullValue = QueryFunction.coalesce("name", null);
        String stringValue = QueryFunction.coalesce("name", "O'Brien");
        String characterValue = QueryFunction.nvl("grade", 'A');
        String numericValue = QueryFunction.nvl("amount", 10);
        String customResult = QueryFunction.coalesce("value", customValue);

        // Then
        assertEquals("COALESCE(name, NULL)", nullValue);
        assertEquals("COALESCE(name, 'O''Brien')", stringValue);
        assertEquals("NVL(grade, 'A')", characterValue);
        assertEquals("NVL(amount, 10)", numericValue);
        assertEquals("COALESCE(value, DEFAULT_EXPRESSION)", customResult);
    }

    @Test
    void should_ReturnEscapedTemporalExpressions_When_ValueOrFormatContainsQuote() {
        // Given
        String value = "2026-01-01' OR '1'='1";
        String format = "YYYY-MM-DD'X";

        // When
        String date = QueryFunction.toDate(value, format);
        String timestamp = QueryFunction.toTimestamp(value, format);

        // Then
        assertEquals("TO_DATE('2026-01-01'' OR ''1''=''1', 'YYYY-MM-DD''X')", date);
        assertEquals("TO_TIMESTAMP('2026-01-01'' OR ''1''=''1', 'YYYY-MM-DD''X')", timestamp);
    }

    @Test
    void should_ReturnConcatExpression_When_ValuesAreProvided() {
        // Given
        String[] values = {"first_name", "' '", "last_name"};

        // When
        String result = QueryFunction.concat(values);

        // Then
        assertEquals("CONCAT(first_name, ' ', last_name)", result);
    }

    @Test
    void should_ReturnKeywords_When_KeywordFunctionsAreCalled() {
        // Given
        String expectedDate = "CURRENT_DATE";

        // When
        String currentDate = QueryFunction.currentDate();
        String currentTimestamp = QueryFunction.currentTimestamp();
        String ascending = QueryFunction.ascending();
        String descending = QueryFunction.descending();

        // Then
        assertEquals(expectedDate, currentDate);
        assertEquals("CURRENT_TIMESTAMP", currentTimestamp);
        assertEquals("ASC", ascending);
        assertEquals("DESC", descending);
    }

    @Test
    void should_ThrowNullPointerException_When_RequiredTemporalOrConcatValuesAreNull() {
        // Given
        String value = null;

        // When
        NullPointerException dateException = assertThrows(NullPointerException.class,
                () -> QueryFunction.toDate(value, "YYYY"));
        NullPointerException formatException = assertThrows(NullPointerException.class,
                () -> QueryFunction.toTimestamp("2026", null));
        NullPointerException concatException = assertThrows(NullPointerException.class,
                () -> QueryFunction.concat((String[]) null));

        // Then
        assertNotNull(dateException);
        assertNotNull(formatException);
        assertNotNull(concatException);
    }

    @Test
    void should_ExposeOnlyPrivateConstructor_When_UtilityClassIsInspected() {
        // Given
        Constructor<?>[] constructors = QueryFunction.class.getDeclaredConstructors();

        // When
        Constructor<?> constructor = constructors[0];

        // Then
        assertEquals(1, constructors.length);
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
    }
}
