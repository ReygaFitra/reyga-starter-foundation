package reyga.starter.foundation.common_database.annotation.processor;

import oracle.sql.TIMESTAMP;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.RowMapper;
import reyga.starter.foundation.common.logging.CommonLogger;
import reyga.starter.foundation.common_database.annotation.MapperColumn;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MapperColumnProcessorTest {

    @Test
    void should_MapAnnotatedNumericBooleanAndStringFields_When_ResultSetContainsCompatibleValues() throws Exception {
        // Given
        MapperColumnProcessor<ScalarEntity> processor = new MapperColumnProcessor<>(ScalarEntity.class);
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getObject("primitive_long")).thenReturn(BigDecimal.valueOf(11));
        when(resultSet.getObject("boxed_long")).thenReturn(12);
        when(resultSet.getObject("primitive_int")).thenReturn(13L);
        when(resultSet.getObject("boxed_int")).thenReturn(BigDecimal.valueOf(14));
        when(resultSet.getObject("double_value")).thenReturn(15);
        when(resultSet.getObject("float_value")).thenReturn(16D);
        when(resultSet.getObject("short_value")).thenReturn(17);
        when(resultSet.getObject("primitive_byte")).thenReturn(18);
        when(resultSet.getObject("boxed_byte")).thenReturn(19);
        when(resultSet.getObject("big_integer")).thenReturn(BigDecimal.valueOf(20));
        when(resultSet.getObject("enabled")).thenReturn("1");
        when(resultSet.getObject("disabled")).thenReturn("0");
        when(resultSet.getObject("name")).thenReturn(new StringBuilder("mapped"));

        // When
        ScalarEntity result = processor.generate().mapRow(resultSet, 7);

        // Then
        assertEquals(11L, result.primitiveLong);
        assertEquals(12L, result.boxedLong);
        assertEquals(13, result.primitiveInt);
        assertEquals(14, result.boxedInt);
        assertEquals(15D, result.doubleValue);
        assertEquals(16F, result.floatValue);
        assertEquals((short) 17, result.shortValue);
        assertEquals((byte) 18, result.primitiveByte);
        assertEquals((byte) 19, result.boxedByte);
        assertEquals(BigInteger.valueOf(20), result.bigInteger);
        assertTrue(result.enabled);
        assertFalse(result.disabled);
        assertEquals("mapped", result.name);
        assertEquals("unchanged", result.unannotated);
        verify(resultSet).getObject("primitive_long");
        verify(resultSet).getObject("boxed_long");
        verify(resultSet).getObject("primitive_int");
        verify(resultSet).getObject("boxed_int");
        verify(resultSet).getObject("double_value");
        verify(resultSet).getObject("float_value");
        verify(resultSet).getObject("short_value");
        verify(resultSet).getObject("primitive_byte");
        verify(resultSet).getObject("boxed_byte");
        verify(resultSet).getObject("big_integer");
        verify(resultSet).getObject("enabled");
        verify(resultSet).getObject("disabled");
        verify(resultSet).getObject("name");
        verifyNoMoreInteractions(resultSet);
    }

    @Test
    void should_KeepDefaultsAndForceBooleanFalse_When_ResultSetContainsNullValues() throws Exception {
        // Given
        MapperColumnProcessor<NullEntity> processor = new MapperColumnProcessor<>(NullEntity.class);
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getObject("number")).thenReturn(null);
        when(resultSet.getObject("primitive_number")).thenReturn(null);
        when(resultSet.getObject("active")).thenReturn(null);

        // When
        NullEntity result = processor.generate().mapRow(resultSet, 0);

        // Then
        assertNull(result.number);
        assertEquals(0, result.primitiveNumber);
        assertFalse(result.active);
        verify(resultSet).getObject("number");
        verify(resultSet).getObject("primitive_number");
        verify(resultSet).getObject("active");
        verifyNoMoreInteractions(resultSet);
    }

    @Test
    void should_ConvertOracleTimestamps_When_TargetFieldsUseSupportedTemporalTypes() throws Exception {
        // Given
        MapperColumnProcessor<TemporalEntity> processor = new MapperColumnProcessor<>(TemporalEntity.class);
        ResultSet resultSet = mock(ResultSet.class);
        LocalDateTime dateTime = LocalDateTime.of(2026, 8, 28, 10, 15, 30);
        TIMESTAMP timestamp = new TIMESTAMP(dateTime);
        when(resultSet.getObject("local_date")).thenReturn(timestamp);
        when(resultSet.getObject("local_date_time")).thenReturn(timestamp);
        when(resultSet.getObject("legacy_date")).thenReturn(timestamp);

        // When
        TemporalEntity result = processor.generate().mapRow(resultSet, 0);

        // Then
        assertEquals(dateTime.toLocalDate(), result.localDate);
        assertEquals(dateTime, result.localDateTime);
        assertEquals(Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant()), result.legacyDate);
        verify(resultSet).getObject("local_date");
        verify(resultSet).getObject("local_date_time");
        verify(resultSet).getObject("legacy_date");
        verifyNoMoreInteractions(resultSet);
    }

    @Test
    void should_ThrowRuntimeExceptionAndLogCause_When_EntityHasNoDefaultConstructor() throws Exception {
        // Given
        MapperColumnProcessor<NoDefaultConstructorEntity> processor =
                new MapperColumnProcessor<>(NoDefaultConstructorEntity.class);
        CommonLogger logger = mock(CommonLogger.class);
        ResultSet resultSet = mock(ResultSet.class);
        processor.logger = logger;
        RowMapper<NoDefaultConstructorEntity> mapper = processor.generate();

        // When
        RuntimeException exception = assertThrows(RuntimeException.class, () -> mapper.mapRow(resultSet, 0));

        // Then
        assertInstanceOf(NoSuchMethodException.class, exception.getCause());
        verify(logger).error(exception.getCause().getMessage(), exception.getCause());
        verifyNoMoreInteractions(logger);
        verifyNoInteractions(resultSet);
    }

    @Test
    void should_ThrowRuntimeExceptionWithoutLogging_When_LoggerIsUnavailableAndEntityCannotBeInstantiated() throws Exception {
        // Given
        MapperColumnProcessor<AbstractEntity> processor = new MapperColumnProcessor<>(AbstractEntity.class);
        ResultSet resultSet = mock(ResultSet.class);

        // When
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> processor.generate().mapRow(resultSet, 0));

        // Then
        assertInstanceOf(InstantiationException.class, exception.getCause());
        assertNull(processor.logger);
        verifyNoInteractions(resultSet);
    }

    @Test
    void should_PropagateSQLException_When_ResultSetColumnCannotBeRead() throws Exception {
        // Given
        MapperColumnProcessor<NullEntity> processor = new MapperColumnProcessor<>(NullEntity.class);
        ResultSet resultSet = mock(ResultSet.class);
        SQLException failure = new SQLException("column read failed");
        when(resultSet.getObject("number")).thenThrow(failure);

        // When
        SQLException exception = assertThrows(SQLException.class,
                () -> processor.generate().mapRow(resultSet, 0));

        // Then
        assertSame(failure, exception);
        verify(resultSet).getObject("number");
        verifyNoMoreInteractions(resultSet);
    }

    static final class ScalarEntity {
        @MapperColumn(columnName = "primitive_long") private long primitiveLong;
        @MapperColumn(columnName = "boxed_long") private Long boxedLong;
        @MapperColumn(columnName = "primitive_int") private int primitiveInt;
        @MapperColumn(columnName = "boxed_int") private Integer boxedInt;
        @MapperColumn(columnName = "double_value") private double doubleValue;
        @MapperColumn(columnName = "float_value") private Float floatValue;
        @MapperColumn(columnName = "short_value") private short shortValue;
        @MapperColumn(columnName = "primitive_byte") private byte primitiveByte;
        @MapperColumn(columnName = "boxed_byte") private Byte boxedByte;
        @MapperColumn(columnName = "big_integer") private BigInteger bigInteger;
        @MapperColumn(columnName = "enabled", isBoolean = true) private boolean enabled;
        @MapperColumn(columnName = "disabled", isBoolean = true) private boolean disabled;
        @MapperColumn(columnName = "name") private String name;
        private String unannotated = "unchanged";
    }

    static final class NullEntity {
        @MapperColumn(columnName = "number") private Long number;
        @MapperColumn(columnName = "primitive_number") private int primitiveNumber;
        @MapperColumn(columnName = "active", isBoolean = true) private boolean active = true;
    }

    static final class TemporalEntity {
        @MapperColumn(columnName = "local_date") private LocalDate localDate;
        @MapperColumn(columnName = "local_date_time") private LocalDateTime localDateTime;
        @MapperColumn(columnName = "legacy_date") private Date legacyDate;
    }

    static final class NoDefaultConstructorEntity {
        private NoDefaultConstructorEntity(String value) {
        }
    }

    abstract static class AbstractEntity {
    }
}
