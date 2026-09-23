package reyga.starter.foundation.common_io.operations;

import org.apache.tika.metadata.Metadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import reyga.starter.foundation.common.logging.CommonLogger;
import reyga.starter.foundation.common_io.enumeration.IOOperation;
import reyga.starter.foundation.common_io.enumeration.IOPermissions;
import reyga.starter.foundation.common_io.enumeration.IOStorageType;
import reyga.starter.foundation.common_io.exception.IOFaultException;
import reyga.starter.foundation.common_io.exception.IOFaultMetadata;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class DefaultFileInspectorTest {

    private static final byte[] TEXT_BYTES = "foundation content".getBytes(StandardCharsets.UTF_8);

    @TempDir
    Path tempDirectory;

    private DefaultFileInspector inspector;
    private CommonLogger logger;

    @BeforeEach
    void setUp() {
        inspector = new DefaultFileInspector();
        logger = mock(CommonLogger.class);
        inspector.logger = logger;
    }

    @Test
    void should_DetectContentTypes_When_AllSupportedSourcesAreValid() throws Exception {
        // Given
        Path path = Files.write(tempDirectory.resolve("sample.txt"), TEXT_BYTES);
        File file = path.toFile();
        URL url = path.toUri().toURL();
        Metadata metadata = new Metadata();
        metadata.set("resourceName", "sample.txt");

        // When
        String streamType = inspector.checkContentType(new ByteArrayInputStream(TEXT_BYTES));
        String metadataType = inspector.checkContentType(new ByteArrayInputStream(TEXT_BYTES), metadata);
        String namedStreamType = inspector.checkContentType(new ByteArrayInputStream(TEXT_BYTES), "sample.txt");
        String bytesType = inspector.checkContentType(TEXT_BYTES);
        String namedBytesType = inspector.checkContentType(TEXT_BYTES, "sample.txt");
        String fileType = inspector.checkContentType(file);
        String pathType = inspector.checkContentType(path);
        String nameType = inspector.checkContentType("sample.txt");
        String urlType = inspector.checkContentType(url);

        // Then
        assertEquals("text/plain", streamType);
        assertEquals("text/plain", metadataType);
        assertEquals("text/plain", namedStreamType);
        assertEquals("text/plain", bytesType);
        assertEquals("text/plain", namedBytesType);
        assertEquals("text/plain", fileType);
        assertEquals("text/plain", pathType);
        assertEquals("text/plain", nameType);
        assertEquals("text/plain", urlType);
        verifyNoInteractions(logger);
    }

    @Test
    void should_ExtractContentAsString_When_AllSupportedSourcesAreValid() throws Exception {
        // Given
        Path path = Files.write(tempDirectory.resolve("content.txt"), TEXT_BYTES);
        Metadata metadata = new Metadata();

        // When
        String streamContent = inspector.extractContent(new ByteArrayInputStream(TEXT_BYTES));
        String metadataContent = inspector.extractContent(new ByteArrayInputStream(TEXT_BYTES), metadata);
        String limitedContent = inspector.extractContent(new ByteArrayInputStream(TEXT_BYTES), new Metadata(), 10);
        String urlContent = inspector.extractContent(path.toUri().toURL());
        String fileContent = inspector.extractContent(path.toFile());
        String pathContent = inspector.extractContent(path);

        // Then
        assertNotNull(streamContent);
        assertNotNull(metadataContent);
        assertNotNull(limitedContent);
        assertNotNull(urlContent);
        assertNotNull(fileContent);
        assertNotNull(pathContent);
        assertTrue(limitedContent.length() <= 10);
        verifyNoInteractions(logger);
    }

    @Test
    void should_ReturnReadableReaders_When_AllSupportedParseSourcesAreValid() throws Exception {
        // Given
        Path path = Files.write(tempDirectory.resolve("reader.txt"), TEXT_BYTES);
        Metadata metadata = new Metadata();

        // When
        Reader streamReader = inspector.extract(new ByteArrayInputStream(TEXT_BYTES));
        Reader metadataReader = inspector.extract(new ByteArrayInputStream(TEXT_BYTES), metadata);
        Reader urlReader = inspector.extract(path.toUri().toURL());
        Reader fileReader = inspector.extract(path.toFile());
        Reader fileMetadataReader = inspector.extract(path.toFile(), new Metadata());
        Reader pathReader = inspector.extract(path);
        Reader pathMetadataReader = inspector.extract(path, new Metadata());

        // Then
        assertNotNull(streamReader);
        assertNotNull(metadataReader);
        assertNotNull(urlReader);
        assertNotNull(fileReader);
        assertNotNull(fileMetadataReader);
        assertNotNull(pathReader);
        assertNotNull(pathMetadataReader);
        assertFalse(streamReader.read() < -1);
        assertFalse(metadataReader.read() < -1);
        assertFalse(urlReader.read() < -1);
        assertFalse(fileReader.read() < -1);
        assertFalse(fileMetadataReader.read() < -1);
        assertFalse(pathReader.read() < -1);
        assertFalse(pathMetadataReader.read() < -1);
        verifyNoInteractions(logger);
    }

    @Test
    void should_ThrowExpectedFaultsWithoutLogging_When_DetectionArgumentsAreInvalid() {
        // Given
        InputStream nullStream = null;
        byte[] nullData = null;
        File nullFile = null;
        Path nullPath = null;
        URL nullUrl = null;

        // When
        IOFaultException stream = assertThrows(IOFaultException.class,
                () -> inspector.checkContentType(nullStream));
        IOFaultException streamMetadata = assertThrows(IOFaultException.class,
                () -> inspector.checkContentType(nullStream, new Metadata()));
        IOFaultException namedStream = assertThrows(IOFaultException.class,
                () -> inspector.checkContentType(nullStream, "sample.txt"));
        IOFaultException bytes = assertThrows(IOFaultException.class,
                () -> inspector.checkContentType(nullData));
        IOFaultException namedBytes = assertThrows(IOFaultException.class,
                () -> inspector.checkContentType(nullData, "sample.txt"));
        IOFaultException file = assertThrows(IOFaultException.class,
                () -> inspector.checkContentType(nullFile));
        IOFaultException path = assertThrows(IOFaultException.class,
                () -> inspector.checkContentType(nullPath));
        IOFaultException nullName = assertThrows(IOFaultException.class,
                () -> inspector.checkContentType((String) null));
        IOFaultException blankName = assertThrows(IOFaultException.class,
                () -> inspector.checkContentType("   "));
        IOFaultException url = assertThrows(IOFaultException.class,
                () -> inspector.checkContentType(nullUrl));

        // Then
        assertFault(stream, "InputStream must not be null", IOOperation.DETECT_CONTENT_TYPE, IOStorageType.MEMORY);
        assertFault(streamMetadata, "InputStream must not be null", IOOperation.DETECT_CONTENT_TYPE, IOStorageType.MEMORY);
        assertFault(namedStream, "InputStream must not be null", IOOperation.DETECT_CONTENT_TYPE, IOStorageType.MEMORY);
        assertFault(bytes, "Data must not be null", IOOperation.DETECT_CONTENT_TYPE, IOStorageType.MEMORY);
        assertFault(namedBytes, "Data must not be null", IOOperation.DETECT_CONTENT_TYPE, IOStorageType.MEMORY);
        assertFault(file, "File must not be null", IOOperation.DETECT_CONTENT_TYPE, IOStorageType.LOCAL);
        assertFault(path, "Path must not be null", IOOperation.DETECT_CONTENT_TYPE, IOStorageType.LOCAL);
        assertFault(nullName, "Name must not be null or blank", IOOperation.DETECT_CONTENT_TYPE, IOStorageType.MEMORY);
        assertFault(blankName, "Name must not be null or blank", IOOperation.DETECT_CONTENT_TYPE, IOStorageType.MEMORY);
        assertFault(url, "URL must not be null", IOOperation.DETECT_CONTENT_TYPE, IOStorageType.HTTP);
        verifyNoInteractions(logger);
    }

    @Test
    void should_ThrowExpectedFaultsWithoutLogging_When_StringExtractionArgumentsAreNull() {
        // Given
        InputStream nullStream = null;
        URL nullUrl = null;
        File nullFile = null;
        Path nullPath = null;

        // When
        IOFaultException stream = assertThrows(IOFaultException.class,
                () -> inspector.extractContent(nullStream));
        IOFaultException metadata = assertThrows(IOFaultException.class,
                () -> inspector.extractContent(nullStream, new Metadata()));
        IOFaultException limited = assertThrows(IOFaultException.class,
                () -> inspector.extractContent(nullStream, new Metadata(), 10));
        IOFaultException url = assertThrows(IOFaultException.class,
                () -> inspector.extractContent(nullUrl));
        IOFaultException file = assertThrows(IOFaultException.class,
                () -> inspector.extractContent(nullFile));
        IOFaultException path = assertThrows(IOFaultException.class,
                () -> inspector.extractContent(nullPath));

        // Then
        assertFault(stream, "InputStream must not be null", IOOperation.PARSE_TO_STRING, IOStorageType.MEMORY);
        assertFault(metadata, "InputStream must not be null", IOOperation.PARSE_TO_STRING, IOStorageType.MEMORY);
        assertFault(limited, "InputStream must not be null", IOOperation.PARSE_TO_STRING, IOStorageType.MEMORY);
        assertFault(url, "URL must not be null", IOOperation.PARSE_TO_STRING, IOStorageType.HTTP);
        assertFault(file, "File must not be null", IOOperation.PARSE_TO_STRING, IOStorageType.LOCAL);
        assertFault(path, "Path must not be null", IOOperation.PARSE_TO_STRING, IOStorageType.LOCAL);
        verifyNoInteractions(logger);
    }

    @Test
    void should_ThrowExpectedFaultsWithoutLogging_When_ReaderExtractionArgumentsAreNull() {
        // Given
        InputStream nullStream = null;
        URL nullUrl = null;
        File nullFile = null;
        Path nullPath = null;

        // When
        IOFaultException stream = assertThrows(IOFaultException.class, () -> inspector.extract(nullStream));
        IOFaultException metadata = assertThrows(IOFaultException.class,
                () -> inspector.extract(nullStream, new Metadata()));
        IOFaultException url = assertThrows(IOFaultException.class, () -> inspector.extract(nullUrl));
        IOFaultException file = assertThrows(IOFaultException.class, () -> inspector.extract(nullFile));
        IOFaultException fileMetadata = assertThrows(IOFaultException.class,
                () -> inspector.extract(nullFile, new Metadata()));
        IOFaultException path = assertThrows(IOFaultException.class, () -> inspector.extract(nullPath));
        IOFaultException pathMetadata = assertThrows(IOFaultException.class,
                () -> inspector.extract(nullPath, new Metadata()));

        // Then
        assertFault(stream, "InputStream must not be null", IOOperation.PARSE, IOStorageType.MEMORY);
        assertFault(metadata, "InputStream must not be null", IOOperation.PARSE, IOStorageType.MEMORY);
        assertFault(url, "URL must not be null", IOOperation.PARSE, IOStorageType.HTTP);
        assertFault(file, "File must not be null", IOOperation.PARSE, IOStorageType.LOCAL);
        assertFault(fileMetadata, "File must not be null", IOOperation.PARSE, IOStorageType.LOCAL);
        assertFault(path, "Path must not be null", IOOperation.PARSE, IOStorageType.LOCAL);
        assertFault(pathMetadata, "Path must not be null", IOOperation.PARSE, IOStorageType.LOCAL);
        verifyNoInteractions(logger);
    }

    @Test
    void should_WrapAndLogCause_When_InputStreamDetectionFails() {
        // Given
        IOException cause = new IOException("stream failed");
        InputStream inputStream = failingInputStream(cause);

        // When
        IOFaultException exception = assertThrows(IOFaultException.class,
                () -> inspector.checkContentType(inputStream));

        // Then
        assertFault(exception, "Failed to detect content type", IOOperation.DETECT_CONTENT_TYPE, IOStorageType.MEMORY);
        assertSame(cause, exception.getCause());
        verify(logger, atLeastOnce()).warn(eq("Error Message :"), any(Object[].class));
    }

    @Test
    void should_IncludeLocalSourceMetadataAndLog_When_FileOperationsFail() {
        // Given
        Path missingPath = tempDirectory.resolve("missing.txt");
        File missingFile = missingPath.toFile();

        // When
        IOFaultException detection = assertThrows(IOFaultException.class,
                () -> inspector.checkContentType(missingFile));
        IOFaultException content = assertThrows(IOFaultException.class,
                () -> inspector.extractContent(missingPath));
        IOFaultException reader = assertThrows(IOFaultException.class,
                () -> inspector.extract(missingFile, new Metadata()));

        // Then
        assertLocalFault(detection, "Failed to detect content type", IOOperation.DETECT_CONTENT_TYPE, missingPath);
        assertLocalFault(content, "Failed to extract content", IOOperation.PARSE_TO_STRING, missingPath);
        assertLocalFault(reader, "Failed to parse content", IOOperation.PARSE, missingPath);
        assertNotNull(detection.getCause());
        assertNotNull(content.getCause());
        assertNotNull(reader.getCause());
        verify(logger, atLeastOnce()).warn(eq("Error Message :"), any(Object[].class));
    }

    @Test
    void should_IncludeRemoteStorageMetadataAndLog_When_UrlOperationsFail() throws Exception {
        // Given
        URL httpsUrl = failingUrl("https", new IOException("https failed"));
        URL ftpUrl = failingUrl("ftp", new IOException("ftp failed"));
        URL sftpUrl = failingUrl("sftp", new IOException("sftp failed"));

        // When
        IOFaultException detection = assertThrows(IOFaultException.class,
                () -> inspector.checkContentType(httpsUrl));
        IOFaultException content = assertThrows(IOFaultException.class,
                () -> inspector.extractContent(ftpUrl));
        IOFaultException reader = assertThrows(IOFaultException.class,
                () -> inspector.extract(sftpUrl));

        // Then
        assertUrlFault(detection, "Failed to detect content type", IOOperation.DETECT_CONTENT_TYPE,
                IOStorageType.HTTPS, httpsUrl);
        assertUrlFault(content, "Failed to extract content", IOOperation.PARSE_TO_STRING,
                IOStorageType.FTP, ftpUrl);
        assertUrlFault(reader, "Failed to parse content", IOOperation.PARSE,
                IOStorageType.SFTP, sftpUrl);
        assertNotNull(detection.getCause());
        assertNotNull(content.getCause());
        assertNotNull(reader.getCause());
        verify(logger, atLeastOnce()).warn(eq("Error Message :"), any(Object[].class));
    }

    @Test
    void should_ThrowTranslationFaultWithoutLogging_When_TextIsNull() {
        // Given
        String text = null;

        // When
        IOFaultException targetOnly = assertThrows(IOFaultException.class,
                () -> inspector.translate(text, "id"));
        IOFaultException sourceAndTarget = assertThrows(IOFaultException.class,
                () -> inspector.translate(text, "en", "id"));

        // Then
        assertFault(targetOnly, "Text must not be null", IOOperation.TRANSLATE, IOStorageType.MEMORY);
        assertFault(sourceAndTarget, "Text must not be null", IOOperation.TRANSLATE, IOStorageType.MEMORY);
        verifyNoInteractions(logger);
    }

    @Test
    void should_WrapAndLogTranslationFailure_When_TranslatorIsUnavailable() {
        // Given
        String text = "hello";

        // When
        IOFaultException targetOnly = assertThrows(IOFaultException.class,
                () -> inspector.translate(text, "id"));
        IOFaultException sourceAndTarget = assertThrows(IOFaultException.class,
                () -> inspector.translate(text, "en", "id"));

        // Then
        assertFault(targetOnly, "Failed to translate content", IOOperation.TRANSLATE, IOStorageType.MEMORY);
        assertFault(sourceAndTarget, "Failed to translate content", IOOperation.TRANSLATE, IOStorageType.MEMORY);
        assertNotNull(targetOnly.getCause());
        assertNotNull(sourceAndTarget.getCause());
        verify(logger, atLeastOnce()).warn(eq("Error Message :"), any(Object[].class));
    }

    private InputStream failingInputStream(IOException cause) {
        return new InputStream() {
            @Override
            public int read() throws IOException {
                throw cause;
            }
        };
    }

    private URL failingUrl(String protocol, IOException cause) throws Exception {
        return new URL(null, protocol + "://example.test/file.txt", new URLStreamHandler() {
            @Override
            protected URLConnection openConnection(URL url) {
                return new URLConnection(url) {
                    @Override
                    public void connect() {
                    }

                    @Override
                    public InputStream getInputStream() throws IOException {
                        throw cause;
                    }
                };
            }
        });
    }

    private void assertUrlFault(IOFaultException exception, String message, IOOperation operation,
                                IOStorageType storageType, URL url) throws Exception {
        assertFault(exception, message, operation, storageType);
        assertEquals(url.toURI(), exception.getMetadata().getUri());
    }

    private void assertLocalFault(IOFaultException exception, String message, IOOperation operation, Path path) {
        assertFault(exception, message, operation, IOStorageType.LOCAL);
        assertEquals(path.getFileName().toString(), exception.getMetadata().getFileName());
        assertEquals(path.toString(), exception.getMetadata().getFilePath());
        assertEquals(path.toUri(), exception.getMetadata().getUri());
    }

    private void assertFault(IOFaultException exception, String message, IOOperation operation,
                             IOStorageType storageType) {
        assertEquals(message, exception.getMessage());
        IOFaultMetadata metadata = exception.getMetadata();
        assertNotNull(metadata);
        assertEquals(operation, metadata.getOperation());
        assertEquals(IOPermissions.READ_ONLY, metadata.getPermissions());
        assertEquals(storageType, metadata.getStorageType());
        assertNull(exception.getMetadataList());
    }
}
