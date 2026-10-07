package reyga.starter.foundation.common_database.util;

import java.util.List;

import org.junit.jupiter.api.Test;

import reyga.starter.foundation.common_database.enumeration.QueryJoinType;
import reyga.starter.foundation.common_database.enumeration.QueryOperator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class QueryBuilderAliasTest {
    @Test
    void should_PreserveSqlAndBindings_When_AliasesAreNotUsed() {
        QueryBuilder query = QueryBuilder.builder().select("id", "username")
                .from("app_user").where().equals("status", "ACTIVE").done()
                .orderBy("id ASC").paginate(2, 10);

        assertEquals("SELECT id, username FROM app_user WHERE status = ? ORDER BY id ASC LIMIT ? OFFSET ?", query.build());
        assertEquals(List.of("ACTIVE", 10, 10), query.getParameters());
        assertEquals("SELECT COUNT(*) FROM (SELECT id, username FROM app_user WHERE status = ?) query_count", query.buildCount());
        assertEquals(List.of("ACTIVE"), query.getCountParameters());
        assertEquals("SELECT * FROM app_user", QueryBuilder.builder().select().from("app_user").build());
    }

    @Test
    void should_PreserveLegacyAliases_When_AsIsNotUsed() {
        QueryBuilder query = QueryBuilder.builder()
                .select("u.id AS user_id", "r.name AS role_name").from("app_user u")
                .join(QueryJoinType.LEFT, "roles", "r")
                .on("u.role_id", QueryOperator.EQUALS, "r.id").done()
                .join("profiles p").on("p.user_id", QueryOperator.EQUALS, "u.id").done()
                .where().equals("u.status", "ACTIVE").done();

        assertEquals("SELECT u.id AS user_id, r.name AS role_name FROM app_user u LEFT JOIN roles r ON u.role_id = r.id INNER JOIN profiles p ON p.user_id = u.id WHERE u.status = ?", query.build());
        assertEquals(List.of("ACTIVE"), query.getParameters());
    }

    @Test
    void should_ProduceEquivalentQueries_When_LegacyAndFluentAliasesAreMixed() {
        QueryBuilder legacy = QueryBuilder.builder().select("u.id AS user_id", "r.name")
                .from("app_user u").join("roles", "r")
                .on("u.role_id", QueryOperator.EQUALS, "r.id").done()
                .where().equals("u.status", "ACTIVE").done().limit(5);
        QueryBuilder mixed = QueryBuilder.builder().select("u.id").as("user_id").select("r.name")
                .from("app_user u").join("roles").as("r")
                .on("u.role_id", QueryOperator.EQUALS, "r.id").done()
                .where().equals("u.status", "ACTIVE").done().limit(5);

        assertEquals(legacy.build(), mixed.build());
        assertEquals(legacy.getParameters(), mixed.getParameters());
        assertEquals(legacy.buildCount(), mixed.buildCount());
        assertEquals(legacy.getCountParameters(), mixed.getCountParameters());
    }

    @Test
    void should_PreserveWriteStatements_When_AliasesAreNotUsed() {
        QueryBuilder insert = QueryBuilder.builder().insertInto("users", "id", "name").values(1, "Rey");
        QueryBuilder update = QueryBuilder.builder().update("users").set("name", "Rey")
                .where().equals("id", 1).done();
        QueryBuilder delete = QueryBuilder.builder().deleteFrom("users").where().equals("id", 1).done();

        assertEquals("INSERT INTO users (id, name) VALUES (?, ?)", insert.build());
        assertEquals(List.of(1, "Rey"), insert.getParameters());
        assertEquals("UPDATE users SET name = ? WHERE id = ?", update.build());
        assertEquals(List.of("Rey", 1), update.getParameters());
        assertEquals("DELETE FROM users WHERE id = ?", delete.build());
        assertEquals(List.of(1), delete.getParameters());
    }

    @Test
    void should_AliasProjectionsAndSources_When_AsIsChained() {
        QueryBuilder query = QueryBuilder.builder()
                .select("u.id").as("user_id")
                .select("u.username").as("login_name")
                .from("app_user").as("u")
                .join(QueryJoinType.LEFT, "user_role").as("r")
                .on("u.id", QueryOperator.EQUALS, "r.user_id").done()
                .where().equals("u.status", "ACTIVE").done().limit(10);

        assertEquals("SELECT u.id AS user_id, u.username AS login_name FROM app_user u LEFT JOIN user_role r ON u.id = r.user_id WHERE u.status = ? LIMIT ?", query.build());
        assertEquals(List.of("ACTIVE", 10), query.getParameters());
        assertEquals("SELECT COUNT(*) FROM (SELECT u.id AS user_id, u.username AS login_name FROM app_user u LEFT JOIN user_role r ON u.id = r.user_id WHERE u.status = ?) query_count", query.buildCount());
        assertEquals(List.of("ACTIVE"), query.getCountParameters());
    }

    @Test
    void should_AliasOnlyFinalProjection_When_SelectHasMultipleColumns() {
        QueryBuilder query = QueryBuilder.builder().select("id", "COUNT(*)").as("total")
                .from("users").groupBy("id");
        assertEquals("SELECT id, COUNT(*) AS total FROM users GROUP BY id", query.build());
    }

    @Test
    void should_PreserveCorrelatedBindings_When_SubqueryUsesAliases() {
        QueryBuilder child = QueryBuilder.builder().select("o.user_id").as("customer_id")
                .from("orders").as("o").where()
                .compareColumns("o.user_id", QueryOperator.EQUALS, "u.id").and()
                .equals("o.status", "PAID").done();
        QueryBuilder query = QueryBuilder.builder().select("u.id").as("user_id")
                .from("users").as("u").where().exists(child).done();
        assertEquals("SELECT u.id AS user_id FROM users u WHERE EXISTS (SELECT o.user_id AS customer_id FROM orders o WHERE o.user_id = u.id AND o.status = ?)", query.build());
        assertEquals(List.of("PAID"), query.getParameters());
    }

    @Test
    void should_RejectInvalidAliasWithoutMutation_When_AliasIsUnsafe() {
        QueryBuilder query = QueryBuilder.builder().select("id");
        for (String alias : new String[]{null, "", " ", "u.id", "user id", "x; DROP TABLE users", "1alias"}) {
            IllegalArgumentException failure = assertThrows(IllegalArgumentException.class, () -> query.as(alias));
            assertEquals("alias must be a simple SQL identifier", failure.getMessage());
        }
        query.as("user_id").from("users");
        assertEquals("SELECT id AS user_id FROM users", query.build());
    }

    @Test
    void should_RejectMissingAndRepeatedTargets_When_AsIsMisplaced() {
        assertThrows(IllegalStateException.class, () -> QueryBuilder.builder().as("u"));
        assertThrows(IllegalStateException.class, () -> QueryBuilder.builder().select().as("u"));
        assertThrows(IllegalStateException.class, () -> QueryBuilder.builder().update("users").as("u"));
        assertThrows(IllegalStateException.class, () -> QueryBuilder.builder().select("id").from("users").where().done().as("u"));
        assertThrows(IllegalStateException.class, () -> QueryBuilder.builder().select("id").from(" ").as("u"));
        QueryBuilder query = QueryBuilder.builder().select("id").as("user_id");
        assertThrows(IllegalStateException.class, () -> query.as("other"));
        query.from("users").as("u");
        assertThrows(IllegalStateException.class, () -> query.as("other"));
        assertEquals("SELECT id AS user_id FROM users u", query.build());
        QueryBuilder.JoinBuilder join = query.join("roles", "r");
        assertThrows(IllegalStateException.class, () -> join.as("other"));
        QueryBuilder.JoinBuilder unaliased = query.join("roles");
        assertThrows(IllegalArgumentException.class, () -> unaliased.as("bad alias"));
        unaliased.as("r");
        assertThrows(IllegalStateException.class, () -> unaliased.as("other"));
    }
}
