package reyga.starter.foundation.common_database.util;

import java.util.Objects;

/**
 * SQL expression helpers intended for trusted query composition with {@link QueryBuilder}.
 *
 * <p>Arguments representing identifiers or SQL expressions are not bind parameters and must
 * never come directly from untrusted input. Prefer {@code QueryBuilder} condition methods for
 * values, because those methods bind values through JDBC placeholders.</p>
 */
public class QueryFunction {

    private static final String ABS = "ABS";
    private static final String ADD_MONTHS = "ADD_MONTHS";
    private static final String AGE = "AGE";
    private static final String ARRAY_AGG = "ARRAY_AGG";
    private static final String AVG = "AVG";
    private static final String CEIL = "CEIL";
    private static final String CEILING = "CEILING";
    private static final String CHAR_LENGTH = "CHAR_LENGTH";
    private static final String CHARINDEX = "CHARINDEX";
    private static final String CONCAT_WS = "CONCAT_WS";
    private static final String CONVERT = "CONVERT";
    private static final String DATE = "DATE";
    private static final String DATE_FORMAT = "DATE_FORMAT";
    private static final String DATE_PART = "DATE_PART";
    private static final String DATE_TRUNC = "DATE_TRUNC";
    private static final String DATEADD = "DATEADD";
    private static final String DATEDIFF = "DATEDIFF";
    private static final String DATEFROMPARTS = "DATEFROMPARTS";
    private static final String DATENAME = "DATENAME";
    private static final String DATEPART = "DATEPART";
    private static final String DATETIME = "DATETIME";
    private static final String DAY = "DAY";
    private static final String EOMONTH = "EOMONTH";
    private static final String FLOOR = "FLOOR";
    private static final String FORMAT = "FORMAT";
    private static final String GETDATE = "GETDATE";
    private static final String GROUP_CONCAT = "GROUP_CONCAT";
    private static final String HEX = "HEX";
    private static final String IFNULL = "IFNULL";
    private static final String INSTR = "INSTR";
    private static final String ISJSON = "ISJSON";
    private static final String ISNULL = "ISNULL";
    private static final String JSON = "JSON";
    private static final String JSON_AGG = "JSON_AGG";
    private static final String JSON_ARRAY_LENGTH = "JSON_ARRAY_LENGTH";
    private static final String JSON_ARRAYAGG = "JSON_ARRAYAGG";
    private static final String JSON_CONTAINS = "JSON_CONTAINS";
    private static final String JSON_EXISTS = "JSON_EXISTS";
    private static final String JSON_EXTRACT = "JSON_EXTRACT";
    private static final String JSON_EXTRACT_PATH_TEXT = "JSON_EXTRACT_PATH_TEXT";
    private static final String JSON_GROUP_ARRAY = "JSON_GROUP_ARRAY";
    private static final String JSON_GROUP_OBJECT = "JSON_GROUP_OBJECT";
    private static final String JSON_LENGTH = "JSON_LENGTH";
    private static final String JSON_MODIFY = "JSON_MODIFY";
    private static final String JSON_OBJECTAGG = "JSON_OBJECTAGG";
    private static final String JSON_QUERY = "JSON_QUERY";
    private static final String JSON_REMOVE = "JSON_REMOVE";
    private static final String JSON_SET = "JSON_SET";
    private static final String JSON_TYPE = "JSON_TYPE";
    private static final String JSON_UNQUOTE = "JSON_UNQUOTE";
    private static final String JSON_VALID = "JSON_VALID";
    private static final String JSON_VALUE = "JSON_VALUE";
    private static final String JSONB_AGG = "JSONB_AGG";
    private static final String JSONB_ARRAY_LENGTH = "JSONB_ARRAY_LENGTH";
    private static final String JSONB_EXTRACT_PATH_TEXT = "JSONB_EXTRACT_PATH_TEXT";
    private static final String JSONB_OBJECT_AGG = "JSONB_OBJECT_AGG";
    private static final String JSONB_PATH_EXISTS = "JSONB_PATH_EXISTS";
    private static final String JSONB_SET = "JSONB_SET";
    private static final String JSONB_TYPEOF = "JSONB_TYPEOF";
    private static final String JULIANDAY = "JULIANDAY";
    private static final String LAST_DAY = "LAST_DAY";
    private static final String LEFT = "LEFT";
    private static final String LEN = "LEN";
    private static final String LENGTH = "LENGTH";
    private static final String LISTAGG = "LISTAGG";
    private static final String LOCATE = "LOCATE";
    private static final String LPAD = "LPAD";
    private static final String MAKE_DATE = "MAKE_DATE";
    private static final String MAX = "MAX";
    private static final String MIN = "MIN";
    private static final String MONTH = "MONTH";
    private static final String MONTHS_BETWEEN = "MONTHS_BETWEEN";
    private static final String NEWID = "NEWID";
    private static final String NEXT_DAY = "NEXT_DAY";
    private static final String NOW = "NOW";
    private static final String NULLIF = "NULLIF";
    private static final String NVL2 = "NVL2";
    private static final String PATINDEX = "PATINDEX";
    private static final String POWER = "POWER";
    private static final String PRINTF = "PRINTF";
    private static final String RANDOM = "RANDOM";
    private static final String REGEXP_REPLACE = "REGEXP_REPLACE";
    private static final String REGEXP_SUBSTR = "REGEXP_SUBSTR";
    private static final String REPLACE = "REPLACE";
    private static final String REPLICATE = "REPLICATE";
    private static final String RIGHT = "RIGHT";
    private static final String ROUND = "ROUND";
    private static final String RPAD = "RPAD";
    private static final String SPLIT_PART = "SPLIT_PART";
    private static final String STR_TO_DATE = "STR_TO_DATE";
    private static final String STRFTIME = "STRFTIME";
    private static final String STRING_AGG = "STRING_AGG";
    private static final String STUFF = "STUFF";
    private static final String SUBSTR = "SUBSTR";
    private static final String SUBSTRING = "SUBSTRING";
    private static final String SUBSTRING_INDEX = "SUBSTRING_INDEX";
    private static final String SUM = "SUM";
    private static final String SYS_GUID = "SYS_GUID";
    private static final String SYSDATETIME = "SYSDATETIME";
    private static final String TIME = "TIME";
    private static final String TIMESTAMPADD = "TIMESTAMPADD";
    private static final String TIMESTAMPDIFF = "TIMESTAMPDIFF";
    private static final String TO_CHAR = "TO_CHAR";
    private static final String TO_DATE = "TO_DATE";
    private static final String TO_NUMBER = "TO_NUMBER";
    private static final String TO_TIMESTAMP = "TO_TIMESTAMP";
    private static final String TOTAL = "TOTAL";
    private static final String TRIM = "TRIM";
    private static final String TRUNC = "TRUNC";
    private static final String TRY_CONVERT = "TRY_CONVERT";
    private static final String UNIXEPOCH = "UNIXEPOCH";
    private static final String YEAR = "YEAR";

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


