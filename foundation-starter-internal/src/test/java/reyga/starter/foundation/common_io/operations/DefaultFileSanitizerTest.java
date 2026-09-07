package reyga.starter.foundation.common_io.operations;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
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
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

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

class DefaultFileSanitizerTest {

    private static final String MALICIOUS_HTML = "<script>alert('x')</script><b>safe</b>";
    private static final String SAFE_HTML = "<b>safe</b>";
    private static final String SQL_PAYLOAD = "admin' OR 1 = 1";
    private static final String SAFE_CONTENT = "ordinary customer input";

    @TempDir
    Path tempDirectory;

    private DefaultFileSanitizer sanitizer;
    private CommonLogger logger;

    @BeforeEach
    void setUp() {
        sanitizer = new DefaultFileSanitizer();
        logger = mock(CommonLogger.class);
        sanitizer.logger = logger;
    }

    @Test
    void should_RemoveUnsafeHtmlAndHonorCustomPolicy_When_StringInputIsValid() {
        // Given
        PolicyFactory formattingOnly = Sanitizers.FORMATTING;

        // When
        String defaultResult = sanitizer.sanitizeHtml(MALICIOUS_HTML);
        String customResult = sanitizer.sanitizeHtml("<a href='https://example.com'>link</a><b>safe</b>", formattingOnly);

        // Then
        assertFalse(defaultResult.contains("<script"));
        assertTrue(defaultResult.contains("safe"));
        assertFalse(customResult.contains("<a"));
        assertTrue(customResult.contains("<b>safe</b>"));
        verifyNoInteractions(logger);
    }

    @Test
    void should_SanitizeBytesAndStreams_When_CharsetIsProvidedOrDefaultsToUtf8() {
        // Given
        byte[] data = MALICIOUS_HTML.getBytes(StandardCharsets.UTF_8);
        PolicyFactory policy = Sanitizers.FORMATTING;

        // When
        String defaultBytes = sanitizer.sanitizeHtml(data);
        String charsetBytes = sanitizer.sanitizeHtml(data, StandardCharsets.UTF_8);
        String nullCharsetBytes = sanitizer.sanitizeHtml(data, null, policy);
        String defaultStream = sanitizer.sanitizeHtml(new ByteArrayInputStream(data));
        String charsetStream = sanitizer.sanitizeHtml(new ByteArrayInputStream(data), StandardCharsets.UTF_8);
        String customStream = sanitizer.sanitizeHtml(new ByteArrayInputStream(data), null, policy);

        // Then
        assertFalse(defaultBytes.contains("<script"));
        assertEquals(defaultBytes, charsetBytes);
        assertFalse(nullCharsetBytes.contains("<script"));
        assertEquals(defaultBytes, defaultStream);
        assertEquals(defaultBytes, charsetStream);
        assertFalse(customStream.contains("<script"));
        assertTrue(customStream.contains("<b>safe</b>"));
        verifyNoInteractions(logger);
    }

    @Test
    void should_SanitizeLocalAndUrlSources_When_AllOverloadsAreUsed() throws Exception {
        // Given
        Path path = Files.writeString(tempDirectory.resolve("unsafe.html"), MALICIOUS_HTML);
        File file = path.toFile();
        URL url = path.toUri().toURL();
        PolicyFactory policy = Sanitizers.FORMATTING;

        // When
        String defaultPath = sanitizer.sanitizeHtml(path);
        String charsetPath = sanitizer.sanitizeHtml(path, StandardCharsets.UTF_8);
        String customPath = sanitizer.sanitizeHtml(path, null, policy);
        String defaultFile = sanitizer.sanitizeHtml(file);
        String charsetFile = sanitizer.sanitizeHtml(file, StandardCharsets.UTF_8);
        String customFile = sanitizer.sanitizeHtml(file, null, policy);
        String defaultUrl = sanitizer.sanitizeHtml(url);
        String charsetUrl = sanitizer.sanitizeHtml(url, StandardCharsets.UTF_8);
        String customUrl = sanitizer.sanitizeHtml(url, null, policy);

        // Then
        assertEquals(defaultPath, charsetPath);
        assertFalse(customPath.contains("<script"));
        assertEquals(defaultPath, defaultFile);
        assertEquals(defaultPath, charsetFile);
        assertFalse(customFile.contains("<script"));
        assertEquals(defaultPath, defaultUrl);
        assertEquals(defaultPath, charsetUrl);
        assertFalse(customUrl.contains("<script"));
        verifyNoInteractions(logger);
    }

