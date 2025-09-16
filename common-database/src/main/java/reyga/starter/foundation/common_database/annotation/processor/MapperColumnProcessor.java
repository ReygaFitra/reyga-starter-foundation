package reyga.starter.foundation.common_database.annotation.processor;

import lombok.RequiredArgsConstructor;
import oracle.sql.TIMESTAMP;
import org.springframework.jdbc.core.RowMapper;
import reyga.starter.foundation.common.logging.BaseLogging;
import reyga.starter.foundation.common.logging.CustomLogger;
import reyga.starter.foundation.common_database.annotation.MapperColumn;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.time.ZoneId;

@RequiredArgsConstructor
public class MapperColumnProcessor<E> extends BaseLogging {

    private final Class<E> clazz;

    public RowMapper<E> generate() {
        return (rs, rowNum) -> {
            E instance;
            try {
                instance = clazz.getDeclaredConstructor().newInstance();

                for (var field : clazz.getDeclaredFields()) {
                    var anot = field.getAnnotation(MapperColumn.class);
                    if (anot==null) continue;

                    field.setAccessible(true);
                    Object queryValue = rs.getObject(anot.columnName());

                    if (queryValue==null){
                        if (anot.isBoolean())
                            field.set(instance,false);
                        continue;
                    }

                    if (anot.isBoolean()){
                        field.set(instance,queryValue.equals("1"));
                        continue;
                    }

                    if(queryValue instanceof TIMESTAMP ts){
                        queryValue = switch (field.getType().getSimpleName()){
                            case "LocalDate" -> ts.toLocalDate();
                            case "LocalDateTime" -> ts.toLocalDateTime();
                            default -> Date.from(ts.toLocalDateTime().atZone(ZoneId.systemDefault()).toInstant());
                        };
                    } else
                        queryValue = convertType(queryValue,field.getType());

                    field.set(instance,queryValue);
                }

            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }

            return instance;
        };
    }

    private Object convertType(Object value, Class<?> targetType){
        if (value==null) return null;

        if (targetType.equals(long.class) || targetType.equals(Long.class)) {
            return ((Number) value).longValue();
        } else if (targetType.equals(int.class) || targetType.equals(Integer.class)) {
            return ((Number) value).intValue();
        } else if (targetType.equals(double.class) || targetType.equals(Double.class)) {
            return ((Number) value).doubleValue();
        } else if (targetType.equals(float.class) || targetType.equals(Float.class)) {
            return ((Number) value).floatValue();
        } else if (targetType.equals(short.class) || targetType.equals(Short.class)) {
            return ((Number) value).shortValue();
        } else if (targetType == Byte.class || targetType == byte.class) {
            return ((Number) value).byteValue();
        } else if (targetType == BigInteger.class && value instanceof BigDecimal) {
            return ((BigDecimal) value).toBigInteger();
        } else {
            return value.toString();
        }
    }
}
