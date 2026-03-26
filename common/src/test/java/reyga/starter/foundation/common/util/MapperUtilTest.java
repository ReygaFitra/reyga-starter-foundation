package reyga.starter.foundation.common.util;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MapperUtilTest {

    @Test
    void convertDtoToJsonString_handlesNull() {
        assertEquals("null", MapperUtil.convertDtoToJsonString(null));
    }

    @Test
    void convertDtoToJsonString_includesFields() {
        SampleDto dto = new SampleDto("a", 1);
        String result = MapperUtil.convertDtoToJsonString(dto);
        assertTrue(result.contains("name='a'"));
        assertTrue(result.contains("count='1'"));
    }

    @Test
    void convertDtoToJsonString_includesSuperclassWhenRequested() {
        ChildDto dto = new ChildDto("parent", "child");
        String result = MapperUtil.convertDtoToJsonString(dto, true);
        assertTrue(result.contains("\"parent\""));
        assertTrue(result.contains("\"child\""));
    }

    @Test
    void convertDtoToJsonString_handlesCollectionsAndMaps() {
        ComplexDto dto = new ComplexDto(List.of("a", "b"), Map.of("k", "v"));
        String result = MapperUtil.convertDtoToJsonString(dto, false);
        assertTrue(result.contains("\"list\""));
        assertTrue(result.contains("\"map\""));
    }

    @Test
    void convertDtoToJsonString_handlesCircularReference() {
        Node node = new Node();
        node.next = node;
        String result = MapperUtil.convertDtoToJsonString(node, false);
        assertTrue(result.contains("<circular-reference>"));
    }

    private static class SampleDto {
        private final String name;
        private final int count;
        SampleDto(String name, int count) {
            this.name = name;
            this.count = count;
        }
    }

    private static class ParentDto {
        private final String parent;
        ParentDto(String parent) {
            this.parent = parent;
        }
    }

    private static class ChildDto extends ParentDto {
        private final String child;
        ChildDto(String parent, String child) {
            super(parent);
            this.child = child;
        }
    }

    private static class ComplexDto {
        private final List<String> list;
        private final Map<String, String> map;
        ComplexDto(List<String> list, Map<String, String> map) {
            this.list = list;
            this.map = map;
        }
    }

    private static class Node {
        private Node next;
    }
}
