package reyga.starter.foundation.common_database.util;

import org.junit.jupiter.api.Test;
import reyga.starter.foundation.common_database.enumeration.QueryJoinType;

import static org.junit.jupiter.api.Assertions.*;

class QueryBuilderTest {

    @Test
    void build_selectWithWhereAndJoin() {
        QueryBuilder builder = new QueryBuilder()
                .select("a", "b")
                .from("table t")
                .join(QueryJoinType.LEFT, "other o").on("t.id", "=", "o.id").done()
                .where().equals("t.id", 1).done()
                .orderBy("a");

        String sql = builder.build();
        assertTrue(sql.contains("SELECT a, b FROM table t"));
        assertTrue(sql.contains("LEFT other o ON t.id = o.id"));
        assertTrue(sql.contains("WHERE t.id = ?"));
        assertTrue(sql.contains("ORDER BY a"));
        assertEquals(1, builder.getParameters().size());
    }

    @Test
    void build_insertThrowsOnMismatchedColumnsAndValues() {
        QueryBuilder builder = new QueryBuilder()
                .insertInto("table", "a", "b")
                .values(1);

        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void build_updateAndDelete() {
        QueryBuilder update = new QueryBuilder()
                .update("table")
                .set("a", 1)
                .where().equals("id", 1).done();
        assertTrue(update.build().startsWith("UPDATE table SET a = ?"));

        QueryBuilder delete = new QueryBuilder()
                .deleteFrom("table")
                .where().equals("id", 1).done();
        assertTrue(delete.build().startsWith("DELETE FROM table"));
    }

    @Test
    void whereIn_throwsWhenNoValues() {
        QueryBuilder builder = new QueryBuilder().select("*").from("table");
        assertThrows(IllegalArgumentException.class, () -> builder.where().in("id"));
    }
}
