package reyga.starter.foundation.common_database.util;

/**
 * SQL expression helpers intended for trusted query composition with {@link QueryBuilder}.
 *
 * <p>Arguments representing identifiers or SQL expressions are not bind parameters and must
 * never come directly from untrusted input. Prefer {@code QueryBuilder} condition methods for
 * values, because those methods bind values through JDBC placeholders.</p>
 */
public class QueryFunction {

    private static final String CURRENT_DATE = "CURRENT_DATE";
    private static final String CURRENT_TIMESTAMP = "CURRENT_TIMESTAMP";
    private static final String ASC = "ASC";
    private static final String DESC = "DESC";

    private QueryFunction() {
    }

    /**
     * Builds an Oracle-style date conversion expression.
     *
     * @param value date literal
     * @param format database date format
     * @return escaped Oracle-style {@code TO_DATE} expression
     */
    public static String toDate(String value, String format) {
        return "TO_DATE('" + escape(value) + "', '" + escape(format) + "')";
    }

    /**
     * Builds an Oracle-style timestamp conversion expression.
     *
     * @param value timestamp literal
     * @param format database timestamp format
     * @return escaped Oracle-style {@code TO_TIMESTAMP} expression
     */
    public static String toTimestamp(String value, String format) {
        return "TO_TIMESTAMP('" + escape(value) + "', '" + escape(format) + "')";
    }

    /**
     * Builds a count aggregation expression.
     *
     * @param column trusted column or expression; blank and star select all rows
     * @return {@code COUNT(*)} or {@code COUNT(column)}
     */
    public static String count(String column) {
        if (column == null || column.trim().isEmpty() || column.equals("*")) {
            return "COUNT(*)";
        }
        return "COUNT(" + column + ")";
    }

    /**
     * Builds an uppercase expression.
     *
     * @param column trusted column or expression
     * @return value wrapped with {@code UPPER}
     */
    public static String upper(String column) {
        return "UPPER(" + column + ")";
    }

    /**
     * Builds a lowercase expression.
     *
     * @param column trusted column or expression
     * @return value wrapped with {@code LOWER}
     */
    public static String lower(String column) {
        return "LOWER(" + column + ")";
    }

    /**
     * Builds a string-length expression.
     *
     * @param column trusted column or expression
     * @return value wrapped with {@code LENGTH}
     */
    public static String length(String column) {
        return "LENGTH(" + column + ")";
    }

    /**
     * Builds a standard null fallback expression.
     *
     * @param column trusted column or expression
     * @param defaultValue fallback literal
     * @return {@code COALESCE} expression
     */
    public static String coalesce(String column, Object defaultValue) {
        return "COALESCE(" + column + ", " + formatValue(defaultValue) + ")";
    }

    /**
     * Builds an Oracle-style null fallback expression.
     *
     * @param column trusted column or expression
     * @param defaultValue fallback literal
     * @return Oracle-style {@code NVL} expression
     */
    public static String nvl(String column, Object defaultValue) {
        return "NVL(" + column + ", " + formatValue(defaultValue) + ")";
    }

    /**
     * Builds a concatenation expression.
     *
     * @param values trusted SQL fragments
     * @return {@code CONCAT} expression
     */
    public static String concat(String... values) {
        return "CONCAT(" + String.join(", ", values) + ")";
    }

    /**
     * Returns the current database date keyword.
     *
     * @return SQL {@code CURRENT_DATE} keyword
     */
    public static String currentDate() {
        return CURRENT_DATE;
    }

    /**
     * Returns the current database timestamp keyword.
     *
     * @return SQL {@code CURRENT_TIMESTAMP} keyword
     */
    public static String currentTimestamp() {
        return CURRENT_TIMESTAMP;
    }

    /**
     * Returns ascending sort direction.
     *
     * @return SQL {@code ASC} keyword
     */
    public static String ascending() {
        return ASC;
    }

    /**
     * Returns descending sort direction.
     *
     * @return SQL {@code DESC} keyword
     */
    public static String descending() {
        return DESC;
    }

    private static String escape(String value) {
        return value.replace("'", "''");
    }

    private static String formatValue(Object v) {
        if (v == null) return "NULL";
        if (v instanceof String || v instanceof Character) {
            return "'" + escape(v.toString()) + "'";
        }
        return v.toString();
    }
}

