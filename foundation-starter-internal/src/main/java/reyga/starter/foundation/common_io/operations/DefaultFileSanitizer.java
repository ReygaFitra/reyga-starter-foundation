package reyga.starter.foundation.common_io.operations;

import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import reyga.starter.foundation.common.logging.BaseLogging;
import reyga.starter.foundation.common_io.enumeration.IOOperation;
import reyga.starter.foundation.common_io.enumeration.IOPermissions;
import reyga.starter.foundation.common_io.enumeration.IOStorageType;
import reyga.starter.foundation.common_io.exception.IOFaultException;
import reyga.starter.foundation.common_io.exception.IOFaultMetadata;
import reyga.starter.foundation.common_io.security.SqlInjectionPatterns;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

public class DefaultFileSanitizer extends BaseLogging implements FileSanitizer {

    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private static final PolicyFactory DEFAULT_POLICY = Sanitizers.FORMATTING
            .and(Sanitizers.BLOCKS)
            .and(Sanitizers.LINKS)
            .and(Sanitizers.IMAGES)
            .and(Sanitizers.STYLES);
    private static final List<Pattern> SQL_INJECTION_PATTERNS = SqlInjectionPatterns.defaults();

    @Override
    public String sanitizeHtml(String html) {
        return sanitizeHtml(html, DEFAULT_POLICY);
    }

    @Override
    public String sanitizeHtml(String html, PolicyFactory policy) {
        if (html == null) {
            throw new IOFaultException("Html must not be null", metadataFor(IOOperation.SANITIZE, DEFAULT_CHARSET, IOStorageType.MEMORY));
        }
        if (policy == null) {
            throw new IOFaultException("Policy must not be null", metadataFor(IOOperation.SANITIZE, DEFAULT_CHARSET, IOStorageType.MEMORY));
        }
        return policy.sanitize(html);
    }

    @Override
    public String sanitizeHtml(byte[] data) {
        return sanitizeHtml(data, DEFAULT_CHARSET, DEFAULT_POLICY);
    }

    @Override
    public String sanitizeHtml(byte[] data, Charset charset) {
        return sanitizeHtml(data, charset, DEFAULT_POLICY);
    }

    @Override
    public String sanitizeHtml(byte[] data, Charset charset, PolicyFactory policy) {
        if (data == null) {
            throw new IOFaultException("Data must not be null", metadataFor(IOOperation.SANITIZE, DEFAULT_CHARSET, IOStorageType.MEMORY));
        }
        Charset resolvedCharset = charset != null ? charset : DEFAULT_CHARSET;
        return sanitizeHtml(new String(data, resolvedCharset), policy);
    }

    @Override
    public String sanitizeHtml(InputStream inputStream) {
        return sanitizeHtml(inputStream, DEFAULT_CHARSET, DEFAULT_POLICY);
    }

    @Override
    public String sanitizeHtml(InputStream inputStream, Charset charset) {
        return sanitizeHtml(inputStream, charset, DEFAULT_POLICY);
    }

    @Override
    public String sanitizeHtml(InputStream inputStream, Charset charset, PolicyFactory policy) {
        if (inputStream == null) {
            throw new IOFaultException("InputStream must not be null", metadataFor(IOOperation.SANITIZE, DEFAULT_CHARSET, IOStorageType.MEMORY));
        }
        try {
            return sanitizeHtml(inputStream.readAllBytes(), charset, policy);
        } catch (IOException ex) {
            printLog(ex);
            throw new IOFaultException("Failed to sanitize content", metadataFor(IOOperation.SANITIZE, DEFAULT_CHARSET, IOStorageType.MEMORY), ex);
        }
    }

    @Override
    public String sanitizeHtml(Path path) {
        return sanitizeHtml(path, DEFAULT_CHARSET, DEFAULT_POLICY);
    }

    @Override
    public String sanitizeHtml(Path path, Charset charset) {
        return sanitizeHtml(path, charset, DEFAULT_POLICY);
    }

