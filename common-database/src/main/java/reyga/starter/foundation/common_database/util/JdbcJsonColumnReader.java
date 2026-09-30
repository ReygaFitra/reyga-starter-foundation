package reyga.starter.foundation.common_database.util;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.jspecify.annotations.Nullable;

/**
 * Retrieves a JSON column as text using a JDBC driver or storage-specific strategy.
 *
 * <p>Implementations must return null only for SQL {@code NULL}, preserve the JSON
 * document, and finish reading and closing any streams they open before returning.
 * They must not advance or close the result set. Native binary JSON must be converted
 * by a compatible driver or reader; arbitrary binary values are not UTF-8 JSON.</p>
 *
 * <p>Consumers can implement this contract when a driver does not support a standard
 * reader from {@link JdbcJsonColumnReaders}. Reused implementations must be
 * thread-safe and should enforce a document size limit.</p>
 */
@FunctionalInterface
public interface JdbcJsonColumnReader {

    /**
     * Reads the designated column without changing the JDBC cursor position.
     *
     * @param resultSet result set positioned on the current row; must not be null
     * @param column column label or alias; must not be null or blank
     * @return the complete JSON text, or null only for SQL {@code NULL}
     * @throws SQLException if reading, conversion, or a configured size limit fails
     */
    @Nullable String read(ResultSet resultSet, String column) throws SQLException;
}
