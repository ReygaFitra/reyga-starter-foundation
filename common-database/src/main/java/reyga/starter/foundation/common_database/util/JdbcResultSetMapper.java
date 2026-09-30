package reyga.starter.foundation.common_database.util;

import java.io.IOException;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLDataException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Reads columns of the current JDBC row with explicit nullability.
 *
 * <p>Instances are supplied by {@link JdbcResultSetBuilder} and are valid only during
 * its mapping callback. They are not thread-safe, do not advance or close the result
 * set, and must not be retained by the mapped object. Each typed read declares whether
 * SQL {@code NULL} is permitted. Use wrapper classes such as {@link Integer} for
 * numeric values so that a nullable column can retain {@code null}.</p>
 *
 * <p>The original static getters in {@link Nullable} and {@link Mandatory} retain
 * their historical behavior for compatibility. The typed {@code getObject} methods
 * and instance methods provide the strict nullability contract.</p>
 */
public class JdbcResultSetMapper {

    private final ResultSet resultSet;
    private final JdbcJsonColumnReader jsonColumnReader;

    JdbcResultSetMapper(ResultSet resultSet) {
        this(resultSet, JdbcJsonColumnReaders.text());
    }

    JdbcResultSetMapper(ResultSet resultSet, JdbcJsonColumnReader jsonColumnReader) {
        this.resultSet = Objects.requireNonNull(resultSet, "resultSet must not be null");
        this.jsonColumnReader = Objects.requireNonNull(jsonColumnReader, "jsonColumnReader must not be null");
    }

    /**
     * Reads a column, preserving SQL {@code NULL} as Java {@code null}.
     *
     * <p>Column aliases are accepted. Primitive class tokens are rejected; use their
     * wrapper classes. {@link LocalDate} and {@link LocalDateTime} values are converted
     * through JDBC date and timestamp getters without an explicit time-zone
     * conversion. Other types use JDBC typed conversion,
     * whose availability depends on the driver.</p>
     *
     * @param column column label or alias; must not be null or blank
     * @param type reference type expected from the column; must not be null or Void
     * @param <T> column value type
     * @return the column value, or {@code null} for SQL {@code NULL}
     * @throws NullPointerException if an argument is null
     * @throws IllegalArgumentException if the column is blank or the type is primitive or Void
     * @throws SQLException if the column cannot be read or converted
     */
    public <T> @org.jspecify.annotations.Nullable T getNullable(String column, Class<T> type) throws SQLException {
        return Nullable.getObject(resultSet, column, type);
    }

    /**
     * Reads a column that must contain a non-null SQL value.
     *
     * <p>A numeric zero, false boolean, or empty string returned by the driver is a
     * valid non-null value. Conversion follows {@link #getNullable(String, Class)}.</p>
     *
     * @param column column label or alias; must not be null or blank
     * @param type reference type expected from the column; must not be null or Void
     * @param <T> column value type
     * @return the non-null column value
     * @throws NullPointerException if an argument is null
     * @throws IllegalArgumentException if the column is blank or the type is primitive or Void
     * @throws SQLDataException if the column contains SQL {@code NULL}; SQL state is {@code 22004}
     * @throws SQLException if the column cannot be read or converted
     */
    public <T> T getMandatory(String column, Class<T> type) throws SQLException {
        return Mandatory.getObject(resultSet, column, type);
    }

    /**
     * Decodes a nullable JSON column using the reader selected by the builder.
     *
     * <p>SQL {@code NULL} returns null without calling the decoder. JSON literal
     * {@code null} is passed to the decoder, which can return Java null or a tree
     * representation. Nested objects and arrays are handled by the decoder.</p>
     *
     * @param column column label or alias; must not be null or blank
     * @param decoder JSON decoder; must not be null
     * @param <T> decoded value type, including a generic type captured by the decoder
     * @return the decoded value, or null for SQL {@code NULL} or a null decoder result
     * @throws NullPointerException if an argument is null
     * @throws IllegalArgumentException if the column is blank
     * @throws SQLDataException if JSON text is blank, cannot be decoded, or exceeds the reader limit
     * @throws SQLException if the JDBC reader fails
     */
    public <T> @org.jspecify.annotations.Nullable T getNullableJson(String column, JdbcJsonDecoder<T> decoder) throws SQLException {
        return getNullableJson(column, jsonColumnReader, decoder);
    }