    @Test
    void should_DetectXssPayloadAcrossAllSources_When_UnsafeAndSafeContentAreProvided() throws Exception {
        // Given
        Path unsafePath = Files.writeString(tempDirectory.resolve("unsafe-xss.html"), MALICIOUS_HTML);
        Path safePath = Files.writeString(tempDirectory.resolve("safe-xss.html"), SAFE_HTML);
        byte[] unsafeBytes = MALICIOUS_HTML.getBytes(StandardCharsets.UTF_8);

        // When
        boolean unsafeString = sanitizer.checkXssPayload(MALICIOUS_HTML);
        boolean safeString = sanitizer.checkXssPayload(SAFE_HTML, Sanitizers.FORMATTING);
        boolean defaultBytes = sanitizer.checkXssPayload(unsafeBytes);
        boolean charsetBytes = sanitizer.checkXssPayload(unsafeBytes, StandardCharsets.UTF_8);
        boolean customBytes = sanitizer.checkXssPayload(unsafeBytes, null, Sanitizers.FORMATTING);
        boolean defaultStream = sanitizer.checkXssPayload(new ByteArrayInputStream(unsafeBytes));
        boolean charsetStream = sanitizer.checkXssPayload(new ByteArrayInputStream(unsafeBytes), StandardCharsets.UTF_8);
        boolean customStream = sanitizer.checkXssPayload(new ByteArrayInputStream(unsafeBytes), null, Sanitizers.FORMATTING);
        boolean defaultPath = sanitizer.checkXssPayload(unsafePath);
        boolean charsetPath = sanitizer.checkXssPayload(unsafePath, StandardCharsets.UTF_8);
        boolean customPath = sanitizer.checkXssPayload(unsafePath, null, Sanitizers.FORMATTING);
        boolean defaultFile = sanitizer.checkXssPayload(unsafePath.toFile());
        boolean charsetFile = sanitizer.checkXssPayload(unsafePath.toFile(), StandardCharsets.UTF_8);
        boolean customFile = sanitizer.checkXssPayload(unsafePath.toFile(), null, Sanitizers.FORMATTING);
        boolean defaultUrl = sanitizer.checkXssPayload(unsafePath.toUri().toURL());
        boolean charsetUrl = sanitizer.checkXssPayload(unsafePath.toUri().toURL(), StandardCharsets.UTF_8);
        boolean customUrl = sanitizer.checkXssPayload(unsafePath.toUri().toURL(), null, Sanitizers.FORMATTING);
        boolean safeFile = sanitizer.checkXssPayload(safePath.toFile());

        // Then
        assertTrue(unsafeString);
        assertFalse(safeString);
        assertTrue(defaultBytes);
        assertTrue(charsetBytes);
        assertTrue(customBytes);
        assertTrue(defaultStream);
        assertTrue(charsetStream);
        assertTrue(customStream);
        assertTrue(defaultPath);
        assertTrue(charsetPath);
        assertTrue(customPath);
        assertTrue(defaultFile);
        assertTrue(charsetFile);
        assertTrue(customFile);
        assertTrue(defaultUrl);
        assertTrue(charsetUrl);
        assertTrue(customUrl);
        assertFalse(safeFile);
        verifyNoInteractions(logger);
    }

