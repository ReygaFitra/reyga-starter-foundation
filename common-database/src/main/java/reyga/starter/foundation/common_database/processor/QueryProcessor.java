package reyga.starter.foundation.common_database.processor;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import reyga.starter.foundation.common_database.util.QueryBuilder;

import java.util.List;
import java.util.Optional;

public interface QueryProcessor {

    <T> List<T> fetch(QueryBuilder builder, RowMapper<T> rowMapper);

    <T> Optional<List<T>> optionalFetch(QueryBuilder builder, RowMapper<T> rowMapper);

    <T> T fetchOne(QueryBuilder builder, RowMapper<T> rowMapper);

    <T> Optional<T> optionalFetchOne(QueryBuilder builder, RowMapper<T> rowMapper);

    <T> T fetchOneBy(QueryBuilder builder, RowMapper<T> rowMapper);

    <T> Optional<T> optionalFetchOneBy(QueryBuilder builder, RowMapper<T> rowMapper);

    int execute(QueryBuilder builder);

    int[] batchExecute(QueryBuilder builder, List<Object[]> batchParams);

    int[] batchExecute(List<QueryBuilder> builders);

    int[] batchExecute(String sql, List<Object[]> batchParams);

    <T> List<T> fetchRaw(String sql, RowMapper<T> rowMapper, Object... params) throws DataAccessException;

    <T> Optional<T> fetchOneRaw(String sql, RowMapper<T> rowMapper, Object... params);

    int executeRaw(String sql, Object... params) throws DataAccessException;
}