    /**
     * Decodes a nullable JSON column using an explicit storage-specific reader.
     *
     * <p>Null handling follows {@link #getNullableJson(String, JdbcJsonDecoder)}.
     * Reader SQL exceptions propagate unchanged. Decoder I/O and runtime failures
     * are wrapped with a column-only message and their original cause. Causes may
     * contain parser diagnostic content and must be handled accordingly when logged.</p>
     *
     * @param column column label or alias; must not be null or blank
     * @param reader JSON text reader; must not be null
     * @param decoder JSON decoder; must not be null
     * @param <T> decoded value type
     * @return the decoded value, or null for SQL {@code NULL} or a null decoder result
     * @throws NullPointerException if an argument is null
     * @throws IllegalArgumentException if the column is blank
     * @throws SQLDataException if JSON text is blank, cannot be decoded, or exceeds the reader limit
     * @throws SQLException if the JDBC reader fails
     */
    public <T> @org.jspecify.annotations.Nullable T getNullableJson(String column, JdbcJsonColumnReader reader, JdbcJsonDecoder<T> decoder) throws SQLException {
        validateJsonArguments(column, reader, decoder);
        String json = reader.read(resultSet, column);
        return json == null ? null : decodeJson(column, json, decoder);
    }

    /**
     * Decodes a mandatory JSON column using the reader selected by the builder.
     *
     * <p>Rejects SQL {@code NULL}, JSON literal {@code null}, and null decoded values.
     * A valid JSON object or array is accepted only if the decoder supports its
     * shape. Mandatory applies to the complete document, not each nested property.</p>
     *
     * @param column column label or alias; must not be null or blank
     * @param decoder JSON decoder; must not be null
     * @param <T> decoded value type, including a generic type captured by the decoder
     * @return the non-null decoded value
     * @throws NullPointerException if an argument is null
     * @throws IllegalArgumentException if the column is blank
     * @throws SQLDataException if nullability, decoding, or a reader size limit fails
     * @throws SQLException if the JDBC reader fails
     */
    public <T> T getMandatoryJson(String column, JdbcJsonDecoder<T> decoder) throws SQLException {
        return getMandatoryJson(column, jsonColumnReader, decoder);
    }

    /**
     * Decodes a mandatory JSON column using an explicit storage-specific reader.
     *
     * <p>Null handling follows {@link #getMandatoryJson(String, JdbcJsonDecoder)}.
     * Reader SQL exceptions propagate unchanged. Decoder I/O and runtime failures
     * are wrapped with a column-only message and their original cause.</p>
     *
     * @param column column label or alias; must not be null or blank
     * @param reader JSON text reader; must not be null
     * @param decoder JSON decoder; must not be null
     * @param <T> decoded value type
     * @return the non-null decoded value
     * @throws NullPointerException if an argument is null
     * @throws IllegalArgumentException if the column is blank
     * @throws SQLDataException if nullability, decoding, or a reader size limit fails
     * @throws SQLException if the JDBC reader fails
     */
    public <T> T getMandatoryJson(String column, JdbcJsonColumnReader reader, JdbcJsonDecoder<T> decoder) throws SQLException {
        validateJsonArguments(column, reader, decoder);
        String json = reader.read(resultSet, column);
        if (json == null) {
            throw new SQLDataException("Mandatory JSON column '" + column + "' must not contain SQL NULL", "22004");
        }
        if (json.strip().equals("null")) {
            throw new SQLDataException("Mandatory JSON column '" + column + "' must not contain JSON null", "22004");
        }
        T value = decodeJson(column, json, decoder);
        if (value == null) {
            throw new SQLDataException("Mandatory JSON column '" + column + "' must decode to a non-null value", "22004");
        }
        return value;
    }

    private void validateJsonArguments(String column, JdbcJsonColumnReader reader, JdbcJsonDecoder<?> decoder) {
        validateReadArguments(resultSet, column, String.class);
        Objects.requireNonNull(reader, "reader must not be null");
        Objects.requireNonNull(decoder, "decoder must not be null");
    }

