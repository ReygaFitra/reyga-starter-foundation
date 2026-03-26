package reyga.starter.foundation.common_database.util;

import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JdbcResultSetMapperTest {

    @Test
    void nullableGetInt_returnsZeroWhenNull() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getInt("col")).thenReturn(0);
        when(rs.wasNull()).thenReturn(true);

        assertEquals(0, JdbcResultSetMapper.Nullable.getInt(rs, "col"));
    }

    @Test
    void nullableGetLocalDateTime_returnsNullWhenTimestampNull() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getTimestamp("col")).thenReturn(null);

        assertNull(JdbcResultSetMapper.Nullable.getLocalDateTime(rs, "col"));
    }

    @Test
    void mandatoryGetLocalDateTime_returnsValue() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        Timestamp ts = Timestamp.valueOf(LocalDateTime.of(2024, 1, 1, 0, 0));
        when(rs.getTimestamp("col")).thenReturn(ts);

        assertEquals(ts.toLocalDateTime(), JdbcResultSetMapper.Mandatory.getLocalDateTime(rs, "col"));
    }
}
