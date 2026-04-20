package reyga.starter.foundation.common_database.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class QueryFunction {

    private static final String CURRENT_DATE = "CURRENT_DATE";
    private static final String CURRENT_TIMESTAMP = "CURRENT_TIMESTAMP";
    private static final String ASC = "ASC";
    private static final String DESC = "DESC";

    public static String toDate(String value, String format) {
        return "TO_DATE('" + escape(value) + "', '" + escape(format) + "')";
    }

    public static String toTimestamp(String value, String format) {
        return "TO_TIMESTAMP('" + escape(value) + "', '" + escape(format) + "')";
    }

    public static String count(String column) {
        if (column == null || column.trim().isEmpty() || column.equals("*")) {
            return "COUNT(*)";
        }
        return "COUNT(" + column + ")";
    }

    public static String upper(String column) {
        return "UPPER(" + column + ")";
    }

    public static String lower(String column) {
        return "LOWER(" + column + ")";
    }

    public static String length(String column) {
        return "LENGTH(" + column + ")";
    }

    public static String coalesce(String column, Object defaultValue) {
        return "COALESCE(" + column + ", " + formatValue(defaultValue) + ")";
    }

    public static String nvl(String column, Object defaultValue) {
        return "NVL(" + column + ", " + formatValue(defaultValue) + ")";
    }

    public static String concat(String... values) {
        return "CONCAT(" + String.join(", ", values) + ")";
    }

    public static String currentDate() {
        return CURRENT_DATE;
    }

    public static String currentTimestamp() {
        return CURRENT_TIMESTAMP;
    }

    public static String ascending() {
        return ASC;
    }

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

