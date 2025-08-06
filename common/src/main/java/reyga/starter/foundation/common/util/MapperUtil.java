package reyga.starter.foundation.common.util;

import java.lang.reflect.Field;
import java.time.temporal.Temporal;
import java.util.*;

public class MapperUtil {

    public static String convertDtoToJsonString(Object dto) {
        if (dto == null) {
            return "null";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("{");

        Class<?> dtoClass = dto.getClass();
        Field[] fields = dtoClass.getDeclaredFields();

        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            field.setAccessible(true);
            try {
                Object value = field.get(dto);
                builder.append(field.getName()).append("='").append(value).append("'");
            } catch (IllegalAccessException e) {
                builder.append(field.getName()).append("='ERROR'");
            }

            if (i < fields.length - 1) {
                builder.append(", ");
            }
        }

        builder.append("}");
        return builder.toString();
    }

    public static String convertDtoToJsonString(Object dto, boolean includeSuperClass) {
        return convertDtoToJsonString(dto, includeSuperClass, Collections.newSetFromMap(new IdentityHashMap<>()));
    }

    private static String convertDtoToJsonString(Object dto, boolean includeSuperClass, Set<Object> visited) {
        if (dto == null) {
            return "null";
        }

        if (visited.contains(dto)) {
            return "\"<circular-reference>\"";
        }

        visited.add(dto);

        StringBuilder builder = new StringBuilder();
        builder.append("{");

        List<Field> allFields = new ArrayList<>();
        Class<?> currentClass = dto.getClass();

        while (currentClass != null && currentClass != Object.class) {
            allFields.addAll(Arrays.asList(currentClass.getDeclaredFields()));
            currentClass = includeSuperClass ? currentClass.getSuperclass() : null;
        }

        for (int i = 0; i < allFields.size(); i++) {
            Field field = allFields.get(i);
            field.setAccessible(true);
            builder.append("\"").append(field.getName()).append("\": ");

            try {
                Object value = field.get(dto);
                if (value == null) {
                    builder.append("null");
                } else if (isPrimitiveOrWrapper(value.getClass()) || value instanceof String || value instanceof Temporal) {
                    builder.append("\"").append(value).append("\"");
                } else if (value instanceof Collection<?>) {
                    builder.append("[");
                    Collection<?> collection = (Collection<?>) value;
                    Iterator<?> iterator = collection.iterator();
                    while (iterator.hasNext()) {
                        builder.append(convertDtoToJsonString(iterator.next(), includeSuperClass, visited));
                        if (iterator.hasNext()) builder.append(", ");
                    }
                    builder.append("]");
                } else if (value.getClass().isArray()) {
                    builder.append("[");
                    int length = java.lang.reflect.Array.getLength(value);
                    for (int j = 0; j < length; j++) {
                        Object arrayElement = java.lang.reflect.Array.get(value, j);
                        builder.append(convertDtoToJsonString(arrayElement, includeSuperClass, visited));
                        if (j < length - 1) builder.append(", ");
                    }
                    builder.append("]");
                } else if (value instanceof Map<?, ?>) {
                    builder.append("{");
                    Map<?, ?> map = (Map<?, ?>) value;
                    Iterator<? extends Map.Entry<?, ?>> it = map.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<?, ?> entry = it.next();
                        builder.append("\"").append(entry.getKey()).append("\": ")
                                .append(convertDtoToJsonString(entry.getValue(), includeSuperClass, visited));
                        if (it.hasNext()) builder.append(", ");
                    }
                    builder.append("}");
                } else {
                    builder.append(convertDtoToJsonString(value, includeSuperClass, visited));
                }
            } catch (IllegalAccessException e) {
                builder.append("\"ERROR\"");
            }

            if (i < allFields.size() - 1) {
                builder.append(", ");
            }
        }

        builder.append("}");
        visited.remove(dto);
        return builder.toString();
    }

    private static boolean isPrimitiveOrWrapper(Class<?> type) {
        return type.isPrimitive()
                || type == Boolean.class
                || type == Integer.class
                || type == Long.class
                || type == Double.class
                || type == Float.class
                || type == Short.class
                || type == Byte.class
                || type == Character.class;
    }

}
