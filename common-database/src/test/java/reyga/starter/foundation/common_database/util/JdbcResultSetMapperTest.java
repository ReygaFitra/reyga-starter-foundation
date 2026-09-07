package reyga.starter.foundation.common_database.util;

import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JdbcResultSetMapperTest {

    @Test
    void should_ReturnNullableValues_When_ResultSetColumnsContainValues() throws Exception {
        // Given
        ResultSet resultSet = mock(ResultSet.class);
        Timestamp timestamp = Timestamp.valueOf("2026-08-28 10:15:30");
        when(resultSet.getString("text_col")).thenReturn("value");
        when(resultSet.getInt("int_col")).thenReturn(10);
        when(resultSet.getLong("long_col")).thenReturn(20L);
        when(resultSet.getBoolean("boolean_col")).thenReturn(true);
        when(resultSet.getTimestamp("timestamp_col")).thenReturn(timestamp);
        when(resultSet.wasNull()).thenReturn(false);

        // When
        String text = JdbcResultSetMapper.Nullable.getString(resultSet, "text_col");
        int integer = JdbcResultSetMapper.Nullable.getInt(resultSet, "int_col");
        long longValue = JdbcResultSetMapper.Nullable.getLong(resultSet, "long_col");
        boolean booleanValue = JdbcResultSetMapper.Nullable.getBoolean(resultSet, "boolean_col");
        Timestamp timestampValue = JdbcResultSetMapper.Nullable.getTimestamp(resultSet, "timestamp_col");
        LocalDateTime dateTime = JdbcResultSetMapper.Nullable.getLocalDateTime(resultSet, "timestamp_col");

        // Then
        assertEquals("value", text);
        assertEquals(10, integer);
        assertEquals(20L, longValue);
        assertTrue(booleanValue);
        assertSame(timestamp, timestampValue);
        assertEquals(timestamp.toLocalDateTime(), dateTime);
        verify(resultSet).getString("text_col");
        verify(resultSet).getInt("int_col");
        verify(resultSet).getLong("long_col");
        verify(resultSet).getBoolean("boolean_col");
        verify(resultSet, times(2)).getTimestamp("timestamp_col");
        verify(resultSet, times(3)).wasNull();
        verifyNoMoreInteractions(resultSet);
    }

    @Test
    void should_ReturnNullableDefaults_When_ResultSetColumnsAreSqlNull() throws Exception {
        // Given
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getInt("int_col")).thenReturn(0);
        when(resultSet.getLong("long_col")).thenReturn(0L);
        when(resultSet.getBoolean("boolean_col")).thenReturn(true);
        when(resultSet.getTimestamp("timestamp_col")).thenReturn(null);
        when(resultSet.getString("text_col")).thenReturn(null);
        when(resultSet.wasNull()).thenReturn(true);

        // When
        String text = JdbcResultSetMapper.Nullable.getString(resultSet, "text_col");
        int integer = JdbcResultSetMapper.Nullable.getInt(resultSet, "int_col");
        long longValue = JdbcResultSetMapper.Nullable.getLong(resultSet, "long_col");
        boolean booleanValue = JdbcResultSetMapper.Nullable.getBoolean(resultSet, "boolean_col");
        Timestamp timestamp = JdbcResultSetMapper.Nullable.getTimestamp(resultSet, "timestamp_col");
        LocalDateTime dateTime = JdbcResultSetMapper.Nullable.getLocalDateTime(resultSet, "timestamp_col");

        // Then
        assertNull(text);
        assertEquals(0, integer);
        assertEquals(0L, longValue);
        assertFalse(booleanValue);
        assertNull(timestamp);
        assertNull(dateTime);
        verify(resultSet).getString("text_col");
        verify(resultSet).getInt("int_col");
        verify(resultSet).getLong("long_col");
        verify(resultSet).getBoolean("boolean_col");
        verify(resultSet, times(2)).getTimestamp("timestamp_col");
        verify(resultSet, times(3)).wasNull();
        verifyNoMoreInteractions(resultSet);
    }

    @Test
    void should_ReturnMandatoryValuesWithoutNullChecks_When_ResultSetColumnsAreRead() throws Exception {
        // Given
        ResultSet resultSet = mock(ResultSet.class);
        Timestamp timestamp = Timestamp.valueOf("2026-08-28 10:15:30");
        when(resultSet.getString("text_col")).thenReturn("value");
        when(resultSet.getInt("int_col")).thenReturn(10);
        when(resultSet.getLong("long_col")).thenReturn(20L);
        when(resultSet.getBoolean("boolean_col")).thenReturn(true);
        when(resultSet.getTimestamp("timestamp_col")).thenReturn(timestamp);

        // When
        String text = JdbcResultSetMapper.Mandatory.getString(resultSet, "text_col");
        int integer = JdbcResultSetMapper.Mandatory.getInt(resultSet, "int_col");
        long longValue = JdbcResultSetMapper.Mandatory.getLong(resultSet, "long_col");
        boolean booleanValue = JdbcResultSetMapper.Mandatory.getBoolean(resultSet, "boolean_col");
        Timestamp timestampValue = JdbcResultSetMapper.Mandatory.getTimestamp(resultSet, "timestamp_col");
        LocalDateTime dateTime = JdbcResultSetMapper.Mandatory.getLocalDateTime(resultSet, "timestamp_col");

        // Then
        assertEquals("value", text);
        assertEquals(10, integer);
        assertEquals(20L, longValue);
        assertTrue(booleanValue);
        assertSame(timestamp, timestampValue);
        assertEquals(timestamp.toLocalDateTime(), dateTime);
        verify(resultSet).getString("text_col");
        verify(resultSet).getInt("int_col");
        verify(resultSet).getLong("long_col");
        verify(resultSet).getBoolean("boolean_col");
        verify(resultSet, times(2)).getTimestamp("timestamp_col");
        verify(resultSet, never()).wasNull();
        verifyNoMoreInteractions(resultSet);
    }

    @Test
    void should_ThrowNullPointerException_When_MandatoryTimestampIsNull() throws Exception {
        // Given
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getTimestamp("timestamp_col")).thenReturn(null);

        // When
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> JdbcResultSetMapper.Mandatory.getLocalDateTime(resultSet, "timestamp_col"));

        // Then
        assertNotNull(exception);
        verify(resultSet).getTimestamp("timestamp_col");
        verifyNoMoreInteractions(resultSet);
    }

    @Test
    void should_PropagateSQLException_When_ResultSetReadFails() throws Exception {
        // Given
        ResultSet resultSet = mock(ResultSet.class);
        SQLException failure = new SQLException("read failed");
        when(resultSet.getString("text_col")).thenThrow(failure);

        // When
        SQLException exception = assertThrows(SQLException.class,
                () -> JdbcResultSetMapper.Nullable.getString(resultSet, "text_col"));

        // Then
        assertSame(failure, exception);
        verify(resultSet).getString("text_col");
        verifyNoMoreInteractions(resultSet);
    }
}