    @Override
    public String sanitizeHtml(Path path, Charset charset, PolicyFactory policy) {
        if (path == null) {
            throw new IOFaultException("Path must not be null", metadataFor(IOOperation.SANITIZE, DEFAULT_CHARSET, IOStorageType.LOCAL));
        }
        try {
            Charset resolvedCharset = charset != null ? charset : DEFAULT_CHARSET;
            return sanitizeHtml(Files.readString(path, resolvedCharset), policy);
        } catch (IOException ex) {
            printLog(ex);
            throw new IOFaultException("Failed to sanitize content", metadataFor(IOOperation.SANITIZE, DEFAULT_CHARSET, IOStorageType.LOCAL).toBuilder()
                    .fileName(path.getFileName() != null ? path.getFileName().toString() : null)
                    .filePath(path.toString())
                    .uri(path.toUri())
                    .build(), ex);
        }
    }

    @Override
    public String sanitizeHtml(File file) {
        if (file == null) {
            throw new IOFaultException("File must not be null", metadataFor(IOOperation.SANITIZE, DEFAULT_CHARSET, IOStorageType.LOCAL));
        }
        return sanitizeHtml(file.toPath(), DEFAULT_CHARSET, DEFAULT_POLICY);
    }

    @Override
    public String sanitizeHtml(File file, Charset charset) {
        if (file == null) {
            throw new IOFaultException("File must not be null", metadataFor(IOOperation.SANITIZE, DEFAULT_CHARSET, IOStorageType.LOCAL));
        }
        return sanitizeHtml(file.toPath(), charset, DEFAULT_POLICY);
    }

    @Override
    public String sanitizeHtml(File file, Charset charset, PolicyFactory policy) {
        if (file == null) {
            throw new IOFaultException("File must not be null", metadataFor(IOOperation.SANITIZE, DEFAULT_CHARSET, IOStorageType.LOCAL));
        }
        return sanitizeHtml(file.toPath(), charset, policy);
    }

    @Override
    public String sanitizeHtml(URL url) {
        return sanitizeHtml(url, DEFAULT_CHARSET, DEFAULT_POLICY);
    }

    @Override
    public String sanitizeHtml(URL url, Charset charset) {
        return sanitizeHtml(url, charset, DEFAULT_POLICY);
    }

    @Override
    public String sanitizeHtml(URL url, Charset charset, PolicyFactory policy) {
        if (url == null) {
            throw new IOFaultException("URL must not be null", metadataFor(IOOperation.SANITIZE, DEFAULT_CHARSET, storageTypeForUrl(url)));
        }
        try (InputStream inputStream = url.openStream()) {
            return sanitizeHtml(inputStream, charset, policy);
        } catch (IOException ex) {
            printLog(ex);
            throw new IOFaultException("Failed to sanitize content", metadataFor(IOOperation.SANITIZE, DEFAULT_CHARSET, storageTypeForUrl(url)).toBuilder()
                    .uri(toUriSafe(url))
                    .build(), ex);
        }
    }

    @Override
    public boolean checkXssPayload(String html) {
        return checkXssPayload(html, DEFAULT_POLICY);
    }

    @Override
    public boolean checkXssPayload(String html, PolicyFactory policy) {
        if (html == null) {
            throw new IOFaultException("Html must not be null", metadataFor(IOOperation.CHECK_XSS, DEFAULT_CHARSET, IOStorageType.MEMORY));
        }
        if (policy == null) {
            throw new IOFaultException("Policy must not be null", metadataFor(IOOperation.CHECK_XSS, DEFAULT_CHARSET, IOStorageType.MEMORY));
        }
        String sanitized = sanitizeHtml(html, policy);
        return !sanitized.equals(html);
    }

    @Override
    public boolean checkXssPayload(byte[] data) {
        return checkXssPayload(data, DEFAULT_CHARSET, DEFAULT_POLICY);
    }

    @Override
    public boolean checkXssPayload(byte[] data, Charset charset) {
        return checkXssPayload(data, charset, DEFAULT_POLICY);
    }

    @Override
    public boolean checkXssPayload(byte[] data, Charset charset, PolicyFactory policy) {
        if (data == null) {
            throw new IOFaultException("Data must not be null", metadataFor(IOOperation.CHECK_XSS, DEFAULT_CHARSET, IOStorageType.MEMORY));
        }
        Charset resolvedCharset = charset != null ? charset : DEFAULT_CHARSET;
        String original = new String(data, resolvedCharset);
        String sanitized = sanitizeHtml(original, policy);
        return !sanitized.equals(original);
    }

