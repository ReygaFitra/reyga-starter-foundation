package reyga.starter.foundation.common_database.processor;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.RowMapper;
import reyga.starter.foundation.common_database.util.QueryBuilder;

import java.util.List;
import java.util.Optional;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
class QueryProcessorTest {

    @Test
    void should_ReturnPageUsingExistingContracts_When_ImplementationUsesDefaultPaginationMethod() {
        // Given
        QueryProcessor processor = mock(QueryProcessor.class, CALLS_REAL_METHODS);
        QueryBuilder builder = QueryBuilder.builder().select("id").from("users")
                .where().equals("active", true).done();
        RowMapper<String> rowMapper = mock(RowMapper.class);
        String countSql = "SELECT COUNT(*) FROM (SELECT id FROM users WHERE active = ?) query_count";
        doReturn(List.of("one", "two")).when(processor).fetch(same(builder), same(rowMapper));
        doReturn(Optional.of(3L)).when(processor)
                .fetchOneRaw(eq(countSql), any(RowMapper.class), any(Object[].class));

        // When
        Page<String> result = processor.fetchPage(builder, rowMapper, PageRequest.of(0, 2));

        // Then
        assertEquals(List.of("one", "two"), result.getContent());
        assertEquals(3L, result.getTotalElements());
        assertEquals(List.of(true, 2, 0L), builder.getParameters());
        verify(processor).fetchPage(same(builder), same(rowMapper), eq(PageRequest.of(0, 2)));
        verify(processor).fetch(same(builder), same(rowMapper));
        @SuppressWarnings("unchecked")
        org.mockito.ArgumentCaptor<RowMapper<Long>> countMapper = org.mockito.ArgumentCaptor.forClass(RowMapper.class);
        verify(processor).fetchOneRaw(eq(countSql), countMapper.capture(), aryEq(new Object[]{true}));
        ResultSet countResultSet = mock(ResultSet.class);
        try {
            when(countResultSet.getLong(1)).thenReturn(3L);
            assertEquals(3L, countMapper.getValue().mapRow(countResultSet, 0));
            verify(countResultSet).getLong(1);
            verifyNoMoreInteractions(countResultSet);
        } catch (java.sql.SQLException exception) {
            fail(exception);
        }
        verifyNoMoreInteractions(processor, rowMapper);
    }

    @Test
    void should_ReturnUnpagedContentWithoutCountCall_When_PageableIsUnpaged() {
        // Given
        QueryProcessor processor = mock(QueryProcessor.class, CALLS_REAL_METHODS);
        QueryBuilder builder = QueryBuilder.builder().select("id").from("users");
        RowMapper<String> rowMapper = mock(RowMapper.class);
        Pageable pageable = Pageable.unpaged(Sort.by(Sort.Order.desc("id")));
        doReturn(List.of("two", "one")).when(processor).fetch(same(builder), same(rowMapper));

        // When
        Page<String> result = processor.fetchPage(builder, rowMapper, pageable);

        // Then
        assertEquals(List.of("two", "one"), result.getContent());
        assertEquals(2L, result.getTotalElements());
        assertTrue(result.getPageable().isUnpaged());
        assertEquals("SELECT id FROM users ORDER BY id DESC", builder.build());
        assertTrue(builder.getParameters().isEmpty());
        verify(processor).fetchPage(same(builder), same(rowMapper), same(pageable));
        verify(processor).fetch(same(builder), same(rowMapper));
        verify(processor, never()).fetchOneRaw(anyString(), any(RowMapper.class), any(Object[].class));
        verifyNoMoreInteractions(processor, rowMapper);
    }

    @Test
    void should_ReturnZeroTotal_When_CountQueryReturnsEmptyOptional() {
        // Given
        QueryProcessor processor = mock(QueryProcessor.class, CALLS_REAL_METHODS);
        QueryBuilder builder = QueryBuilder.builder().select("id").from("users");
        RowMapper<String> rowMapper = mock(RowMapper.class);
        PageRequest pageable = PageRequest.of(0, 10);
        String countSql = "SELECT COUNT(*) FROM (SELECT id FROM users) query_count";
        doReturn(List.of()).when(processor).fetch(same(builder), same(rowMapper));
        doReturn(Optional.empty()).when(processor)
                .fetchOneRaw(eq(countSql), any(RowMapper.class), any(Object[].class));

        // When
        Page<String> result = processor.fetchPage(builder, rowMapper, pageable);

        // Then
        assertTrue(result.isEmpty());
        assertEquals(0L, result.getTotalElements());
        assertEquals(List.of(10, 0L), builder.getParameters());
        verify(processor).fetchPage(same(builder), same(rowMapper), same(pageable));
        verify(processor).fetch(same(builder), same(rowMapper));
        verify(processor).fetchOneRaw(eq(countSql), any(RowMapper.class), aryEq(new Object[]{}));
        verifyNoMoreInteractions(processor, rowMapper);
    }

    @Test
    void should_ThrowNullPointerExceptionWithoutDelegation_When_RequiredArgumentsAreNull() {
        // Given
        QueryProcessor processor = mock(QueryProcessor.class, CALLS_REAL_METHODS);
        QueryBuilder builder = QueryBuilder.builder().select("id").from("users");
        RowMapper<String> rowMapper = mock(RowMapper.class);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        NullPointerException builderException = assertThrows(NullPointerException.class,
                () -> processor.fetchPage(null, rowMapper, pageable));
        NullPointerException mapperException = assertThrows(NullPointerException.class,
                () -> processor.fetchPage(builder, null, pageable));
        NullPointerException pageableException = assertThrows(NullPointerException.class,
                () -> processor.fetchPage(builder, rowMapper, null));

        // Then
        assertEquals("builder must not be null", builderException.getMessage());
        assertEquals("rowMapper must not be null", mapperException.getMessage());
        assertEquals("pageable must not be null", pageableException.getMessage());
        verify(processor).fetchPage(isNull(), same(rowMapper), same(pageable));
        verify(processor).fetchPage(same(builder), isNull(), same(pageable));
        verify(processor).fetchPage(same(builder), same(rowMapper), isNull());
        verifyNoMoreInteractions(processor);
        verifyNoInteractions(rowMapper);
    }

    @Test
    void should_PropagateDataAccessExceptionWithoutCountCall_When_ContentFetchFails() {
        // Given
        QueryProcessor processor = mock(QueryProcessor.class, CALLS_REAL_METHODS);
        QueryBuilder builder = QueryBuilder.builder().select("id").from("users");
        RowMapper<String> rowMapper = mock(RowMapper.class);
        Pageable pageable = PageRequest.of(0, 10);
        DataAccessResourceFailureException failure = new DataAccessResourceFailureException("database unavailable");
        doThrow(failure).when(processor).fetch(same(builder), same(rowMapper));

        // When
        DataAccessResourceFailureException exception = assertThrows(DataAccessResourceFailureException.class,
                () -> processor.fetchPage(builder, rowMapper, pageable));

        // Then
        assertSame(failure, exception);
        assertEquals(List.of(10, 0L), builder.getParameters());
        verify(processor).fetchPage(same(builder), same(rowMapper), same(pageable));
        verify(processor).fetch(same(builder), same(rowMapper));
        verify(processor, never()).fetchOneRaw(anyString(), any(RowMapper.class), any(Object[].class));
        verifyNoMoreInteractions(processor, rowMapper);
    }
}