    /**
     * Builds a database or schema-qualified function call, including zero-argument calls.
     * This extension point does not check function availability, arity or database versions.
     * Arguments are trusted SQL fragments, never automatically quoted or bound. Instances of
     * this utility hold no state and may be used concurrently.
     * @param functionName identifier, optionally qualified by unquoted schema identifiers
     * @param arguments trusted nonblank SQL expressions
     * @return function call
     * @throws NullPointerException if the name, array or an argument is null
     * @throws IllegalArgumentException if the name is invalid or an argument is blank
     */
    public static String call(String functionName, String... arguments) {
        Objects.requireNonNull(functionName, "functionName");
        Objects.requireNonNull(arguments, "arguments");
        if (!isValidFunctionName(functionName)) {
            throw new IllegalArgumentException("functionName must contain unquoted SQL identifiers");
        }
        for (String argument : arguments) {
            requireExpression(argument);
        }
        return functionName + "(" + String.join(", ", arguments) + ")";
    }

    /**
     * Builds a {@code SUM} expression using trusted SQL arguments.
     * Arguments are inserted verbatim; placeholders must be bound separately.
     * @param expression nonblank trusted SQL expression
     * @return SQL function expression; database null and type semantics apply
     * @throws NullPointerException if an argument is null
     * @throws IllegalArgumentException if an argument is blank
     */
    public static String sum(String expression) {
        return call(SUM, expression);
    }

    /**
     * Builds a {@code AVG} expression using trusted SQL arguments.
     * Arguments are inserted verbatim; placeholders must be bound separately.
     * @param expression nonblank trusted SQL expression
     * @return SQL function expression; database null and type semantics apply
     * @throws NullPointerException if an argument is null
     * @throws IllegalArgumentException if an argument is blank
     */
    public static String average(String expression) {
        return call(AVG, expression);
    }

    /**
     * Builds a {@code MIN} expression using trusted SQL arguments.
     * Arguments are inserted verbatim; placeholders must be bound separately.
     * @param expression nonblank trusted SQL expression
     * @return SQL function expression; database null and type semantics apply
     * @throws NullPointerException if an argument is null
     * @throws IllegalArgumentException if an argument is blank
     */
    public static String minimum(String expression) {
        return call(MIN, expression);
    }

    /**
     * Builds a {@code MAX} expression using trusted SQL arguments.
     * Arguments are inserted verbatim; placeholders must be bound separately.
     * @param expression nonblank trusted SQL expression
     * @return SQL function expression; database null and type semantics apply
     * @throws NullPointerException if an argument is null
     * @throws IllegalArgumentException if an argument is blank
     */
    public static String maximum(String expression) {
        return call(MAX, expression);
    }

    /**
     * Builds a {@code ABS} expression using trusted SQL arguments.
     * Arguments are inserted verbatim; placeholders must be bound separately.
     * @param expression nonblank trusted SQL expression
     * @return SQL function expression; database null and type semantics apply
     * @throws NullPointerException if an argument is null
     * @throws IllegalArgumentException if an argument is blank
     */
    public static String absolute(String expression) {
        return call(ABS, expression);
    }

    /**
     * Builds a {@code ROUND} expression using trusted SQL arguments.
     * Arguments are inserted verbatim; placeholders must be bound separately.
     * @param expression nonblank trusted SQL expression
     * @param scale nonblank trusted SQL expression
     * @return SQL function expression; database null and type semantics apply
     * @throws NullPointerException if an argument is null
     * @throws IllegalArgumentException if an argument is blank
     */
    public static String round(String expression, String scale) {
        return call(ROUND, expression, scale);
    }

    /**
     * Builds a {@code TRIM} expression using trusted SQL arguments.
     * Arguments are inserted verbatim; placeholders must be bound separately.
     * @param expression nonblank trusted SQL expression
     * @return SQL function expression; database null and type semantics apply
     * @throws NullPointerException if an argument is null
     * @throws IllegalArgumentException if an argument is blank
     */
    public static String trim(String expression) {
        return call(TRIM, expression);
    }

    /**
     * Builds a {@code REPLACE} expression using trusted SQL arguments.
     * Arguments are inserted verbatim; placeholders must be bound separately.
     * @param expression nonblank trusted SQL expression
     * @param search nonblank trusted SQL expression
     * @param replacement nonblank trusted SQL expression
     * @return SQL function expression; database null and type semantics apply
     * @throws NullPointerException if an argument is null
     * @throws IllegalArgumentException if an argument is blank
     */
    public static String replace(String expression, String search, String replacement) {
        return call(REPLACE, expression, search, replacement);
    }

    /**
     * Builds a {@code NULLIF} expression using trusted SQL arguments.
     * Arguments are inserted verbatim; placeholders must be bound separately.
     * @param expression nonblank trusted SQL expression
     * @param comparison nonblank trusted SQL expression
     * @return SQL function expression; database null and type semantics apply
     * @throws NullPointerException if an argument is null
     * @throws IllegalArgumentException if an argument is blank
     */
    public static String nullIf(String expression, String comparison) {
        return call(NULLIF, expression, comparison);
    }

