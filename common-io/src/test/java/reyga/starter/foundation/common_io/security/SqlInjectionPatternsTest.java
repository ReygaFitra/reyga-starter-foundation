package reyga.starter.foundation.common_io.security;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SqlInjectionPatternsTest {

    @Test
    void should_ReturnImmutableDefaultPatterns_When_DefaultsAreRequested() {
        // Given
        List<String> maliciousPayloads = List.of(
                "OR 1 = 1", "AND 'a' = 'a'", "UNION ALL SELECT password FROM users",
                "UNION SELECT password FROM users", "SELECT password FROM users",
                "INSERT INTO users VALUES (1)", "UPDATE users SET admin = 1",
                "DELETE FROM users", "DROP TABLE users", "TRUNCATE TABLE users",
                "EXECUTE stored_proc", "sp_configure", "-- comment", "/* comment */",
                "WAITFOR DELAY '00:00:05'", "; DROP TABLE users"
        );

        // When
        List<Pattern> patterns = SqlInjectionPatterns.defaults();
        UnsupportedOperationException exception = assertThrows(
                UnsupportedOperationException.class,
                () -> patterns.add(Pattern.compile("unexpected"))
        );

        // Then
        assertEquals(16, patterns.size());
        assertEquals(patterns.size(), maliciousPayloads.size());
        for (int index = 0; index < patterns.size(); index++) {
            assertTrue(patterns.get(index).matcher(maliciousPayloads.get(index)).find(),
                    "Pattern did not match payload at index " + index);
        }
        assertEquals(UnsupportedOperationException.class, exception.getClass());
    }

    @Test
    void should_NotMatchBenignContent_When_DefaultPatternsAreUsed() {
        // Given
        String content = "Order number 123 and selected delivery option";

        // When
        boolean result = SqlInjectionPatterns.defaults().stream()
                .anyMatch(pattern -> pattern.matcher(content).find());

        // Then
        assertFalse(result);
        assertEquals(16, SqlInjectionPatterns.defaults().size());
    }

    @Test
    void should_FilterNullsAndPreserveOrder_When_ListPatternsAreMerged() {
        // Given
        Pattern first = Pattern.compile("first");
        Pattern second = Pattern.compile("second");
        List<Pattern> input = java.util.Arrays.asList(first, null, second);

        // When
        List<Pattern> result = SqlInjectionPatterns.merge(input);
        UnsupportedOperationException exception = assertThrows(
                UnsupportedOperationException.class,
                () -> result.clear()
        );

        // Then
        assertEquals(2, result.size());
        assertSame(first, result.get(0));
        assertSame(second, result.get(1));
        assertEquals(UnsupportedOperationException.class, exception.getClass());
    }

    @Test
    void should_FilterNullsAndPreserveOrder_When_VarargPatternsAreMerged() {
        // Given
        Pattern first = Pattern.compile("first");
        Pattern second = Pattern.compile("second");

        // When
        List<Pattern> result = SqlInjectionPatterns.merge(first, null, second);

        // Then
        assertEquals(2, result.size());
        assertSame(first, result.get(0));
        assertSame(second, result.get(1));
        assertTrue(result.stream().allMatch(pattern -> pattern != null));
    }

    @Test
    void should_ReturnEmptyImmutableList_When_MergeInputIsNullOrEmpty() {
        // Given
        List<Pattern> emptyList = List.of();

        // When
        List<Pattern> nullListResult = SqlInjectionPatterns.merge((List<Pattern>) null);
        List<Pattern> emptyListResult = SqlInjectionPatterns.merge(emptyList);
        List<Pattern> nullArrayResult = SqlInjectionPatterns.merge((Pattern[]) null);
        List<Pattern> emptyArrayResult = SqlInjectionPatterns.merge();

        // Then
        assertTrue(nullListResult.isEmpty());
        assertTrue(emptyListResult.isEmpty());
        assertTrue(nullArrayResult.isEmpty());
        assertTrue(emptyArrayResult.isEmpty());
        assertThrows(UnsupportedOperationException.class,
                () -> nullListResult.add(Pattern.compile("unexpected")));
        assertThrows(UnsupportedOperationException.class,
                () -> nullArrayResult.add(Pattern.compile("unexpected")));
    }
}
