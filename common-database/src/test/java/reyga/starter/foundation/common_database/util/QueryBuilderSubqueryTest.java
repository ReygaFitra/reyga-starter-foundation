package reyga.starter.foundation.common_database.util;

import java.util.List;

import org.junit.jupiter.api.Test;

import reyga.starter.foundation.common_database.enumeration.QueryOperator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class QueryBuilderSubqueryTest {
    @Test
    void should_PreserveNestedBindingsAndSnapshot_When_ChildChangesAfterAttachment() {
        QueryBuilder child = QueryBuilder.builder().select("user_id").from("orders")
                .where().equals("status", "PAID").done().limit(3);
        QueryBuilder query = QueryBuilder.builder().select("id").from("users").limit(10)
                .where().equals("active", true).and()
                .conditionSubquery("id", QueryOperator.IN, child).and().equals("tenant_id", 7).done();
        child.where().and().equals("status", "NEW").done().limit(8);

        assertEquals("SELECT id FROM users WHERE active = ? AND id IN (SELECT user_id FROM orders WHERE status = ? LIMIT ?) AND tenant_id = ? LIMIT ?", query.build());
        assertEquals(List.of(true, "PAID", 3, 7, 10), query.getParameters());
        assertEquals(List.of(true, "PAID", 3, 7), query.getCountParameters());
        assertEquals("SELECT COUNT(*) FROM (SELECT id FROM users WHERE active = ? AND id IN (SELECT user_id FROM orders WHERE status = ? LIMIT ?) AND tenant_id = ?) query_count", query.buildCount());
    }

    @Test
    void should_BuildCorrelatedAndNestedPredicates_When_ExistsIsUsed() {
        QueryBuilder child = QueryBuilder.builder().select("1").from("orders").as("o")
                .where().compareColumns("o.user_id", QueryOperator.EQUALS, "u.id")
                .and().equals("o.status", "PAID").done();
        QueryBuilder nested = QueryBuilder.builder().select("1").from("users").as("u")
                .where().notExists(child).done();
        QueryBuilder query = QueryBuilder.builder().deleteFrom("audit")
                .where().exists(nested).done();
        assertEquals("DELETE FROM audit WHERE EXISTS (SELECT 1 FROM users u WHERE NOT EXISTS (SELECT 1 FROM orders o WHERE o.user_id = u.id AND o.status = ?))", query.build());
        assertEquals(List.of("PAID"), query.getParameters());
    }

    @Test
    void should_RenderSupportedOperators_When_SubqueryPredicateIsAttached() {
        for (QueryOperator operator : List.of(QueryOperator.IN, QueryOperator.NOT_IN, QueryOperator.EQUALS, QueryOperator.GREATER_THAN)) {
            QueryBuilder child = QueryBuilder.builder().select("MAX(score)").from("results")
                    .where().equals("category", "A").done();
            QueryBuilder query = QueryBuilder.builder().select("id").from("users")
                    .where().conditionSubquery("score", operator, child).done();
            assertEquals("SELECT id FROM users WHERE score " + operator.getSql()
                    + " (SELECT MAX(score) FROM results WHERE category = ?)", query.build());
            assertEquals(List.of("A"), query.getParameters());
        }
    }

    @Test
    void should_RejectInvalidInputWithoutMutation_When_SubqueryIsInvalid() {
        QueryBuilder query = QueryBuilder.builder().select("id").from("users");
        QueryBuilder child = QueryBuilder.builder().select("id").from("orders");
        QueryBuilder.WhereBuilder where = query.where();
        assertThrows(IllegalArgumentException.class, () -> where.exists(query));
        assertThrows(IllegalArgumentException.class, () -> where.exists(QueryBuilder.builder().deleteFrom("orders")));
        assertThrows(NullPointerException.class, () -> where.exists(null));
        assertThrows(IllegalArgumentException.class, () -> where.conditionSubquery("id", QueryOperator.BETWEEN, child));
        assertThrows(IllegalArgumentException.class, () -> where.conditionSubquery(" ", QueryOperator.IN, child));
        assertThrows(IllegalArgumentException.class, () -> where.compareColumns("id", QueryOperator.IN, "o.id"));
        assertEquals("SELECT id FROM users", query.build());
        assertEquals(List.of(), query.getParameters());
    }
}
