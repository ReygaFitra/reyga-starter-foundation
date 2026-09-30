package reyga.starter.foundation.common_database.util;

import java.sql.ResultSet;
import java.sql.SQLDataException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.RowMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class JdbcResultSetBuilderTest {

    @Test
    void should_MapRecordWithNullableValues_When_RowContainsSqlNull() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        Timestamp timestamp = Timestamp.valueOf("2026-09-30 10:15:30");
        when(resultSet.getObject("user_id", Long.class)).thenReturn(5L);
        when(resultSet.getTimestamp("created_at")).thenReturn(timestamp);
        RowMapper<UserRow> mapper = JdbcResultSetBuilder.<UserRow>builder()
                .map((row, rowNumber) -> new UserRow(
                        row.getMandatory("user_id", Long.class),
                        row.getNullable("score", Integer.class),
                        row.getNullable("enabled", Boolean.class),
                        row.getMandatory("created_at", LocalDateTime.class),
                        rowNumber
                ))
                .build();

        UserRow result = mapper.mapRow(resultSet, 3);

        assertEquals(new UserRow(5L, null, null, timestamp.toLocalDateTime(), 3), result);
        verify(resultSet, never()).next();
        verify(resultSet, never()).close();
    }

    @Test
    void should_RejectMandatorySqlNull_When_RowIsMapped() {
        ResultSet resultSet = mock(ResultSet.class);
        RowMapper<Long> mapper = JdbcResultSetBuilder.<Long>builder()
                .map((row, rowNumber) -> row.getMandatory("user_id", Long.class))
                .build();

        SQLDataException exception = assertThrows(SQLDataException.class, () -> mapper.mapRow(resultSet, 0));

        assertEquals("22004", exception.getSQLState());
        assertTrue(exception.getMessage().contains("user_id"));
        assertTrue(exception.getMessage().contains("SQL NULL"));
    }

    @Test
    void should_PreserveBuiltMapping_When_BuilderCallbackChanges() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getObject("id", Long.class)).thenReturn(8L);
        JdbcResultSetBuilder<Long> builder = JdbcResultSetBuilder.<Long>builder()
                .map((row, rowNumber) -> row.getMandatory("id", Long.class));
        RowMapper<Long> originalMapper = builder.build();

        builder.map((row, rowNumber) -> row.getMandatory("id", Long.class) + 1);
        RowMapper<Long> updatedMapper = builder.build();

        assertEquals(8L, originalMapper.mapRow(resultSet, 0));
        assertEquals(9L, updatedMapper.mapRow(resultSet, 0));
    }

    @Test
    void should_UseIndependentReaders_When_MapperIsReusedAcrossResultSets() throws SQLException {
        ResultSet firstResultSet = mock(ResultSet.class);
        ResultSet secondResultSet = mock(ResultSet.class);
        when(firstResultSet.getObject("id", Long.class)).thenReturn(1L);
        when(secondResultSet.getObject("id", Long.class)).thenReturn(2L);
        JdbcResultSetMapper[] readers = new JdbcResultSetMapper[2];
        RowMapper<Long> mapper = JdbcResultSetBuilder.<Long>builder()
                .map((row, rowNumber) -> {
                    readers[rowNumber] = row;
                    return row.getMandatory("id", Long.class);
                })
                .build();

        Long firstResult = mapper.mapRow(firstResultSet, 0);
        Long secondResult = mapper.mapRow(secondResultSet, 1);

        assertEquals(1L, firstResult);
        assertEquals(2L, secondResult);
        assertNotSame(readers[0], readers[1]);
    }

    @Test
    void should_RejectBuild_When_MappingIsMissing() {
        JdbcResultSetBuilder<Long> builder = JdbcResultSetBuilder.builder();

        IllegalStateException exception = assertThrows(IllegalStateException.class, builder::build);

        assertTrue(exception.getMessage().contains("map(mapping)"));
    }

    @Test
    void should_RejectCallback_When_MappingIsNull() {
        JdbcResultSetBuilder<Long> builder = JdbcResultSetBuilder.builder();

        NullPointerException exception = assertThrows(NullPointerException.class, () -> builder.map(null));

        assertEquals("mapping must not be null", exception.getMessage());
    }

    @Test
    void should_RejectMissingResultSet_When_MapperIsInvoked() {
        RowMapper<Long> mapper = JdbcResultSetBuilder.<Long>builder()
                .map((row, rowNumber) -> row.getMandatory("id", Long.class))
                .build();

        NullPointerException exception = assertThrows(NullPointerException.class, () -> mapper.mapRow(null, 0));

        assertEquals("resultSet must not be null", exception.getMessage());
    }

    @Test
    void should_PropagateOriginalSQLException_When_ColumnReadFails() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        SQLException failure = new SQLException("Column cannot be converted", "22005");
        when(resultSet.getObject("id", Long.class)).thenThrow(failure);
        RowMapper<Long> mapper = JdbcResultSetBuilder.<Long>builder()
                .map((row, rowNumber) -> row.getMandatory("id", Long.class))
                .build();

        SQLException exception = assertThrows(SQLException.class, () -> mapper.mapRow(resultSet, 0));

        assertSame(failure, exception);
        assertEquals("22005", exception.getSQLState());
    }

    @Test
    void should_PropagateOriginalRuntimeException_When_ObjectConstructionFails() {
        ResultSet resultSet = mock(ResultSet.class);
        IllegalStateException failure = new IllegalStateException("Domain construction failed");
        RowMapper<Long> mapper = JdbcResultSetBuilder.<Long>builder()
                .map((row, rowNumber) -> {
                    throw failure;
                })
                .build();

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> mapper.mapRow(resultSet, 0));

        assertSame(failure, exception);
        verifyNoInteractions(resultSet);
    }

    @Test
    void should_AllowNullMappedObject_When_CallbackUsesRowMapperNullContract() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        RowMapper<Long> mapper = JdbcResultSetBuilder.<Long>builder()
                .map((row, rowNumber) -> null)
                .build();

        Long result = mapper.mapRow(resultSet, 0);

        assertNull(result);
        verifyNoInteractions(resultSet);
    }

    private record UserRow(Long id, Integer score, Boolean enabled, LocalDateTime createdAt, int rowNumber) {
    }
}