    /**
     * PostgreSql SQL helpers. Arguments are trusted SQL expressions, including quoted format
     * and path literals or placeholders where the database permits them. No values are bound.
     * Availability, null behavior and result types depend on the database version.
     */
    public static final class PostgreSql {
        /**
         * Builds {@code FLOOR} with trusted numeric SQL expressions.
         * @param expression nonblank trusted numeric SQL expression
         * @return SQL expression; database precision, domain and null behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String floor(String expression) {
            return call(FLOOR, expression);
        }

        /**
         * Builds {@code CEIL} with trusted numeric SQL expressions.
         * @param expression nonblank trusted numeric SQL expression
         * @return SQL expression; database precision, domain and null behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String ceiling(String expression) {
            return call(CEIL, expression);
        }

        /**
         * Builds {@code POWER} with trusted numeric SQL expressions.
         * @param base nonblank trusted numeric SQL expression
         * @param exponent nonblank trusted numeric SQL expression
         * @return SQL expression; database precision, domain and null behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String power(String base, String exponent) {
            return call(POWER, base, exponent);
        }

        /**
         * Builds {@code SPLIT_PART} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @param delimiter nonblank trusted SQL expression or database keyword
         * @param field nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String splitPart(String expression, String delimiter, String field) {
            return call(SPLIT_PART, expression, delimiter, field);
        }

        /**
         * Builds {@code SUBSTR} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @param start nonblank trusted SQL expression or database keyword
         * @param length nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String substring(String expression, String start, String length) {
            return call(SUBSTR, expression, start, length);
        }

        /**
         * Builds {@code LEFT} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @param length nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String left(String expression, String length) {
            return call(LEFT, expression, length);
        }

        /**
         * Builds {@code RIGHT} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @param length nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String right(String expression, String length) {
            return call(RIGHT, expression, length);
        }

        /**
         * Builds {@code LPAD} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @param length nonblank trusted SQL expression or database keyword
         * @param padding nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String lpad(String expression, String length, String padding) {
            return call(LPAD, expression, length, padding);
        }

        /**
         * Builds {@code RPAD} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @param length nonblank trusted SQL expression or database keyword
         * @param padding nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String rpad(String expression, String length, String padding) {
            return call(RPAD, expression, length, padding);
        }

        /**
         * Builds {@code REGEXP_REPLACE} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @param pattern nonblank trusted SQL expression or database keyword
         * @param replacement nonblank trusted SQL expression or database keyword
         * @param flags nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String regexpReplace(String expression, String pattern, String replacement, String flags) {
            return call(REGEXP_REPLACE, expression, pattern, replacement, flags);
        }

        /**
         * Builds {@code DATE_PART} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param field nonblank trusted SQL expression or database keyword
         * @param expression nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String datePart(String field, String expression) {
            return call(DATE_PART, field, expression);
        }

        /**
         * Builds {@code AGE} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param end nonblank trusted SQL expression or database keyword
         * @param start nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String age(String end, String start) {
            return call(AGE, end, start);
        }

        /**
         * Builds {@code MAKE_DATE} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param year nonblank trusted SQL expression or database keyword
         * @param month nonblank trusted SQL expression or database keyword
         * @param day nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String makeDate(String year, String month, String day) {
            return call(MAKE_DATE, year, month, day);
        }

        /**
         * Builds {@code TO_DATE} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @param format nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String toDate(String expression, String format) {
            return call(TO_DATE, expression, format);
        }

        /**
         * Builds {@code TO_TIMESTAMP} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @param format nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String toTimestamp(String expression, String format) {
            return call(TO_TIMESTAMP, expression, format);
        }

        /**
         * Builds {@code ARRAY_AGG} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String arrayAgg(String expression) {
            return call(ARRAY_AGG, expression);
        }

        /**
         * Builds {@code JSONB_AGG} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String jsonbAgg(String expression) {
            return call(JSONB_AGG, expression);
        }

        /**
         * Builds {@code JSONB_ARRAY_LENGTH} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String jsonbArrayLength(String expression) {
            return call(JSONB_ARRAY_LENGTH, expression);
        }

        /**
         * Builds {@code JSONB_TYPEOF} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String jsonbTypeof(String expression) {
            return call(JSONB_TYPEOF, expression);
        }

        /**
         * Builds {@code JSONB_SET} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @param path nonblank trusted SQL expression or database keyword
         * @param value nonblank trusted SQL expression or database keyword
         * @param createMissing nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String jsonbSet(String expression, String path, String value, String createMissing) {
            return call(JSONB_SET, expression, path, value, createMissing);
        }

        /**
         * Builds {@code JSONB_PATH_EXISTS} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @param path nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String jsonbPathExists(String expression, String path) {
            return call(JSONB_PATH_EXISTS, expression, path);
        }

        /**
         * Builds {@code JSONB_OBJECT_AGG} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param key nonblank trusted SQL expression or database keyword
         * @param value nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String jsonbObjectAgg(String key, String value) {
            return call(JSONB_OBJECT_AGG, key, value);
        }

        /**
         * Builds {@code NOW} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.

         * @return SQL expression; database null, conversion and failure behavior applies
         */
        public static String now() {
            return call(NOW);
        }

        private PostgreSql() {
        }

