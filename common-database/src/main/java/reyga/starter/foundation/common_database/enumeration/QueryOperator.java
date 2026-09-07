package reyga.starter.foundation.common_database.enumeration;

import lombok.Getter;
import reyga.starter.foundation.common_database.util.QueryBuilder;

import java.util.Collections;

/**
 * Closed set of SQL predicate operators supported by {@link QueryBuilder}.
 * Logical composition uses the builder's and/or methods; join types use QueryJoinType.
 * Dialect-specific arithmetic, JSON, regex and subquery operators are not modeled here.
 * SQL support (especially boolean and DISTINCT predicates) depends on the database.
 */
public enum QueryOperator {
    EQUALS("=", 1),
    NOT_EQUALS("<>", 1),
    GREATER_THAN(">", 1),
    GREATER_THAN_OR_EQUALS(">=", 1),
    LESS_THAN("<", 1),
    LESS_THAN_OR_EQUALS("<=", 1),
    LIKE("LIKE", 1),
    NOT_LIKE("NOT LIKE", 1),
    IN("IN", -1),
    NOT_IN("NOT IN", -1),
    BETWEEN("BETWEEN", 2),
    NOT_BETWEEN("NOT BETWEEN", 2),
    IS_NULL("IS NULL", 0),
    IS_NOT_NULL("IS NOT NULL", 0),
    IS_TRUE("IS TRUE", 0),
    IS_NOT_TRUE("IS NOT TRUE", 0),
    IS_FALSE("IS FALSE", 0),
    IS_NOT_FALSE("IS NOT FALSE", 0),
    IS_UNKNOWN("IS UNKNOWN", 0),
    IS_NOT_UNKNOWN("IS NOT UNKNOWN", 0),
    IS_DISTINCT_FROM("IS DISTINCT FROM", 1),
    IS_NOT_DISTINCT_FROM("IS NOT DISTINCT FROM", 1);

    /**
     * -- GETTER --
     * Returns the SQL token, never a caller-provided expression.
     */
    @Getter
    private final String sql;
    private final int operandCount;

    QueryOperator(String sql, int operandCount) {
        this.sql = sql;
        this.operandCount = operandCount;
    }

    /** Whether this operator accepts one right-hand SQL expression in an ON clause. */
    public boolean supportsColumnComparison() { return operandCount == 1; }

    /**
     * Renders the operator and JDBC placeholders without a left-hand expression.
     * Validates operand count before the caller mutates query state.
     * @param count number of bound values; zero for unary predicates
     * @return SQL suffix, for example {@code BETWEEN ? AND ?}
     * @throws IllegalArgumentException if the count does not match this operator
     */
    public String placeholders(int count) {
        if (operandCount == -1) {
            if (count < 1) throw new IllegalArgumentException("values required");
            return sql + " (" + String.join(", ", Collections.nCopies(count, "?")) + ")";
        }
        if (count != operandCount) {
            throw new IllegalArgumentException(name() + " requires " + operandCount + " value(s)");
        }
        return switch (operandCount) {
            case 0 -> sql;
            case 2 -> sql + " ? AND ?";
            default -> sql + " ?";
        };
    }

    @Override
    public String toString() { return sql; }
}
