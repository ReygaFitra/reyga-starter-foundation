package reyga.starter.foundation.common_database.annotation.processor;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.RowMapper;
import reyga.starter.foundation.common_database.annotation.MapperColumn;

import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MapperColumnProcessorTest {

    @Test
    void generate_mapsAnnotatedFields() throws Exception {
        MapperColumnProcessor<Sample> processor = new MapperColumnProcessor<>(Sample.class);
        RowMapper<Sample> mapper = processor.generate();

        ResultSet rs = mock(ResultSet.class);
        when(rs.getObject("num")).thenReturn(1);
        when(rs.getObject("flag")).thenReturn("1");
        when(rs.getObject("name")).thenReturn("abc");

        Sample sample = mapper.mapRow(rs, 0);

        assertEquals(1L, sample.num);
        assertTrue(sample.flag);
        assertEquals("abc", sample.name);
    }

    @Test
    void generate_setsFalseForNullBoolean() throws Exception {
        MapperColumnProcessor<Sample> processor = new MapperColumnProcessor<>(Sample.class);
        RowMapper<Sample> mapper = processor.generate();

        ResultSet rs = mock(ResultSet.class);
        when(rs.getObject("num")).thenReturn(1);
        when(rs.getObject("flag")).thenReturn(null);
        when(rs.getObject("name")).thenReturn("abc");

        Sample sample = mapper.mapRow(rs, 0);
        assertFalse(sample.flag);
    }

    static class Sample {
        @MapperColumn(columnName = "num")
        private Long num;
        @MapperColumn(columnName = "flag", isBoolean = true)
        private boolean flag;
        @MapperColumn(columnName = "name")
        private String name;
    }
}