        /**
         * Builds a {@code DATE_TRUNC} expression using trusted SQL arguments.
         * Arguments are inserted verbatim; placeholders must be bound separately.
         * @param precision nonblank trusted SQL expression
         * @param expression nonblank trusted SQL expression
         * @return SQL function expression; database null and type semantics apply
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String dateTrunc(String precision, String expression) {
            return call(DATE_TRUNC, precision, expression);
        }

        /**
         * Builds a {@code TO_CHAR} expression using trusted SQL arguments.
         * Arguments are inserted verbatim; placeholders must be bound separately.
         * @param expression nonblank trusted SQL expression
         * @param format nonblank trusted SQL expression
         * @return SQL function expression; database null and type semantics apply
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String toChar(String expression, String format) {
            return call(TO_CHAR, expression, format);
        }

        /**
         * Builds a {@code STRING_AGG} expression using trusted SQL arguments.
         * Arguments are inserted verbatim; placeholders must be bound separately.
         * @param expression nonblank trusted SQL expression
         * @param delimiter nonblank trusted SQL expression
         * @return SQL function expression; database null and type semantics apply
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String stringAgg(String expression, String delimiter) {
            return call(STRING_AGG, expression, delimiter);
        }

        /**
         * Builds a {@code JSON_EXTRACT_PATH_TEXT} expression using trusted SQL arguments.
         * Arguments are inserted verbatim; placeholders must be bound separately.
         * @param expression nonblank trusted SQL expression
         * @param path nonblank trusted SQL expression
         * @return SQL function expression; database null and type semantics apply
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String jsonExtractPathText(String expression, String path) {
            return call(JSON_EXTRACT_PATH_TEXT, expression, path);
        }

        /**
         * Builds a {@code JSONB_EXTRACT_PATH_TEXT} expression using trusted SQL arguments.
         * Arguments are inserted verbatim; placeholders must be bound separately.
         * @param expression nonblank trusted SQL expression
         * @param path nonblank trusted SQL expression
         * @return SQL function expression; database null and type semantics apply
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String jsonbExtractPathText(String expression, String path) {
            return call(JSONB_EXTRACT_PATH_TEXT, expression, path);
        }

        /**
         * Builds a {@code JSON_AGG} expression using trusted SQL arguments.
         * Arguments are inserted verbatim; placeholders must be bound separately.
         * @param expression nonblank trusted SQL expression
         * @return SQL function expression; database null and type semantics apply
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String jsonAgg(String expression) {
            return call(JSON_AGG, expression);
        }

    }

    /**
     * MySql SQL helpers. Arguments are trusted SQL expressions, including quoted format
     * and path literals or placeholders where the database permits them. No values are bound.
     * Availability, null behavior and result types depend on the database version.
     */
    public static final class MySql {
        /**
         * Builds {@code FLOOR} with trusted numeric SQL expressions.
         * @param expression nonblank trusted numeric SQL expression
         * @return SQL expression; database precision, domain and null behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String floor(String expression) {
            return call(FLOOR, expression);
        }

        /**
         * Builds {@code CEILING} with trusted numeric SQL expressions.
         * @param expression nonblank trusted numeric SQL expression
         * @return SQL expression; database precision, domain and null behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String ceiling(String expression) {
            return call(CEILING, expression);
        }

        /**
         * Builds {@code POWER} with trusted numeric SQL expressions.
         * @param base nonblank trusted numeric SQL expression
         * @param exponent nonblank trusted numeric SQL expression
         * @return SQL expression; database precision, domain and null behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String power(String base, String exponent) {
            return call(POWER, base, exponent);
        }

        /**
         * Builds {@code SUBSTRING} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @param start nonblank trusted SQL expression or database keyword
         * @param length nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String substring(String expression, String start, String length) {
            return call(SUBSTRING, expression, start, length);
        }

        /**
         * Builds {@code SUBSTRING_INDEX} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @param delimiter nonblank trusted SQL expression or database keyword
         * @param count nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String substringIndex(String expression, String delimiter, String count) {
            return call(SUBSTRING_INDEX, expression, delimiter, count);
        }

        /**
         * Builds {@code LOCATE} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param search nonblank trusted SQL expression or database keyword
         * @param expression nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String locate(String search, String expression) {
            return call(LOCATE, search, expression);
        }

        /**
         * Builds {@code LEFT} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @param length nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String left(String expression, String length) {
            return call(LEFT, expression, length);
        }

        /**
         * Builds {@code RIGHT} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @param length nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String right(String expression, String length) {
            return call(RIGHT, expression, length);
        }

        /**
         * Builds {@code LPAD} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @param length nonblank trusted SQL expression or database keyword
         * @param padding nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String lpad(String expression, String length, String padding) {
            return call(LPAD, expression, length, padding);
        }

        /**
         * Builds {@code RPAD} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @param length nonblank trusted SQL expression or database keyword
         * @param padding nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String rpad(String expression, String length, String padding) {
            return call(RPAD, expression, length, padding);
        }

        /**
         * Builds {@code CHAR_LENGTH} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String charLength(String expression) {
            return call(CHAR_LENGTH, expression);
        }

        /**
         * Builds {@code REGEXP_REPLACE} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @param pattern nonblank trusted SQL expression or database keyword
         * @param replacement nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String regexpReplace(String expression, String pattern, String replacement) {
            return call(REGEXP_REPLACE, expression, pattern, replacement);
        }

        /**
         * Builds {@code TIMESTAMPADD} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param unit nonblank trusted SQL expression or database keyword
         * @param amount nonblank trusted SQL expression or database keyword
         * @param expression nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String timestampAdd(String unit, String amount, String expression) {
            return call(TIMESTAMPADD, unit, amount, expression);
        }

        /**
         * Builds {@code TIMESTAMPDIFF} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param unit nonblank trusted SQL expression or database keyword
         * @param start nonblank trusted SQL expression or database keyword
         * @param end nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String timestampDiff(String unit, String start, String end) {
            return call(TIMESTAMPDIFF, unit, start, end);
        }

        /**
         * Builds {@code DATEDIFF} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param end nonblank trusted SQL expression or database keyword
         * @param start nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String dateDiff(String end, String start) {
            return call(DATEDIFF, end, start);
        }

        /**
         * Builds {@code LAST_DAY} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String lastDay(String expression) {
            return call(LAST_DAY, expression);
        }

        /**
         * Builds {@code YEAR} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String year(String expression) {
            return call(YEAR, expression);
        }

        /**
         * Builds {@code MONTH} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String month(String expression) {
            return call(MONTH, expression);
        }

        /**
         * Builds {@code DAY} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String day(String expression) {
            return call(DAY, expression);
        }

        /**
         * Builds {@code JSON_SET} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @param path nonblank trusted SQL expression or database keyword
         * @param value nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String jsonSet(String expression, String path, String value) {
            return call(JSON_SET, expression, path, value);
        }

        /**
         * Builds {@code JSON_REMOVE} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @param path nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String jsonRemove(String expression, String path) {
            return call(JSON_REMOVE, expression, path);
        }

        /**
         * Builds {@code JSON_CONTAINS} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @param candidate nonblank trusted SQL expression or database keyword
         * @param path nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String jsonContains(String expression, String candidate, String path) {
            return call(JSON_CONTAINS, expression, candidate, path);
        }

        /**
         * Builds {@code JSON_LENGTH} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String jsonLength(String expression) {
            return call(JSON_LENGTH, expression);
        }

        /**
         * Builds {@code JSON_VALID} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String jsonValid(String expression) {
            return call(JSON_VALID, expression);
        }

        /**
         * Builds {@code JSON_OBJECTAGG} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param key nonblank trusted SQL expression or database keyword
         * @param value nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String jsonObjectAgg(String key, String value) {
            return call(JSON_OBJECTAGG, key, value);
        }

        /**
         * Builds {@code NOW} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.

         * @return SQL expression; database null, conversion and failure behavior applies
         */
        public static String now() {
            return call(NOW);
        }

        private MySql() {
        }

