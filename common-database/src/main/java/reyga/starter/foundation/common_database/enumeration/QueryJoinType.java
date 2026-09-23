package reyga.starter.foundation.common_database.enumeration;

public enum QueryJoinType {
    INNER("INNER JOIN"),
    LEFT("LEFT JOIN"),
    RIGHT("RIGHT JOIN"),
    FULL("FULL JOIN"),
    CROSS("CROSS JOIN");

    private final String sql;

    QueryJoinType(String sql) { this.sql = sql; }
    @Override public String toString() { return sql; }
}