    private static <T> T decodeJson(String column, String json, JdbcJsonDecoder<T> decoder) throws SQLException {
        if (json.isBlank()) {
            throw new SQLDataException("JSON column '" + column + "' must contain a non-blank JSON document", "22000");
        }
        try {
            return decoder.decode(json);
        } catch (IOException | RuntimeException exception) {
            throw new SQLDataException("Cannot decode JSON column '" + column + "'", "22000", exception);
        }
    }

    /**
     * Static nullable column readers.
     *
     * <p>The original primitive getters return zero or false for SQL {@code NULL}.
     * Use {@link #getObject(ResultSet, String, Class)} to preserve nullable values.</p>
     */
    public static class Nullable {

        private Nullable() {
        }

        /**
         * Reads a typed value while preserving SQL {@code NULL}.
         *
         * @param rs result set positioned on the row; must not be null
         * @param column column label or alias; must not be null or blank
         * @param type reference type; must not be null, primitive, or Void
         * @param <T> column value type
         * @return the converted value, or {@code null} for SQL {@code NULL}
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if the column is blank or the type is primitive or Void
         * @throws SQLException if the column cannot be read or converted
         * @see JdbcResultSetMapper#getNullable(String, Class)
         */
        public static <T> @org.jspecify.annotations.Nullable T getObject(ResultSet rs, String column, Class<T> type) throws SQLException {
            validateReadArguments(rs, column, type);
            if (type == LocalDateTime.class) {
                Timestamp value = rs.getTimestamp(column);
                return value == null ? null : type.cast(value.toLocalDateTime());
            }
            if (type == LocalDate.class) {
                Date value = rs.getDate(column);
                return value == null ? null : type.cast(value.toLocalDate());
            }
            return rs.getObject(column, type);
        }

        /**
         * Reads a string using the original nullable contract.
         *
         * @param rs result set positioned on the row
         * @param column column label or alias
         * @return the value, or {@code null} for SQL {@code NULL}
         * @throws SQLException if the column cannot be read
         */
        public static String getString(ResultSet rs, String column) throws SQLException {
            return rs.getString(column);
        }

        /**
         * Reads an integer using the original zero-default contract.
         *
         * @param rs result set positioned on the row
         * @param column column label or alias
         * @return the value, or zero for SQL {@code NULL}
         * @throws SQLException if the column cannot be read
         */
        public static int getInt(ResultSet rs, String column) throws SQLException {
            int value = rs.getInt(column);
            return rs.wasNull() ? 0 : value;
        }

        /**
         * Reads a long integer using the original zero-default contract.
         *
         * @param rs result set positioned on the row
         * @param column column label or alias
         * @return the value, or zero for SQL {@code NULL}
         * @throws SQLException if the column cannot be read
         */
        public static long getLong(ResultSet rs, String column) throws SQLException {
            long value = rs.getLong(column);
            return rs.wasNull() ? 0L : value;
        }

        /**
         * Reads a boolean using the original false-default contract.
         *
         * @param rs result set positioned on the row
         * @param column column label or alias
         * @return the value, or false for SQL {@code NULL}
         * @throws SQLException if the column cannot be read
         */
        public static boolean getBoolean(ResultSet rs, String column) throws SQLException {
            boolean value = rs.getBoolean(column);
            return !rs.wasNull() && value;
        }

        /**
         * Reads a nullable JDBC timestamp.
         *
         * @param rs result set positioned on the row
         * @param column column label or alias
         * @return the timestamp, or {@code null} for SQL {@code NULL}
         * @throws SQLException if the column cannot be read
         */
        public static Timestamp getTimestamp(ResultSet rs, String column) throws SQLException {
            return rs.getTimestamp(column);
        }

        /**
         * Converts a nullable JDBC timestamp to a local date and time without a time zone.
         *
         * @param rs result set positioned on the row
         * @param column column label or alias
         * @return the local date and time, or {@code null} for SQL {@code NULL}
         * @throws SQLException if the column cannot be read
         */
        public static LocalDateTime getLocalDateTime(ResultSet rs, String column) throws SQLException {
            Timestamp ts = rs.getTimestamp(column);
            return ts != null ? ts.toLocalDateTime() : null;
        }
    }