    @Test
    void should_DetectDefaultSqlPatterns_When_StringContentIsMalicious() {
        // Given
        List<String> payloads = List.of(
                "OR 1 = 1", "AND 'a' = 'a'", "UNION ALL SELECT password FROM users",
                "UNION SELECT password FROM users", "SELECT password FROM users",
                "INSERT INTO users VALUES (1)", "UPDATE users SET admin = 1",
                "DELETE FROM users", "DROP TABLE users", "TRUNCATE TABLE users",
                "EXEC stored_proc", "sp_configure", "-- comment", "/* comment */",
                "WAITFOR DELAY '00:00:05'", "; ALTER TABLE users ADD admin INT"
        );

        // When
        List<Boolean> results = payloads.stream().map(sanitizer::checkSqlInjection).toList();
        boolean safeResult = sanitizer.checkSqlInjection(SAFE_CONTENT);

        // Then
        assertEquals(16, results.size());
        assertTrue(results.stream().allMatch(Boolean.TRUE::equals));
        assertFalse(safeResult);
        verifyNoInteractions(logger);
    }

    @Test
    void should_UseOnlyProvidedPatterns_When_CustomSqlPatternsAreSupplied() {
        // Given
        Pattern custom = Pattern.compile("(?i)danger-token");
        Pattern ignoredNull = null;

        // When
        boolean varargMatch = sanitizer.checkSqlInjection("DANGER-TOKEN", custom, ignoredNull);
        boolean listMatch = sanitizer.checkSqlInjection("danger-token", List.of(custom));
        boolean defaultPayloadWithCustomPatterns = sanitizer.checkSqlInjection(SQL_PAYLOAD, custom);
        boolean emptyVararg = sanitizer.checkSqlInjection(SQL_PAYLOAD, new Pattern[0]);
        boolean nullList = sanitizer.checkSqlInjection(SQL_PAYLOAD, (List<Pattern>) null);

        // Then
        assertTrue(varargMatch);
        assertTrue(listMatch);
        assertFalse(defaultPayloadWithCustomPatterns);
        assertFalse(emptyVararg);
        assertFalse(nullList);
        verifyNoInteractions(logger);
    }

    @Test
    void should_DetectSqlInjectionFromBytesAndStreams_When_AllOverloadsAreUsed() {
        // Given
        byte[] data = SQL_PAYLOAD.getBytes(StandardCharsets.UTF_8);
        Pattern custom = Pattern.compile("(?i)or\s+1\s*=\s*1");
        List<Pattern> patterns = List.of(custom);

        // When
        boolean defaultBytes = sanitizer.checkSqlInjection(data);
        boolean charsetBytes = sanitizer.checkSqlInjection(data, StandardCharsets.UTF_8);
        boolean varargBytes = sanitizer.checkSqlInjection(data, null, custom);
        boolean listBytes = sanitizer.checkSqlInjection(data, null, patterns);
        boolean defaultStream = sanitizer.checkSqlInjection(new ByteArrayInputStream(data));
        boolean charsetStream = sanitizer.checkSqlInjection(new ByteArrayInputStream(data), StandardCharsets.UTF_8);
        boolean varargStream = sanitizer.checkSqlInjection(new ByteArrayInputStream(data), null, custom);
        boolean listStream = sanitizer.checkSqlInjection(new ByteArrayInputStream(data), null, patterns);

        // Then
        assertTrue(defaultBytes);
        assertTrue(charsetBytes);
        assertTrue(varargBytes);
        assertTrue(listBytes);
        assertTrue(defaultStream);
        assertTrue(charsetStream);
        assertTrue(varargStream);
        assertTrue(listStream);
        verifyNoInteractions(logger);
    }

