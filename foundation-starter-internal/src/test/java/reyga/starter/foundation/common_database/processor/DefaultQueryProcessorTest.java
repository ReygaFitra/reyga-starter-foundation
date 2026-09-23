package reyga.starter.foundation.common_database.processor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import reyga.starter.foundation.common.logging.CommonLogger;
import reyga.starter.foundation.common_database.util.QueryBuilder;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class DefaultQueryProcessorTest {

    private static final String SELECT_SQL = "SELECT id FROM users WHERE active = ?";
    private static final String UPDATE_SQL = "UPDATE users SET name = ? WHERE id = ?";

    private JdbcTemplate jdbcTemplate;
    private CommonLogger logger;
    private RowMapper<String> rowMapper;
    private DefaultQueryProcessor processor;

    @BeforeEach
    void setUp() {
        jdbcTemplate = mock(JdbcTemplate.class);
        logger = mock(CommonLogger.class);
        rowMapper = mock(RowMapper.class);
        processor = new DefaultQueryProcessor(jdbcTemplate);
        processor.logger = logger;
    }

    @Test
    void should_ReturnRowsAndLogQuery_When_FetchSucceeds() {
        // given
        QueryBuilder builder = selectBuilder();
        List<String> expected = List.of("one", "two");
        when(jdbcTemplate.query(eq(SELECT_SQL), eq(rowMapper), any(Object[].class)))
                .thenReturn(expected);

        // when
        List<String> result = processor.fetch(builder, rowMapper);

        // then
        assertSame(expected, result);
        verify(logger).info("Executing query: " + SELECT_SQL);
        verify(jdbcTemplate).query(eq(SELECT_SQL), eq(rowMapper), aryEq(new Object[]{true}));
        verifyNoMoreInteractions(jdbcTemplate, logger, rowMapper);
    }

    @Test
    void should_PropagateDataAccessExceptionAfterLogging_When_FetchFails() {
        // given
        QueryBuilder builder = selectBuilder();
        DataAccessResourceFailureException expected =
                new DataAccessResourceFailureException("database unavailable");
        when(jdbcTemplate.query(eq(SELECT_SQL), eq(rowMapper), any(Object[].class)))
                .thenThrow(expected);

        // when
        DataAccessResourceFailureException result = assertThrows(
                DataAccessResourceFailureException.class,
                () -> processor.fetch(builder, rowMapper)
        );

        // then
        assertSame(expected, result);
        verify(logger).info("Executing query: " + SELECT_SQL);
        verify(jdbcTemplate).query(eq(SELECT_SQL), eq(rowMapper), aryEq(new Object[]{true}));
        verifyNoMoreInteractions(jdbcTemplate, logger, rowMapper);
    }

    @Test
    void should_ReturnRowsInsideOptional_When_OptionalFetchSucceeds() {
        // given
        QueryBuilder builder = selectBuilder();
        List<String> expected = List.of("one");
        when(jdbcTemplate.query(eq(SELECT_SQL), eq(rowMapper), any(Object[].class)))
                .thenReturn(expected);

        // when
        Optional<List<String>> result = processor.optionalFetch(builder, rowMapper);

        // then
        assertTrue(result.isPresent());
        assertSame(expected, result.orElseThrow());
        verify(logger).info("Executing query: " + SELECT_SQL);
        verify(jdbcTemplate).query(eq(SELECT_SQL), eq(rowMapper), aryEq(new Object[]{true}));
        verifyNoMoreInteractions(jdbcTemplate, logger, rowMapper);
    }

    @Test
    void should_ReturnEmptyOptional_When_OptionalFetchHasNoResult() {
        // given
        QueryBuilder builder = selectBuilder();
        when(jdbcTemplate.query(eq(SELECT_SQL), eq(rowMapper), any(Object[].class)))
                .thenThrow(new EmptyResultDataAccessException(1));

        // when
        Optional<List<String>> result = processor.optionalFetch(builder, rowMapper);

        // then
        assertTrue(result.isEmpty());
        verify(logger).info("Executing query: " + SELECT_SQL);
        verify(jdbcTemplate).query(eq(SELECT_SQL), eq(rowMapper), aryEq(new Object[]{true}));
        verifyNoMoreInteractions(jdbcTemplate, logger, rowMapper);
    }

    @Test
    void should_ReturnFirstRow_When_FetchOneReturnsMultipleRows() {
        // given
        QueryBuilder builder = selectBuilder();
        when(jdbcTemplate.query(eq(SELECT_SQL), eq(rowMapper), any(Object[].class)))
                .thenReturn(List.of("first", "second"));

        // when
        String result = processor.fetchOne(builder, rowMapper);

        // then
        assertEquals("first", result);
        verify(logger).info("Executing query: " + SELECT_SQL);
        verify(jdbcTemplate).query(eq(SELECT_SQL), eq(rowMapper), aryEq(new Object[]{true}));
        verifyNoMoreInteractions(jdbcTemplate, logger, rowMapper);
    }

    @Test
    void should_ThrowNoSuchElementException_When_FetchOneReturnsNoRows() {
        // given
        QueryBuilder builder = selectBuilder();
        when(jdbcTemplate.query(eq(SELECT_SQL), eq(rowMapper), any(Object[].class)))
                .thenReturn(List.of());

        // when
        NoSuchElementException result = assertThrows(
                NoSuchElementException.class,
                () -> processor.fetchOne(builder, rowMapper)
        );

        // then
        assertEquals(NoSuchElementException.class, result.getClass());
        verify(logger).info("Executing query: " + SELECT_SQL);
        verify(jdbcTemplate).query(eq(SELECT_SQL), eq(rowMapper), aryEq(new Object[]{true}));
        verifyNoMoreInteractions(jdbcTemplate, logger, rowMapper);
    }

    @Test
    void should_ReturnFirstRowInsideOptional_When_OptionalFetchOneReturnsRows() {
        // given
        QueryBuilder builder = selectBuilder();
        when(jdbcTemplate.query(eq(SELECT_SQL), eq(rowMapper), any(Object[].class)))
                .thenReturn(List.of("first", "second"));

        // when
        Optional<String> result = processor.optionalFetchOne(builder, rowMapper);

        // then
        assertEquals(Optional.of("first"), result);
        verify(logger).info("Executing query: " + SELECT_SQL);
        verify(jdbcTemplate).query(eq(SELECT_SQL), eq(rowMapper), aryEq(new Object[]{true}));
        verifyNoMoreInteractions(jdbcTemplate, logger, rowMapper);
    }

    @Test
    void should_ReturnEmptyOptional_When_OptionalFetchOneReturnsNoRows() {
        // given
        QueryBuilder builder = selectBuilder();
        when(jdbcTemplate.query(eq(SELECT_SQL), eq(rowMapper), any(Object[].class)))
                .thenReturn(List.of());

        // when
        Optional<String> result = processor.optionalFetchOne(builder, rowMapper);

        // then
        assertTrue(result.isEmpty());
        verify(logger).info("Executing query: " + SELECT_SQL);
        verify(jdbcTemplate).query(eq(SELECT_SQL), eq(rowMapper), aryEq(new Object[]{true}));
        verifyNoMoreInteractions(jdbcTemplate, logger, rowMapper);
    }

    @Test
    void should_ReturnMappedRowAndLogQuery_When_FetchOneBySucceeds() {
        // given
        QueryBuilder builder = selectBuilder();
        when(jdbcTemplate.queryForObject(eq(SELECT_SQL), eq(rowMapper), any(Object[].class)))
                .thenReturn("one");

        // when
        String result = processor.fetchOneBy(builder, rowMapper);

        // then
        assertEquals("one", result);
        verify(logger).info("Executing query: " + SELECT_SQL);
        verify(jdbcTemplate).queryForObject(eq(SELECT_SQL), eq(rowMapper), aryEq(new Object[]{true}));
        verifyNoMoreInteractions(jdbcTemplate, logger, rowMapper);
    }

    @Test
    void should_ReturnMappedRowInsideOptional_When_OptionalFetchOneBySucceeds() {
        // given
        QueryBuilder builder = selectBuilder();
        when(jdbcTemplate.queryForObject(eq(SELECT_SQL), eq(rowMapper), any(Object[].class)))
                .thenReturn("one");

        // when
        Optional<String> result = processor.optionalFetchOneBy(builder, rowMapper);

        // then
        assertEquals(Optional.of("one"), result);
        verify(logger).info("Executing query: " + SELECT_SQL);
        verify(jdbcTemplate).queryForObject(eq(SELECT_SQL), eq(rowMapper), aryEq(new Object[]{true}));
        verifyNoMoreInteractions(jdbcTemplate, logger, rowMapper);
    }

    @Test
    void should_ReturnEmptyOptional_When_OptionalFetchOneByReturnsNull() {
        // given
        QueryBuilder builder = selectBuilder();
        when(jdbcTemplate.queryForObject(eq(SELECT_SQL), eq(rowMapper), any(Object[].class)))
                .thenReturn(null);

        // when
        Optional<String> result = processor.optionalFetchOneBy(builder, rowMapper);

        // then
        assertTrue(result.isEmpty());
        verify(logger).info("Executing query: " + SELECT_SQL);
        verify(jdbcTemplate).queryForObject(eq(SELECT_SQL), eq(rowMapper), aryEq(new Object[]{true}));
        verifyNoMoreInteractions(jdbcTemplate, logger, rowMapper);
    }

    @Test
    void should_ReturnEmptyOptional_When_OptionalFetchOneByHasNoResult() {
        // given
        QueryBuilder builder = selectBuilder();
        when(jdbcTemplate.queryForObject(eq(SELECT_SQL), eq(rowMapper), any(Object[].class)))
                .thenThrow(new EmptyResultDataAccessException(1));

        // when
        Optional<String> result = processor.optionalFetchOneBy(builder, rowMapper);

        // then
        assertTrue(result.isEmpty());
        verify(logger).info("Executing query: " + SELECT_SQL);
        verify(jdbcTemplate).queryForObject(eq(SELECT_SQL), eq(rowMapper), aryEq(new Object[]{true}));
        verifyNoMoreInteractions(jdbcTemplate, logger, rowMapper);
    }

    @Test
    void should_ReturnPageWithContentAndTotal_When_PagedQueryIsRequested() {
        // given
        QueryBuilder builder = QueryBuilder.builder().select("id", "name").from("users")
                .where().equals("active", true).done();
        Pageable pageable = PageRequest.of(1, 2, Sort.by(Sort.Order.desc("name")));
        String contentSql = "SELECT id, name FROM users WHERE active = ? ORDER BY name DESC LIMIT ? OFFSET ?";
        String countSql = "SELECT COUNT(*) FROM (SELECT id, name FROM users WHERE active = ?) query_count";
        when(jdbcTemplate.query(eq(contentSql), eq(rowMapper), any(Object[].class)))
                .thenReturn(List.of("third", "fourth"));
        when(jdbcTemplate.queryForObject(eq(countSql), eq(Long.class), any(Object[].class)))
                .thenReturn(5L);

        // when
        Page<String> result = processor.fetchPage(builder, rowMapper, pageable);

        // then
        assertEquals(List.of("third", "fourth"), result.getContent());
        assertEquals(1, result.getNumber());
        assertEquals(2, result.getSize());
        assertEquals(5L, result.getTotalElements());
        assertEquals(3, result.getTotalPages());
        assertEquals(Sort.by(Sort.Order.desc("name")), result.getSort());
        verify(logger).info("Executing query: " + contentSql);
        verify(logger).info("Executing query: " + countSql);
        verify(jdbcTemplate).query(eq(contentSql), eq(rowMapper), aryEq(new Object[]{true, 2, 2L}));
        verify(jdbcTemplate).queryForObject(eq(countSql), eq(Long.class), aryEq(new Object[]{true}));
        verifyNoMoreInteractions(jdbcTemplate, logger, rowMapper);
    }

    @Test
    void should_ReturnUnpagedContentWithoutCountQuery_When_UnpagedQueryIsRequested() {
        // given
        QueryBuilder builder = QueryBuilder.builder().select("id").from("users");
        Pageable pageable = Pageable.unpaged(Sort.by("id"));
        String contentSql = "SELECT id FROM users ORDER BY id ASC";
        when(jdbcTemplate.query(eq(contentSql), eq(rowMapper), any(Object[].class)))
                .thenReturn(List.of("one", "two"));

        // when
        Page<String> result = processor.fetchPage(builder, rowMapper, pageable);

        // then
        assertEquals(List.of("one", "two"), result.getContent());
        assertEquals(2L, result.getTotalElements());
        assertFalse(result.getPageable().isPaged());
        assertEquals(Sort.unsorted(), result.getSort());
        verify(logger).info("Executing query: " + contentSql);
        verify(jdbcTemplate).query(eq(contentSql), eq(rowMapper), aryEq(new Object[]{}));
        verify(jdbcTemplate, never()).queryForObject(anyString(), eq(Long.class), any(Object[].class));
        verifyNoMoreInteractions(jdbcTemplate, logger, rowMapper);
    }

    @Test
    void should_UseZeroTotal_When_CountQueryReturnsNull() {
        // given
        QueryBuilder builder = QueryBuilder.builder().select("id").from("users");
        Pageable pageable = PageRequest.of(0, 10);
        String contentSql = "SELECT id FROM users LIMIT ? OFFSET ?";
        String countSql = "SELECT COUNT(*) FROM (SELECT id FROM users) query_count";
        when(jdbcTemplate.query(eq(contentSql), eq(rowMapper), any(Object[].class)))
                .thenReturn(List.of());
        when(jdbcTemplate.queryForObject(eq(countSql), eq(Long.class), any(Object[].class)))
                .thenReturn(null);

        // when
        Page<String> result = processor.fetchPage(builder, rowMapper, pageable);

        // then
        assertTrue(result.isEmpty());
        assertEquals(0L, result.getTotalElements());
        assertEquals(0, result.getTotalPages());
        verify(logger).info("Executing query: " + contentSql);
        verify(logger).info("Executing query: " + countSql);
        verify(jdbcTemplate).query(eq(contentSql), eq(rowMapper), aryEq(new Object[]{10, 0L}));
        verify(jdbcTemplate).queryForObject(eq(countSql), eq(Long.class), aryEq(new Object[]{}));
        verifyNoMoreInteractions(jdbcTemplate, logger, rowMapper);
    }

    @Test
    void should_ThrowNullPointerExceptionWithoutDependencyCall_When_PageBuilderIsNull() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        NullPointerException result = assertThrows(
                NullPointerException.class,
                () -> processor.fetchPage(null, rowMapper, pageable)
        );

        // then
        assertEquals("builder must not be null", result.getMessage());
        verifyNoInteractions(jdbcTemplate, logger, rowMapper);
    }

    @Test
    void should_ThrowNullPointerExceptionWithoutDependencyCall_When_PageRowMapperIsNull() {
        // given
        QueryBuilder builder = QueryBuilder.builder().select("id").from("users");
        Pageable pageable = PageRequest.of(0, 10);

        // when
        NullPointerException result = assertThrows(
                NullPointerException.class,
                () -> processor.fetchPage(builder, null, pageable)
        );

        // then
        assertEquals("rowMapper must not be null", result.getMessage());
        verifyNoInteractions(jdbcTemplate, logger, rowMapper);
    }

    @Test
    void should_ThrowNullPointerExceptionWithoutDependencyCall_When_PageableIsNull() {
        // given
        QueryBuilder builder = QueryBuilder.builder().select("id").from("users");

        // when
        NullPointerException result = assertThrows(
                NullPointerException.class,
                () -> processor.fetchPage(builder, rowMapper, null)
        );

        // then
        assertEquals("pageable must not be null", result.getMessage());
        verifyNoInteractions(jdbcTemplate, logger, rowMapper);
    }

    @Test
    void should_ReturnAffectedRowCountAndLogQuery_When_ExecuteSucceeds() {
        // given
        QueryBuilder builder = updateBuilder();
        when(jdbcTemplate.update(eq(UPDATE_SQL), any(Object[].class))).thenReturn(1);

        // when
        int result = processor.execute(builder);

        // then
        assertEquals(1, result);
        verify(logger).info("Executing query: " + UPDATE_SQL);
        verify(jdbcTemplate).update(eq(UPDATE_SQL), aryEq(new Object[]{"Reyga", "CUS-01"}));
        verifyNoMoreInteractions(jdbcTemplate, logger);
        verifyNoInteractions(rowMapper);
    }

    @Test
    void should_ReturnBatchCountsAndLogQuery_When_BuilderBatchExecuteSucceeds() {
        // given
        QueryBuilder builder = QueryBuilder.builder().insertInto("users", "id", "name");
        List<Object[]> batchParameters = List.of(
                new Object[]{"CUS-01", "Reyga"},
                new Object[]{"CUS-02", "Ayu"}
        );
        String sql = "INSERT INTO users (id, name) VALUES ()";
        int[] expected = {1, 1};
        when(jdbcTemplate.batchUpdate(sql, batchParameters)).thenReturn(expected);

        // when
        int[] result = processor.batchExecute(builder, batchParameters);

        // then
        assertSame(expected, result);
        assertArrayEquals(new int[]{1, 1}, result);
        verify(logger).info("Executing query: " + sql);
        verify(jdbcTemplate).batchUpdate(sql, batchParameters);
        verifyNoMoreInteractions(jdbcTemplate, logger);
        verifyNoInteractions(rowMapper);
    }

    @Test
    void should_ReturnIndividualCountsAndLogEveryQuery_When_BuilderListBatchExecuteSucceeds() {
        // given
        QueryBuilder firstBuilder = updateBuilder();
        QueryBuilder secondBuilder = QueryBuilder.builder().deleteFrom("users")
                .where().equals("id", "CUS-02").done();
        String secondSql = "DELETE FROM users WHERE id = ?";
        when(jdbcTemplate.update(eq(UPDATE_SQL), any(Object[].class))).thenReturn(1);
        when(jdbcTemplate.update(eq(secondSql), any(Object[].class))).thenReturn(2);

        // when
        int[] result = processor.batchExecute(List.of(firstBuilder, secondBuilder));

        // then
        assertArrayEquals(new int[]{1, 2}, result);
        verify(logger).info("Query ==> : " + UPDATE_SQL);
        verify(logger).info("Query ==> : " + secondSql);
        verify(jdbcTemplate).update(eq(UPDATE_SQL), aryEq(new Object[]{"Reyga", "CUS-01"}));
        verify(jdbcTemplate).update(eq(secondSql), aryEq(new Object[]{"CUS-02"}));
        verifyNoMoreInteractions(jdbcTemplate, logger);
        verifyNoInteractions(rowMapper);
    }

    @Test
    void should_ReturnEmptyCountsWithoutDependencyCall_When_BuilderListIsEmpty() {
        // given
        List<QueryBuilder> builders = List.of();

        // when
        int[] result = processor.batchExecute(builders);

        // then
        assertArrayEquals(new int[]{}, result);
        verifyNoInteractions(jdbcTemplate, logger, rowMapper);
    }

    @Test
    void should_BindEveryParameterAndReturnCounts_When_RawBatchExecuteSucceeds() throws SQLException {
        // given
        String sql = "INSERT INTO users (id, name) VALUES (?, ?)";
        List<Object[]> batchParameters = List.of(
                new Object[]{"CUS-01", "Reyga"},
                new Object[]{"CUS-02", "Ayu"}
        );
        int[] expected = {1, 1};
        org.mockito.ArgumentCaptor<BatchPreparedStatementSetter> captor =
                org.mockito.ArgumentCaptor.forClass(BatchPreparedStatementSetter.class);
        when(jdbcTemplate.batchUpdate(eq(sql), any(BatchPreparedStatementSetter.class)))
                .thenReturn(expected);

        // when
        int[] result = processor.batchExecute(sql, batchParameters);

        // then
        assertSame(expected, result);
        assertArrayEquals(new int[]{1, 1}, result);
        verify(jdbcTemplate).batchUpdate(eq(sql), captor.capture());
        BatchPreparedStatementSetter setter = captor.getValue();
        assertEquals(2, setter.getBatchSize());
        PreparedStatement statement = mock(PreparedStatement.class);
        setter.setValues(statement, 1);
        verify(statement).setObject(1, "CUS-02");
        verify(statement).setObject(2, "Ayu");
        verifyNoMoreInteractions(statement, jdbcTemplate);
        verifyNoInteractions(logger, rowMapper);
    }

    @Test
    void should_PropagateSQLException_When_RawBatchParameterBindingFails() throws SQLException {
        // given
        String sql = "INSERT INTO users (id) VALUES (?)";
        List<Object[]> batchParameters = List.<Object[]>of(new Object[]{"CUS-01"});
        org.mockito.ArgumentCaptor<BatchPreparedStatementSetter> captor =
                org.mockito.ArgumentCaptor.forClass(BatchPreparedStatementSetter.class);
        when(jdbcTemplate.batchUpdate(eq(sql), any(BatchPreparedStatementSetter.class)))
                .thenReturn(new int[]{1});
        processor.batchExecute(sql, batchParameters);
        verify(jdbcTemplate).batchUpdate(eq(sql), captor.capture());
        PreparedStatement statement = mock(PreparedStatement.class);
        SQLException expected = new SQLException("bind failed");
        doThrow(expected).when(statement).setObject(1, "CUS-01");

        // when
        SQLException result = assertThrows(
                SQLException.class,
                () -> captor.getValue().setValues(statement, 0)
        );

        // then
        assertSame(expected, result);
        verify(statement).setObject(1, "CUS-01");
        verifyNoMoreInteractions(statement, jdbcTemplate);
        verifyNoInteractions(logger, rowMapper);
    }

    @Test
    void should_ReturnRowsWithoutLogging_When_FetchRawSucceeds() {
        // given
        List<String> expected = List.of("one");
        when(jdbcTemplate.query(eq(SELECT_SQL), eq(rowMapper), any(Object[].class)))
                .thenReturn(expected);

        // when
        List<String> result = processor.fetchRaw(SELECT_SQL, rowMapper, true);

        // then
        assertSame(expected, result);
        verify(jdbcTemplate).query(eq(SELECT_SQL), eq(rowMapper), aryEq(new Object[]{true}));
        verifyNoMoreInteractions(jdbcTemplate);
        verifyNoInteractions(logger, rowMapper);
    }

    @Test
    void should_ReturnMappedRowInsideOptionalAndLogQuery_When_FetchOneRawSucceeds() {
        // given
        when(jdbcTemplate.queryForObject(eq(SELECT_SQL), eq(rowMapper), any(Object[].class)))
                .thenReturn("one");

        // when
        Optional<String> result = processor.fetchOneRaw(SELECT_SQL, rowMapper, true);

        // then
        assertEquals(Optional.of("one"), result);
        verify(logger).info("Executing query: " + SELECT_SQL);
        verify(jdbcTemplate).queryForObject(eq(SELECT_SQL), eq(rowMapper), aryEq(new Object[]{true}));
        verifyNoMoreInteractions(jdbcTemplate, logger, rowMapper);
    }

    @Test
    void should_ReturnEmptyOptional_When_FetchOneRawReturnsNull() {
        // given
        when(jdbcTemplate.queryForObject(eq(SELECT_SQL), eq(rowMapper), any(Object[].class)))
                .thenReturn(null);

        // when
        Optional<String> result = processor.fetchOneRaw(SELECT_SQL, rowMapper, true);

        // then
        assertTrue(result.isEmpty());
        verify(logger).info("Executing query: " + SELECT_SQL);
        verify(jdbcTemplate).queryForObject(eq(SELECT_SQL), eq(rowMapper), aryEq(new Object[]{true}));
        verifyNoMoreInteractions(jdbcTemplate, logger, rowMapper);
    }

    @Test
    void should_ReturnEmptyOptional_When_FetchOneRawHasNoResult() {
        // given
        when(jdbcTemplate.queryForObject(eq(SELECT_SQL), eq(rowMapper), any(Object[].class)))
                .thenThrow(new EmptyResultDataAccessException(1));

        // when
        Optional<String> result = processor.fetchOneRaw(SELECT_SQL, rowMapper, true);

        // then
        assertTrue(result.isEmpty());
        verify(logger).info("Executing query: " + SELECT_SQL);
        verify(jdbcTemplate).queryForObject(eq(SELECT_SQL), eq(rowMapper), aryEq(new Object[]{true}));
        verifyNoMoreInteractions(jdbcTemplate, logger, rowMapper);
    }

    @Test
    void should_ReturnAffectedRowCountWithoutLogging_When_ExecuteRawSucceeds() {
        // given
        when(jdbcTemplate.update(eq(UPDATE_SQL), any(Object[].class))).thenReturn(1);

        // when
        int result = processor.executeRaw(UPDATE_SQL, "Reyga", "CUS-01");

        // then
        assertEquals(1, result);
        verify(jdbcTemplate).update(eq(UPDATE_SQL), aryEq(new Object[]{"Reyga", "CUS-01"}));
        verifyNoMoreInteractions(jdbcTemplate);
        verifyNoInteractions(logger, rowMapper);
    }

    private QueryBuilder selectBuilder() {
        return QueryBuilder.builder().select("id").from("users")
                .where().equals("active", true).done();
    }

    private QueryBuilder updateBuilder() {
        return QueryBuilder.builder().update("users").set("name", "Reyga")
                .where().equals("id", "CUS-01").done();
    }
}
