package reyga.starter.foundation.common_database;

import org.junit.jupiter.api.Test;
import reyga.starter.foundation.common_database.annotation.MapperColumn;
import reyga.starter.foundation.common_database.enumeration.QueryJoinType;
import reyga.starter.foundation.common_database.enumeration.QueryType;
import reyga.starter.foundation.common_database.repository.BaseJdbcRepository;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CommonDatabaseContractTest {

    @Test
    void should_ExposeRuntimeFieldMetadata_When_MapperColumnAnnotationIsInspected() throws Exception {
        // Given
        Field field = AnnotatedEntity.class.getDeclaredField("active");

        // When
        MapperColumn mapperColumn = field.getAnnotation(MapperColumn.class);
        Retention retention = MapperColumn.class.getAnnotation(Retention.class);
        Target target = MapperColumn.class.getAnnotation(Target.class);

        // Then
        assertNotNull(mapperColumn);
        assertEquals("is_active", mapperColumn.columnName());
        assertTrue(mapperColumn.isBoolean());
        assertEquals(RetentionPolicy.RUNTIME, retention.value());
        assertArrayEquals(new ElementType[]{ElementType.FIELD}, target.value());
    }

    @Test
    void should_DefaultBooleanMappingToFalse_When_IsBooleanIsNotConfigured() throws Exception {
        // Given
        Field field = AnnotatedEntity.class.getDeclaredField("name");

        // When
        MapperColumn mapperColumn = field.getAnnotation(MapperColumn.class);

        // Then
        assertEquals("name", mapperColumn.columnName());
        assertFalse(mapperColumn.isBoolean());
    }

    @Test
    void should_ReturnSqlJoinKeywords_When_AllJoinTypesAreRead() {
        // Given
        List<String> expected = List.of("INNER JOIN", "LEFT JOIN", "RIGHT JOIN", "FULL JOIN", "CROSS JOIN");

        // When
        List<String> result = java.util.Arrays.stream(QueryJoinType.values()).map(QueryJoinType::toString).toList();

        // Then
        assertEquals(expected, result);
        assertEquals(QueryJoinType.LEFT, QueryJoinType.valueOf("LEFT"));
    }

    @Test
    void should_ReturnSupportedQueryTypes_When_QueryTypeValuesAreRead() {
        // Given
        List<QueryType> expected = List.of(QueryType.SELECT, QueryType.INSERT, QueryType.UPDATE, QueryType.DELETE);

        // When
        List<QueryType> result = List.of(QueryType.values());

        // Then
        assertEquals(expected, result);
        assertEquals(QueryType.SELECT, QueryType.valueOf("SELECT"));
    }

    @Test
    void should_DelegateRepositoryContract_When_InMemoryImplementationIsUsed() {
        // Given
        BaseJdbcRepository<String, Integer> repository = new InMemoryRepository();

        // When
        String saved = repository.save("one");
        List<String> all = repository.findAll();
        Optional<String> found = repository.findById(0);
        Optional<String> missing = repository.findById(1);

        // Then
        assertEquals("one", saved);
        assertEquals(List.of("one"), all);
        assertEquals(Optional.of("one"), found);
        assertTrue(missing.isEmpty());
    }

    private static final class AnnotatedEntity {
        @MapperColumn(columnName = "is_active", isBoolean = true)
        private boolean active;
        @MapperColumn(columnName = "name")
        private String name;
    }

    private static final class InMemoryRepository implements BaseJdbcRepository<String, Integer> {
        private final List<String> values = new ArrayList<>();
        public String save(String entity) { values.add(entity); return entity; }
        public List<String> findAll() { return List.copyOf(values); }
        public Optional<String> findById(Integer id) {
            return id >= 0 && id < values.size() ? Optional.of(values.get(id)) : Optional.empty();
        }
    }
}
