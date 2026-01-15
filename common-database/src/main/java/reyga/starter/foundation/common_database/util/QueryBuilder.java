package reyga.starter.foundation.common_database.util;

import reyga.starter.foundation.common_database.enumeration.QueryJoinType;
import reyga.starter.foundation.common_database.enumeration.QueryType;

import java.util.*;

public class QueryBuilder {
    private QueryType type;
    private String table;
    private final List<String> columns = new ArrayList<>();
    private final List<String> joins = new ArrayList<>();
    private final List<String> groupBy = new ArrayList<>();
    private final List<String> orderBy = new ArrayList<>();
    private String limit;
    private String offset;

    private final List<String> whereConditions = new ArrayList<>();
    private final List<String> insertValues = new ArrayList<>();
    private final List<String> setClauses = new ArrayList<>();

    private final List<Object> parameters = new LinkedList<>();

    public QueryBuilder select(String... cols) {
        this.type = QueryType.SELECT;
        columns.addAll(Arrays.asList(cols));
        return this;
    }

    public QueryBuilder from(String table) {
        this.table = table;
        return this;
    }

    public QueryBuilder joinOn(String table, String leftColumn, String op, String rightColumn) {
        return join(table).on(leftColumn, op, rightColumn).done();
    }

    public JoinBuilder join(String table, String alias) {
        return new JoinBuilder(this, QueryJoinType.INNER, table, alias);
    }

    public JoinBuilder join(String table) {
        return new JoinBuilder(this, QueryJoinType.INNER, table, null);
    }

    public JoinBuilder join(QueryJoinType type, String table) {
        return new JoinBuilder(this, type, table, null);
    }

    public JoinBuilder join(QueryJoinType type, String table, String alias) {
        return new JoinBuilder(this, type, table, alias);
    }

    public QueryBuilder orderBy(String... cols) {
        orderBy.addAll(Arrays.asList(cols));
        return this;
    }

    public QueryBuilder groupBy(String... cols) {
        groupBy.addAll(Arrays.asList(cols));
        return this;
    }

    public QueryBuilder limit(int limit) {
        this.limit = "?";
        this.parameters.add(limit);
        return this;
    }

    public QueryBuilder offset(int offset) {
        this.offset = "?";
        this.parameters.add(offset);
        return this;
    }

    public QueryBuilder paginate(int page, int pageSize) {
        this.limit = "?";
        this.offset = "?";
        this.parameters.add(pageSize);
        this.parameters.add((page - 1) * pageSize);
        return this;
    }

    public QueryBuilder insertInto(String table, String... cols) {
        this.type = QueryType.INSERT;
        this.table = table;
        if (cols != null && cols.length > 0) this.columns.addAll(Arrays.asList(cols));
        return this;
    }

    public QueryBuilder values(Object... vals) {
        if (vals != null) {
            for (Object v : vals) {
                this.insertValues.add("?");
                this.parameters.add(v);
            }
        }
        return this;
    }

    public QueryBuilder update(String table) {
        this.type = QueryType.UPDATE;
        this.table = table;
        return this;
    }

    public QueryBuilder set(String column, Object value) {
        this.setClauses.add(column + " = ?");
        this.parameters.add(value);
        return this;
    }

    public QueryBuilder deleteFrom(String table) {
        this.type = QueryType.DELETE;
        this.table = table;
        return this;
    }

    public List<Object> getParameters() {
        return this.parameters;
    }

    public WhereBuilder where() {
        return new WhereBuilder(this);
    }

    private void addCondition(String condition) {
        this.whereConditions.add(condition);
    }

    public String build() {
        StringBuilder sql = new StringBuilder();

        switch (type) {
            case SELECT:
                sql.append("SELECT ").append(columns.isEmpty() ? "*" : String.join(", ", columns))
                        .append(" FROM ").append(table);
                if (!joins.isEmpty()) sql.append(" ").append(String.join(" ", joins));
                if (!whereConditions.isEmpty()) sql.append(" WHERE ").append(String.join(" ", whereConditions));
                if (!groupBy.isEmpty()) sql.append(" GROUP BY ").append(String.join(", ", groupBy));
                if (!orderBy.isEmpty()) sql.append(" ORDER BY ").append(String.join(", ", orderBy));
                if (limit != null) sql.append(" LIMIT ").append(limit);
                if (offset != null) sql.append(" OFFSET ").append(offset);
                break;

            case INSERT:
                if (!columns.isEmpty() && !insertValues.isEmpty() && columns.size() != insertValues.size())
                    throw new IllegalStateException("Columns count and values count do not match.");
                sql.append("INSERT INTO ").append(table);
                if (!columns.isEmpty()) sql.append(" (").append(String.join(", ", columns)).append(")");
                sql.append(" VALUES (").append(String.join(", ", insertValues)).append(")");
                break;

            case UPDATE:
                sql.append("UPDATE ").append(table).append(" SET ").append(String.join(", ", setClauses));
                if (!whereConditions.isEmpty()) sql.append(" WHERE ").append(String.join(" ", whereConditions));
                break;

            case DELETE:
                sql.append("DELETE FROM ").append(table);
                if (!whereConditions.isEmpty()) sql.append(" WHERE ").append(String.join(" ", whereConditions));
                break;
        }

        return sql.toString().trim();
    }

