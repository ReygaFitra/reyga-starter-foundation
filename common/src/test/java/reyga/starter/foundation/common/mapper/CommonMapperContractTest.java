package reyga.starter.foundation.common.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.MapperConfig;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommonMapperContractTest {

    @Test
    void should_NotExposeCompileTimeConfiguration_When_CommonMapperConfigIsInspectedAtRuntime() {
        // Given
        MapperConfig annotation = CommonMapperConfig.class.getAnnotation(MapperConfig.class);

        // When
        int declaredMethodCount = CommonMapperConfig.class.getDeclaredMethods().length;

        // Then
        assertNull(annotation);
        assertEquals(0, declaredMethodCount);
        assertTrue(CommonMapperConfig.class.isInterface());
    }

    @Test
    void should_MapAndUpdateValues_When_BaseMapperContractIsImplemented() {
        // Given
        BaseMapper<Entity, Dto> mapper = new TestMapper();
        Entity entity = new Entity("one");
        Dto dto = new Dto("two");

        // When
        Dto mappedDto = mapper.toDto(entity);
        Entity mappedEntity = mapper.toEntity(dto);
        List<Dto> dtoList = mapper.toDtoList(List.of(entity));
        List<Entity> entityList = mapper.toEntityList(List.of(dto));
        mapper.updateEntity(dto, entity);

        // Then
        assertEquals("one", mappedDto.value);
        assertEquals("two", mappedEntity.value);
        assertEquals("one", dtoList.getFirst().value);
        assertEquals("two", entityList.getFirst().value);
        assertEquals("two", entity.value);
    }

    private static final class Entity {
        private String value;
        private Entity(String value) { this.value = value; }
    }

    private record Dto(String value) {}

    private static final class TestMapper implements BaseMapper<Entity, Dto> {
        public Dto toDto(Entity entity) { return new Dto(entity.value); }
        public Entity toEntity(Dto dto) { return new Entity(dto.value); }
        public List<Dto> toDtoList(List<Entity> entities) {
            List<Dto> result = new ArrayList<>();
            entities.forEach(entity -> result.add(toDto(entity)));
            return result;
        }
        public List<Entity> toEntityList(List<Dto> dtos) {
            List<Entity> result = new ArrayList<>();
            dtos.forEach(dto -> result.add(toEntity(dto)));
            return result;
        }
        public void updateEntity(Dto dto, Entity entity) { entity.value = dto.value; }
    }
}
