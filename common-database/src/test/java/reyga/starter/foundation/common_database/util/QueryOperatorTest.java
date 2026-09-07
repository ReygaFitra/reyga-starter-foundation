package reyga.starter.foundation.common_database.util;

import reyga.starter.foundation.common_database.enumeration.QueryOperator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.data.domain.PageRequest;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QueryOperatorTest {
    @ParameterizedTest
    @EnumSource(QueryOperator.class)
    void should_RejectNegativeOperandCount_When_PublicPlaceholderRendererIsCalled(QueryOperator operator) {
        // given
        int count = -1;

        // when
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> operator.placeholders(count));

        // then
        String expected = switch (operator) {
            case IN, NOT_IN -> "values required";
            case BETWEEN, NOT_BETWEEN -> operator.name() + " requires 2 value(s)";
            default -> operator.name() + " requires " + (operator.supportsColumnComparison() ? 1 : 0) + " value(s)";
        };
        assertEquals(expected, error.getMessage());
        assertEquals("reyga.starter.foundation.common_database.enumeration", operator.getClass().getPackageName());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "EQUALS|=|1", "NOT_EQUALS|<>|1", "GREATER_THAN|>|1", "GREATER_THAN_OR_EQUALS|>=|1",
            "LESS_THAN|<|1", "LESS_THAN_OR_EQUALS|<=|1", "LIKE|LIKE|1", "NOT_LIKE|NOT LIKE|1",
            "IN|IN|-1", "NOT_IN|NOT IN|-1", "BETWEEN|BETWEEN|2", "NOT_BETWEEN|NOT BETWEEN|2",
            "IS_NULL|IS NULL|0", "IS_NOT_NULL|IS NOT NULL|0", "IS_TRUE|IS TRUE|0",
            "IS_NOT_TRUE|IS NOT TRUE|0", "IS_FALSE|IS FALSE|0", "IS_NOT_FALSE|IS NOT FALSE|0",
            "IS_UNKNOWN|IS UNKNOWN|0", "IS_NOT_UNKNOWN|IS NOT UNKNOWN|0",
            "IS_DISTINCT_FROM|IS DISTINCT FROM|1", "IS_NOT_DISTINCT_FROM|IS NOT DISTINCT FROM|1"
    })
    void should_RenderTypedPredicateAndParameters_When_OperatorHasValidOperands(QueryOperator operator, String token, int arity) {
        // given
        QueryBuilder builder = QueryBuilder.builder().select("id").from("items").paginate(PageRequest.of(1, 5));
        Object[] values = arity == 0 ? new Object[0] : arity == 1 ? new Object[]{"input' OR 1=1"} : new Object[]{10, 20};
        String suffix = arity == 0 ? "" : arity == 1 ? " ?" : arity == 2 ? " ? AND ?" : " (?, ?)";
        String base = "SELECT id FROM items WHERE value " + token + suffix;

        // when
        QueryBuilder returned = builder.where().condition("value", operator, values).done();
        String sql = builder.build();

        // then
        assertSame(builder, returned);
        assertEquals(token, operator.getSql());
        assertEquals(token, operator.toString());
        assertEquals(arity == 1, operator.supportsColumnComparison());
        assertEquals(base + " LIMIT ? OFFSET ?", sql);
        assertEquals(sql, builder.getSql());
        assertEquals("SELECT COUNT(*) FROM (" + base + ") query_count", builder.buildCount());
        assertEquals(Arrays.asList(values), builder.getCountParameters());
        var expected = new java.util.ArrayList<>(Arrays.asList(values));
        expected.add(5);
        expected.add(5L);
        assertEquals(expected, builder.getParameters());
    }

    @ParameterizedTest
    @EnumSource(QueryOperator.class)
    void should_RejectInvalidArityWithoutMutation_When_OperandCountIsInvalid(QueryOperator operator) {
        // given
        QueryBuilder builder = QueryBuilder.builder().select("*").from("items");
        Object[] invalid = switch (operator) {
            case IN, NOT_IN -> new Object[0];
            default -> new Object[]{1, 2, 3};
        };

        // when
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> builder.where().condition("value", operator, invalid));

        // then
        String expectedMessage = switch (operator) {
            case IN, NOT_IN -> "values required";
            case BETWEEN, NOT_BETWEEN -> operator.name() + " requires 2 value(s)";
            default -> operator.name() + " requires " + (operator.supportsColumnComparison() ? 1 : 0) + " value(s)";
        };
        assertEquals(expectedMessage, error.getMessage());
        assertEquals("SELECT * FROM items", builder.build());
        assertEquals(List.of(), builder.getParameters());
        assertEquals(List.of(), builder.getCountParameters());
    }

    @ParameterizedTest
    @EnumSource(value = QueryOperator.class, names = {"EQUALS", "NOT_EQUALS", "GREATER_THAN", "GREATER_THAN_OR_EQUALS",
            "LESS_THAN", "LESS_THAN_OR_EQUALS", "LIKE", "NOT_LIKE", "IS_DISTINCT_FROM", "IS_NOT_DISTINCT_FROM"})
    void should_RenderJoinWithoutBindingColumns_When_BinaryOperatorIsUsed(QueryOperator operator) {
        // given
        QueryBuilder builder = QueryBuilder.builder().select("a.id").from("items a");

        // when
        QueryBuilder result = builder.joinOn("items b", "a.id", operator, "b.id");

        // then
        assertSame(builder, result);
        assertEquals("SELECT a.id FROM items a INNER JOIN items b ON a.id " + operator.getSql() + " b.id", result.build());
        assertTrue(result.getParameters().isEmpty());
        assertTrue(result.getCountParameters().isEmpty());
    }

    @ParameterizedTest
    @EnumSource(value = QueryOperator.class, names = {"IN", "NOT_IN", "BETWEEN", "NOT_BETWEEN", "IS_NULL", "IS_NOT_NULL",
            "IS_TRUE", "IS_NOT_TRUE", "IS_FALSE", "IS_NOT_FALSE", "IS_UNKNOWN", "IS_NOT_UNKNOWN"})
    void should_RejectJoinWithoutMutation_When_OperatorIsNotBinary(QueryOperator operator) {
        // given
        QueryBuilder builder = QueryBuilder.builder().select("*").from("items a");
        QueryBuilder.JoinBuilder join = builder.join("items b");

        // when
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> join.on("a.id", operator, "b.id"));

        // then
        assertEquals("Operator " + operator.name() + " does not support column comparison", error.getMessage());
        assertEquals("SELECT * FROM items a INNER JOIN items b ON a.id = b.id",
                join.on("a.id", QueryOperator.EQUALS, "b.id").done().build());
        assertTrue(builder.getParameters().isEmpty());
    }

    @Test
    void should_RejectNullArgumentsWithoutMutation_When_OperatorOrValuesArrayIsNull() {
        // given
        QueryBuilder builder = QueryBuilder.builder().select("*").from("items");

        // when
        var whereError = assertThrows(NullPointerException.class, () -> builder.where().condition("id", null, 1));
        var joinError = assertThrows(NullPointerException.class, () -> builder.joinOn("other", "id", null, "other.id"));
        var valuesError = assertThrows(IllegalArgumentException.class,
                () -> builder.where().condition("id", QueryOperator.EQUALS, (Object[]) null));

        // then
        assertEquals("operator must not be null", whereError.getMessage());
        assertEquals("operator must not be null", joinError.getMessage());
        assertEquals("values must not be null", valuesError.getMessage());
        assertEquals("SELECT * FROM items", builder.build());
        assertTrue(builder.getParameters().isEmpty());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void should_RejectBlankExpressionsWithoutMutation_When_ExpressionIsInvalid(String expression) {
        // given
        QueryBuilder builder = QueryBuilder.builder().select("*").from("items");
        QueryBuilder.JoinBuilder join = builder.join("other");

        // when
        var columnError = assertThrows(IllegalArgumentException.class, () -> builder.where().condition(expression, QueryOperator.EQUALS, 1));
        var leftError = assertThrows(IllegalArgumentException.class, () -> join.on(expression, QueryOperator.EQUALS, "other.id"));
        var rightError = assertThrows(IllegalArgumentException.class, () -> join.on("id", QueryOperator.EQUALS, expression));
        var rawError = assertThrows(IllegalArgumentException.class, () -> join.onRaw(expression));

        // then
        assertEquals("column must not be blank", columnError.getMessage());
        assertEquals("join expressions must not be blank", leftError.getMessage());
        assertEquals(leftError.getMessage(), rightError.getMessage());
        assertEquals("condition must not be blank", rawError.getMessage());
        assertEquals("SELECT * FROM items INNER JOIN other ON id = other.id", join.on("id", QueryOperator.EQUALS, "other.id").done().build());
        assertTrue(builder.getParameters().isEmpty());
    }

    @Test
    void should_BindNullAndPreserveLikePattern_When_ValuesAreExplicit() {
        // given
        QueryBuilder builder = QueryBuilder.builder().select("*").from("items");

        // when
        builder.where().condition("value", QueryOperator.IS_NOT_DISTINCT_FROM, (Object) null)
                .and().condition("name", QueryOperator.LIKE, "prefix%").done();

        // then
        assertEquals("SELECT * FROM items WHERE value IS NOT DISTINCT FROM ? AND name LIKE ?", builder.build());
        assertEquals(Arrays.asList(null, "prefix%"), builder.getParameters());
        assertEquals(Arrays.asList(null, "prefix%"), builder.getCountParameters());
    }

    @Test
    void should_RenderConveniencePredicates_When_NewHelpersAreUsed() {
        // given
        QueryBuilder builder = QueryBuilder.builder().select("*").from("items");

        // when
        builder.where().greaterThanOrEquals("a", 1).and().lessThanOrEquals("b", 2)
                .and().notLike("name", "test").and().notBetween("c", 3, 4).and().notIn("d", 5, 6).done();

        // then
        assertEquals("SELECT * FROM items WHERE a >= ? AND b <= ? AND name NOT LIKE ? AND c NOT BETWEEN ? AND ? AND d NOT IN (?, ?)", builder.build());
        assertEquals(List.of(1, 2, "%test%", 3, 4, 5, 6), builder.getParameters());
        assertEquals(builder.getParameters(), builder.getCountParameters());
    }

    @Test
    void should_RejectMissingNotInValues_When_ArrayIsNullOrEmpty() {
        // given
        QueryBuilder builder = QueryBuilder.builder().select("*").from("items");

        // when
        var nullError = assertThrows(IllegalArgumentException.class, () -> builder.where().notIn("id", (Object[]) null));
        var emptyError = assertThrows(IllegalArgumentException.class, () -> builder.where().notIn("id"));

        // then
        assertEquals("values required", nullError.getMessage());
        assertEquals(nullError.getMessage(), emptyError.getMessage());
        assertEquals("SELECT * FROM items", builder.build());
        assertTrue(builder.getParameters().isEmpty());
    }

    @Test
    @SuppressWarnings("deprecation")
    void should_PreserveRawOnCompatibility_When_TrustedExpressionIsSupplied() {
        // given
        QueryBuilder legacy = QueryBuilder.builder().select("*").from("items a");
        QueryBuilder explicit = QueryBuilder.builder().select("*").from("items a");

        // when
        legacy.join("items b").on("a.id = b.id").endJoin();
        explicit.join("items b").onRaw("a.id = b.id").endJoin();

        // then
        assertEquals("SELECT * FROM items a INNER JOIN items b ON a.id = b.id", legacy.build());
        assertEquals(legacy.build(), explicit.build());
        assertTrue(legacy.getParameters().isEmpty());
        assertTrue(explicit.getParameters().isEmpty());
    }

    @Test
    void should_PreserveExistingPredicateAndConnector_When_ValidationFails() {
        // given
        QueryBuilder builder = QueryBuilder.builder().select("*").from("items");
        QueryBuilder.WhereBuilder where = builder.where().equals("active", true).and();

        // when
        var error = assertThrows(IllegalArgumentException.class,
                () -> where.condition("id", QueryOperator.BETWEEN, 1));
        where.condition("id", QueryOperator.IN, 7).done();

        // then
        assertEquals("BETWEEN requires 2 value(s)", error.getMessage());
        assertEquals("SELECT * FROM items WHERE active = ? AND id IN (?)", builder.build());
        assertEquals(List.of(true, 7), builder.getParameters());
        assertEquals(List.of(true, 7), builder.getCountParameters());
    }

    @Test
    void should_BindTypedFiltersAfterSetValues_When_UpdateAndDeleteAreBuilt() {
        // given
        QueryBuilder update = QueryBuilder.builder().update("items").set("name", "new");
        QueryBuilder delete = QueryBuilder.builder().deleteFrom("items");

        // when
        update.where().condition("version", QueryOperator.LESS_THAN_OR_EQUALS, 3).done();
        delete.where().condition("id", QueryOperator.NOT_IN, 1).done();

        // then
        assertEquals("UPDATE items SET name = ? WHERE version <= ?", update.build());
        assertEquals(List.of("new", 3), update.getParameters());
        assertEquals("DELETE FROM items WHERE id NOT IN (?)", delete.build());
        assertEquals(List.of(1), delete.getParameters());
    }
}
