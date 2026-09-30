package reyga.starter.foundation.common_database.util;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLDataException;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.jdbc.core.RowMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class JdbcResultSetJsonMappingTest {

    @Test
    void should_SkipDecoder_When_NullableJsonColumnIsSqlNull() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        AtomicInteger calls = new AtomicInteger();
        RowMapper<String> mapper = JdbcResultSetBuilder.<String>builder()
                .map((row, rowNumber) -> row.getNullableJson("metadata", json -> {
                    calls.incrementAndGet();
                    return json;
                }))
                .build();

        String result = mapper.mapRow(resultSet, 0);

        assertNull(result);
        assertEquals(0, calls.get());
    }

    @Test
    void should_RejectSqlNullWithoutDecoding_When_JsonColumnIsMandatory() {
        ResultSet resultSet = mock(ResultSet.class);
        AtomicInteger calls = new AtomicInteger();
        RowMapper<String> mapper = JdbcResultSetBuilder.<String>builder()
                .map((row, rowNumber) -> row.getMandatoryJson("metadata", json -> {
                    calls.incrementAndGet();
                    return json;
                }))
                .build();

        SQLDataException exception = assertThrows(SQLDataException.class, () -> mapper.mapRow(resultSet, 0));

        assertEquals("22004", exception.getSQLState());
        assertTrue(exception.getMessage().contains("metadata"));
        assertTrue(exception.getMessage().contains("SQL NULL"));
        assertEquals(0, calls.get());
    }

    @ParameterizedTest
    @ValueSource(strings = {"null", " \t\r\nnull \r\n"})
    void should_RejectJsonNullWithoutDecoding_When_JsonColumnIsMandatory(String document) throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString("metadata")).thenReturn(document);
        AtomicInteger calls = new AtomicInteger();
        RowMapper<String> mapper = JdbcResultSetBuilder.<String>builder()
                .map((row, rowNumber) -> row.getMandatoryJson("metadata", json -> {
                    calls.incrementAndGet();
                    return json;
                }))
                .build();

        SQLDataException exception = assertThrows(SQLDataException.class, () -> mapper.mapRow(resultSet, 0));

        assertEquals("22004", exception.getSQLState());
        assertTrue(exception.getMessage().contains("JSON null"));
        assertEquals(0, calls.get());
    }

    @Test
    void should_PassJsonNullToDecoder_When_JsonColumnIsNullable() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString("metadata")).thenReturn("null");
        AtomicInteger calls = new AtomicInteger();
        RowMapper<String> mapper = JdbcResultSetBuilder.<String>builder()
                .map((row, rowNumber) -> row.getNullableJson("metadata", json -> {
                    assertEquals("null", json);
                    calls.incrementAndGet();
                    return null;
                }))
                .build();

        String result = mapper.mapRow(resultSet, 0);

        assertNull(result);
        assertEquals(1, calls.get());
    }

    @Test
    void should_RejectNullDecodedValue_When_JsonColumnIsMandatory() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString("metadata")).thenReturn("{}");
        RowMapper<String> mapper = JdbcResultSetBuilder.<String>builder()
                .map((row, rowNumber) -> row.getMandatoryJson("metadata", json -> null))
                .build();

        SQLDataException exception = assertThrows(SQLDataException.class, () -> mapper.mapRow(resultSet, 0));

        assertEquals("22004", exception.getSQLState());
        assertTrue(exception.getMessage().contains("non-null value"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\t\r\n"})
    void should_RejectBlankDocumentBeforeDecoding_When_JsonColumnIsRead(String document) throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString("metadata")).thenReturn(document);
        AtomicInteger calls = new AtomicInteger();
        JdbcJsonDecoder<String> decoder = json -> {
            calls.incrementAndGet();
            return json;
        };
        JdbcResultSetMapper row = new JdbcResultSetMapper(resultSet);

        SQLDataException nullableFailure = assertThrows(SQLDataException.class,
                () -> row.getNullableJson("metadata", decoder));
        SQLDataException mandatoryFailure = assertThrows(SQLDataException.class,
                () -> row.getMandatoryJson("metadata", decoder));

        assertEquals("22000", nullableFailure.getSQLState());
        assertEquals("22000", mandatoryFailure.getSQLState());
        assertEquals(0, calls.get());
    }

    @Test
    void should_PreserveDecoderCauseWithoutIncludingPayload_When_DecodingFails() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        String sensitiveJson = "{\"secret\":\"private-value\"}";
        when(resultSet.getString("metadata")).thenReturn(sensitiveJson);
        IOException failure = new IOException("Decoder rejected input");
        RowMapper<String> mapper = JdbcResultSetBuilder.<String>builder()
                .map((row, rowNumber) -> row.getMandatoryJson("metadata", json -> {
                    throw failure;
                }))
                .build();

        SQLDataException exception = assertThrows(SQLDataException.class, () -> mapper.mapRow(resultSet, 0));

        assertEquals("Cannot decode JSON column 'metadata'", exception.getMessage());
        assertEquals("22000", exception.getSQLState());
        assertSame(failure, exception.getCause());
    }

    @Test
    void should_PreserveRuntimeDecoderCause_When_DecodingFails() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString("metadata")).thenReturn("{}");
        IllegalArgumentException failure = new IllegalArgumentException("Target shape mismatch");
        RowMapper<String> mapper = JdbcResultSetBuilder.<String>builder()
                .map((row, rowNumber) -> row.getNullableJson("metadata", json -> {
                    throw failure;
                }))
                .build();

        SQLDataException exception = assertThrows(SQLDataException.class, () -> mapper.mapRow(resultSet, 0));

        assertEquals("22000", exception.getSQLState());
        assertSame(failure, exception.getCause());
    }

    @Test
    void should_PropagateJdbcFailure_When_ReadingJsonFails() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        SQLException failure = new SQLException("Unsupported JSON conversion", "0A000");
        when(resultSet.getString("metadata")).thenThrow(failure);
        RowMapper<String> mapper = JdbcResultSetBuilder.<String>builder()
                .map((row, rowNumber) -> row.getMandatoryJson("metadata", json -> json))
                .build();

        SQLException exception = assertThrows(SQLException.class, () -> mapper.mapRow(resultSet, 0));

        assertSame(failure, exception);
        assertEquals("0A000", exception.getSQLState());
    }

    @Test
    void should_KeepReaderSnapshot_When_BuilderReaderChanges() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        JdbcJsonColumnReader firstReader = (rs, column) -> "{\"source\":\"first\"}";
        JdbcJsonColumnReader secondReader = (rs, column) -> "{\"source\":\"second\"}";
        JdbcResultSetBuilder<String> builder = JdbcResultSetBuilder.<String>builder()
                .jsonColumnReader(firstReader)
                .map((row, rowNumber) -> row.getMandatoryJson("metadata", json -> json));
        RowMapper<String> originalMapper = builder.build();

        builder.jsonColumnReader(secondReader);
        RowMapper<String> updatedMapper = builder.build();

        assertEquals("{\"source\":\"first\"}", originalMapper.mapRow(resultSet, 0));
        assertEquals("{\"source\":\"second\"}", updatedMapper.mapRow(resultSet, 0));
    }

    @Test
    void should_UseColumnReaderOverride_When_ColumnsUseDifferentStorage() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString("text_json")).thenReturn("{}");
        JdbcJsonColumnReader customReader = (rs, column) -> "[]";
        JdbcResultSetMapper row = new JdbcResultSetMapper(resultSet);

        String defaultResult = row.getMandatoryJson("text_json", json -> json);
        String mandatoryResult = row.getMandatoryJson("binary_json", customReader, json -> json);
        String nullableResult = row.getNullableJson("binary_json", customReader, json -> json);

        assertEquals("{}", defaultResult);
        assertEquals("[]", mandatoryResult);
        assertEquals("[]", nullableResult);
    }

    @Test
    void should_RejectInvalidConfigurationBeforeReading_When_JsonArgumentsAreInvalid() {
        ResultSet resultSet = mock(ResultSet.class);
        JdbcResultSetMapper row = new JdbcResultSetMapper(resultSet);
        JdbcJsonDecoder<String> decoder = json -> json;

        assertThrows(NullPointerException.class, () -> row.getNullableJson("metadata", null));
        assertThrows(NullPointerException.class, () -> row.getMandatoryJson(null, decoder));
        assertThrows(NullPointerException.class, () -> row.getMandatoryJson("metadata", null, decoder));
        assertThrows(IllegalArgumentException.class, () -> row.getNullableJson(" ", decoder));
        assertThrows(NullPointerException.class, () -> JdbcResultSetBuilder.builder().jsonColumnReader(null));
        verifyNoInteractions(resultSet);
    }
}
