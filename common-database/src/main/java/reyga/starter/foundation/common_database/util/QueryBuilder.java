package reyga.starter.foundation.common_database.util;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import reyga.starter.foundation.common_database.enumeration.QueryJoinType;
import reyga.starter.foundation.common_database.enumeration.QueryOperator;
import reyga.starter.foundation.common_database.enumeration.QueryType;

import java.util.*;

/**
 * Mutable fluent builder for parameterized SELECT, INSERT, UPDATE, and DELETE statements.
 * Values are stored as JDBC parameters, while identifiers and SQL expressions are expected
 * to be trusted application constants.
 *
 * <p>A builder is intended for one logical query and should not be shared between threads.
 * Switching statement type does not reset accumulated state. Add UPDATE assignments before
 * WHERE predicates; only pagination parameters are reordered automatically. Pagination uses
 * LIMIT/OFFSET syntax and therefore requires a compatible SQL dialect.</p>
 * <p>This is not a SQL parser: identifiers, raw expressions, connector placement, and
 * database-specific syntax remain the caller's responsibility. UPDATE/DELETE without
 * WHERE are permitted. Parameter snapshots are immutable lists, not deep copies of values.</p>
 * <pre>{@code
 * QueryBuilder query = QueryBuilder.builder().select("u.id").from("users u")
 *     .join("roles", "r").on("u.role_id", QueryOperator.EQUALS, "r.id").done()
 *     .where().condition("u.active", QueryOperator.EQUALS, true).done();
 * String statement = query.build();
 * List<Object> bindings = query.getParameters();
 * }</pre>
 */
public class QueryBuilder {
    private QueryType type;
    private String table;
    private final List<String> columns = new ArrayList<>();
    private final List<String> joins = new ArrayList<>();
    private final List<String> groupBy = new ArrayList<>();
    private final List<String> orderBy = new ArrayList<>();
    private final List<String> pageableOrderBy = new ArrayList<>();
    private Number limit;
    private Number offset;

    private final List<String> whereConditions = new ArrayList<>();
    private final List<String> insertValues = new ArrayList<>();
    private final List<String> setClauses = new ArrayList<>();

    private final List<Object> parameters = new LinkedList<>();

    private String sql;

    /**
     * Returns SQL cached by the latest successful build.
     * Mutations and buildCount do not refresh this snapshot.
     * @return last built SQL, or null before the first successful build
     */
    public String getSql() {
        return sql;
    }

    private static final String WHERE = " WHERE ";
    private static final String COUNT_ALIAS = "query_count";
    private static final String SAFE_SORT_EXPRESSION =
            "[A-Za-z_][A-Za-z0-9_$]*(\\.[A-Za-z_][A-Za-z0-9_$]*)*";

    /** Creates an empty query builder. */
    public QueryBuilder() {
    }

    /**
     * Creates a fresh mutable builder for one SQL statement.
     * @return a new empty builder
     */
    public static QueryBuilder builder() {
        return new QueryBuilder();
    }

    /**
     * Selects SELECT and appends projections without clearing existing builder state.
     * @param cols trusted SQL expressions; empty projection means SELECT *
     * @return this builder
     */
    public QueryBuilder select(String... cols) {
        this.type = QueryType.SELECT;
        columns.addAll(Arrays.asList(cols));
        return this;
    }

    /**
     * Sets the trusted SELECT source expression without quoting or validation.
     * @param table trusted table expression, optionally with an alias
     * @return this builder
     */
    public QueryBuilder from(String table) {
        this.table = table;
        return this;
    }

    /**
     * Adds an INNER JOIN comparing trusted SQL expressions with a typed binary operator.
     * @param table trusted table expression, optionally with an alias
     * @param leftColumn trusted left-hand SQL expression
     * @param op non-null operator supporting a single right-hand expression
     * @param rightColumn trusted right-hand SQL expression
     * @return this builder
     * @throws IllegalArgumentException if expressions are blank or the typed operator is not binary
     * @throws NullPointerException if operator is null
     */
    public QueryBuilder joinOn(String table, String leftColumn, QueryOperator op, String rightColumn) {
        return join(table).on(leftColumn, op, rightColumn).done();
    }