    @Override
    public boolean checkXssPayload(InputStream inputStream) {
        return checkXssPayload(inputStream, DEFAULT_CHARSET, DEFAULT_POLICY);
    }

    @Override
    public boolean checkXssPayload(InputStream inputStream, Charset charset) {
        return checkXssPayload(inputStream, charset, DEFAULT_POLICY);
    }

    @Override
    public boolean checkXssPayload(InputStream inputStream, Charset charset, PolicyFactory policy) {
        if (inputStream == null) {
            throw new IOFaultException("InputStream must not be null", metadataFor(IOOperation.CHECK_XSS, DEFAULT_CHARSET, IOStorageType.MEMORY));
        }
        try {
            return checkXssPayload(inputStream.readAllBytes(), charset, policy);
        } catch (IOException ex) {
            printLog(ex);
            throw new IOFaultException("Failed to check XSS payload", metadataFor(IOOperation.CHECK_XSS, DEFAULT_CHARSET, IOStorageType.MEMORY), ex);
        }
    }

    @Override
    public boolean checkXssPayload(Path path) {
        return checkXssPayload(path, DEFAULT_CHARSET, DEFAULT_POLICY);
    }

    @Override
    public boolean checkXssPayload(Path path, Charset charset) {
        return checkXssPayload(path, charset, DEFAULT_POLICY);
    }

    @Override
    public boolean checkXssPayload(Path path, Charset charset, PolicyFactory policy) {
        if (path == null) {
            throw new IOFaultException("Path must not be null", metadataFor(IOOperation.CHECK_XSS, DEFAULT_CHARSET, IOStorageType.LOCAL));
        }
        try {
            Charset resolvedCharset = charset != null ? charset : DEFAULT_CHARSET;
            String original = Files.readString(path, resolvedCharset);
            String sanitized = sanitizeHtml(original, policy);
            return !sanitized.equals(original);
        } catch (IOException ex) {
            printLog(ex);
            throw new IOFaultException("Failed to check XSS payload", metadataFor(IOOperation.CHECK_XSS, DEFAULT_CHARSET, IOStorageType.LOCAL).toBuilder()
                    .fileName(path.getFileName() != null ? path.getFileName().toString() : null)
                    .filePath(path.toString())
                    .uri(path.toUri())
                    .build(), ex);
        }
    }

    @Override
    public boolean checkXssPayload(File file) {
        return checkXssPayload(file, DEFAULT_CHARSET, DEFAULT_POLICY);
    }

    @Override
    public boolean checkXssPayload(File file, Charset charset) {
        return checkXssPayload(file, charset, DEFAULT_POLICY);
    }

    @Override
    public boolean checkXssPayload(File file, Charset charset, PolicyFactory policy) {
        if (file == null) {
            throw new IOFaultException("File must not be null", metadataFor(IOOperation.CHECK_XSS, DEFAULT_CHARSET, IOStorageType.LOCAL));
        }
        return checkXssPayload(file.toPath(), charset, policy);
    }

    @Override
    public boolean checkXssPayload(URL url) {
        return checkXssPayload(url, DEFAULT_CHARSET, DEFAULT_POLICY);
    }

    @Override
    public boolean checkXssPayload(URL url, Charset charset) {
        return checkXssPayload(url, charset, DEFAULT_POLICY);
    }

    @Override
    public boolean checkXssPayload(URL url, Charset charset, PolicyFactory policy) {
        if (url == null) {
            throw new IOFaultException("URL must not be null", metadataFor(IOOperation.CHECK_XSS, DEFAULT_CHARSET, storageTypeForUrl(url)));
        }
        try (InputStream inputStream = url.openStream()) {
            return checkXssPayload(inputStream, charset, policy);
        } catch (IOException ex) {
            printLog(ex);
            throw new IOFaultException("Failed to check XSS payload", metadataFor(IOOperation.CHECK_XSS, DEFAULT_CHARSET, storageTypeForUrl(url)).toBuilder()
                    .uri(toUriSafe(url))
                    .build(), ex);
        }
    }

