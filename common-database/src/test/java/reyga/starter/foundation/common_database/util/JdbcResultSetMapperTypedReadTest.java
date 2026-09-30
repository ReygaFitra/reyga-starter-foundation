package reyga.starter.foundation.common_database.util;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLDataException;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class JdbcResultSetMapperTypedReadTest {

    @ParameterizedTest
    @MethodSource("nullableValueTypes")
    void should_PreserveSqlNull_When_NullableTypedColumnIsRead(Class<?> type) throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);

        Object result = JdbcResultSetMapper.Nullable.getObject(resultSet, "optional_column", type);

        assertNull(result);
        verify(resultSet).getObject("optional_column", type);
        verifyNoMoreInteractions(resultSet);
    }

    @ParameterizedTest
    @MethodSource("nullableValueTypes")
    void should_RejectSqlNull_When_MandatoryTypedColumnIsRead(Class<?> type) {
        ResultSet resultSet = mock(ResultSet.class);

        SQLDataException exception = assertThrows(SQLDataException.class,
                () -> JdbcResultSetMapper.Mandatory.getObject(resultSet, "required_column", type));

        assertEquals("22004", exception.getSQLState());
        assertTrue(exception.getMessage().contains("required_column"));
        assertTrue(exception.getMessage().contains("SQL NULL"));
    }

    @Test
    void should_AcceptZeroFalseAndEmptyString_When_MandatoryValuesAreNotSqlNull() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getObject("quantity", Integer.class)).thenReturn(0);
        when(resultSet.getObject("balance", Long.class)).thenReturn(0L);
        when(resultSet.getObject("active", Boolean.class)).thenReturn(false);
        when(resultSet.getObject("label", String.class)).thenReturn("");

        Integer quantity = JdbcResultSetMapper.Mandatory.getObject(resultSet, "quantity", Integer.class);
        Long balance = JdbcResultSetMapper.Mandatory.getObject(resultSet, "balance", Long.class);
        Boolean active = JdbcResultSetMapper.Mandatory.getObject(resultSet, "active", Boolean.class);
        String label = JdbcResultSetMapper.Mandatory.getObject(resultSet, "label", String.class);

        assertEquals(0, quantity);
        assertEquals(0L, balance);
        assertEquals(false, active);
        assertEquals("", label);
    }

    @Test
    void should_KeepNullableZeroAndFalse_When_ValuesAreNotSqlNull() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getObject("count", Integer.class)).thenReturn(0);
        when(resultSet.getObject("enabled", Boolean.class)).thenReturn(false);

        Integer count = JdbcResultSetMapper.Nullable.getObject(resultSet, "count", Integer.class);
        Boolean enabled = JdbcResultSetMapper.Nullable.getObject(resultSet, "enabled", Boolean.class);

        assertEquals(0, count);
        assertEquals(false, enabled);
    }

    @Test
    void should_DelegateDriverSpecificConversion_When_CustomReferenceTypeIsRequested() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        UUID identifier = UUID.fromString("ea9b23bc-645f-401b-8c01-f0073d37bf9b");
        when(resultSet.getObject("identifier", UUID.class)).thenReturn(identifier);

        UUID result = JdbcResultSetMapper.Mandatory.getObject(resultSet, "identifier", UUID.class);

        assertSame(identifier, result);
        verify(resultSet).getObject("identifier", UUID.class);
    }

    @Test
    void should_ConvertTemporalValues_When_LocalDateAndTimeAreRequested() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        Timestamp timestamp = Timestamp.valueOf("2026-09-30 10:15:30.123456789");
        Date date = Date.valueOf("2026-09-30");
        LocalTime time = LocalTime.of(10, 15, 30, 123456789);
        when(resultSet.getTimestamp("created_at")).thenReturn(timestamp);
        when(resultSet.getDate("birth_date")).thenReturn(date);
        when(resultSet.getObject("opening_time", LocalTime.class)).thenReturn(time);

        LocalDateTime createdAt = JdbcResultSetMapper.Mandatory.getObject(resultSet, "created_at", LocalDateTime.class);
        LocalDate birthDate = JdbcResultSetMapper.Nullable.getObject(resultSet, "birth_date", LocalDate.class);
        LocalTime openingTime = JdbcResultSetMapper.Nullable.getObject(resultSet, "opening_time", LocalTime.class);

        assertEquals(timestamp.toLocalDateTime(), createdAt);
        assertEquals(123456789, createdAt.getNano());
        assertEquals(date.toLocalDate(), birthDate);
        assertEquals(time, openingTime);
    }

    @ParameterizedTest
    @MethodSource("localTemporalTypes")
    void should_PreserveSqlNull_When_NullableLocalTemporalColumnIsRead(Class<?> type) throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);

        Object result = JdbcResultSetMapper.Nullable.getObject(resultSet, "optional_date", type);

        assertNull(result);
    }

    @ParameterizedTest
    @MethodSource("localTemporalTypes")
    void should_RejectSqlNull_When_MandatoryLocalTemporalColumnIsRead(Class<?> type) {
        ResultSet resultSet = mock(ResultSet.class);

        SQLDataException exception = assertThrows(SQLDataException.class,
                () -> JdbcResultSetMapper.Mandatory.getObject(resultSet, "required_date", type));

        assertEquals("22004", exception.getSQLState());
        assertTrue(exception.getMessage().contains("required_date"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\t", "\r\n"})
    void should_RejectBlankColumn_When_TypedReadIsRequested(String column) {
        ResultSet resultSet = mock(ResultSet.class);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> JdbcResultSetMapper.Nullable.getObject(resultSet, column, String.class));

        assertEquals("column must not be blank", exception.getMessage());
        verifyNoInteractions(resultSet);
    }

    @ParameterizedTest
    @MethodSource("invalidTypes")
    void should_RejectNonReferenceValueType_When_TypedReadIsRequested(Class<?> type) {
        ResultSet resultSet = mock(ResultSet.class);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> JdbcResultSetMapper.Mandatory.getObject(resultSet, "value", type));

        assertTrue(exception.getMessage().contains("reference value type"));
        verifyNoInteractions(resultSet);
    }

    @Test
    void should_RejectNullArguments_When_TypedReadIsRequested() {
        ResultSet resultSet = mock(ResultSet.class);

        assertEquals("resultSet must not be null", assertThrows(NullPointerException.class,
                () -> JdbcResultSetMapper.Nullable.getObject(null, "id", Long.class)).getMessage());
        assertEquals("column must not be null", assertThrows(NullPointerException.class,
                () -> JdbcResultSetMapper.Mandatory.getObject(resultSet, null, Long.class)).getMessage());
        assertEquals("type must not be null", assertThrows(NullPointerException.class,
                () -> JdbcResultSetMapper.Nullable.getObject(resultSet, "id", null)).getMessage());
        verifyNoInteractions(resultSet);
    }

    @Test
    void should_PropagateSQLExceptionWithoutWrapping_When_DriverConversionFails() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        SQLException failure = new SQLException("Unsupported conversion", "22005");
        when(resultSet.getObject("amount", BigDecimal.class)).thenThrow(failure);

        SQLException exception = assertThrows(SQLException.class,
                () -> JdbcResultSetMapper.Mandatory.getObject(resultSet, "amount", BigDecimal.class));

        assertSame(failure, exception);
        assertEquals("22005", exception.getSQLState());
    }

    private static Stream<Arguments> nullableValueTypes() {
        return Stream.of(String.class, Integer.class, Long.class, Boolean.class,
                        Double.class, Float.class, Short.class, Byte.class, BigDecimal.class, byte[].class,
                        Timestamp.class, Date.class, Time.class)
                .map(Arguments::of);
    }

    private static Stream<Arguments> localTemporalTypes() {
        return Stream.of(LocalDate.class, LocalDateTime.class, LocalTime.class).map(Arguments::of);
    }

    private static Stream<Arguments> invalidTypes() {
        return Stream.of(int.class, long.class, boolean.class, double.class, float.class,
                        short.class, byte.class, char.class, void.class, Void.class)
                .map(Arguments::of);
    }
}
