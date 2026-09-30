package reyga.starter.foundation.common_database.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLDataException;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Provides bounded JSON text readers using standard JDBC getters.
 *
 * <p>Readers are immutable and thread-safe. Each reads one current-row column,
 * preserves SQL {@code NULL}, and never advances or closes the result set. Stream
 * readers close their owned streams on both success and failure. Actual SQL type
 * conversion depends on the database and JDBC driver.</p>
 */
public final class JdbcJsonColumnReaders {

    /**
     * Default document limit of 1,048,576 Java UTF-16 code units, not bytes.
     */
    public static final int DEFAULT_MAX_CHARACTERS = 1_048_576;

    private JdbcJsonColumnReaders() {
    }

    /**
     * Creates the default text reader using {@link ResultSet#getString(String)}.
     *
     * @return a reader with {@link #DEFAULT_MAX_CHARACTERS} as its document limit
     */
    public static JdbcJsonColumnReader text() {
        return text(DEFAULT_MAX_CHARACTERS);
    }

    /**
     * Reads JSON through {@link ResultSet#getString(String)} with a document limit.
     *
     * <p>This is suitable for text columns and native JSON types whose drivers support
     * text conversion. The driver materializes the string before its length is checked;
     * use a stream reader to limit buffering while reading large LOB values.</p>
     *
     * @param maxCharacters maximum Java UTF-16 code units; must be positive
     * @return an immutable text reader
     * @throws IllegalArgumentException if the limit is not positive
     */
    public static JdbcJsonColumnReader text(int maxCharacters) {
        validateLimit(maxCharacters);
        return (resultSet, column) -> {
            validateColumn(resultSet, column);
            String json = resultSet.getString(column);
            if (json != null && json.length() > maxCharacters) {
                throw limitFailure(column, maxCharacters);
            }
            return json;
        };
    }

    /**
     * Creates a character-stream reader using the default document limit.
     *
     * @return a reader with {@link #DEFAULT_MAX_CHARACTERS} as its document limit
     */
    public static JdbcJsonColumnReader characterStream() {
        return characterStream(DEFAULT_MAX_CHARACTERS);
    }

    /**
     * Reads text or CLOB JSON through {@link ResultSet#getCharacterStream(String)}.
     *
     * <p>The stream is fully consumed and closed before another column is read.
     * NCLOB conversion is supported only where the driver supports this getter;
     * otherwise use a custom reader with the driver's national-character API.</p>
     *
     * @param maxCharacters maximum Java UTF-16 code units; must be positive
     * @return an immutable character-stream reader
     * @throws IllegalArgumentException if the limit is not positive
     */
    public static JdbcJsonColumnReader characterStream(int maxCharacters) {
        validateLimit(maxCharacters);
        return (resultSet, column) -> {
            validateColumn(resultSet, column);
            try (Reader reader = resultSet.getCharacterStream(column)) {
                return reader == null ? null : readCharacters(reader, column, maxCharacters);
            } catch (IOException exception) {
                throw readFailure(column, exception);
            }
        };
    }

    /**
     * Creates a UTF-8 binary-stream reader using the default document limit.
     *
     * @return a reader with {@link #DEFAULT_MAX_CHARACTERS} as its document limit
     */
    public static JdbcJsonColumnReader utf8BinaryStream() {
        return utf8BinaryStream(DEFAULT_MAX_CHARACTERS);
    }

    /**
     * Reads UTF-8 JSON text stored in binary or BLOB columns.
     *
     * <p>Uses {@link ResultSet#getBinaryStream(String)} and rejects malformed UTF-8.
     * This reader is not a decoder for vendor-native binary JSON, compressed data,
     * or binary content in other encodings. The owned stream is always closed.</p>
     *
     * @param maxCharacters maximum decoded Java UTF-16 code units; must be positive
     * @return an immutable UTF-8 binary-stream reader
     * @throws IllegalArgumentException if the limit is not positive
     */
    public static JdbcJsonColumnReader utf8BinaryStream(int maxCharacters) {
        validateLimit(maxCharacters);
        return (resultSet, column) -> {
            validateColumn(resultSet, column);
            InputStream stream = resultSet.getBinaryStream(column);
            if (stream == null) {
                return null;
            }
            try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT))) {
                return readCharacters(reader, column, maxCharacters);
            } catch (IOException exception) {
                throw readFailure(column, exception);
            }
        };
    }

    private static String readCharacters(Reader reader, String column, int maxCharacters) throws IOException, SQLException {
        StringBuilder json = new StringBuilder(Math.min(maxCharacters, 4096));
        char[] buffer = new char[Math.min(maxCharacters, 4096)];
        int count;
        while ((count = reader.read(buffer)) != -1) {
            if (count > maxCharacters - json.length()) {
                throw limitFailure(column, maxCharacters);
            }
            json.append(buffer, 0, count);
        }
        return json.toString();
    }

    private static void validateLimit(int maxCharacters) {
        if (maxCharacters <= 0) {
            throw new IllegalArgumentException("maxCharacters must be positive");
        }
    }

    private static void validateColumn(ResultSet resultSet, String column) {
        Objects.requireNonNull(resultSet, "resultSet must not be null");
        Objects.requireNonNull(column, "column must not be null");
        if (column.isBlank()) {
            throw new IllegalArgumentException("column must not be blank");
        }
    }

    private static SQLDataException limitFailure(String column, int maxCharacters) {
        return new SQLDataException("JSON column '" + column + "' exceeds the maximum of "
                + maxCharacters + " characters", "22001");
    }

    private static SQLDataException readFailure(String column, IOException cause) {
        return new SQLDataException("Cannot read JSON column '" + column + "'", "22000", cause);
    }
}
