package reyga.starter.foundation.common_database.processor;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import reyga.starter.foundation.common.logging.CommonLogger;
import reyga.starter.foundation.common.logging.InjectLogger;
import reyga.starter.foundation.common_database.util.QueryBuilder;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Default JDBC implementation of {@link QueryProcessor}.
 *
 * <p>When the optional foundation logger is available, each JDBC execution emits a
 * debug-level {@code DATABASE_QUERY} event containing operational metadata only. SQL
 * text, bind parameters, exception messages, and mapped data are never included in that
 * event.</p>
 */
public class DefaultQueryProcessor implements QueryProcessor {

    private static final String QUERY_LOG_KEY = "DATABASE_QUERY";
    private static final String ROWS = "rows=";
    private static final String AFFECTED_ROWS = "affectedRows=";

    /**
     * Optional foundation logger injected by the runtime logger processor.
     */
    @InjectLogger
    protected CommonLogger logger;

    private final JdbcTemplate jdbcTemplate;

    /**
     * Creates a processor backed by the supplied JDBC template.
     *
     * @param jdbcTemplate template used for all database operations
     * @throws NullPointerException if {@code jdbcTemplate} is {@code null}
     */
    public DefaultQueryProcessor(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "jdbcTemplate must not be null");
    }

    @Override
    public <T> List<T> fetch(QueryBuilder builder, RowMapper<T> rowMapper) {
        String sql = builder.build();
        List<Object> params = builder.getParameters();
        return executeLogged(
                sql,
                "FETCH_MANY",
                () -> jdbcTemplate.query(sql, rowMapper, params.toArray()),
                result -> ROWS + result.size()
        );
    }

    @Override
    public <T> Optional<List<T>> optionalFetch(QueryBuilder builder, RowMapper<T> rowMapper) {
        String sql = builder.build();
        List<Object> params = builder.getParameters();
        return executeLogged(
                sql,
                "FETCH_OPTIONAL_MANY",
                () -> {
                    try {
                        return Optional.of(jdbcTemplate.query(sql, rowMapper, params.toArray()));
                    } catch (EmptyResultDataAccessException _) {
                        return Optional.empty();
                    }
                },
                result -> ROWS + result.map(List::size).orElse(0)
        );
    }

    @Override
    public <T> T fetchOne(QueryBuilder builder, RowMapper<T> rowMapper) {
        String sql = builder.build();
        List<Object> params = builder.getParameters();
        List<T> results = executeLogged(
                sql,
                "FETCH_ONE",
                () -> jdbcTemplate.query(sql, rowMapper, params.toArray()),
                result -> ROWS + result.size()
        );
        return results.getFirst();
    }

    @Override
    public <T> Optional<T> optionalFetchOne(QueryBuilder builder, RowMapper<T> rowMapper) {
        String sql = builder.build();
        List<Object> params = builder.getParameters();
        List<T> results = executeLogged(
                sql,
                "FETCH_OPTIONAL_ONE",
                () -> jdbcTemplate.query(sql, rowMapper, params.toArray()),
                result -> ROWS + result.size()
        );
        return results.isEmpty() ? Optional.empty() : Optional.of(results.getFirst());
    }

    @Override
    public <T> T fetchOneBy(QueryBuilder builder, RowMapper<T> rowMapper) {
        String sql = builder.build();
        List<Object> params = builder.getParameters();
        return executeLogged(
                sql,
                "FETCH_ONE_BY",
                () -> jdbcTemplate.queryForObject(sql, rowMapper, params.toArray()),
                DefaultQueryProcessor::singleRowMetadata
        );
    }

    @Override
    public <T> Optional<T> optionalFetchOneBy(QueryBuilder builder, RowMapper<T> rowMapper) {
        String sql = builder.build();
        List<Object> params = builder.getParameters();
        return optionalQueryForObject(sql, rowMapper, params, "FETCH_OPTIONAL_ONE_BY");
    }

    @Override
    public <T> Page<T> fetchPage(QueryBuilder builder, RowMapper<T> rowMapper, Pageable pageable) {
        Objects.requireNonNull(builder, "builder must not be null");
        Objects.requireNonNull(rowMapper, "rowMapper must not be null");
        Objects.requireNonNull(pageable, "pageable must not be null");

        if (pageable.isUnpaged()) {
            builder.paginate(pageable);
            String contentSql = builder.build();
            List<T> content = executeLogged(
                    contentSql,
                    "FETCH_PAGE_CONTENT",
                    () -> jdbcTemplate.query(contentSql, rowMapper, builder.getParameters().toArray()),
                    result -> ROWS + result.size()
            );
            return new PageImpl<>(content);
        }

        String countSql = builder.buildCount();
        List<Object> countParameters = builder.getCountParameters();
        builder.paginate(pageable);
        String contentSql = builder.build();

        List<T> content = executeLogged(
                contentSql,
                "FETCH_PAGE_CONTENT",
                () -> jdbcTemplate.query(contentSql, rowMapper, builder.getParameters().toArray()),
                result -> ROWS + result.size()
        );
        Long total = executeLogged(
                countSql,
                "FETCH_PAGE_COUNT",
                () -> jdbcTemplate.queryForObject(countSql, Long.class, countParameters.toArray()),
                DefaultQueryProcessor::singleRowMetadata
        );
        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    @Override
    public int execute(QueryBuilder builder) {
        String sql = builder.build();
        List<Object> params = builder.getParameters();
        return executeLogged(
                sql,
                "EXECUTE",
                () -> jdbcTemplate.update(sql, params.toArray()),
                result -> AFFECTED_ROWS + result
        );
    }

    @Override
    public int[] batchExecute(QueryBuilder builder, List<Object[]> batchParams) {
        String sql = builder.build();
        return executeLogged(
                sql,
                "BATCH_EXECUTE",
                () -> jdbcTemplate.batchUpdate(sql, batchParams),
                DefaultQueryProcessor::batchMetadata
        );
    }

    @Override
    public int[] batchExecute(List<QueryBuilder> builders) {
        List<Integer> results = new ArrayList<>();
        for (QueryBuilder builder : builders) {
            String sql = builder.build();
            int affectedRows = executeLogged(
                    sql,
                    "BATCH_ITEM_EXECUTE",
                    () -> jdbcTemplate.update(sql, builder.getParameters().toArray()),
                    result -> AFFECTED_ROWS + result
            );
            results.add(affectedRows);
        }
        return results.stream().mapToInt(Integer::intValue).toArray();
    }

    @Override
    public int[] batchExecute(String sql, List<Object[]> batchParams) {
        return executeLogged(
                sql,
                "RAW_BATCH_EXECUTE",
                () -> jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
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
                }),
                DefaultQueryProcessor::batchMetadata
        );
    }

    @Override
    public <T> List<T> fetchRaw(String sql, RowMapper<T> rowMapper, Object... params) throws DataAccessException {
        return executeLogged(
                sql,
                "RAW_FETCH_MANY",
                () -> jdbcTemplate.query(sql, rowMapper, params),
                result -> ROWS + result.size()
        );
    }

    @Override
    public <T> Optional<T> fetchOneRaw(String sql, RowMapper<T> rowMapper, Object... params) {
        return optionalQueryForObject(sql, rowMapper, Arrays.asList(params), "RAW_FETCH_OPTIONAL_ONE");
    }

    @Override
    public int executeRaw(String sql, Object... params) throws DataAccessException {
        return executeLogged(
                sql,
                "RAW_EXECUTE",
                () -> jdbcTemplate.update(sql, params),
                result -> AFFECTED_ROWS + result
        );
    }

    private <T> Optional<T> optionalQueryForObject(
            String sql,
            RowMapper<T> rowMapper,
            List<Object> params,
            String execution
    ) {
        return executeLogged(
                sql,
                execution,
                () -> {
                    try {
                        return Optional.ofNullable(jdbcTemplate.queryForObject(
                                sql,
                                rowMapper,
                                params.toArray()
                        ));
                    } catch (EmptyResultDataAccessException _) {
                        return Optional.empty();
                    }
                },
                result -> ROWS + (result.isPresent() ? 1 : 0)
        );
    }

    private <T> T executeLogged(
            String sql,
            String execution,
            Supplier<T> operation,
            Function<T, String> successMetadata
    ) {
        long startedAt = System.nanoTime();
        try {
            T result = operation.get();
            logQueryExecution(
                    sql,
                    execution,
                    "SUCCESS",
                    elapsedMilliseconds(startedAt),
                    successMetadata.apply(result)
            );
            return result;
        } catch (RuntimeException exception) {
            logQueryExecution(
                    sql,
                    execution,
                    "FAILURE",
                    elapsedMilliseconds(startedAt),
                    "failureType=" + exception.getClass().getSimpleName()
            );
            throw exception;
        }
    }

    private void logQueryExecution(
            String sql,
            String execution,
            String outcome,
            long durationMilliseconds,
            String resultMetadata
    ) {
        if (logger == null) {
            return;
        }
        String metadata = "operation=" + resolveOperation(sql)
                + " | execution=" + execution
                + " | outcome=" + outcome
                + " | durationMs=" + durationMilliseconds
                + " | " + resultMetadata;
        logger.info(QUERY_LOG_KEY, metadata);
    }

    private static long elapsedMilliseconds(long startedAt) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
    }

    private static String resolveOperation(String sql) {
        if (sql == null || sql.isBlank()) {
            return "UNKNOWN";
        }
        String statement = sql.stripLeading();
        int keywordEnd = 0;
        while (keywordEnd < statement.length() && Character.isLetter(statement.charAt(keywordEnd))) {
            keywordEnd++;
        }
        if (keywordEnd == 0) {
            return "OTHER";
        }
        String keyword = statement.substring(0, keywordEnd).toUpperCase(Locale.ROOT);
        return switch (keyword) {
            case "SELECT", "INSERT", "UPDATE", "DELETE", "MERGE", "CALL" -> keyword;
            case "WITH" -> "COMPOUND";
            default -> "OTHER";
        };
    }

    private static String singleRowMetadata(Object result) {
        return ROWS + (result == null ? 0 : 1);
    }

    private static String batchMetadata(int[] results) {
        long affectedRows = 0;
        int unknownResults = 0;
        int failedResults = 0;
        for (int result : results) {
            if (result >= 0) {
                affectedRows += result;
            } else if (result == java.sql.Statement.SUCCESS_NO_INFO) {
                unknownResults++;
            } else if (result == java.sql.Statement.EXECUTE_FAILED) {
                failedResults++;
            }
        }
        return "batchResults=" + results.length
                + " | affectedRows=" + affectedRows
                + " | unknownResults=" + unknownResults
                + " | failedResults=" + failedResults;
    }
}