    @Override
    public boolean checkSqlInjection(String content) {
        if (content == null) {
            throw new IOFaultException("Content must not be null", metadataFor(IOOperation.CHECK_SQL, DEFAULT_CHARSET, IOStorageType.MEMORY));
        }
        return SQL_INJECTION_PATTERNS.stream().anyMatch(pattern -> pattern.matcher(content).find());
    }

    @Override
    public boolean checkSqlInjection(String content, Pattern... additionalPatterns) {
        if (content == null) {
            throw new IOFaultException("Content must not be null", metadataFor(IOOperation.CHECK_SQL, DEFAULT_CHARSET, IOStorageType.MEMORY));
        }
        return SqlInjectionPatterns.merge(additionalPatterns)
                .stream()
                .anyMatch(pattern -> pattern.matcher(content).find());
    }

    @Override
    public boolean checkSqlInjection(String content, List<Pattern> patterns) {
        if (content == null) {
            throw new IOFaultException("Content must not be null", metadataFor(IOOperation.CHECK_SQL, DEFAULT_CHARSET, IOStorageType.MEMORY));
        }
        return SqlInjectionPatterns.merge(patterns)
                .stream()
                .anyMatch(pattern -> pattern.matcher(content).find());
    }

    @Override
    public boolean checkSqlInjection(byte[] data) {
        return checkSqlInjection(data, DEFAULT_CHARSET);
    }

    @Override
    public boolean checkSqlInjection(byte[] data, Charset charset) {
        if (data == null) {
            throw new IOFaultException("Data must not be null", metadataFor(IOOperation.CHECK_SQL, DEFAULT_CHARSET, IOStorageType.MEMORY));
        }
        Charset resolvedCharset = charset != null ? charset : DEFAULT_CHARSET;
        return checkSqlInjection(new String(data, resolvedCharset));
    }

    @Override
    public boolean checkSqlInjection(byte[] data, Charset charset, Pattern... additionalPatterns) {
        if (data == null) {
            throw new IOFaultException("Data must not be null", metadataFor(IOOperation.CHECK_SQL, DEFAULT_CHARSET, IOStorageType.MEMORY));
        }
        Charset resolvedCharset = charset != null ? charset : DEFAULT_CHARSET;
        return checkSqlInjection(new String(data, resolvedCharset), additionalPatterns);
    }

    @Override
    public boolean checkSqlInjection(byte[] data, Charset charset, List<Pattern> patterns) {
        if (data == null) {
            throw new IOFaultException("Data must not be null", metadataFor(IOOperation.CHECK_SQL, DEFAULT_CHARSET, IOStorageType.MEMORY));
        }
        Charset resolvedCharset = charset != null ? charset : DEFAULT_CHARSET;
        return checkSqlInjection(new String(data, resolvedCharset), patterns);
    }

    @Override
    public boolean checkSqlInjection(InputStream inputStream) {
        return checkSqlInjection(inputStream, DEFAULT_CHARSET);
    }

    @Override
    public boolean checkSqlInjection(InputStream inputStream, Charset charset) {
        if (inputStream == null) {
            throw new IOFaultException("InputStream must not be null", metadataFor(IOOperation.CHECK_SQL, DEFAULT_CHARSET, IOStorageType.MEMORY));
        }
        try {
            return checkSqlInjection(inputStream.readAllBytes(), charset);
        } catch (IOException ex) {
            printLog(ex);
            throw new IOFaultException("Failed to check SQL injection", metadataFor(IOOperation.CHECK_SQL, DEFAULT_CHARSET, IOStorageType.MEMORY), ex);
        }
    }

    @Override
    public boolean checkSqlInjection(InputStream inputStream, Charset charset, Pattern... additionalPatterns) {
        if (inputStream == null) {
            throw new IOFaultException("InputStream must not be null", metadataFor(IOOperation.CHECK_SQL, DEFAULT_CHARSET, IOStorageType.MEMORY));
        }
        try {
            return checkSqlInjection(inputStream.readAllBytes(), charset, additionalPatterns);
        } catch (IOException ex) {
            printLog(ex);
            throw new IOFaultException("Failed to check SQL injection", metadataFor(IOOperation.CHECK_SQL, DEFAULT_CHARSET, IOStorageType.MEMORY), ex);
        }
    }

