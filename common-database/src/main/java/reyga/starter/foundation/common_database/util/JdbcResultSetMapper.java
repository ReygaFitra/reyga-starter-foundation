package reyga.starter.foundation.common_database.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JdbcResultSetMapper {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Nullable {
        public static String getString(ResultSet rs, String column) throws SQLException {
            return rs.getString(column);
        }

        public static int getInt(ResultSet rs, String column) throws SQLException {
            int value = rs.getInt(column);
            return rs.wasNull() ? 0 : value;
        }

        public static long getLong(ResultSet rs, String column) throws SQLException {
            long value = rs.getLong(column);
            return rs.wasNull() ? 0L : value;
        }

        public static boolean getBoolean(ResultSet rs, String column) throws SQLException {
            boolean value = rs.getBoolean(column);
            return !rs.wasNull() && value;
        }

        public static Timestamp getTimestamp(ResultSet rs, String column) throws SQLException {
            return rs.getTimestamp(column);
        }

        public static LocalDateTime getLocalDateTime(ResultSet rs, String column) throws SQLException {
            Timestamp ts = rs.getTimestamp(column);
            return ts != null ? ts.toLocalDateTime() : null;
        }
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Mandatory {
        public static String getString(ResultSet rs, String column) throws SQLException {
            return rs.getString(column);
        }

        public static int getInt(ResultSet rs, String column) throws SQLException {
            return rs.getInt(column);
        }

        public static long getLong(ResultSet rs, String column) throws SQLException {
            return rs.getLong(column);
        }

        public static boolean getBoolean(ResultSet rs, String column) throws SQLException {
            return rs.getBoolean(column);
        }

        public static Timestamp getTimestamp(ResultSet rs, String column) throws SQLException {
            return rs.getTimestamp(column);
        }

        public static LocalDateTime getLocalDateTime(ResultSet rs, String column) throws SQLException {
            return rs.getTimestamp(column).toLocalDateTime();
        }
    }
}
