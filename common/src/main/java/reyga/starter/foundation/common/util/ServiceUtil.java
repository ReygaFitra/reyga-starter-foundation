package reyga.starter.foundation.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ServiceUtil {

    public static ObjectMapper useObjectMapper() {
        return ObjectMapperFactory.getInstance();
    }

    static class ObjectMapperFactory {

        private static final ObjectMapper objectMapper = new ObjectMapper();

        private ObjectMapperFactory() {
        }

        public static ObjectMapper getInstance() {
            return objectMapper;
        }
    }

}
