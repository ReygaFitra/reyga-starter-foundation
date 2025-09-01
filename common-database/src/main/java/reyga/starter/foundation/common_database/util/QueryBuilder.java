package reyga.starter.foundation.common_database.util;

import reyga.starter.foundation.common_database.enumeration.QueryType;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import java.util.Arrays;

public class QueryBuilder {
    private QueryType type;
    private String table;
    private List<String> columns = new ArrayList<>();
    private List<String> joins = new ArrayList<>();
    private List<String> groupBy = new ArrayList<>();
    private List<String> orderBy = new ArrayList<>();
    private String limit;
    private String offset;

    private List<String> whereConditions = new ArrayList<>();
    private final List<String> insertValues = new ArrayList<>();
    private final List<String> setClauses = new ArrayList<>();

    private final List<Object> parameters = new LinkedList<>();

    public QueryBuilder select(String... cols) {
        this.type = QueryType.SELECT;
        for (String col : cols) columns.add(col);
        return this;
    }

    public QueryBuilder from(String table) {
        this.table = table;
        return this;
    }

    public QueryBuilder join(String joinClause) {
        this.joins.add(joinClause);
        return this;
    }

    public WhereBuilder where() {
        return new WhereBuilder(this);
    }

    public QueryBuilder orderBy(String... cols) {
        for (String col : cols) orderBy.add(col);
        return this;
    }

    public QueryBuilder groupBy(String... cols) {
        for (String col : cols) groupBy.add(col);
        return this;
    }

    public QueryBuilder limit(int limit) {
        this.limit = String.valueOf(limit);
        this.parameters.add(limit);
        return this;
    }

    public QueryBuilder offset(int offset) {
        this.offset = String.valueOf(offset);
        this.parameters.add(offset);
        return this;
    }

    public QueryBuilder paginate(int page, int pageSize) {
        this.limit = String.valueOf(pageSize);
        this.offset = String.valueOf((page - 1) * pageSize);
        this.parameters.add(pageSize);
        this.parameters.add((page - 1) * pageSize);
        return this;
    }

    public QueryBuilder insertInto(String table, String... cols) {
        this.type = QueryType.INSERT;
        this.table = table;
        if (cols != null && cols.length > 0) {
            this.columns.addAll(Arrays.asList(cols));
        }
        return this;
    }

    public QueryBuilder values(Object... vals) {
        if (vals != null) {
            for (Object v : vals) {
                this.insertValues.add(formatValue(v));
                this.parameters.add(formatValue(v));
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
        this.setClauses.add(column + " = " + formatValue(value));
        this.parameters.add(formatValue(value));
        return this;
    }

    public QueryBuilder deleteFrom(String table) {
        this.type = QueryType.DELETE;
        this.table = table;
        return this;
    }

    public QueryBuilder toDate(String dateString, String format) {
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
                if (!columns.isEmpty() && !insertValues.isEmpty() && columns.size() != insertValues.size()) {
                    throw new IllegalStateException("Columns count and values count do not match.");
                }
                sql.append("INSERT INTO ").append(table);
                if (!columns.isEmpty()) {
                    sql.append(" (").append(String.join(", ", columns)).append(")");
                }
                sql.append(" VALUES (").append(String.join(", ", insertValues)).append(")");
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

    private QueryBuilder addCondition(String condition) {
        this.whereConditions.add(condition);
        return this;
    }

    private String formatValue(Object v) {
        if (v == null) return "NULL";
        if (v instanceof String || v instanceof Character) {
            String s = v.toString().replace("'", "''");
            return "'" + s + "'";
        }
        return v.toString();
    }

    public static class WhereBuilder {
        private final QueryBuilder parent;
        private boolean lastConditionAdded = false;

        public WhereBuilder(QueryBuilder parent) {
            this.parent = parent;
        }

        public WhereBuilder equals(String column, Object value) {
            return addCondition(column, "=", value);
        }

        public WhereBuilder notEquals(String column, Object value) {
            return addCondition(column, "<>", value);
        }

        public WhereBuilder greaterThan(String column, Object value) {
            return addCondition(column, ">", value);
        }

        public WhereBuilder lessThan(String column, Object value) {
            return addCondition(column, "<", value);
        }

        public WhereBuilder like(String column, String value) {
            return addCondition(column, "LIKE", "%" + value + "%");
        }

        public WhereBuilder between(String column, Object value) {
            return addCondition(column, "BETWEEN", value);
        }

        public WhereBuilder in(String column, Object... values) {
            StringBuilder sb = new StringBuilder();
            sb.append(column).append(" IN (");
            List<String> valList = new ArrayList<>();
            for (Object v : values) {
                if (v == null) {
                    valList.add("NULL");
                } else if (v instanceof String || v instanceof Character) {
                    valList.add("'" + v.toString().replace("'", "''") + "'");
                } else {
                    valList.add(v.toString());
                }
            }
            sb.append(String.join(", ", valList)).append(")");
            parent.addCondition(sb.toString());
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

        private WhereBuilder addCondition(String column, String operator, Object value) {
            String formatted = (value == null)
                    ? "NULL"
                    : (value instanceof String || value instanceof Character)
                    ? "'" + value.toString().replace("'", "''") + "'"
                    : value.toString();
            parent.addCondition(column + " " + operator + " " + formatted);
            parent.parameters.add(formatted);
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

        public QueryBuilder done() {
            return parent;
        }
    }
}