    @Test
    void should_DetectSqlInjectionFromLocalAndUrlSources_When_AllOverloadsAreUsed() throws Exception {
        // Given
        Path path = Files.writeString(tempDirectory.resolve("sql.txt"), SQL_PAYLOAD);
        File file = path.toFile();
        URL url = path.toUri().toURL();
        Pattern custom = Pattern.compile("(?i)or\s+1\s*=\s*1");
        List<Pattern> patterns = List.of(custom);

        // When
        boolean defaultPath = sanitizer.checkSqlInjection(path);
        boolean charsetPath = sanitizer.checkSqlInjection(path, StandardCharsets.UTF_8);
        boolean varargPath = sanitizer.checkSqlInjection(path, null, custom);
        boolean listPath = sanitizer.checkSqlInjection(path, null, patterns);
        boolean defaultFile = sanitizer.checkSqlInjection(file);
        boolean charsetFile = sanitizer.checkSqlInjection(file, StandardCharsets.UTF_8);
        boolean varargFile = sanitizer.checkSqlInjection(file, null, custom);
        boolean listFile = sanitizer.checkSqlInjection(file, null, patterns);
        boolean defaultUrl = sanitizer.checkSqlInjection(url);
        boolean charsetUrl = sanitizer.checkSqlInjection(url, StandardCharsets.UTF_8);
        boolean varargUrl = sanitizer.checkSqlInjection(url, null, custom);
        boolean listUrl = sanitizer.checkSqlInjection(url, null, patterns);

        // Then
        assertTrue(defaultPath);
        assertTrue(charsetPath);
        assertTrue(varargPath);
        assertTrue(listPath);
        assertTrue(defaultFile);
        assertTrue(charsetFile);
        assertTrue(varargFile);
        assertTrue(listFile);
        assertTrue(defaultUrl);
        assertTrue(charsetUrl);
        assertTrue(varargUrl);
        assertTrue(listUrl);
        verifyNoInteractions(logger);
    }

    @Test
    void should_ThrowExpectedFaultsWithoutLogging_When_SanitizeArgumentsAreInvalid() {
        // Given
        byte[] nullData = null;
        InputStream nullStream = null;
        Path nullPath = null;
        File nullFile = null;
        URL nullUrl = null;

        // When
        IOFaultException html = assertThrows(IOFaultException.class,
                () -> sanitizer.sanitizeHtml((String) null));
        IOFaultException policy = assertThrows(IOFaultException.class,
                () -> sanitizer.sanitizeHtml(SAFE_HTML, null));
        IOFaultException data = assertThrows(IOFaultException.class,
                () -> sanitizer.sanitizeHtml(nullData));
        IOFaultException stream = assertThrows(IOFaultException.class,
                () -> sanitizer.sanitizeHtml(nullStream));
        IOFaultException path = assertThrows(IOFaultException.class,
                () -> sanitizer.sanitizeHtml(nullPath));
        IOFaultException file = assertThrows(IOFaultException.class,
                () -> sanitizer.sanitizeHtml(nullFile));
        IOFaultException url = assertThrows(IOFaultException.class,
                () -> sanitizer.sanitizeHtml(nullUrl));

        // Then
        assertFault(html, "Html must not be null", IOOperation.SANITIZE, IOStorageType.MEMORY);
        assertFault(policy, "Policy must not be null", IOOperation.SANITIZE, IOStorageType.MEMORY);
        assertFault(data, "Data must not be null", IOOperation.SANITIZE, IOStorageType.MEMORY);
        assertFault(stream, "InputStream must not be null", IOOperation.SANITIZE, IOStorageType.MEMORY);
        assertFault(path, "Path must not be null", IOOperation.SANITIZE, IOStorageType.LOCAL);
        assertFault(file, "File must not be null", IOOperation.SANITIZE, IOStorageType.LOCAL);
        assertFault(url, "URL must not be null", IOOperation.SANITIZE, IOStorageType.HTTP);
        verifyNoInteractions(logger);
    }