    @Override
    public boolean checkSqlInjection(InputStream inputStream, Charset charset, List<Pattern> patterns) {
        if (inputStream == null) {
            throw new IOFaultException("InputStream must not be null", metadataFor(IOOperation.CHECK_SQL, DEFAULT_CHARSET, IOStorageType.MEMORY));
        }
        try {
            return checkSqlInjection(inputStream.readAllBytes(), charset, patterns);
        } catch (IOException ex) {
            printLog(ex);
            throw new IOFaultException("Failed to check SQL injection", metadataFor(IOOperation.CHECK_SQL, DEFAULT_CHARSET, IOStorageType.MEMORY), ex);
        }
    }

    @Override
    public boolean checkSqlInjection(Path path) {
        return checkSqlInjection(path, DEFAULT_CHARSET);
    }

    @Override
    public boolean checkSqlInjection(Path path, Charset charset) {
        if (path == null) {
            throw new IOFaultException("Path must not be null", metadataFor(IOOperation.CHECK_SQL, DEFAULT_CHARSET, IOStorageType.LOCAL));
        }
        try {
            Charset resolvedCharset = charset != null ? charset : DEFAULT_CHARSET;
            return checkSqlInjection(Files.readString(path, resolvedCharset));
        } catch (IOException ex) {
            printLog(ex);
            throw new IOFaultException("Failed to check SQL injection", metadataFor(IOOperation.CHECK_SQL, DEFAULT_CHARSET, IOStorageType.LOCAL).toBuilder()
                    .fileName(path.getFileName() != null ? path.getFileName().toString() : null)
                    .filePath(path.toString())
                    .uri(path.toUri())
                    .build(), ex);
        }
    }

    @Override
    public boolean checkSqlInjection(Path path, Charset charset, Pattern... additionalPatterns) {
        if (path == null) {
            throw new IOFaultException("Path must not be null", metadataFor(IOOperation.CHECK_SQL, DEFAULT_CHARSET, IOStorageType.LOCAL));
        }
        try {
            Charset resolvedCharset = charset != null ? charset : DEFAULT_CHARSET;
            return checkSqlInjection(Files.readString(path, resolvedCharset), additionalPatterns);
        } catch (IOException ex) {
            printLog(ex);
            throw new IOFaultException("Failed to check SQL injection", metadataFor(IOOperation.CHECK_SQL, DEFAULT_CHARSET, IOStorageType.LOCAL).toBuilder()
                    .fileName(path.getFileName() != null ? path.getFileName().toString() : null)
                    .filePath(path.toString())
                    .uri(path.toUri())
                    .build(), ex);
        }
    }

    @Override
    public boolean checkSqlInjection(Path path, Charset charset, List<Pattern> patterns) {
        if (path == null) {
            throw new IOFaultException("Path must not be null", metadataFor(IOOperation.CHECK_SQL, DEFAULT_CHARSET, IOStorageType.LOCAL));
        }
        try {
            Charset resolvedCharset = charset != null ? charset : DEFAULT_CHARSET;
            return checkSqlInjection(Files.readString(path, resolvedCharset), patterns);
        } catch (IOException ex) {
            printLog(ex);
            throw new IOFaultException("Failed to check SQL injection", metadataFor(IOOperation.CHECK_SQL, DEFAULT_CHARSET, IOStorageType.LOCAL).toBuilder()
                    .fileName(path.getFileName() != null ? path.getFileName().toString() : null)
                    .filePath(path.toString())
                    .uri(path.toUri())
                    .build(), ex);
        }
    }

    @Override
    public boolean checkSqlInjection(File file) {
        if (file == null) {
            throw new IOFaultException("File must not be null", metadataFor(IOOperation.CHECK_SQL, DEFAULT_CHARSET, IOStorageType.LOCAL));
        }
        return checkSqlInjection(file.toPath(), DEFAULT_CHARSET);
    }

    @Override
    public boolean checkSqlInjection(File file, Charset charset) {
        if (file == null) {
            throw new IOFaultException("File must not be null", metadataFor(IOOperation.CHECK_SQL, DEFAULT_CHARSET, IOStorageType.LOCAL));
        }
        return checkSqlInjection(file.toPath(), charset);
    }

