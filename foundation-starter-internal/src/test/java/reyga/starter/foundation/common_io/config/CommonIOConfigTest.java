package reyga.starter.foundation.common_io.config;

import org.junit.jupiter.api.Test;
import reyga.starter.foundation.common_io.operations.DefaultFileInspector;
import reyga.starter.foundation.common_io.operations.DefaultFileSanitizer;
import reyga.starter.foundation.common_io.operations.FileInspector;
import reyga.starter.foundation.common_io.operations.FileSanitizer;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;

class CommonIOConfigTest {

    @Test
    void should_ReturnDefaultFileInspector_When_FileInspectorBeanIsCreated() {
        // Given
        CommonIOConfig config = new CommonIOConfig();

        // When
        FileInspector first = config.fileInspector();
        FileInspector second = config.fileInspector();

        // Then
        assertInstanceOf(DefaultFileInspector.class, first);
        assertInstanceOf(DefaultFileInspector.class, second);
        assertNotSame(first, second);
    }

    @Test
    void should_ReturnDefaultFileSanitizer_When_FileSanitizerBeanIsCreated() {
        // Given
        CommonIOConfig config = new CommonIOConfig();

        // When
        FileSanitizer first = config.fileSanitizer();
        FileSanitizer second = config.fileSanitizer();

        // Then
        assertInstanceOf(DefaultFileSanitizer.class, first);
        assertInstanceOf(DefaultFileSanitizer.class, second);
        assertNotSame(first, second);
    }
}
