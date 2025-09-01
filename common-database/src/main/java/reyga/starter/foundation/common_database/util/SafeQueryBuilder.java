package reyga.starter.foundation.common_database.util;

import reyga.starter.foundation.common_database.enumeration.QueryType;

import java.util.*;

public class SafeQueryBuilder {
    private QueryType type;
    private String table;
    private List<String> columns = new ArrayList<>();
    private List<String> joins = new ArrayList<>();
    private List<String> groupBy = new ArrayList<>();
    private List<String> orderBy = new ArrayList<>();
    private String limit;
    private String offset;

    private List<String> whereConditions = new ArrayList<>();
    private final List<List<Object>> insertValues = new ArrayList<>();
    private final List<String> setClauses = new ArrayList<>();

    private final List<Object> parameters = new LinkedList<>();

    public SafeQueryBuilder select(String... cols) {
        this.type = QueryType.SELECT;
        Collections.addAll(columns, cols);
        return this;
    }

    public SafeQueryBuilder from(String table) {
        this.table = table;
        return this;
    }

    public SafeQueryBuilder join(String joinClause) {
        this.joins.add(joinClause);
        return this;
    }

    public WhereBuilder where() {
        return new WhereBuilder(this);
    }

    public SafeQueryBuilder orderBy(String... cols) {
        Collections.addAll(orderBy, cols);
        return this;
    }

    public SafeQueryBuilder groupBy(String... cols) {
        Collections.addAll(groupBy, cols);
        return this;
    }

    public SafeQueryBuilder limit(int limit) {
        this.limit = "?";
        this.parameters.add(limit);
        return this;
    }

    public SafeQueryBuilder offset(int offset) {
        this.offset = "?";
        this.parameters.add(offset);
        return this;
    }

    public SafeQueryBuilder paginate(int page, int pageSize) {
        this.limit = "?";
        this.offset = "?";
        this.parameters.add(pageSize);
        this.parameters.add((page - 1) * pageSize);
        return this;
    }

    public SafeQueryBuilder insertInto(String table, String... cols) {
        this.type = QueryType.INSERT;
        this.table = table;
        if (cols != null && cols.length > 0) {
            this.columns.addAll(Arrays.asList(cols));
        }
        return this;
    }

    public SafeQueryBuilder values(Object... vals) {
        if (vals != null) {
            List<Object> row = new ArrayList<>();
            row.addAll(Arrays.asList(vals));
            this.insertValues.add(row);
            this.parameters.addAll(row);
        }
        return this;
    }

    public SafeQueryBuilder update(String table) {
        this.type = QueryType.UPDATE;
        this.table = table;
        return this;
    }

    public SafeQueryBuilder set(String column, Object value) {
        this.setClauses.add(column + " = ?");
        this.parameters.add(value);
        return this;
    }

    public SafeQueryBuilder deleteFrom(String table) {
        this.type = QueryType.DELETE;
        this.table = table;
        return this;
    }

    public List<Object> getParameters() {
        return this.parameters;
    }

    public String build() {
        StringBuilder sql = new StringBuilder();

        switch (type) {
            case SELECT:
                sql.append("SELECT ")
                        .append(columns.isEmpty() ? "*" : String.join(", ", columns))
                        .append(" FROM ").append(table);

                if (!joins.isEmpty()) {
                    sql.append(" ").append(String.join(" ", joins));
                }
                if (!whereConditions.isEmpty()) {
                    sql.append(" WHERE ").append(String.join(" ", whereConditions));
                }
                if (!groupBy.isEmpty()) {
                    sql.append(" GROUP BY ").append(String.join(", ", groupBy));
                }
                if (!orderBy.isEmpty()) {
                    sql.append(" ORDER BY ").append(String.join(", ", orderBy));
                }
                if (limit != null) {
                    sql.append(" LIMIT ").append(limit);
                }
                if (offset != null) {
                    sql.append(" OFFSET ").append(offset);
                }
                break;

            case INSERT:
                if (!columns.isEmpty() && !insertValues.isEmpty() && columns.size() != insertValues.get(0).size()) {
                    throw new IllegalStateException("Columns count and values count do not match.");
                }
                sql.append("INSERT INTO ").append(table);
                if (!columns.isEmpty()) {
                    sql.append(" (").append(String.join(", ", columns)).append(")");
                }
                sql.append(" VALUES ");
                List<String> rows = new ArrayList<>();
                for (List<Object> row : insertValues) {
                    rows.add("(" + String.join(", ", Collections.nCopies(row.size(), "?")) + ")");
                }
                sql.append(String.join(", ", rows));
                break;

            case UPDATE:
                sql.append("UPDATE ").append(table)
                        .append(" SET ").append(String.join(", ", setClauses));
                if (!whereConditions.isEmpty()) {
                    sql.append(" WHERE ").append(String.join(" ", whereConditions));
                }
                break;

            case DELETE:
                sql.append("DELETE FROM ").append(table);
                if (!whereConditions.isEmpty()) {
                    sql.append(" WHERE ").append(String.join(" ", whereConditions));
                }
                break;
        }

        return sql.toString().trim();
    }

    private SafeQueryBuilder addCondition(String condition, Object... values) {
        this.whereConditions.add(condition);
        if (values != null) {
            this.parameters.addAll(Arrays.asList(values));
        }
        return this;
    }

    public static class WhereBuilder {
        private final SafeQueryBuilder parent;
        private boolean lastConditionAdded = false;

        public WhereBuilder(SafeQueryBuilder parent) {
            this.parent = parent;
        }

        public WhereBuilder equals(String column, Object value) {
            return addCondition(column + " = ?", value);
        }

        public WhereBuilder notEquals(String column, Object value) {
            return addCondition(column + " <> ?", value);
        }

        public WhereBuilder greaterThan(String column, Object value) {
            return addCondition(column + " > ?", value);
        }

        public WhereBuilder lessThan(String column, Object value) {
            return addCondition(column + " < ?", value);
        }

        public WhereBuilder like(String column, String value) {
            return addCondition(column + " LIKE ?", "%" + value + "%");
        }

        public WhereBuilder between(String column, Object value) {
            parent.addCondition(column + " BETWEEN ?", value);
            lastConditionAdded = true;
            return this;
        }

        public WhereBuilder between(String column, Object start, Object end) {
            parent.addCondition(column + " BETWEEN ? AND ?", start, end);
            lastConditionAdded = true;
            return this;
        }

        public WhereBuilder in(String column, Object... values) {
            String placeholders = String.join(", ", Collections.nCopies(values.length, "?"));
            parent.addCondition(column + " IN (" + placeholders + ")", values);
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

        private WhereBuilder addCondition(String condition, Object... values) {
            parent.addCondition(condition, values);
            lastConditionAdded = true;
            return this;
        }

        public WhereBuilder and() {
            if (lastConditionAdded) {
                parent.whereConditions.add("AND");
                lastConditionAdded = false;
            }
            return this;
        }

        public WhereBuilder or() {
            if (lastConditionAdded) {
                parent.whereConditions.add("OR");
                lastConditionAdded = false;
            }
            return this;
        }

        public SafeQueryBuilder done() {
            return parent;
        }
    }
}

