package reyga.starter.foundation.common_io.enumeration;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CommonIoEnumerationTest {

    @Test
    void should_ReturnAllSupportedOperations_When_OperationValuesAreRead() {
        // Given
        List<IOOperation> expected = List.of(
                IOOperation.READ, IOOperation.WRITE, IOOperation.APPEND, IOOperation.DELETE,
                IOOperation.COPY, IOOperation.MOVE, IOOperation.RENAME, IOOperation.CREATE,
                IOOperation.OPEN, IOOperation.CLOSE, IOOperation.SEEK, IOOperation.LIST,
                IOOperation.MKDIR, IOOperation.MKDIRS, IOOperation.EXISTS, IOOperation.METADATA,
                IOOperation.DETECT_CONTENT_TYPE, IOOperation.PARSE_TO_STRING, IOOperation.PARSE,
                IOOperation.TRANSLATE, IOOperation.SANITIZE, IOOperation.CHECK_XSS,
                IOOperation.CHECK_SQL, IOOperation.VALIDATE
        );

        // When
        List<IOOperation> result = List.of(IOOperation.values());

        // Then
        assertEquals(expected, result);
        assertEquals(IOOperation.CHECK_SQL, IOOperation.valueOf("CHECK_SQL"));
    }

    @Test
    void should_ReturnAllSupportedPermissions_When_PermissionValuesAreRead() {
        // Given
        List<IOPermissions> expected = List.of(
                IOPermissions.READ_ONLY, IOPermissions.WRITE_ONLY, IOPermissions.READ_WRITE,
                IOPermissions.EXECUTE_ONLY, IOPermissions.NO_ACCESS
        );

        // When
        List<IOPermissions> result = List.of(IOPermissions.values());

        // Then
        assertEquals(expected, result);
        assertEquals(IOPermissions.READ_WRITE, IOPermissions.valueOf("READ_WRITE"));
    }

    @Test
    void should_ReturnAllSupportedStorageTypes_When_StorageTypeValuesAreRead() {
        // Given
        List<IOStorageType> expected = List.of(
                IOStorageType.LOCAL, IOStorageType.NFS, IOStorageType.SMB, IOStorageType.FTP,
                IOStorageType.SFTP, IOStorageType.HTTP, IOStorageType.HTTPS, IOStorageType.S3,
                IOStorageType.GCS, IOStorageType.AZURE_BLOB, IOStorageType.HDFS,
                IOStorageType.DATABASE, IOStorageType.MEMORY, IOStorageType.TEMP
        );

        // When
        List<IOStorageType> result = List.of(IOStorageType.values());

        // Then
        assertEquals(expected, result);
        assertEquals(IOStorageType.AZURE_BLOB, IOStorageType.valueOf("AZURE_BLOB"));
    }

    @Test
    void should_ReturnExtensionAndMimeType_When_AllReportTypesAreRead() {
        // Given
        List<String> expectedExtensions = List.of(
                "pdf", "html", "xls", "xlsx", "csv", "docx", "pptx", "rtf", "odt",
                "ods", "odp", "xml", "json", "txt", "jrprint", "png", "jpg", "gif", "svg"
        );
        List<String> expectedMimeTypes = List.of(
                "application/pdf", "text/html", "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "text/csv",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                "application/rtf", "application/vnd.oasis.opendocument.text",
                "application/vnd.oasis.opendocument.spreadsheet",
                "application/vnd.oasis.opendocument.presentation", "application/xml",
                "application/json", "text/plain", "application/octet-stream", "image/png",
                "image/jpeg", "image/gif", "image/svg+xml"
        );

        // When
        List<ReportType> types = List.of(ReportType.values());
        List<String> extensions = types.stream().map(ReportType::getExtension).toList();
        List<String> mimeTypes = types.stream().map(ReportType::getMimeType).toList();

        // Then
        assertEquals(19, types.size());
        assertEquals(expectedExtensions, extensions);
        assertEquals(expectedMimeTypes, mimeTypes);
        assertEquals(ReportType.JPEG, ReportType.valueOf("JPEG"));
    }
}