    @Override
    public boolean checkSqlInjection(File file, Charset charset, Pattern... additionalPatterns) {
        if (file == null) {
            throw new IOFaultException("File must not be null", metadataFor(IOOperation.CHECK_SQL, DEFAULT_CHARSET, IOStorageType.LOCAL));
        }
        return checkSqlInjection(file.toPath(), charset, additionalPatterns);
    }

    @Override
    public boolean checkSqlInjection(File file, Charset charset, List<Pattern> patterns) {
        if (file == null) {
            throw new IOFaultException("File must not be null", metadataFor(IOOperation.CHECK_SQL, DEFAULT_CHARSET, IOStorageType.LOCAL));
        }
        return checkSqlInjection(file.toPath(), charset, patterns);
    }

    @Override
    public boolean checkSqlInjection(URL url) {
        return checkSqlInjection(url, DEFAULT_CHARSET);
    }

    @Override
    public boolean checkSqlInjection(URL url, Charset charset) {
        if (url == null) {
            throw new IOFaultException("URL must not be null", metadataFor(IOOperation.CHECK_SQL, DEFAULT_CHARSET, storageTypeForUrl(url)));
        }
        try (InputStream inputStream = url.openStream()) {
            return checkSqlInjection(inputStream, charset);
        } catch (IOException ex) {
            printLog(ex);
            throw new IOFaultException("Failed to check SQL injection", metadataFor(IOOperation.CHECK_SQL, DEFAULT_CHARSET, storageTypeForUrl(url)).toBuilder()
                    .uri(toUriSafe(url))
                    .build(), ex);
        }
    }

    @Override
    public boolean checkSqlInjection(URL url, Charset charset, Pattern... additionalPatterns) {
        if (url == null) {
            throw new IOFaultException("URL must not be null", metadataFor(IOOperation.CHECK_SQL, DEFAULT_CHARSET, storageTypeForUrl(url)));
        }
        try (InputStream inputStream = url.openStream()) {
            return checkSqlInjection(inputStream, charset, additionalPatterns);
        } catch (IOException ex) {
            printLog(ex);
            throw new IOFaultException("Failed to check SQL injection", metadataFor(IOOperation.CHECK_SQL, DEFAULT_CHARSET, storageTypeForUrl(url)).toBuilder()
                    .uri(toUriSafe(url))
                    .build(), ex);
        }
    }

    @Override
    public boolean checkSqlInjection(URL url, Charset charset, List<Pattern> patterns) {
        if (url == null) {
            throw new IOFaultException("URL must not be null", metadataFor(IOOperation.CHECK_SQL, DEFAULT_CHARSET, storageTypeForUrl(url)));
        }
        try (InputStream inputStream = url.openStream()) {
            return checkSqlInjection(inputStream, charset, patterns);
        } catch (IOException ex) {
            printLog(ex);
            throw new IOFaultException("Failed to check SQL injection", metadataFor(IOOperation.CHECK_SQL, DEFAULT_CHARSET, storageTypeForUrl(url)).toBuilder()
                    .uri(toUriSafe(url))
                    .build(), ex);
        }
    }

    private IOFaultMetadata metadataFor(IOOperation operation, Charset charset, IOStorageType storageType) {
        return IOFaultMetadata.builder()
                .operation(operation)
                .permissions(IOPermissions.READ_ONLY)
                .storageType(storageType)
                .charset(charset != null ? charset.name() : null)
                .build();
    }

    private IOStorageType storageTypeForUrl(URL url) {
        if (url == null || url.getProtocol() == null) {
            return IOStorageType.HTTP;
        }
        String protocol = url.getProtocol().toLowerCase();
        return switch (protocol) {
            case "https" -> IOStorageType.HTTPS;
            case "ftp" -> IOStorageType.FTP;
            case "sftp" -> IOStorageType.SFTP;
            default -> IOStorageType.HTTP;
        };
    }

    private URI toUriSafe(URL url) {
        try {
            return url.toURI();
        } catch (Exception ex) {
            printLog(ex);
            return null;
        }
    }

    private void printLog(Exception e) {
        log.warn("Error Message : {}", e.getMessage());
    }
}
