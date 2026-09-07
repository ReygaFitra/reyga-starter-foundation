package reyga.starter.foundation.common_database.processor;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import reyga.starter.foundation.common_database.util.QueryBuilder;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Execution abstraction for SQL represented by {@link QueryBuilder} or supplied as raw SQL.
 * Implementations own the JDBC interaction, while repository implementations own query
 * composition and row mapping.
 *
 * <p>Builder-based operations build the latest builder state before execution. Raw methods
 * should be reserved for database-specific SQL that cannot be represented by the builder.</p>
 */
public interface QueryProcessor {

    /**
     * Executes a query and returns all mapped rows.
     *
     * @param builder SELECT query builder
     * @param rowMapper row mapper
     * @param <T> result type
     * @return all mapped rows
     */
    <T> List<T> fetch(QueryBuilder builder, RowMapper<T> rowMapper);

    /**
     * Executes a query and wraps its row list.
     *
     * @param builder SELECT query builder
     * @param rowMapper row mapper
     * @param <T> result type
     * @return optional mapped row list
     */
    <T> Optional<List<T>> optionalFetch(QueryBuilder builder, RowMapper<T> rowMapper);

    /**
     * Executes a query and returns its first row.
     *
     * @param builder SELECT query builder
     * @param rowMapper row mapper
     * @param <T> result type
     * @return first mapped row
     */
    <T> T fetchOne(QueryBuilder builder, RowMapper<T> rowMapper);

    /**
     * Executes a query and optionally returns its first row.
     *
     * @param builder SELECT query builder
     * @param rowMapper row mapper
     * @param <T> result type
     * @return first mapped row or an empty optional
     */
    <T> Optional<T> optionalFetchOne(QueryBuilder builder, RowMapper<T> rowMapper);

    /**
     * Executes a query using single-row semantics.
     *
     * @param builder single-row SELECT query builder
     * @param rowMapper row mapper
     * @param <T> result type
     * @return mapped row
     */
    <T> T fetchOneBy(QueryBuilder builder, RowMapper<T> rowMapper);

    /**
     * Executes a query using optional single-row semantics.
     *
     * @param builder single-row SELECT query builder
     * @param rowMapper row mapper
     * @param <T> result type
     * @return mapped row or an empty optional
     */
    <T> Optional<T> optionalFetchOneBy(QueryBuilder builder, RowMapper<T> rowMapper);

    /**
     * Executes a data modification builder.
     *
     * Executes a paginated select and derives its count query from the supplied builder.
     * The builder must describe a SELECT query and must not contain untrusted SQL fragments.
     *
     * @param builder query used for both page content and total count
     * @param rowMapper mapper for page content
     * @param pageable zero-based page request; {@link Pageable#unpaged()} is supported
     * @param <T> content type
     * @return page content and total number of matching rows
     */
    default <T> Page<T> fetchPage(QueryBuilder builder, RowMapper<T> rowMapper, Pageable pageable) {
        Objects.requireNonNull(builder, "builder must not be null");
        Objects.requireNonNull(rowMapper, "rowMapper must not be null");
        Objects.requireNonNull(pageable, "pageable must not be null");

        if (pageable.isUnpaged()) {
            builder.paginate(pageable);
            return new PageImpl<>(fetch(builder, rowMapper));
        }

        String countSql = builder.buildCount();
        Object[] countParameters = builder.getCountParameters().toArray();
        builder.paginate(pageable);
        List<T> content = fetch(builder, rowMapper);
        long total = fetchOneRaw(countSql, (resultSet, rowNumber) -> resultSet.getLong(1), countParameters)
                .orElse(0L);
        return new PageImpl<>(content, pageable, total);
    }

    /**
     * Executes one statement for multiple parameter groups.
     *
     * @param builder INSERT, UPDATE, or DELETE builder
     * @return affected row count
     */
    int execute(QueryBuilder builder);

    /**
     * Executes multiple independently built statements.
     *
     * @param builder statement builder
     * @param batchParams parameter groups in placeholder order
     * @return affected row counts
     */
    int[] batchExecute(QueryBuilder builder, List<Object[]> batchParams);

    /**
     * Executes one raw statement for multiple parameter groups.
     *
     * @param builders statement builders
     * @return affected row counts
     */
    int[] batchExecute(List<QueryBuilder> builders);

    /**
     * Executes a raw query and returns all mapped rows.
     *
     * @param sql raw parameterized SQL
     * @param batchParams parameter groups in placeholder order
     * @return affected row counts
     */
    int[] batchExecute(String sql, List<Object[]> batchParams);

    /**
     * Executes a raw query using optional single-row semantics.
     *
     * @param sql raw parameterized SELECT
     * @param rowMapper row mapper
     * @param params bind parameters
     * @param <T> result type
     * @return all mapped rows
     * @throws DataAccessException when JDBC execution fails
     */
    <T> List<T> fetchRaw(String sql, RowMapper<T> rowMapper, Object... params) throws DataAccessException;

    /**
     * Executes a raw query using optional single-row semantics.
     *
     * @param sql raw parameterized SELECT
     * @param rowMapper row mapper
     * @param params bind parameters
     * @param <T> result type
     * @return mapped row or an empty optional
     */
    <T> Optional<T> fetchOneRaw(String sql, RowMapper<T> rowMapper, Object... params);

    /**
     * Executes a raw data modification statement.
     *
     * @param sql raw parameterized INSERT, UPDATE, or DELETE
     * @param params bind parameters
     * @return affected row count
     * @throws DataAccessException when JDBC execution fails
     */
    int executeRaw(String sql, Object... params) throws DataAccessException;
}
