package reyga.starter.foundation.common_io.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import reyga.starter.foundation.common_io.enumeration.IOOperation;
import reyga.starter.foundation.common_io.enumeration.IOPermissions;
import reyga.starter.foundation.common_io.enumeration.IOStorageType;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IOFaultMetadataTest {

    @Test
    void should_ReturnJavaDefaults_When_NoArgsConstructorIsUsed() {
        // Given
        IOFaultMetadata metadata = new IOFaultMetadata();

        // When
        boolean directory = metadata.isDirectory();
        long sizeBytes = metadata.getSizeBytes();
        long lastModified = metadata.getLastModifiedEpochMillis();

        // Then
        assertNull(metadata.getFileName());
        assertNull(metadata.getFilePath());
        assertNull(metadata.getUri());
        assertNull(metadata.getMimeType());
        assertNull(metadata.getCharset());
        assertNull(metadata.getOperation());
        assertNull(metadata.getPermissions());
        assertNull(metadata.getStorageType());
        assertNull(metadata.getChecksum());
        assertFalse(directory);
        assertEquals(0L, sizeBytes);
        assertEquals(0L, lastModified);
        assertNull(metadata.getOffset());
        assertNull(metadata.getLength());
    }

    @Test
    void should_ReturnAllAssignedValues_When_SettersAreUsed() {
        // Given
        IOFaultMetadata metadata = new IOFaultMetadata();
        URI uri = URI.create("file:///tmp/report.txt");

        // When
        metadata.setFileName("report.txt");
        metadata.setFilePath("/tmp/report.txt");
        metadata.setUri(uri);
        metadata.setMimeType(MediaType.TEXT_PLAIN);
        metadata.setCharset("UTF-8");
        metadata.setOperation(IOOperation.READ);
        metadata.setPermissions(IOPermissions.READ_ONLY);
        metadata.setStorageType(IOStorageType.LOCAL);
        metadata.setChecksum("abc123");
        metadata.setDirectory(true);
        metadata.setSizeBytes(128L);
        metadata.setLastModifiedEpochMillis(1_000L);
        metadata.setOffset(4L);
        metadata.setLength(32L);

        // Then
        assertEquals("report.txt", metadata.getFileName());
        assertEquals("/tmp/report.txt", metadata.getFilePath());
        assertEquals(uri, metadata.getUri());
        assertEquals(MediaType.TEXT_PLAIN, metadata.getMimeType());
        assertEquals("UTF-8", metadata.getCharset());
        assertEquals(IOOperation.READ, metadata.getOperation());
        assertEquals(IOPermissions.READ_ONLY, metadata.getPermissions());
        assertEquals(IOStorageType.LOCAL, metadata.getStorageType());
        assertEquals("abc123", metadata.getChecksum());
        assertTrue(metadata.isDirectory());
        assertEquals(128L, metadata.getSizeBytes());
        assertEquals(1_000L, metadata.getLastModifiedEpochMillis());
        assertEquals(4L, metadata.getOffset());
        assertEquals(32L, metadata.getLength());
    }

    @Test
    void should_CopyExistingValuesWithoutMutatingOriginal_When_ToBuilderIsUsed() {
        // Given
        IOFaultMetadata original = IOFaultMetadata.builder()
                .fileName("source.txt")
                .filePath("/tmp/source.txt")
                .operation(IOOperation.COPY)
                .permissions(IOPermissions.READ_WRITE)
                .storageType(IOStorageType.LOCAL)
                .sizeBytes(64L)
                .build();

        // When
        IOFaultMetadata copy = original.toBuilder()
                .fileName("copy.txt")
                .operation(IOOperation.WRITE)
                .build();

        // Then
        assertEquals("source.txt", original.getFileName());
        assertEquals(IOOperation.COPY, original.getOperation());
        assertEquals("copy.txt", copy.getFileName());
        assertEquals("/tmp/source.txt", copy.getFilePath());
        assertEquals(IOOperation.WRITE, copy.getOperation());
        assertEquals(IOPermissions.READ_WRITE, copy.getPermissions());
        assertEquals(IOStorageType.LOCAL, copy.getStorageType());
        assertEquals(64L, copy.getSizeBytes());
    }

    @Test
    void should_ReturnEveryValue_When_AllArgsConstructorIsUsed() {
        // Given
        URI uri = URI.create("s3://bucket/archive.zip");

        // When
        IOFaultMetadata metadata = new IOFaultMetadata(
                "archive.zip", "bucket/archive.zip", uri, MediaType.APPLICATION_OCTET_STREAM,
                "UTF-8", IOOperation.METADATA, IOPermissions.READ_ONLY, IOStorageType.S3,
                "checksum", false, 2_048L, 5_000L, 10L, 100L
        );

        // Then
        assertEquals("archive.zip", metadata.getFileName());
        assertEquals("bucket/archive.zip", metadata.getFilePath());
        assertEquals(uri, metadata.getUri());
        assertEquals(MediaType.APPLICATION_OCTET_STREAM, metadata.getMimeType());
        assertEquals("UTF-8", metadata.getCharset());
        assertEquals(IOOperation.METADATA, metadata.getOperation());
        assertEquals(IOPermissions.READ_ONLY, metadata.getPermissions());
        assertEquals(IOStorageType.S3, metadata.getStorageType());
        assertEquals("checksum", metadata.getChecksum());
        assertFalse(metadata.isDirectory());
        assertEquals(2_048L, metadata.getSizeBytes());
        assertEquals(5_000L, metadata.getLastModifiedEpochMillis());
        assertEquals(10L, metadata.getOffset());
        assertEquals(100L, metadata.getLength());
    }
}
