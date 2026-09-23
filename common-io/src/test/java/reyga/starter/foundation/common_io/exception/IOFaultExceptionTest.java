package reyga.starter.foundation.common_io.exception;

import org.junit.jupiter.api.Test;
import reyga.starter.foundation.common_io.enumeration.IOOperation;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class IOFaultExceptionTest {

    @Test
    void should_StoreOnlyMessage_When_MessageConstructorIsUsed() {
        // Given
        String message = "File processing failed";

        // When
        IOFaultException exception = new IOFaultException(message);

        // Then
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
        assertNull(exception.getMetadata());
        assertNull(exception.getMetadataList());
    }

    @Test
    void should_StoreMessageAndCause_When_CauseConstructorIsUsed() {
        // Given
        String message = "File processing failed";
        RuntimeException cause = new RuntimeException("disk error");

        // When
        IOFaultException exception = new IOFaultException(message, cause);

        // Then
        assertEquals(message, exception.getMessage());
        assertSame(cause, exception.getCause());
        assertNull(exception.getMetadata());
        assertNull(exception.getMetadataList());
    }

    @Test
    void should_StoreMetadataWithOptionalCause_When_MetadataConstructorsAreUsed() {
        // Given
        IOFaultMetadata metadata = IOFaultMetadata.builder().operation(IOOperation.READ).build();
        RuntimeException cause = new RuntimeException("read error");

        // When
        IOFaultException withoutCause = new IOFaultException("failed", metadata);
        IOFaultException withCause = new IOFaultException("failed", metadata, cause);

        // Then
        assertEquals("failed", withoutCause.getMessage());
        assertSame(metadata, withoutCause.getMetadata());
        assertNull(withoutCause.getMetadataList());
        assertNull(withoutCause.getCause());
        assertEquals("failed", withCause.getMessage());
        assertSame(metadata, withCause.getMetadata());
        assertSame(cause, withCause.getCause());
        assertNull(withCause.getMetadataList());
    }

    @Test
    void should_StoreMetadataListWithOptionalCause_When_MetadataListConstructorsAreUsed() {
        // Given
        List<IOFaultMetadata> metadataList = List.of(
                IOFaultMetadata.builder().operation(IOOperation.READ).build(),
                IOFaultMetadata.builder().operation(IOOperation.WRITE).build()
        );
        RuntimeException cause = new RuntimeException("batch error");

        // When
        IOFaultException withoutCause = new IOFaultException("failed", metadataList);
        IOFaultException withCause = new IOFaultException("failed", metadataList, cause);

        // Then
        assertSame(metadataList, withoutCause.getMetadataList());
        assertNull(withoutCause.getMetadata());
        assertNull(withoutCause.getCause());
        assertSame(metadataList, withCause.getMetadataList());
        assertSame(cause, withCause.getCause());
        assertNull(withCause.getMetadata());
    }

    @Test
    void should_ReplaceMutableMetadataFields_When_SettersAreCalled() {
        // Given
        IOFaultException exception = new IOFaultException("failed");
        IOFaultMetadata metadata = IOFaultMetadata.builder().operation(IOOperation.DELETE).build();
        List<IOFaultMetadata> metadataList = List.of(metadata);

        // When
        exception.setMetadata(metadata);
        exception.setMetadataList(metadataList);

        // Then
        assertSame(metadata, exception.getMetadata());
        assertSame(metadataList, exception.getMetadataList());
        assertEquals("failed", exception.getMessage());
    }
}