    @Test
    void should_ThrowExpectedFaultsWithoutLogging_When_XssArgumentsAreInvalid() {
        // Given
        byte[] nullData = null;
        InputStream nullStream = null;
        Path nullPath = null;
        File nullFile = null;
        URL nullUrl = null;

        // When
        IOFaultException html = assertThrows(IOFaultException.class,
                () -> sanitizer.checkXssPayload((String) null));
        IOFaultException policy = assertThrows(IOFaultException.class,
                () -> sanitizer.checkXssPayload(SAFE_HTML, null));
        IOFaultException data = assertThrows(IOFaultException.class,
                () -> sanitizer.checkXssPayload(nullData));
        IOFaultException stream = assertThrows(IOFaultException.class,
                () -> sanitizer.checkXssPayload(nullStream));
        IOFaultException path = assertThrows(IOFaultException.class,
                () -> sanitizer.checkXssPayload(nullPath));
        IOFaultException file = assertThrows(IOFaultException.class,
                () -> sanitizer.checkXssPayload(nullFile));
        IOFaultException url = assertThrows(IOFaultException.class,
                () -> sanitizer.checkXssPayload(nullUrl));

        // Then
        assertFault(html, "Html must not be null", IOOperation.CHECK_XSS, IOStorageType.MEMORY);
        assertFault(policy, "Policy must not be null", IOOperation.CHECK_XSS, IOStorageType.MEMORY);
        assertFault(data, "Data must not be null", IOOperation.CHECK_XSS, IOStorageType.MEMORY);
        assertFault(stream, "InputStream must not be null", IOOperation.CHECK_XSS, IOStorageType.MEMORY);
        assertFault(path, "Path must not be null", IOOperation.CHECK_XSS, IOStorageType.LOCAL);
        assertFault(file, "File must not be null", IOOperation.CHECK_XSS, IOStorageType.LOCAL);
        assertFault(url, "URL must not be null", IOOperation.CHECK_XSS, IOStorageType.HTTP);
        verifyNoInteractions(logger);
    }

    @Test
    void should_ThrowExpectedFaultsWithoutLogging_When_SqlArgumentsAreInvalid() {
        // Given
        byte[] nullData = null;
        InputStream nullStream = null;
        Path nullPath = null;
        File nullFile = null;
        URL nullUrl = null;

        // When
        IOFaultException content = assertThrows(IOFaultException.class,
                () -> sanitizer.checkSqlInjection((String) null));
        IOFaultException data = assertThrows(IOFaultException.class,
                () -> sanitizer.checkSqlInjection(nullData));
        IOFaultException stream = assertThrows(IOFaultException.class,
                () -> sanitizer.checkSqlInjection(nullStream));
        IOFaultException path = assertThrows(IOFaultException.class,
                () -> sanitizer.checkSqlInjection(nullPath));
        IOFaultException file = assertThrows(IOFaultException.class,
                () -> sanitizer.checkSqlInjection(nullFile));
        IOFaultException url = assertThrows(IOFaultException.class,
                () -> sanitizer.checkSqlInjection(nullUrl));

        // Then
        assertFault(content, "Content must not be null", IOOperation.CHECK_SQL, IOStorageType.MEMORY);
        assertFault(data, "Data must not be null", IOOperation.CHECK_SQL, IOStorageType.MEMORY);
        assertFault(stream, "InputStream must not be null", IOOperation.CHECK_SQL, IOStorageType.MEMORY);
        assertFault(path, "Path must not be null", IOOperation.CHECK_SQL, IOStorageType.LOCAL);
        assertFault(file, "File must not be null", IOOperation.CHECK_SQL, IOStorageType.LOCAL);
        assertFault(url, "URL must not be null", IOOperation.CHECK_SQL, IOStorageType.HTTP);
        verifyNoInteractions(logger);
    }

