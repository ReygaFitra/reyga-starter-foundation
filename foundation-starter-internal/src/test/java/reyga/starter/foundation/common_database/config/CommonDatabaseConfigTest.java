package reyga.starter.foundation.common_database.config;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import reyga.starter.foundation.common_database.processor.DefaultQueryProcessor;
import reyga.starter.foundation.common_database.processor.QueryProcessor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class CommonDatabaseConfigTest {

    @Test
    void should_ReturnDefaultQueryProcessorWithoutDatabaseCall_When_QueryProcessorBeanIsCreated() {
        // given
        CommonDatabaseConfig config = new CommonDatabaseConfig();
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);

        // when
        QueryProcessor result = config.queryProcessor(jdbcTemplate);

        // then
        assertNotNull(result);
        assertEquals(DefaultQueryProcessor.class, result.getClass());
        verifyNoInteractions(jdbcTemplate);
    }
}
