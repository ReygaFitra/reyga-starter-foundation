package reyga.starter.foundation.core.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

class AppFaultExceptionTest {

    @Test
    void buildAppFaultContent_createsExpectedContent() {
        AppFaultContent content = AppFaultContent.buildAppFaultContent(
                "msg", "01", "err", "fault", HttpStatus.BAD_REQUEST
        );

        assertEquals("msg", content.getMessage());
        assertEquals("01", content.getErrorCode());
        assertEquals("err", content.getErrorMessage());
        assertEquals("fault", content.getFaultInfo());
        assertEquals(HttpStatus.BAD_REQUEST, content.getStatusCode());
    }

    @Test
    void appFaultException_constructedFromContent() {
        AppFaultContent content = AppFaultContent.buildAppFaultContent(
                "msg", "02", "err2", "fault2", HttpStatus.CONFLICT
        );

        AppFaultException ex = new AppFaultException(content);

        assertEquals("fault2", ex.getFaultInfo());
        assertEquals("02", ex.getErrorCode());
        assertEquals("err2", ex.getErrorMessage());
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertEquals("msg", ex.getMessage());
    }
}