    private String formatValue(Object v) { return "?"; }

    public static class WhereBuilder {
        private final QueryBuilder parent;
        private boolean lastConditionAdded = false;

        public WhereBuilder(QueryBuilder parent) { this.parent = parent; }

        public WhereBuilder equals(String column, Object value) {
            parent.addCondition(column + " = ?");
            parent.parameters.add(value);
            lastConditionAdded = true;
            return this;
        }

        public WhereBuilder notEquals(String column, Object value) {
            parent.addCondition(column + " <> ?");
            parent.parameters.add(value);
            lastConditionAdded = true;
            return this;
        }

        public WhereBuilder greaterThan(String column, Object value) {
            parent.addCondition(column + " > ?");
            parent.parameters.add(value);
            lastConditionAdded = true;
            return this;
        }

        public WhereBuilder lessThan(String column, Object value) {
            parent.addCondition(column + " < ?");
            parent.parameters.add(value);
            lastConditionAdded = true;
            return this;
        }

        public WhereBuilder like(String column, String value) {
            parent.addCondition(column + " LIKE ?");
            parent.parameters.add("%" + value + "%");
            lastConditionAdded = true;
            return this;
        }

        public WhereBuilder between(String column, Object valueFrom, Object valueTo) {
            parent.addCondition(column + " BETWEEN ? AND ?");
            parent.parameters.add(valueFrom);
            parent.parameters.add(valueTo);
            lastConditionAdded = true;
            return this;
        }

        public WhereBuilder in(String column, Object... values) {
            if (values == null || values.length == 0) throw new IllegalArgumentException("values required");
            String placeholders = String.join(", ", Collections.nCopies(values.length, "?"));
            parent.addCondition(column + " IN (" + placeholders + ")");
            parent.parameters.addAll(Arrays.asList(values));
            lastConditionAdded = true;
            return this;
        }

        public WhereBuilder isNull(String column) {
            parent.addCondition(column + " IS NULL");
            lastConditionAdded = true;
            return this;
        }

        public WhereBuilder isNotNull(String column) {
            parent.addCondition(column + " IS NOT NULL");
            lastConditionAdded = true;
            return this;
        }

        public WhereBuilder and() {
            if (lastConditionAdded) {
                parent.addCondition("AND");
                lastConditionAdded = false;
            }
            return this;
        }

        public WhereBuilder or() {
            if (lastConditionAdded) {
                parent.addCondition("OR");
                lastConditionAdded = false;
            }
            return this;
        }

        public QueryBuilder done() { return parent; }
    }

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

        public JoinBuilder on(String condition) {
            onConditions.add(condition);
            lastOnAdded = true;
            return this;
        }

        public JoinBuilder on(String left, String op, String right) {
            onConditions.add(left + " " + op + " " + right);
            lastOnAdded = true;
            return this;
        }

        public JoinBuilder and() {
            if (lastOnAdded) {
                onConditions.add("AND");
                lastOnAdded = false;
            }
            return this;
        }

        public JoinBuilder or() {
            if (lastOnAdded) {
                onConditions.add("OR");
                lastOnAdded = false;
            }
            return this;
        }

        public QueryBuilder endJoin() {
            return done();
        }

        public QueryBuilder done() {
            StringBuilder sb = new StringBuilder();
            sb.append(type.toString()).append(" ").append(table);
            if (alias != null && !alias.isBlank()) sb.append(" ").append(alias);
            if (!onConditions.isEmpty()) sb.append(" ON ").append(String.join(" ", onConditions));
            parent.joins.add(sb.toString());
            return parent;
        }
    }

    public JoinBuilder joinBuilder(QueryJoinType type, String table, String alias) {
        return new JoinBuilder(this, type, table, alias);
    }
}