    /**
     * Starts a JOIN. Complete the nested builder once using done or endJoin to attach it.
     * @param table trusted table expression, optionally with an alias
     * @param alias optional alias; null or blank omits it
     * @return the JOIN builder for fluent chaining
     */
    public JoinBuilder join(String table, String alias) {
        return new JoinBuilder(this, QueryJoinType.INNER, table, alias);
    }

    /**
     * Starts a JOIN. Complete the nested builder once using done or endJoin to attach it.
     * @param table trusted table expression, optionally with an alias
     * @return the JOIN builder for fluent chaining
     */
    public JoinBuilder join(String table) {
        return new JoinBuilder(this, QueryJoinType.INNER, table, null);
    }

    /**
     * Starts a JOIN. Complete the nested builder once using done or endJoin to attach it.
     * @param type non-null JOIN type
     * @param table trusted table expression, optionally with an alias
     * @return the JOIN builder for fluent chaining
     */
    public JoinBuilder join(QueryJoinType type, String table) {
        return new JoinBuilder(this, type, table, null);
    }

    /**
     * Starts a JOIN. Complete the nested builder once using done or endJoin to attach it.
     * @param type non-null JOIN type
     * @param table trusted table expression, optionally with an alias
     * @param alias optional alias; null or blank omits it
     * @return the JOIN builder for fluent chaining
     */
    public JoinBuilder join(QueryJoinType type, String table, String alias) {
        return new JoinBuilder(this, type, table, alias);
    }

    /**
     * Appends trusted ORDER BY expressions before any Pageable sort; no validation is performed.
     * @param cols trusted ordering expressions, optionally including direction; empty adds nothing
     * @return this builder
     * @throws NullPointerException if the array is null
     */
    public QueryBuilder orderBy(String... cols) {
        orderBy.addAll(Arrays.asList(cols));
        return this;
    }

    /**
     * Appends trusted GROUP BY expressions.
     * @param cols trusted grouping expressions; empty adds nothing
     * @return this builder
     * @throws NullPointerException if the array is null
     */
    public QueryBuilder groupBy(String... cols) {
        groupBy.addAll(Arrays.asList(cols));
        return this;
    }