        /**
         * Aggregates text in a specified order using MySQL separator syntax.
         * @param expression trusted nonblank SQL expression
         * @param delimiter trusted nonblank delimiter expression, normally a quoted literal
         * @param orderBy trusted nonblank ordering expression including optional direction
         * @return GROUP_CONCAT expression; server length limits apply
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String groupConcat(String expression, String delimiter, String orderBy) {
            call(GROUP_CONCAT, expression, delimiter, orderBy);
            return "GROUP_CONCAT(" + expression + " ORDER BY " + orderBy + " SEPARATOR " + delimiter + ")";
        }

        /**
         * Builds a {@code IFNULL} expression using trusted SQL arguments.
         * Arguments are inserted verbatim; placeholders must be bound separately.
         * @param expression nonblank trusted SQL expression
         * @param fallback nonblank trusted SQL expression
         * @return SQL function expression; database null and type semantics apply
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String ifNull(String expression, String fallback) {
            return call(IFNULL, expression, fallback);
        }

        /**
         * Builds a {@code DATE_FORMAT} expression using trusted SQL arguments.
         * Arguments are inserted verbatim; placeholders must be bound separately.
         * @param expression nonblank trusted SQL expression
         * @param format nonblank trusted SQL expression
         * @return SQL function expression; database null and type semantics apply
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String dateFormat(String expression, String format) {
            return call(DATE_FORMAT, expression, format);
        }

        /**
         * Builds a {@code STR_TO_DATE} expression using trusted SQL arguments.
         * Arguments are inserted verbatim; placeholders must be bound separately.
         * @param expression nonblank trusted SQL expression
         * @param format nonblank trusted SQL expression
         * @return SQL function expression; database null and type semantics apply
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String strToDate(String expression, String format) {
            return call(STR_TO_DATE, expression, format);
        }

        /**
         * Builds a {@code JSON_EXTRACT} expression using trusted SQL arguments.
         * Arguments are inserted verbatim; placeholders must be bound separately.
         * @param expression nonblank trusted SQL expression
         * @param path nonblank trusted SQL expression
         * @return SQL function expression; database null and type semantics apply
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String jsonExtract(String expression, String path) {
            return call(JSON_EXTRACT, expression, path);
        }

        /**
         * Builds a {@code JSON_UNQUOTE} expression using trusted SQL arguments.
         * Arguments are inserted verbatim; placeholders must be bound separately.
         * @param expression nonblank trusted SQL expression
         * @return SQL function expression; database null and type semantics apply
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String jsonUnquote(String expression) {
            return call(JSON_UNQUOTE, expression);
        }

        /**
         * Builds a {@code JSON_ARRAYAGG} expression using trusted SQL arguments.
         * Arguments are inserted verbatim; placeholders must be bound separately.
         * @param expression nonblank trusted SQL expression
         * @return SQL function expression; database null and type semantics apply
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String jsonArrayAgg(String expression) {
            return call(JSON_ARRAYAGG, expression);
        }

        /**
         * Builds a {@code CONCAT_WS} expression using trusted SQL arguments.
         * Arguments are inserted verbatim; placeholders must be bound separately.
         * @param separator nonblank trusted SQL expression
         * @param first nonblank trusted SQL expression
         * @param second nonblank trusted SQL expression
         * @return SQL function expression; database null and type semantics apply
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String concatWs(String separator, String first, String second) {
            return call(CONCAT_WS, separator, first, second);
        }

    }

    /**
     * Oracle SQL helpers. Arguments are trusted SQL expressions, including quoted format
     * and path literals or placeholders where the database permits them. No values are bound.
     * Availability, null behavior and result types depend on the database version.
     */
    public static final class Oracle {
        /**
         * Builds {@code FLOOR} with trusted numeric SQL expressions.
         * @param expression nonblank trusted numeric SQL expression
         * @return SQL expression; database precision, domain and null behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String floor(String expression) {
            return call(FLOOR, expression);
        }

        /**
         * Builds {@code CEIL} with trusted numeric SQL expressions.
         * @param expression nonblank trusted numeric SQL expression
         * @return SQL expression; database precision, domain and null behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String ceiling(String expression) {
            return call(CEIL, expression);
        }

        /**
         * Builds {@code POWER} with trusted numeric SQL expressions.
         * @param base nonblank trusted numeric SQL expression
         * @param exponent nonblank trusted numeric SQL expression
         * @return SQL expression; database precision, domain and null behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String power(String base, String exponent) {
            return call(POWER, base, exponent);
        }

        /**
         * Builds {@code SUBSTR} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @param start nonblank trusted SQL expression or database keyword
         * @param length nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String substr(String expression, String start, String length) {
            return call(SUBSTR, expression, start, length);
        }

        /**
         * Builds {@code INSTR} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @param search nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String instr(String expression, String search) {
            return call(INSTR, expression, search);
        }

        /**
         * Builds {@code LPAD} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @param length nonblank trusted SQL expression or database keyword
         * @param padding nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String lpad(String expression, String length, String padding) {
            return call(LPAD, expression, length, padding);
        }

        /**
         * Builds {@code RPAD} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @param length nonblank trusted SQL expression or database keyword
         * @param padding nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String rpad(String expression, String length, String padding) {
            return call(RPAD, expression, length, padding);
        }

        /**
         * Builds {@code REGEXP_REPLACE} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @param pattern nonblank trusted SQL expression or database keyword
         * @param replacement nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String regexpReplace(String expression, String pattern, String replacement) {
            return call(REGEXP_REPLACE, expression, pattern, replacement);
        }

        /**
         * Builds {@code REGEXP_SUBSTR} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @param pattern nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String regexpSubstr(String expression, String pattern) {
            return call(REGEXP_SUBSTR, expression, pattern);
        }

        /**
         * Builds {@code NVL2} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @param whenNotNull nonblank trusted SQL expression or database keyword
         * @param whenNull nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String nvl2(String expression, String whenNotNull, String whenNull) {
            return call(NVL2, expression, whenNotNull, whenNull);
        }

        /**
         * Builds {@code TO_NUMBER} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @param format nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String toNumber(String expression, String format) {
            return call(TO_NUMBER, expression, format);
        }

        /**
         * Builds {@code TO_DATE} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @param format nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String toDate(String expression, String format) {
            return call(TO_DATE, expression, format);
        }

        /**
         * Builds {@code TO_TIMESTAMP} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @param format nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String toTimestamp(String expression, String format) {
            return call(TO_TIMESTAMP, expression, format);
        }

        /**
         * Builds {@code LAST_DAY} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String lastDay(String expression) {
            return call(LAST_DAY, expression);
        }

        /**
         * Builds {@code NEXT_DAY} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @param weekday nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String nextDay(String expression, String weekday) {
            return call(NEXT_DAY, expression, weekday);
        }

        /**
         * Builds {@code ROUND} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @param format nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String roundDate(String expression, String format) {
            return call(ROUND, expression, format);
        }

        /**
         * Builds {@code JSON_EXISTS} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @param path nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String jsonExists(String expression, String path) {
            return call(JSON_EXISTS, expression, path);
        }

        /**
         * Builds {@code JSON_ARRAYAGG} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String jsonArrayAgg(String expression) {
            return call(JSON_ARRAYAGG, expression);
        }

        /**
         * Builds {@code SYS_GUID} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.

         * @return SQL expression; database null, conversion and failure behavior applies
         */
        public static String sysGuid() {
            return call(SYS_GUID);
        }