    @Test
    void should_WrapAndLogCause_When_StreamReadOperationsFail() {
        // Given
        IOException sanitizeCause = new IOException("sanitize read failed");
        IOException xssCause = new IOException("xss read failed");
        IOException sqlCause = new IOException("sql read failed");

        // When
        IOFaultException sanitize = assertThrows(IOFaultException.class,
                () -> sanitizer.sanitizeHtml(failingInputStream(sanitizeCause)));
        IOFaultException xss = assertThrows(IOFaultException.class,
                () -> sanitizer.checkXssPayload(failingInputStream(xssCause)));
        IOFaultException sql = assertThrows(IOFaultException.class,
                () -> sanitizer.checkSqlInjection(failingInputStream(sqlCause)));

        // Then
        assertFault(sanitize, "Failed to sanitize content", IOOperation.SANITIZE, IOStorageType.MEMORY);
        assertSame(sanitizeCause, sanitize.getCause());
        assertFault(xss, "Failed to check XSS payload", IOOperation.CHECK_XSS, IOStorageType.MEMORY);
        assertSame(xssCause, xss.getCause());
        assertFault(sql, "Failed to check SQL injection", IOOperation.CHECK_SQL, IOStorageType.MEMORY);
        assertSame(sqlCause, sql.getCause());
        verify(logger, atLeastOnce()).warn(eq("Error Message : {}"), any(Object[].class));
    }

    @Test
    void should_IncludeLocalMetadataAndLog_When_PathReadOperationsFail() {
        // Given
        Path missing = tempDirectory.resolve("missing.html");

        // When
        IOFaultException sanitize = assertThrows(IOFaultException.class,
                () -> sanitizer.sanitizeHtml(missing));
        IOFaultException xss = assertThrows(IOFaultException.class,
                () -> sanitizer.checkXssPayload(missing));
        IOFaultException sql = assertThrows(IOFaultException.class,
                () -> sanitizer.checkSqlInjection(missing));

        // Then
        assertLocalFault(sanitize, "Failed to sanitize content", IOOperation.SANITIZE, missing);
        assertLocalFault(xss, "Failed to check XSS payload", IOOperation.CHECK_XSS, missing);
        assertLocalFault(sql, "Failed to check SQL injection", IOOperation.CHECK_SQL, missing);
        assertNotNull(sanitize.getCause());
        assertNotNull(xss.getCause());
        assertNotNull(sql.getCause());
        verify(logger, atLeastOnce()).warn(eq("Error Message : {}"), any(Object[].class));
    }

    @Test
    void should_IncludeRemoteStorageMetadataAndLog_When_UrlReadOperationsFail() throws Exception {
        // Given
        URL httpsUrl = failingUrl("https", new IOException("https failed"));
        URL ftpUrl = failingUrl("ftp", new IOException("ftp failed"));
        URL sftpUrl = failingUrl("sftp", new IOException("sftp failed"));

        // When
        IOFaultException sanitize = assertThrows(IOFaultException.class,
                () -> sanitizer.sanitizeHtml(httpsUrl));
        IOFaultException xss = assertThrows(IOFaultException.class,
                () -> sanitizer.checkXssPayload(ftpUrl));
        IOFaultException sql = assertThrows(IOFaultException.class,
                () -> sanitizer.checkSqlInjection(sftpUrl));

        // Then
        assertUrlFault(sanitize, "Failed to sanitize content", IOOperation.SANITIZE,
                IOStorageType.HTTPS, httpsUrl);
        assertUrlFault(xss, "Failed to check XSS payload", IOOperation.CHECK_XSS,
                IOStorageType.FTP, ftpUrl);
        assertUrlFault(sql, "Failed to check SQL injection", IOOperation.CHECK_SQL,
                IOStorageType.SFTP, sftpUrl);
        assertNotNull(sanitize.getCause());
        assertNotNull(xss.getCause());
        assertNotNull(sql.getCause());
        verify(logger, atLeastOnce()).warn(eq("Error Message : {}"), any(Object[].class));
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
        assertEquals(StandardCharsets.UTF_8.name(), metadata.getCharset());
        assertNull(exception.getMetadataList());
    }
}
