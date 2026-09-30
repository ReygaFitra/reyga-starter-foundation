package reyga.starter.foundation.common_database.util;

import java.sql.SQLException;
import java.util.Objects;

import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.RowMapper;

/**
 * Builds Spring JDBC row mappers whose callbacks read columns through
 * {@link JdbcResultSetMapper} instead of a raw JDBC result set.
 *
 * <p>Every column read uses {@link JdbcResultSetMapper#getNullable(String, Class)}
 * or {@link JdbcResultSetMapper#getMandatory(String, Class)}, or the explicit JSON
 * methods, making nullability explicit. Callbacks can construct records or mutable
 * objects. The builder does not create a JDBC result set or execute a query.</p>
 *
 * <p>A builder is mutable and is not thread-safe. Each {@link #build()} captures
 * the current callback and JSON reader; later builder changes do not affect previously
 * built mappers. A built mapper can be reused across queries and threads if its
 * callback, JSON reader, and decoders are thread-safe. A new row-scoped column reader
 * is created for each mapped row.</p>
 *
 * @param <T> mapped row type
 */
public final class JdbcResultSetBuilder<T> {

    private RowMapping<T> mapping;
    private JdbcJsonColumnReader jsonColumnReader = JdbcJsonColumnReaders.text();

    private JdbcResultSetBuilder() {
    }

    /**
     * Creates an empty builder that requires a mapping callback before it can build.
     *
     * @param <T> mapped row type
     * @return a new builder
     */
    public static <T> JdbcResultSetBuilder<T> builder() {
        return new JdbcResultSetBuilder<>();
    }

    /**
     * Selects the callback used to construct an object from each current row.
     *
     * <p>The callback receives only the column reader and the Spring JDBC row
     * number. It must not retain the reader after it returns. Calling this method
     * again replaces the callback for future builds.</p>
     *
     * @param mapping row mapping callback; must not be null
     * @return this builder
     * @throws NullPointerException if the callback is null
     */
    public JdbcResultSetBuilder<T> map(RowMapping<T> mapping) {
        this.mapping = Objects.requireNonNull(mapping, "mapping must not be null");
        return this;
    }

    /**
     * Selects the JDBC reader used by JSON reads that do not specify their own reader.
     *
     * <p>The default is {@link JdbcJsonColumnReaders#text()}. Select a stream reader
     * for LOB storage or supply a driver-specific reader. Only future builds use the
     * changed selection. A reader reused across threads must be thread-safe.</p>
     *
     * @param jsonColumnReader JSON text reader; must not be null
     * @return this builder
     * @throws NullPointerException if the reader is null
     */
    public JdbcResultSetBuilder<T> jsonColumnReader(JdbcJsonColumnReader jsonColumnReader) {
        this.jsonColumnReader = Objects.requireNonNull(jsonColumnReader, "jsonColumnReader must not be null");
        return this;
    }

    /**
     * Builds a reusable row mapper from the selected callback.
     *
     * <p>The mapper neither advances nor closes the result set. SQL exceptions and
     * callback runtime exceptions propagate unchanged. As permitted by
     * {@link RowMapper}, a callback may return null. Column nullability applies
     * independently of the mapped object's nullability.</p>
     *
     * @return a row mapper that uses the selected callback
     * @throws IllegalStateException if no callback was supplied through {@link #map(RowMapping)}
     */
    public RowMapper<T> build() {
        if (mapping == null) {
            throw new IllegalStateException("A row mapping callback is required; call map(mapping) before build()");
        }
        RowMapping<T> selectedMapping = mapping;
        JdbcJsonColumnReader selectedReader = jsonColumnReader;
        return (resultSet, rowNumber) -> selectedMapping.map(new JdbcResultSetMapper(resultSet, selectedReader), rowNumber);
    }

    /**
     * Constructs an object using explicit nullable or mandatory column reads.
     *
     * @param <T> mapped row type
     */
    @FunctionalInterface
    public interface RowMapping<T> {

        /**
         * Maps the current row without direct access to the JDBC cursor.
         *
         * @param row column reader valid only during this invocation
         * @param rowNumber row number supplied by Spring JDBC, normally zero-based
         * @return the mapped object, which may be null
         * @throws SQLException if a column is missing, cannot be converted, or violates mandatory nullability
         */
        @Nullable T map(JdbcResultSetMapper row, int rowNumber) throws SQLException;
    }
}