        private Oracle() {
        }

        /**
         * Aggregates text with mandatory ordered-group syntax.
         * @param expression trusted nonblank SQL expression
         * @param delimiter trusted nonblank delimiter expression
         * @param orderBy trusted nonblank ordering expression including optional direction
         * @return LISTAGG expression; database overflow behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String listAgg(String expression, String delimiter, String orderBy) {
            return call(LISTAGG, expression, delimiter) + " WITHIN GROUP (ORDER BY "
                    + requireExpression(orderBy) + ")";
        }

        /**
         * Builds a {@code TO_CHAR} expression using trusted SQL arguments.
         * Arguments are inserted verbatim; placeholders must be bound separately.
         * @param expression nonblank trusted SQL expression
         * @param format nonblank trusted SQL expression
         * @return SQL function expression; database null and type semantics apply
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String toChar(String expression, String format) {
            return call(TO_CHAR, expression, format);
        }

        /**
         * Builds a {@code TRUNC} expression using trusted SQL arguments.
         * Arguments are inserted verbatim; placeholders must be bound separately.
         * @param expression nonblank trusted SQL expression
         * @param format nonblank trusted SQL expression
         * @return SQL function expression; database null and type semantics apply
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String trunc(String expression, String format) {
            return call(TRUNC, expression, format);
        }

        /**
         * Builds a {@code ADD_MONTHS} expression using trusted SQL arguments.
         * Arguments are inserted verbatim; placeholders must be bound separately.
         * @param expression nonblank trusted SQL expression
         * @param months nonblank trusted SQL expression
         * @return SQL function expression; database null and type semantics apply
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String addMonths(String expression, String months) {
            return call(ADD_MONTHS, expression, months);
        }

        /**
         * Builds a {@code MONTHS_BETWEEN} expression using trusted SQL arguments.
         * Arguments are inserted verbatim; placeholders must be bound separately.
         * @param first nonblank trusted SQL expression
         * @param second nonblank trusted SQL expression
         * @return SQL function expression; database null and type semantics apply
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String monthsBetween(String first, String second) {
            return call(MONTHS_BETWEEN, first, second);
        }

        /**
         * Builds a {@code JSON_VALUE} expression using trusted SQL arguments.
         * Arguments are inserted verbatim; placeholders must be bound separately.
         * @param expression nonblank trusted SQL expression
         * @param path nonblank trusted SQL expression
         * @return SQL function expression; database null and type semantics apply
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String jsonValue(String expression, String path) {
            return call(JSON_VALUE, expression, path);
        }

        /**
         * Builds a {@code JSON_QUERY} expression using trusted SQL arguments.
         * Arguments are inserted verbatim; placeholders must be bound separately.
         * @param expression nonblank trusted SQL expression
         * @param path nonblank trusted SQL expression
         * @return SQL function expression; database null and type semantics apply
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String jsonQuery(String expression, String path) {
            return call(JSON_QUERY, expression, path);
        }

    }

    /**
     * SqlServer SQL helpers. Arguments are trusted SQL expressions, including quoted format
     * and path literals or placeholders where the database permits them. No values are bound.
     * Availability, null behavior and result types depend on the database version.
     */
    public static final class SqlServer {
        /**
         * Builds {@code FLOOR} with trusted numeric SQL expressions.
         * @param expression nonblank trusted numeric SQL expression
         * @return SQL expression; database precision, domain and null behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String floor(String expression) {
            return call(FLOOR, expression);
        }

        /**
         * Builds {@code CEILING} with trusted numeric SQL expressions.
         * @param expression nonblank trusted numeric SQL expression
         * @return SQL expression; database precision, domain and null behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String ceiling(String expression) {
            return call(CEILING, expression);
        }

        /**
         * Builds {@code POWER} with trusted numeric SQL expressions.
         * @param base nonblank trusted numeric SQL expression
         * @param exponent nonblank trusted numeric SQL expression
         * @return SQL expression; database precision, domain and null behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String power(String base, String exponent) {
            return call(POWER, base, exponent);
        }

        /**
         * Builds {@code SUBSTRING} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @param start nonblank trusted SQL expression or database keyword
         * @param length nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String substring(String expression, String start, String length) {
            return call(SUBSTRING, expression, start, length);
        }

        /**
         * Builds {@code CHARINDEX} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param search nonblank trusted SQL expression or database keyword
         * @param expression nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String charIndex(String search, String expression) {
            return call(CHARINDEX, search, expression);
        }

        /**
         * Builds {@code PATINDEX} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param pattern nonblank trusted SQL expression or database keyword
         * @param expression nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String patIndex(String pattern, String expression) {
            return call(PATINDEX, pattern, expression);
        }

        /**
         * Builds {@code LEFT} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @param length nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String left(String expression, String length) {
            return call(LEFT, expression, length);
        }

        /**
         * Builds {@code RIGHT} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @param length nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String right(String expression, String length) {
            return call(RIGHT, expression, length);
        }

        /**
         * Builds {@code REPLICATE} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @param count nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String replicate(String expression, String count) {
            return call(REPLICATE, expression, count);
        }

        /**
         * Builds {@code STUFF} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @param start nonblank trusted SQL expression or database keyword
         * @param length nonblank trusted SQL expression or database keyword
         * @param replacement nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String stuff(String expression, String start, String length, String replacement) {
            return call(STUFF, expression, start, length, replacement);
        }

        /**
         * Builds {@code DATEPART} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param datePart nonblank trusted SQL expression or database keyword
         * @param expression nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String datePart(String datePart, String expression) {
            return call(DATEPART, datePart, expression);
        }

        /**
         * Builds {@code DATENAME} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param datePart nonblank trusted SQL expression or database keyword
         * @param expression nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String dateName(String datePart, String expression) {
            return call(DATENAME, datePart, expression);
        }

        /**
         * Builds {@code EOMONTH} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @param monthOffset nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String eomonth(String expression, String monthOffset) {
            return call(EOMONTH, expression, monthOffset);
        }

        /**
         * Builds {@code DATEFROMPARTS} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param year nonblank trusted SQL expression or database keyword
         * @param month nonblank trusted SQL expression or database keyword
         * @param day nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String dateFromParts(String year, String month, String day) {
            return call(DATEFROMPARTS, year, month, day);
        }

        /**
         * Builds {@code FORMAT} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @param format nonblank trusted SQL expression or database keyword
         * @param culture nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String format(String expression, String format, String culture) {
            return call(FORMAT, expression, format, culture);
        }

        /**
         * Builds {@code CONVERT} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param dataType nonblank trusted SQL expression or database keyword
         * @param expression nonblank trusted SQL expression or database keyword
         * @param style nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String convert(String dataType, String expression, String style) {
            return call(CONVERT, dataType, expression, style);
        }

        /**
         * Builds {@code TRY_CONVERT} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param dataType nonblank trusted SQL expression or database keyword
         * @param expression nonblank trusted SQL expression or database keyword
         * @param style nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String tryConvert(String dataType, String expression, String style) {
            return call(TRY_CONVERT, dataType, expression, style);
        }

        /**
         * Builds {@code ISJSON} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String isJson(String expression) {
            return call(ISJSON, expression);
        }

        /**
         * Builds {@code JSON_MODIFY} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @param path nonblank trusted SQL expression or database keyword
         * @param value nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String jsonModify(String expression, String path, String value) {
            return call(JSON_MODIFY, expression, path, value);
        }

        /**
         * Builds {@code GETDATE} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.

         * @return SQL expression; database null, conversion and failure behavior applies
         */
        public static String getDate() {
            return call(GETDATE);
        }

