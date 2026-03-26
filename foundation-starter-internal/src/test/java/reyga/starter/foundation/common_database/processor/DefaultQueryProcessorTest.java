package reyga.starter.foundation.common_database.processor;

import org.junit.jupiter.api.Test;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import reyga.starter.foundation.common_database.util.QueryBuilder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DefaultQueryProcessorTest {

    @Test
    void fetch_executesQuery() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        DefaultQueryProcessor processor = new DefaultQueryProcessor(jdbcTemplate);
        QueryBuilder builder = new QueryBuilder().select("*").from("table");
        RowMapper<String> rowMapper = mock(RowMapper.class);

        when(jdbcTemplate.query(anyString(), eq(rowMapper), any())).thenReturn(List.of("a"));

        List<String> result = processor.fetch(builder, rowMapper);

        assertEquals(List.of("a"), result);
    }

    @Test
    void optionalFetch_returnsEmptyOnEmptyResult() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        DefaultQueryProcessor processor = new DefaultQueryProcessor(jdbcTemplate);
        QueryBuilder builder = new QueryBuilder().select("*").from("table");
        RowMapper<String> rowMapper = mock(RowMapper.class);

        when(jdbcTemplate.query(anyString(), eq(rowMapper), any())).thenThrow(new EmptyResultDataAccessException(1));

        Optional<List<String>> result = processor.optionalFetch(builder, rowMapper);

        assertTrue(result.isEmpty());
    }

    @Test
    void fetchOne_returnsFirstResult() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        DefaultQueryProcessor processor = new DefaultQueryProcessor(jdbcTemplate);
        QueryBuilder builder = new QueryBuilder().select("*").from("table");
        RowMapper<String> rowMapper = mock(RowMapper.class);

        when(jdbcTemplate.query(anyString(), eq(rowMapper), any())).thenReturn(List.of("a", "b"));

        assertEquals("a", processor.fetchOne(builder, rowMapper));
    }

    @Test
    void optionalFetchOne_returnsEmptyWhenNoResult() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        DefaultQueryProcessor processor = new DefaultQueryProcessor(jdbcTemplate);
        QueryBuilder builder = new QueryBuilder().select("*").from("table");
        RowMapper<String> rowMapper = mock(RowMapper.class);

        when(jdbcTemplate.query(anyString(), eq(rowMapper), any())).thenReturn(List.of());

        assertTrue(processor.optionalFetchOne(builder, rowMapper).isEmpty());
    }

    @Test
    void execute_updatesDatabase() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        DefaultQueryProcessor processor = new DefaultQueryProcessor(jdbcTemplate);
        QueryBuilder builder = new QueryBuilder().update("table").set("name", "x");

        when(jdbcTemplate.update(anyString(), Optional.ofNullable(any()))).thenReturn(1);

        assertEquals(1, processor.execute(builder));
    }
}
