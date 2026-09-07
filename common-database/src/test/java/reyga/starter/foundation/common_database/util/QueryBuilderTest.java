package reyga.starter.foundation.common_database.util;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import reyga.starter.foundation.common_database.enumeration.QueryJoinType;
import reyga.starter.foundation.common_database.enumeration.QueryOperator;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QueryBuilderTest {

    @Test
    void should_BuildSelectWithAllConditions_When_FluentWhereMethodsAreUsed() {
        // Given
        QueryBuilder builder = QueryBuilder.builder()
                .select("id", "name")
                .from("users");

        // When
        String sql = builder
                .where()
                    .equals("status", "ACTIVE").and()
                    .notEquals("type", "SYSTEM").or()
                    .greaterThan("score", 80).and()
                    .lessThan("age", 65).and()
                    .like("name", "rey").and()
                    .between("created_at", 1L, 2L).and()
                    .in("id", 10L, 20L).and()
                    .isNull("deleted_at").or()
                    .isNotNull("verified_at")
                .done()
                .build();

        // Then
        assertEquals("SELECT id, name FROM users WHERE status = ? AND type <> ? OR score > ? AND age < ? AND name LIKE ? AND created_at BETWEEN ? AND ? AND id IN (?, ?) AND deleted_at IS NULL OR verified_at IS NOT NULL", sql);
        assertEquals(List.of("ACTIVE", "SYSTEM", 80, 65, "%rey%", 1L, 2L, 10L, 20L), builder.getParameters());
        assertEquals(sql, builder.getSql());
    }

    @Test
    void should_IgnoreLeadingAndOrOperators_When_NoConditionWasAdded() {
        // Given
        QueryBuilder builder = QueryBuilder.builder()
                .select("*")
                .from("users");

        // When
        String sql = builder
                .where()
                    .and()
                    .or()
                    .equals("id", 1L)
                    .and()
                    .and()
                    .equals("active", true)
                .done()
                .build();

        // Then
        assertEquals("SELECT * FROM users WHERE id = ? AND active = ?", sql);
        assertEquals(List.of(1L, true), builder.getParameters());
    }

    @Test
    void should_BuildAllJoinVariants_When_JoinMethodsAreUsed() {
        // Given
        QueryBuilder builder = QueryBuilder.builder()
                .select("u.id")
                .from("users u");

        // When
        String sql = builder
                .joinOn("profiles p", "u.id", QueryOperator.EQUALS, "p.user_id")
                .join("roles", "r")
                    .on("r.id", QueryOperator.EQUALS, "u.role_id")
                .endJoin()
                .join(QueryJoinType.LEFT, "addresses a").done()
                .join(QueryJoinType.FULL, "archive", "ar").done()
                .join(QueryJoinType.CROSS, "regions").done()
                .joinBuilder(QueryJoinType.RIGHT, "permissions", "pm")
                    .and().or().on("pm.user_id", QueryOperator.EQUALS, "u.id")
                    .and().onRaw("pm.active = 1")
                    .or().onRaw("pm.global = 1").done()
                .build();

        // Then
        assertEquals("SELECT u.id FROM users u INNER JOIN profiles p ON u.id = p.user_id INNER JOIN roles r ON r.id = u.role_id LEFT JOIN addresses a FULL JOIN archive ar CROSS JOIN regions RIGHT JOIN permissions pm ON pm.user_id = u.id AND pm.active = 1 OR pm.global = 1", sql);
        assertTrue(builder.getParameters().isEmpty());
    }

    @Test
    void should_BuildDefaultSelectGroupingOrderingLimitAndOffset_When_OptionalClausesAreProvided() {
        // Given
        QueryBuilder builder = new QueryBuilder();
        assertNull(builder.getSql());

        // When
        String sql = builder.select()
                .from("events")
                .groupBy("type", "status")
                .orderBy("type ASC", "status DESC")
                .limit(10)
                .offset(20)
                .build();

        // Then
        assertEquals("SELECT * FROM events GROUP BY type, status ORDER BY type ASC, status DESC LIMIT ? OFFSET ?", sql);
        assertEquals(List.of(10, 20), builder.getParameters());
        assertTrue(builder.getCountParameters().isEmpty());
    }

    @Test
    void should_AppendOnlyConfiguredPaginationClause_When_LimitOrOffsetIsUsedSeparately() {
        // Given
        QueryBuilder limited = QueryBuilder.builder().select("*").from("users").limit(5);
        QueryBuilder offset = QueryBuilder.builder().select("*").from("users").offset(10);

        // When
        String limitedSql = limited.build();
        String offsetSql = offset.build();

        // Then
        assertEquals("SELECT * FROM users LIMIT ?", limitedSql);
        assertEquals(List.of(5), limited.getParameters());
        assertEquals("SELECT * FROM users OFFSET ?", offsetSql);
        assertEquals(List.of(10), offset.getParameters());
    }

    @Test
    void should_BuildInsertStatements_When_ColumnsAndValuesAreProvidedOrOmitted() {
        // Given
        QueryBuilder withColumns = QueryBuilder.builder().insertInto("users", "id", "name").values(1L, "Reyga");
        QueryBuilder withoutColumns = QueryBuilder.builder().insertInto("audit").values("created");
        QueryBuilder nullValues = QueryBuilder.builder().insertInto("empty", (String[]) null).values((Object[]) null);

        // When
        String withColumnsSql = withColumns.build();
        String withoutColumnsSql = withoutColumns.build();
        String nullValuesSql = nullValues.build();

        // Then
        assertEquals("INSERT INTO users (id, name) VALUES (?, ?)", withColumnsSql);
        assertEquals(List.of(1L, "Reyga"), withColumns.getParameters());
        assertEquals("INSERT INTO audit VALUES (?)", withoutColumnsSql);
        assertEquals(List.of("created"), withoutColumns.getParameters());
        assertEquals("INSERT INTO empty VALUES ()", nullValuesSql);
        assertTrue(nullValues.getParameters().isEmpty());
    }

    @Test
    void should_BuildUpdateAndDeleteStatements_When_ValuesAndFiltersAreProvided() {
        // Given
        QueryBuilder update = QueryBuilder.builder().update("users").set("name", "Reyga").set("active", true)
                .where().equals("id", 1L).done();
        QueryBuilder delete = QueryBuilder.builder().deleteFrom("users").where().equals("id", 2L).done();

        // When
        String updateSql = update.build();
        String deleteSql = delete.build();

        // Then
        assertEquals("UPDATE users SET name = ?, active = ? WHERE id = ?", updateSql);
        assertEquals(List.of("Reyga", true, 1L), update.getParameters());
        assertEquals("DELETE FROM users WHERE id = ?", deleteSql);
        assertEquals(List.of(2L), delete.getParameters());
    }

    @Test
    void should_BuildPagedAndSortedQuery_When_PageableIsProvidedBeforeFilters() {
        // Given
        Pageable pageable = PageRequest.of(2, 25, Sort.by(Sort.Order.desc("u.user_name"), Sort.Order.asc("id$raw")));
        QueryBuilder builder = QueryBuilder.builder().select("id", "user_name").from("users u").paginate(pageable);

        // When
        String sql = builder.where().equals("status", "ACTIVE").done().build();

        // Then
        assertEquals("SELECT id, user_name FROM users u WHERE status = ? ORDER BY u.user_name DESC, id$raw ASC LIMIT ? OFFSET ?", sql);
        assertEquals(List.of("ACTIVE", 25, 50L), builder.getParameters());
    }

    @Test
    void should_RemovePaginationAndReplacePageableSort_When_UnpagedPageableIsApplied() {
        // Given
        QueryBuilder builder = QueryBuilder.builder().select("*").from("users")
                .orderBy("tenant_id").paginate(PageRequest.of(1, 10, Sort.by("old_sort")));
        Pageable unpaged = Pageable.unpaged(Sort.by(Sort.Order.desc("new_sort")));

        // When
        String sql = builder.paginate(unpaged).build();

        // Then
        assertEquals("SELECT * FROM users ORDER BY tenant_id, new_sort DESC", sql);
        assertTrue(builder.getParameters().isEmpty());
    }

    @Test
    void should_ReplacePaginationParameters_When_PaginationIsAppliedMoreThanOnce() {
        // Given
        QueryBuilder builder = QueryBuilder.builder().select("*").from("users").paginate(1, 10);

        // When
        String sql = builder.paginate(PageRequest.of(3, 20)).build();

        // Then
        assertEquals("SELECT * FROM users LIMIT ? OFFSET ?", sql);
        assertEquals(List.of(20, 60L), builder.getParameters());
    }

    @Test
    void should_BuildCountQueryWithoutSortingAndPagination_When_SelectContainsJoinAndGrouping() {
        // Given
        QueryBuilder builder = QueryBuilder.builder().select("department", "COUNT(*)").from("employees e")
                .join(QueryJoinType.LEFT, "tasks t").on("e.id", QueryOperator.EQUALS, "t.employee_id").done()
                .where().equals("e.active", true).done().groupBy("department").orderBy("department").paginate(2, 10);

        // When
        String countSql = builder.buildCount();
        List<Object> countParameters = builder.getCountParameters();

        // Then
        assertEquals("SELECT COUNT(*) FROM (SELECT department, COUNT(*) FROM employees e LEFT JOIN tasks t ON e.id = t.employee_id WHERE e.active = ? GROUP BY department) query_count", countSql);
        assertEquals(List.of(true), countParameters);
        assertEquals(List.of(true, 10, 10), builder.getParameters());
    }

    @Test
    void should_ReturnImmutableParameterSnapshots_When_ParametersAreRequested() {
        // Given
        QueryBuilder builder = QueryBuilder.builder().select("*").from("users")
                .where().equals("active", true).done().paginate(1, 10);
        List<Object> parameters = builder.getParameters();
        List<Object> countParameters = builder.getCountParameters();

        // When
        UnsupportedOperationException contentException = assertThrows(UnsupportedOperationException.class,
                () -> parameters.add("unexpected"));
        UnsupportedOperationException countException = assertThrows(UnsupportedOperationException.class,
                countParameters::clear);

        // Then
        assertNotNull(contentException);
        assertNotNull(countException);
        assertEquals(List.of(true, 10, 0), builder.getParameters());
        assertEquals(List.of(true), builder.getCountParameters());
    }

    @Test
    void should_ThrowIllegalStateException_When_QueryTypeIsMissing() {
        // Given
        QueryBuilder builder = QueryBuilder.builder().from("users");

        // When
        IllegalStateException exception = assertThrows(IllegalStateException.class, builder::build);

        // Then
        assertEquals("Query type (SELECT, INSERT, UPDATE, DELETE) must be specified.", exception.getMessage());
        assertNull(builder.getSql());
    }

    @Test
    void should_ThrowIllegalStateException_When_InsertColumnAndValueCountsDiffer() {
        // Given
        QueryBuilder builder = QueryBuilder.builder().insertInto("users", "id", "name").values(1L);

        // When
        IllegalStateException exception = assertThrows(IllegalStateException.class, builder::build);

        // Then
        assertEquals("Columns count and values count do not match.", exception.getMessage());
        assertEquals(List.of(1L), builder.getParameters());
    }

    @Test
    void should_ThrowIllegalArgumentException_When_WhereInValuesAreNullOrEmpty() {
        // Given
        QueryBuilder builder = QueryBuilder.builder().select("*").from("users");

        // When
        IllegalArgumentException nullException = assertThrows(IllegalArgumentException.class,
                () -> builder.where().in("id", (Object[]) null));
        IllegalArgumentException emptyException = assertThrows(IllegalArgumentException.class,
                () -> builder.where().in("id"));

        // Then
        assertEquals("values required", nullException.getMessage());
        assertEquals("values required", emptyException.getMessage());
        assertTrue(builder.getParameters().isEmpty());
    }

    @Test
    void should_ThrowExpectedExceptions_When_PaginationArgumentsAreInvalid() {
        // Given
        QueryBuilder builder = QueryBuilder.builder().select("*").from("users");

        // When
        IllegalArgumentException page = assertThrows(IllegalArgumentException.class, () -> builder.paginate(0, 10));
        IllegalArgumentException size = assertThrows(IllegalArgumentException.class, () -> builder.paginate(1, 0));
        IllegalArgumentException limit = assertThrows(IllegalArgumentException.class, () -> builder.limit(-1));
        IllegalArgumentException offset = assertThrows(IllegalArgumentException.class, () -> builder.offset(-1));
        ArithmeticException overflow = assertThrows(ArithmeticException.class,
                () -> builder.paginate(Integer.MAX_VALUE, Integer.MAX_VALUE));
        NullPointerException pageable = assertThrows(NullPointerException.class, () -> builder.paginate((Pageable) null));

        // Then
        assertEquals("page must be greater than zero", page.getMessage());
        assertEquals("pageSize must be greater than zero", size.getMessage());
        assertEquals("limit must not be negative", limit.getMessage());
        assertEquals("offset must not be negative", offset.getMessage());
        assertNotNull(overflow);
        assertEquals("pageable must not be null", pageable.getMessage());
    }

    @Test
    void should_ThrowIllegalArgumentException_When_PageableSortPropertyIsUnsafe() {
        // Given
        QueryBuilder builder = QueryBuilder.builder().select("*").from("users");
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id; DROP TABLE users"));

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> builder.paginate(pageable));

        // Then
        assertEquals("Unsafe sort property: id; DROP TABLE users", exception.getMessage());
        assertTrue(builder.getParameters().isEmpty());
    }

    @Test
    void should_ThrowIllegalStateException_When_CountIsBuiltForNonSelectQuery() {
        // Given
        List<QueryBuilder> builders = List.of(
                QueryBuilder.builder(),
                QueryBuilder.builder().insertInto("users").values(1),
                QueryBuilder.builder().update("users").set("active", true),
                QueryBuilder.builder().deleteFrom("users")
        );

        // When
        List<IllegalStateException> exceptions = builders.stream()
                .map(builder -> assertThrows(IllegalStateException.class, builder::buildCount))
                .toList();

        // Then
        exceptions.forEach(exception -> {
            assertEquals("Count query can only be built from a SELECT query.", exception.getMessage());
        });
    }
}