    /**
     * Replaces the SELECT row limit; zero is allowed.
     * @param limit non-negative maximum number of rows
     * @return this builder
     * @throws IllegalArgumentException if the value is negative
     */
    public QueryBuilder limit(int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("limit must not be negative");
        }
        this.limit = limit;
        return this;
    }

    /**
     * Replaces the SELECT row offset; zero is allowed.
     * @param offset non-negative number of rows to skip
     * @return this builder
     * @throws IllegalArgumentException if the value is negative
     */
    public QueryBuilder offset(int offset) {
        if (offset < 0) {
            throw new IllegalArgumentException("offset must not be negative");
        }
        this.offset = offset;
        return this;
    }

    /**
     * Applies legacy one-based pagination.
     * @param page page number starting at {@code 1}
     * @param pageSize positive number of rows per page
     * @return this builder
     * @throws IllegalArgumentException if page or pageSize is less than one
     * @throws ArithmeticException if the computed offset overflows an int
     */
    public QueryBuilder paginate(int page, int pageSize) {
        if (page < 1) {
            throw new IllegalArgumentException("page must be greater than zero");
        }
        if (pageSize < 1) {
            throw new IllegalArgumentException("pageSize must be greater than zero");
        }
        this.limit = pageSize;
        this.offset = Math.multiplyExact(page - 1, pageSize);
        return this;
    }

    /**
     * Applies a Spring Data page request. Pageable page numbers are zero-based. Sort
     * properties are limited to simple or qualified SQL identifiers; callers that need
     * expressions should add trusted expressions explicitly through {@link #orderBy(String...)}.
     * Replaces earlier Pageable sorting; unpaged clears LIMIT/OFFSET but retains its sort.
     * Only property and direction are rendered; ignore-case and null-handling hints are not applied.
     * @param pageable page size, offset, and optional safe sort properties
     * @return this builder
     * @throws IllegalArgumentException if a sort property is not a simple or qualified SQL identifier
     * @throws NullPointerException if pageable is null
     */
    public QueryBuilder paginate(Pageable pageable) {
        Objects.requireNonNull(pageable, "pageable must not be null");
        pageableOrderBy.clear();
        appendPageableSort(pageable.getSort());

        if (pageable.isUnpaged()) {
            this.limit = null;
            this.offset = null;
        } else {
            this.limit = pageable.getPageSize();
            this.offset = pageable.getOffset();
        }
        return this;
    }

    /**
     * Returns query parameters in SQL placeholder order. Pagination parameters are
     * appended after filters regardless of the order in which builder methods were called.
     *
     * @return immutable snapshot of all content-query parameters
     */
    public List<Object> getParameters() {
        List<Object> result = new ArrayList<>(parameters);
        if (limit != null) result.add(limit);
        if (offset != null) result.add(offset);
        return Collections.unmodifiableList(result);
    }

    /**
     * Returns parameters for {@link #buildCount()}, excluding LIMIT and OFFSET values.
     *
     * @return immutable snapshot of count-query parameters
     */
    public List<Object> getCountParameters() {
        return Collections.unmodifiableList(new ArrayList<>(parameters));
    }

    /**
     * Selects INSERT and appends optional columns without clearing existing state.
     * @param table trusted table expression, optionally with an alias
     * @param cols trusted column names; null or empty omits the column list
     * @return this builder
     */
    public QueryBuilder insertInto(String table, String... cols) {
        this.type = QueryType.INSERT;
        this.table = table;
        if (cols != null && cols.length > 0) this.columns.addAll(Arrays.asList(cols));
        return this;
    }

    /**
     * Appends INSERT placeholders and values. A null array adds nothing; null elements are bound.
     * @param vals bound INSERT values, possibly containing null; null array adds nothing
     * @return this builder
     */
    public QueryBuilder values(Object... vals) {
        if (vals != null) {
            for (Object v : vals) {
                this.insertValues.add("?");
                this.parameters.add(v);
            }
        }
        return this;
    }

    /**
     * Selects UPDATE without clearing existing state. Without WHERE, all rows may be updated.
     * @param table trusted table expression, optionally with an alias
     * @return this builder
     */
    public QueryBuilder update(String table) {
        this.type = QueryType.UPDATE;
        this.table = table;
        return this;
    }

    /**
     * Appends an UPDATE assignment. Add all assignments before WHERE to preserve placeholder order.
     * @param column trusted assignment target, not validated or quoted
     * @param value bound JDBC value, possibly null
     * @return this builder
     */
    public QueryBuilder set(String column, Object value) {
        this.setClauses.add(column + " = ?");
        this.parameters.add(value);
        return this;
    }

    /**
     * Selects DELETE without clearing existing state. Without WHERE, all rows may be deleted.
     * @param table trusted table expression, optionally with an alias
     * @return this builder
     */
    public QueryBuilder deleteFrom(String table) {
        this.type = QueryType.DELETE;
        this.table = table;
        return this;
    }

    /**
     * Opens a WHERE chain that immediately appends conditions to the parent. Prefer one chain per query.
     * @return the WHERE builder for fluent chaining
     */
    public WhereBuilder where() {
        return new WhereBuilder(this);
    }

    private void addCondition(String condition) {
        this.whereConditions.add(condition);
    }

    /**
     * Renders and caches SQL without executing JDBC or clearing state. Use getParameters for bound values.
     * @return rendered SQL with JDBC placeholders
     * @throws IllegalStateException if query type is absent or non-empty INSERT column and value counts differ
     */
    public String build() {
        StringBuilder sqlBuilder = new StringBuilder();

        if (type == null) {
            throw new IllegalStateException("Query type (SELECT, INSERT, UPDATE, DELETE) must be specified.");
        }

        switch (type) {
            case SELECT -> this.selectTypeBuilder(sqlBuilder);
            case INSERT -> this.insertTypeBuilder(sqlBuilder);
            case UPDATE -> this.updateTypeBuilder(sqlBuilder);
            case DELETE -> this.deleteTypeBuilder(sqlBuilder);
        }

        this.sql = sqlBuilder.toString().trim();
        return this.sql;
    }

    /**
     * Builds a count query from the current SELECT query. Wrapping the original query
     * preserves the semantics of DISTINCT, joins, and GROUP BY while excluding sorting
     * and pagination, which are irrelevant to the total count.
     * @return SQL count query for the current SELECT
     * @throws IllegalStateException when the builder does not describe a SELECT query
     */
    public String buildCount() {
        if (type != QueryType.SELECT) {
            throw new IllegalStateException("Count query can only be built from a SELECT query.");
        }
        StringBuilder contentQuery = new StringBuilder();
        selectTypeBuilder(contentQuery, false, false);
        return "SELECT " + QueryFunction.count("*") + " FROM ("
                + contentQuery.toString().trim() + ") " + COUNT_ALIAS;
    }

    /**
     * Mutable WHERE chain sharing its parent's parameters and conditions.
     * Supply connectors explicitly between predicates. Conditions are not grouped automatically.
     */
    public static class WhereBuilder {
        private final QueryBuilder parent;
        private boolean lastConditionAdded = false;

        /**
         * Creates a WHERE chain attached to the specified parent.
         * @param parent non-null owning query builder
         */
        public WhereBuilder(QueryBuilder parent) { this.parent = parent; }

        /**
         * Adds a typed predicate with bound JDBC values. IN accepts one or more values,
         * BETWEEN exactly two, unary predicates none, and comparisons exactly one.
         * LIKE patterns are used as supplied (no implicit wildcard). A SQL null value
         * must be passed as {@code (Object) null}; use IS_NULL for a null test.
         * Column expressions must be trusted application constants.
         * @param column trusted column expression; WHERE requires non-null, non-blank text
         * @param operator non-null predicate operator
         * @param values bound values in placeholder order; IN/NOT IN require a non-empty array
         * @return the WHERE builder for fluent chaining
         * @throws NullPointerException if operator is null
         * @throws IllegalArgumentException if column is blank, the values array is null, or operand count is invalid
         */
        public WhereBuilder condition(String column, QueryOperator operator, Object... values) {
            Objects.requireNonNull(operator, "operator must not be null");
            if (column == null || column.isBlank()) {
                throw new IllegalArgumentException("column must not be blank");
            }
            if (values == null) throw new IllegalArgumentException("values must not be null");
            String predicate = column + " " + operator.placeholders(values.length);
            parent.addCondition(predicate);
            parent.parameters.addAll(Arrays.asList(values));
            lastConditionAdded = true;
            return this;
        }

        /**
         * Adds equality with one bound value. SQL null is not rewritten to IS NULL.
         * @param column trusted column expression; WHERE requires non-null, non-blank text
         * @param value bound JDBC value, possibly null
         * @return the WHERE builder for fluent chaining
         * @throws IllegalArgumentException if column is null or blank
         */
        public WhereBuilder equals(String column, Object value) {
            return condition(column, QueryOperator.EQUALS, value);
        }

        /**
         * Adds inequality with one bound value. SQL null semantics are preserved.
         * @param column trusted column expression; WHERE requires non-null, non-blank text
         * @param value bound JDBC value, possibly null
         * @return the WHERE builder for fluent chaining
         * @throws IllegalArgumentException if column is null or blank
         */
        public WhereBuilder notEquals(String column, Object value) {
            return condition(column, QueryOperator.NOT_EQUALS, value);
        }

        /**
         * Adds a greater-than comparison with one bound value.
         * @param column trusted column expression; WHERE requires non-null, non-blank text
         * @param value bound JDBC value, possibly null
         * @return the WHERE builder for fluent chaining
         * @throws IllegalArgumentException if column is null or blank
         */
        public WhereBuilder greaterThan(String column, Object value) {
            return condition(column, QueryOperator.GREATER_THAN, value);
        }

        /**
         * Adds a less-than comparison with one bound value.
         * @param column trusted column expression; WHERE requires non-null, non-blank text
         * @param value bound JDBC value, possibly null
         * @return the WHERE builder for fluent chaining
         * @throws IllegalArgumentException if column is null or blank
         */
        public WhereBuilder lessThan(String column, Object value) {
            return condition(column, QueryOperator.LESS_THAN, value);
        }

        /**
         * Adds a greater-than-or-equal comparison with one bound value.
         * @param column trusted column expression; WHERE requires non-null, non-blank text
         * @param value bound JDBC value, possibly null
         * @return the WHERE builder for fluent chaining
         * @throws IllegalArgumentException if column is null or blank
         */
        public WhereBuilder greaterThanOrEquals(String column, Object value) {
            return condition(column, QueryOperator.GREATER_THAN_OR_EQUALS, value);
        }

        /**
         * Adds a less-than-or-equal comparison with one bound value.
         * @param column trusted column expression; WHERE requires non-null, non-blank text
         * @param value bound JDBC value, possibly null
         * @return the WHERE builder for fluent chaining
         * @throws IllegalArgumentException if column is null or blank
         */
        public WhereBuilder lessThanOrEquals(String column, Object value) {
            return condition(column, QueryOperator.LESS_THAN_OR_EQUALS, value);
        }

        /**
         * Like {@link #like(String, String)}, surrounds the value with percent wildcards.
         * @param column trusted column expression; WHERE requires non-null, non-blank text
         * @param value bound JDBC value, possibly null
         * @return the WHERE builder for fluent chaining
         * @throws IllegalArgumentException if column is null or blank
         */
        public WhereBuilder notLike(String column, String value) {
            return condition(column, QueryOperator.NOT_LIKE, "%" + value + "%");
        }

        /**
         * Adds NOT BETWEEN with bound lower and upper limits.
         * @param column trusted column expression; WHERE requires non-null, non-blank text
         * @param from lower bound, possibly null
         * @param to upper bound, possibly null
         * @return the WHERE builder for fluent chaining
         * @throws IllegalArgumentException if column is null or blank
         */
        public WhereBuilder notBetween(String column, Object from, Object to) {
            return condition(column, QueryOperator.NOT_BETWEEN, from, to);
        }

        /**
         * Adds NOT IN with a placeholder per value. Null elements retain SQL three-valued logic.
         * @param column trusted column expression; WHERE requires non-null, non-blank text
         * @param values bound values in placeholder order; IN/NOT IN require a non-empty array
         * @return the WHERE builder for fluent chaining
         * @throws IllegalArgumentException if column is null or blank, or values are null or empty
         */
        public WhereBuilder notIn(String column, Object... values) {
            if (values == null || values.length == 0) throw new IllegalArgumentException("values required");
            return condition(column, QueryOperator.NOT_IN, values);
        }

        /**
         * Adds LIKE with percent wildcards on both sides. Existing wildcard characters are not escaped; null becomes the text null.
         * @param column trusted column expression; WHERE requires non-null, non-blank text
         * @param value bound JDBC value, possibly null
         * @return the WHERE builder for fluent chaining
         * @throws IllegalArgumentException if column is null or blank
         */
        public WhereBuilder like(String column, String value) {
            return condition(column, QueryOperator.LIKE, "%" + value + "%");
        }

        /**
         * Adds inclusive BETWEEN with bound lower and upper limits.
         * @param column trusted column expression; WHERE requires non-null, non-blank text
         * @param valueFrom lower bound, possibly null
         * @param valueTo upper bound, possibly null
         * @return the WHERE builder for fluent chaining
         * @throws IllegalArgumentException if column is null or blank
         */
        public WhereBuilder between(String column, Object valueFrom, Object valueTo) {
            return condition(column, QueryOperator.BETWEEN, valueFrom, valueTo);
        }

        /**
         * Adds IN with a placeholder per value. Pass individual values, not a collection as one operand.
         * @param column trusted column expression; WHERE requires non-null, non-blank text
         * @param values bound values in placeholder order; IN/NOT IN require a non-empty array
         * @return the WHERE builder for fluent chaining
         * @throws IllegalArgumentException if column is null or blank, or values are null or empty
         */
        public WhereBuilder in(String column, Object... values) {
            if (values == null || values.length == 0) throw new IllegalArgumentException("values required");
            return condition(column, QueryOperator.IN, values);
        }

        /**
         * Adds IS NULL without a bound parameter.
         * @param column trusted column expression; WHERE requires non-null, non-blank text
         * @return the WHERE builder for fluent chaining
         * @throws IllegalArgumentException if column is null or blank
         */
        public WhereBuilder isNull(String column) {
            return condition(column, QueryOperator.IS_NULL);
        }

        /**
         * Adds IS NOT NULL without a bound parameter.
         * @param column trusted column expression; WHERE requires non-null, non-blank text
         * @return the WHERE builder for fluent chaining
         * @throws IllegalArgumentException if column is null or blank
         */
        public WhereBuilder isNotNull(String column) {
            return condition(column, QueryOperator.IS_NOT_NULL);
        }

        /**
         * Appends AND only after a predicate; leading and repeated connectors are ignored. Add a following predicate.
         * @return the WHERE builder for fluent chaining
         */
        public WhereBuilder and() {
            if (lastConditionAdded) {
                parent.addCondition("AND");
                lastConditionAdded = false;
            }
            return this;
        }

        /**
         * Appends OR only after a predicate; leading and repeated connectors are ignored. No grouping is inserted; SQL precedence applies.
         * @return the WHERE builder for fluent chaining
         */
        public WhereBuilder or() {
            if (lastConditionAdded) {
                parent.addCondition("OR");
                lastConditionAdded = false;
            }
            return this;
        }

        /**
         * Returns the parent; conditions were already attached. Trailing connectors are not validated.
         * @return the owning query builder
         */
        public QueryBuilder done() { return parent; }
    }

    /**
     * Mutable JOIN draft. ON operands are trusted SQL expressions, never bound values.
     * Call done or endJoin once to attach the draft. The caller must supply valid
     * JOIN/ON syntax for the selected database (for example CROSS JOIN without ON).
     */
    public static class JoinBuilder {
        private final QueryBuilder parent;
        private final QueryJoinType type;
        private final String table;
        private final String alias;
        private final List<String> onConditions = new ArrayList<>();
        private boolean lastOnAdded = false;

        JoinBuilder(QueryBuilder parent, QueryJoinType type, String table, String alias) {
            this.parent = parent;
            this.type = type;
            this.table = table;
            this.alias = alias;
        }

        /**
         * Adds a trusted raw ON predicate through the legacy entry point.
         * @param condition trusted non-blank SQL expression without unbound placeholders
         * @return the JOIN builder for fluent chaining
         * @throws IllegalArgumentException if condition is null or blank
         */
        public JoinBuilder on(String condition) {
            return onRaw(condition);
        }

        /**
         * Adds trusted SQL only; never interpolate request values into this expression.
         * @param condition trusted non-blank SQL expression without unbound placeholders
         * @return the JOIN builder for fluent chaining
         * @throws IllegalArgumentException if condition is null or blank
         */
        public JoinBuilder onRaw(String condition) {
            if (condition == null || condition.isBlank()) {
                throw new IllegalArgumentException("condition must not be blank");
            }
            onConditions.add(condition);
            lastOnAdded = true;
            return this;
        }

        /**
         * Compares two trusted SQL expressions without binding either as a JDBC value.
         * @param left trusted non-blank left-hand SQL expression
         * @param op non-null operator supporting a single right-hand expression
         * @param right trusted non-blank right-hand SQL expression
         * @return the JOIN builder for fluent chaining
         * @throws IllegalArgumentException if expressions are blank or the typed operator is not binary
         * @throws NullPointerException if operator is null
         */
        public JoinBuilder on(String left, QueryOperator op, String right) {
            Objects.requireNonNull(op, "operator must not be null");
            if (!op.supportsColumnComparison()) {
                throw new IllegalArgumentException("Operator " + op.name() + " does not support column comparison");
            }
            if (left == null || left.isBlank() || right == null || right.isBlank()) {
                throw new IllegalArgumentException("join expressions must not be blank");
            }
            return onRaw(left + " " + op.getSql() + " " + right);
        }

        /**
         * Appends AND only after a predicate; leading and repeated connectors are ignored. Add a following predicate.
         * @return the JOIN builder for fluent chaining
         */
        public JoinBuilder and() {
            if (lastOnAdded) {
                onConditions.add("AND");
                lastOnAdded = false;
            }
            return this;
        }

        /**
         * Appends OR only after a predicate; leading and repeated connectors are ignored. No grouping is inserted; SQL precedence applies.
         * @return the JOIN builder for fluent chaining
         */
        public JoinBuilder or() {
            if (lastOnAdded) {
                onConditions.add("OR");
                lastOnAdded = false;
            }
            return this;
        }

        /**
         * Alias for done; attaches the JOIN and returns the parent.
         * @return the owning query builder
         */
        public QueryBuilder endJoin() {
            return done();
        }

        /**
         * Attaches the JOIN and returns the parent. Call once: repeated calls append duplicate JOINs.
         * An empty predicate list omits ON; join syntax and trailing connectors are not validated.
         * @return the owning query builder
         */
        public QueryBuilder done() {
            StringBuilder sb = new StringBuilder();
            sb.append(type.toString()).append(" ").append(table);
            if (alias != null && !alias.isBlank()) sb.append(" ").append(alias);
            if (!onConditions.isEmpty()) sb.append(" ON ").append(String.join(" ", onConditions));
            parent.joins.add(sb.toString());
            return parent;
        }
    }

    /**
     * Alternative to join(type, table, alias); complete the returned nested builder once.
     * @param type non-null JOIN type
     * @param table trusted table expression, optionally with an alias
     * @param alias optional alias; null or blank omits it
     * @return the JOIN builder for fluent chaining
     */
    public JoinBuilder joinBuilder(QueryJoinType type, String table, String alias) {
        return new JoinBuilder(this, type, table, alias);
    }

    private void selectTypeBuilder(StringBuilder sql) {
        selectTypeBuilder(sql, true, true);
    }

    private void selectTypeBuilder(StringBuilder sql, boolean includeOrderBy, boolean includePagination) {
        sql.append("SELECT ").append(columns.isEmpty() ? "*" : String.join(", ", columns))
                .append(" FROM ").append(table);
        if (!joins.isEmpty()) sql.append(" ").append(String.join(" ", joins));
        if (!whereConditions.isEmpty()) sql.append(WHERE).append(String.join(" ", whereConditions));
        if (!groupBy.isEmpty()) sql.append(" GROUP BY ").append(String.join(", ", groupBy));
        List<String> allOrderBy = new ArrayList<>(orderBy);
        allOrderBy.addAll(pageableOrderBy);
        if (includeOrderBy && !allOrderBy.isEmpty()) sql.append(" ORDER BY ").append(String.join(", ", allOrderBy));
        if (includePagination && limit != null) sql.append(" LIMIT ?");
        if (includePagination && offset != null) sql.append(" OFFSET ?");
    }

    private void appendPageableSort(Sort sort) {
        for (Sort.Order order : sort) {
            String property = order.getProperty();
            if (property == null || !property.matches(SAFE_SORT_EXPRESSION)) {
                throw new IllegalArgumentException("Unsafe sort property: " + property);
            }
            pageableOrderBy.add(property + " " + order.getDirection().name());
        }
    }

    private void insertTypeBuilder(StringBuilder sql) {
        if (!columns.isEmpty() && !insertValues.isEmpty() && columns.size() != insertValues.size())
            throw new IllegalStateException("Columns count and values count do not match.");
        sql.append("INSERT INTO ").append(table);
        if (!columns.isEmpty()) sql.append(" (").append(String.join(", ", columns)).append(")");
        sql.append(" VALUES (").append(String.join(", ", insertValues)).append(")");
    }

    private void updateTypeBuilder(StringBuilder sql) {
        sql.append("UPDATE ").append(table).append(" SET ").append(String.join(", ", setClauses));
        if (!whereConditions.isEmpty()) sql.append(WHERE).append(String.join(" ", whereConditions));
    }

    private void deleteTypeBuilder(StringBuilder sql) {
        sql.append("DELETE FROM ").append(table);
        if (!whereConditions.isEmpty()) sql.append(WHERE).append(String.join(" ", whereConditions));
    }
}