        /**
         * Builds {@code SYSDATETIME} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.

         * @return SQL expression; database null, conversion and failure behavior applies
         */
        public static String sysDateTime() {
            return call(SYSDATETIME);
        }

        /**
         * Builds {@code NEWID} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.

         * @return SQL expression; database null, conversion and failure behavior applies
         */
        public static String newId() {
            return call(NEWID);
        }

        private SqlServer() {
        }

        /**
         * Builds a {@code ISNULL} expression using trusted SQL arguments.
         * Arguments are inserted verbatim; placeholders must be bound separately.
         * @param expression nonblank trusted SQL expression
         * @param fallback nonblank trusted SQL expression
         * @return SQL function expression; database null and type semantics apply
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String isNull(String expression, String fallback) {
            return call(ISNULL, expression, fallback);
        }

        /**
         * Builds a {@code LEN} expression using trusted SQL arguments.
         * Arguments are inserted verbatim; placeholders must be bound separately.
         * @param expression nonblank trusted SQL expression
         * @return SQL function expression; database null and type semantics apply
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String len(String expression) {
            return call(LEN, expression);
        }

        /**
         * Builds a {@code DATEADD} expression using trusted SQL arguments.
         * Arguments are inserted verbatim; placeholders must be bound separately.
         * @param datePart nonblank trusted SQL expression or database date-part keyword
         * @param amount nonblank trusted SQL expression
         * @param expression nonblank trusted SQL expression
         * @return SQL function expression; database null and type semantics apply
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String dateAdd(String datePart, String amount, String expression) {
            return call(DATEADD, datePart, amount, expression);
        }

        /**
         * Builds a {@code DATEDIFF} expression using trusted SQL arguments.
         * Arguments are inserted verbatim; placeholders must be bound separately.
         * @param datePart nonblank trusted SQL expression or database date-part keyword
         * @param start nonblank trusted SQL expression
         * @param end nonblank trusted SQL expression
         * @return SQL function expression; database null and type semantics apply
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String dateDiff(String datePart, String start, String end) {
            return call(DATEDIFF, datePart, start, end);
        }

        /**
         * Builds a {@code STRING_AGG} expression using trusted SQL arguments.
         * Arguments are inserted verbatim; placeholders must be bound separately.
         * @param expression nonblank trusted SQL expression
         * @param delimiter nonblank trusted SQL expression
         * @return SQL function expression; database null and type semantics apply
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String stringAgg(String expression, String delimiter) {
            return call(STRING_AGG, expression, delimiter);
        }

        /**
         * Builds a {@code JSON_VALUE} expression using trusted SQL arguments.
         * Arguments are inserted verbatim; placeholders must be bound separately.
         * @param expression nonblank trusted SQL expression
         * @param path nonblank trusted SQL expression
         * @return SQL function expression; database null and type semantics apply
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String jsonValue(String expression, String path) {
            return call(JSON_VALUE, expression, path);
        }

        /**
         * Builds a {@code JSON_QUERY} expression using trusted SQL arguments.
         * Arguments are inserted verbatim; placeholders must be bound separately.
         * @param expression nonblank trusted SQL expression
         * @param path nonblank trusted SQL expression
         * @return SQL function expression; database null and type semantics apply
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String jsonQuery(String expression, String path) {
            return call(JSON_QUERY, expression, path);
        }

    }

    /**
     * Sqlite SQL helpers. Arguments are trusted SQL expressions, including quoted format
     * and path literals or placeholders where the database permits them. No values are bound.
     * Availability, null behavior and result types depend on the database version.
     */
    public static final class Sqlite {
        /**
         * Builds {@code FLOOR} with trusted numeric SQL expressions.
         * @param expression nonblank trusted numeric SQL expression
         * @return SQL expression; database precision, domain and null behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String floor(String expression) {
            return call(FLOOR, expression);
        }

        /**
         * Builds {@code CEIL} with trusted numeric SQL expressions.
         * @param expression nonblank trusted numeric SQL expression
         * @return SQL expression; database precision, domain and null behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String ceiling(String expression) {
            return call(CEIL, expression);
        }

        /**
         * Builds {@code POWER} with trusted numeric SQL expressions.
         * @param base nonblank trusted numeric SQL expression
         * @param exponent nonblank trusted numeric SQL expression
         * @return SQL expression; database precision, domain and null behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String power(String base, String exponent) {
            return call(POWER, base, exponent);
        }

        /**
         * Builds {@code SUBSTR} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @param start nonblank trusted SQL expression or database keyword
         * @param length nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String substr(String expression, String start, String length) {
            return call(SUBSTR, expression, start, length);
        }

        /**
         * Builds {@code INSTR} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @param search nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String instr(String expression, String search) {
            return call(INSTR, expression, search);
        }

        /**
         * Builds {@code LENGTH} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String length(String expression) {
            return call(LENGTH, expression);
        }

        /**
         * Builds {@code HEX} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String hex(String expression) {
            return call(HEX, expression);
        }

        /**
         * Builds {@code PRINTF} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param format nonblank trusted SQL expression or database keyword
         * @param value nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String printf(String format, String value) {
            return call(PRINTF, format, value);
        }

        /**
         * Builds {@code DATE} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @param modifier nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String date(String expression, String modifier) {
            return call(DATE, expression, modifier);
        }

        /**
         * Builds {@code TIME} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @param modifier nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String time(String expression, String modifier) {
            return call(TIME, expression, modifier);
        }

        /**
         * Builds {@code DATETIME} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @param modifier nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String dateTime(String expression, String modifier) {
            return call(DATETIME, expression, modifier);
        }

        /**
         * Builds {@code JULIANDAY} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @param modifier nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String julianday(String expression, String modifier) {
            return call(JULIANDAY, expression, modifier);
        }

        /**
         * Builds {@code UNIXEPOCH} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @param modifier nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String unixepoch(String expression, String modifier) {
            return call(UNIXEPOCH, expression, modifier);
        }

        /**
         * Builds {@code TOTAL} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String total(String expression) {
            return call(TOTAL, expression);
        }

        /**
         * Builds {@code JSON} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String json(String expression) {
            return call(JSON, expression);
        }

        /**
         * Builds {@code JSON_ARRAY_LENGTH} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @param path nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String jsonArrayLength(String expression, String path) {
            return call(JSON_ARRAY_LENGTH, expression, path);
        }

        /**
         * Builds {@code JSON_TYPE} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @param path nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String jsonType(String expression, String path) {
            return call(JSON_TYPE, expression, path);
        }

        /**
         * Builds {@code JSON_VALID} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String jsonValid(String expression) {
            return call(JSON_VALID, expression);
        }

        /**
         * Builds {@code JSON_SET} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @param path nonblank trusted SQL expression or database keyword
         * @param value nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String jsonSet(String expression, String path, String value) {
            return call(JSON_SET, expression, path, value);
        }

        /**
         * Builds {@code JSON_REMOVE} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param expression nonblank trusted SQL expression or database keyword
         * @param path nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String jsonRemove(String expression, String path) {
            return call(JSON_REMOVE, expression, path);
        }

        /**
         * Builds {@code JSON_GROUP_OBJECT} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.
         * @param key nonblank trusted SQL expression or database keyword
         * @param value nonblank trusted SQL expression or database keyword
         * @return SQL expression; database null, conversion and failure behavior applies
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String jsonGroupObject(String key, String value) {
            return call(JSON_GROUP_OBJECT, key, value);
        }

        /**
         * Builds {@code RANDOM} with database-native argument order and semantics.
         * Trusted SQL arguments are inserted verbatim, without quoting or binding.
         * Availability and result types depend on the database version.

         * @return SQL expression; database null, conversion and failure behavior applies
         */
        public static String random() {
            return call(RANDOM);
        }

