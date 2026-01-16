package reyga.starter.foundation.common_database.processor;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import reyga.starter.foundation.common.logging.BaseLogging;
import reyga.starter.foundation.common_database.util.QueryBuilder;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class QueryProcessor extends BaseLogging {

    private final JdbcTemplate jdbcTemplate;

    public <T> List<T> fetch(QueryBuilder builder, RowMapper<T> rowMapper) {
        String sql = builder.build();
        log.info("Constructed query: " + sql);
        List<Object> params = builder.getParameters();
        return jdbcTemplate.query(sql, rowMapper, params.toArray());
    }

    public <T> Optional<List<T>> optionalFetch(QueryBuilder builder, RowMapper<T> rowMapper) {
        String sql = builder.build();
        log.info("Constructed query : " + sql);
        List<Object> params = builder.getParameters();
        try {
            return Optional.of(jdbcTemplate.query(sql, rowMapper, params.toArray()));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public <T> T fetchOne(QueryBuilder builder, RowMapper<T> rowMapper) {
        List<T> results = fetch(builder, rowMapper);
        return results.get(0);
    }

    public <T> Optional<T> optionalFetchOne(QueryBuilder builder, RowMapper<T> rowMapper) {
        List<T> results = fetch(builder, rowMapper);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public <T> T fetchOneBy(QueryBuilder builder, RowMapper<T> rowMapper) {
        String sql = builder.build();
        List<Object> params = builder.getParameters();
        return this.queryForObject(sql, rowMapper, params);
    }

    public <T> Optional<T> optionalFetchOneBy(QueryBuilder builder, RowMapper<T> rowMapper) {
        String sql = builder.build();
        List<Object> params = builder.getParameters();
        return this.optionalQueryForObject(sql, rowMapper, params);
    }

    public int execute(QueryBuilder builder) {
        String sql = builder.build();
        log.info("Constructed query: " + sql);
        List<Object> params = builder.getParameters();
        return jdbcTemplate.update(sql, params.toArray());
    }

    public int[] batchExecute(QueryBuilder builder, List<Object[]> batchParams) {
        String sql = builder.build();
        log.info("Constructed query: " + sql);
        return jdbcTemplate.batchUpdate(sql, batchParams);
    }

    public int[] batchExecute(List<QueryBuilder> builders) {
        List<Integer> results = new ArrayList<>();
        for (QueryBuilder builder : builders) {
            String sql = builder.build();
            log.info("Query ==> : " + sql);
            results.add(jdbcTemplate.update(sql, builder.getParameters().toArray()));
        }
        return results.stream().mapToInt(Integer::intValue).toArray();
    }

    public int[] batchExecute(String sql, List<Object[]> batchParams) {
        return jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Object[] params = batchParams.get(i);
                for (int j = 0; j < params.length; j++) {
                    ps.setObject(j + 1, params[j]);
                }
            }

            @Override
            public int getBatchSize() {
                return batchParams.size();
            }
        });
    }

    public <T> List<T> fetchRaw(String sql, RowMapper<T> rowMapper, Object... params) throws DataAccessException {
        return jdbcTemplate.query(sql, rowMapper, params);
    }

    public <T> Optional<T> fetchOneRaw(String sql, RowMapper<T> rowMapper, Object... params) {
        log.info("Raw queryForObject: " + sql);
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, params));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public int executeRaw(String sql, Object... params) throws DataAccessException {
        return jdbcTemplate.update(sql, params);
    }

    private <T> T queryForObject(String sql, RowMapper<T> rowMapper, List<Object> params) {
        log.info("Constructed queryForObject: " + sql);
        return jdbcTemplate.queryForObject(sql, rowMapper, params.toArray());
    }

    private <T> Optional<T> optionalQueryForObject(String sql, RowMapper<T> rowMapper, List<Object> params) {
        log.info("Constructed queryForObject: " + sql);
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, params.toArray()));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