    /**
     * Static mandatory column readers.
     *
     * <p>Only {@link #getObject(ResultSet, String, Class)} enforces non-null values.
     * The original getters retain JDBC defaults and historical failure behavior.</p>
     */
    public static class Mandatory {

        private Mandatory() {
        }

        /**
         * Reads a typed value and rejects SQL {@code NULL}.
         *
         * @param rs result set positioned on the row; must not be null
         * @param column column label or alias; must not be null or blank
         * @param type reference type; must not be null, primitive, or Void
         * @param <T> column value type
         * @return the non-null converted value
         * @throws NullPointerException if an argument is null
         * @throws IllegalArgumentException if the column is blank or the type is primitive or Void
         * @throws SQLDataException if the column contains SQL {@code NULL}; SQL state is {@code 22004}
         * @throws SQLException if the column cannot be read or converted
         * @see JdbcResultSetMapper#getMandatory(String, Class)
         */
        public static <T> T getObject(ResultSet rs, String column, Class<T> type) throws SQLException {
            T value = Nullable.getObject(rs, column, type);
            if (value == null) {
                throw new SQLDataException("Mandatory column '" + column + "' must not contain SQL NULL", "22004");
            }
            return value;
        }

        /**
         * Reads a string without enforcing the original mandatory designation.
         *
         * @param rs result set positioned on the row
         * @param column column label or alias
         * @return the value, or {@code null} for SQL {@code NULL}
         * @throws SQLException if the column cannot be read
         */
        public static String getString(ResultSet rs, String column) throws SQLException {
            return rs.getString(column);
        }

        /**
         * Reads an integer without checking for SQL {@code NULL}.
         *
         * @param rs result set positioned on the row
         * @param column column label or alias
         * @return the value, or the JDBC default zero for SQL {@code NULL}
         * @throws SQLException if the column cannot be read
         */
        public static int getInt(ResultSet rs, String column) throws SQLException {
            return rs.getInt(column);
        }

        /**
         * Reads a long integer without checking for SQL {@code NULL}.
         *
         * @param rs result set positioned on the row
         * @param column column label or alias
         * @return the value, or the JDBC default zero for SQL {@code NULL}
         * @throws SQLException if the column cannot be read
         */
        public static long getLong(ResultSet rs, String column) throws SQLException {
            return rs.getLong(column);
        }

        /**
         * Reads a boolean without checking for SQL {@code NULL}.
         *
         * @param rs result set positioned on the row
         * @param column column label or alias
         * @return the value, or the JDBC default false for SQL {@code NULL}
         * @throws SQLException if the column cannot be read
         */
        public static boolean getBoolean(ResultSet rs, String column) throws SQLException {
            return rs.getBoolean(column);
        }

        /**
         * Reads a JDBC timestamp without enforcing the original mandatory designation.
         *
         * @param rs result set positioned on the row
         * @param column column label or alias
         * @return the timestamp, or {@code null} for SQL {@code NULL}
         * @throws SQLException if the column cannot be read
         */
        public static Timestamp getTimestamp(ResultSet rs, String column) throws SQLException {
            return rs.getTimestamp(column);
        }

        /**
         * Converts a JDBC timestamp using the original mandatory contract.
         *
         * @param rs result set positioned on the row
         * @param column column label or alias
         * @return the local date and time without a time zone
         * @throws NullPointerException if the timestamp is SQL {@code NULL}
         * @throws SQLException if the column cannot be read
         */
        public static LocalDateTime getLocalDateTime(ResultSet rs, String column) throws SQLException {
            return rs.getTimestamp(column).toLocalDateTime();
        }
    }

    private static void validateReadArguments(ResultSet rs, String column, Class<?> type) {
        Objects.requireNonNull(rs, "resultSet must not be null");
        Objects.requireNonNull(column, "column must not be null");
        Objects.requireNonNull(type, "type must not be null");
        if (column.isBlank()) {
            throw new IllegalArgumentException("column must not be blank");
        }
        if (type.isPrimitive() || type == Void.class) {
            throw new IllegalArgumentException("type must be a reference value type; use a wrapper class instead of "
                    + type.getName());
        }
    }
}