        private Sqlite() {
        }

        /**
         * Builds a {@code STRFTIME} expression using trusted SQL arguments.
         * Arguments are inserted verbatim; placeholders must be bound separately.
         * @param format nonblank trusted SQL expression
         * @param expression nonblank trusted SQL expression
         * @return SQL function expression; database null and type semantics apply
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String strftime(String format, String expression) {
            return call(STRFTIME, format, expression);
        }

        /**
         * Builds a {@code IFNULL} expression using trusted SQL arguments.
         * Arguments are inserted verbatim; placeholders must be bound separately.
         * @param expression nonblank trusted SQL expression
         * @param fallback nonblank trusted SQL expression
         * @return SQL function expression; database null and type semantics apply
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String ifNull(String expression, String fallback) {
            return call(IFNULL, expression, fallback);
        }

        /**
         * Builds a {@code GROUP_CONCAT} expression using trusted SQL arguments.
         * Arguments are inserted verbatim; placeholders must be bound separately.
         * @param expression nonblank trusted SQL expression
         * @param delimiter nonblank trusted SQL expression
         * @return SQL function expression; database null and type semantics apply
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String groupConcat(String expression, String delimiter) {
            return call(GROUP_CONCAT, expression, delimiter);
        }

        /**
         * Builds a {@code JSON_EXTRACT} expression using trusted SQL arguments.
         * Arguments are inserted verbatim; placeholders must be bound separately.
         * @param expression nonblank trusted SQL expression
         * @param path nonblank trusted SQL expression
         * @return SQL function expression; database null and type semantics apply
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String jsonExtract(String expression, String path) {
            return call(JSON_EXTRACT, expression, path);
        }

        /**
         * Builds a {@code JSON_GROUP_ARRAY} expression using trusted SQL arguments.
         * Arguments are inserted verbatim; placeholders must be bound separately.
         * @param expression nonblank trusted SQL expression
         * @return SQL function expression; database null and type semantics apply
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if an argument is blank
         */
        public static String jsonGroupArray(String expression) {
            return call(JSON_GROUP_ARRAY, expression);
        }

    }

    private static boolean isValidFunctionName(String functionName) {
        boolean identifierStart = true;
        for (int index = 0; index < functionName.length(); index++) {
            char character = functionName.charAt(index);
            if (!isValidFunctionNameCharacter(character, identifierStart)) {
                return false;
            }
            identifierStart = character == '.';
        }
        return !identifierStart;
    }

    private static boolean isValidFunctionNameCharacter(char character, boolean identifierStart) {
        if (identifierStart) {
            return isIdentifierStartCharacter(character);
        }
        return isIdentifierStartCharacter(character) || isAsciiDigit(character) || character == '.';
    }

    private static boolean isIdentifierStartCharacter(char character) {
        return character == '_' || isAsciiLetter(character);
    }

    private static boolean isAsciiLetter(char character) {
        return character >= 'A' && character <= 'Z' || character >= 'a' && character <= 'z';
    }

    private static boolean isAsciiDigit(char character) {
        return character >= '0' && character <= '9';
    }

    private static String requireExpression(String expression) {
        Objects.requireNonNull(expression, "SQL argument");
        if (expression.isBlank()) {
            throw new IllegalArgumentException("SQL arguments must not be blank");
        }
        return expression;
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

