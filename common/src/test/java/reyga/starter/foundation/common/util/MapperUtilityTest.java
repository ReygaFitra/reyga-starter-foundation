package reyga.starter.foundation.common.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MapperUtilityTest {

    @Test
    void should_ReturnNullLiteral_When_DtoIsNull() {
        // Given
        Object dto = null;

        // When
        String simple = MapperUtility.convertDtoToJsonString(dto);
        String recursive = MapperUtility.convertDtoToJsonString(dto, true);

        // Then
        assertEquals("null", simple);
        assertEquals("null", recursive);
    }

    @Test
    void should_ReturnSimpleFieldRepresentation_When_DtoHasFields() {
        // Given
        SampleDto dto = new SampleDto("alpha", 7);

        // When
        String result = MapperUtility.convertDtoToJsonString(dto);

        // Then
        assertEquals("{name='alpha', count='7'}", result);
    }

    @Test
    void should_IncludeInheritedFields_When_IncludeSuperClassIsTrue() {
        // Given
        ChildDto dto = new ChildDto("parent", "child");

        // When
        String result = MapperUtility.convertDtoToJsonString(dto, true);

        // Then
        assertEquals("{\"child\": \"child\", \"parent\": \"parent\"}", result);
    }

    @Test
    void should_ExcludeInheritedFields_When_IncludeSuperClassIsFalse() {
        // Given
        ChildDto dto = new ChildDto("parent", "child");

        // When
        String result = MapperUtility.convertDtoToJsonString(dto, false);

        // Then
        assertEquals("{\"child\": \"child\"}", result);
    }

    @Test
    void should_MapComplexValues_When_CollectionsArraysMapsTemporalAndNullArePresent() {
        // Given
        ComplexDto dto = new ComplexDto(List.of(new ValueDto("list")),
                new ValueDto[]{new ValueDto("array")}, Map.of("key", new ValueDto("map")),
                LocalDate.of(2024, 2, 29), null);

        // When
        String result = MapperUtility.convertDtoToJsonString(dto, false);

        // Then
        assertTrue(result.contains("\"list\": [{\"value\": \"list\"}]"));
        assertTrue(result.contains("\"array\": [{\"value\": \"array\"}]"));
        assertTrue(result.contains("\"map\": {\"key\": {\"value\": \"map\"}}"));
        assertTrue(result.contains("\"date\": \"2024-02-29\""));
        assertTrue(result.contains("\"nullable\": null"));
    }

    @Test
    void should_MarkCircularReference_When_ObjectReferencesItself() {
        // Given
        Node node = new Node();
        node.next = node;

        // When
        String result = MapperUtility.convertDtoToJsonString(node, false);

        // Then
        assertEquals("{\"next\": \"<circular-reference>\"}", result);
    }

    private record SampleDto(String name, int count) {}
    private record ValueDto(String value) {}
    private record ComplexDto(List<ValueDto> list, ValueDto[] array, Map<String, ValueDto> map,
                              LocalDate date, String nullable) {}
    private static class ParentDto {
        private final String parent;
        ParentDto(String parent) { this.parent = parent; }
    }
    private static class ChildDto extends ParentDto {
        private final String child;
        ChildDto(String parent, String child) { super(parent); this.child = child; }
    }
    private static class Node { private Node next; }
}
