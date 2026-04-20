package reyga.starter.foundation.common_io.operations;

import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import reyga.starter.foundation.common.logging.CommonLogger;
import reyga.starter.foundation.common.logging.InjectLogger;
import reyga.starter.foundation.common_io.enumeration.IOOperation;
import reyga.starter.foundation.common_io.enumeration.IOPermissions;
import reyga.starter.foundation.common_io.enumeration.IOStorageType;
import reyga.starter.foundation.common_io.exception.IOFaultException;
import reyga.starter.foundation.common_io.exception.IOFaultMetadata;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;

public class DefaultFileInspector implements FileInspector {

    @InjectLogger
    protected CommonLogger logger;

    private static final Tika TIKA = new Tika();

    @Override
    public String checkContentType(InputStream inputStream) {
        if (inputStream == null) {
            throw new IOFaultException("InputStream must not be null", metadataFor(IOOperation.DETECT_CONTENT_TYPE, IOStorageType.MEMORY));
        }
        try {
            return TIKA.detect(inputStream);
        } catch (Exception ex) {
            printLog(ex);
            throw new IOFaultException("Failed to detect content type", metadataFor(IOOperation.DETECT_CONTENT_TYPE, IOStorageType.MEMORY), ex);
        }
    }

    @Override
    public String checkContentType(InputStream inputStream, Metadata metadata) {
        if (inputStream == null) {
            throw new IOFaultException("InputStream must not be null", metadataFor(IOOperation.DETECT_CONTENT_TYPE, IOStorageType.MEMORY));
        }
        try {
            return TIKA.detect(inputStream, metadata);
        } catch (Exception ex) {
            printLog(ex);
            throw new IOFaultException("Failed to detect content type", metadataFor(IOOperation.DETECT_CONTENT_TYPE, IOStorageType.MEMORY), ex);
        }
    }

    @Override
    public String checkContentType(InputStream inputStream, String name) {
        if (inputStream == null) {
            throw new IOFaultException("InputStream must not be null", metadataFor(IOOperation.DETECT_CONTENT_TYPE, IOStorageType.MEMORY));
        }
        try {
            return TIKA.detect(inputStream, name);
        } catch (Exception ex) {
            printLog(ex);
            throw new IOFaultException("Failed to detect content type", metadataFor(IOOperation.DETECT_CONTENT_TYPE, IOStorageType.MEMORY).toBuilder()
                    .fileName(name)
                    .build(), ex);
        }
    }

    @Override
    public String checkContentType(byte[] data) {
        if (data == null) {
            throw new IOFaultException("Data must not be null", metadataFor(IOOperation.DETECT_CONTENT_TYPE, IOStorageType.MEMORY));
        }
        return TIKA.detect(data);
    }

    @Override
    public String checkContentType(byte[] data, String name) {
        if (data == null) {
            throw new IOFaultException("Data must not be null", metadataFor(IOOperation.DETECT_CONTENT_TYPE, IOStorageType.MEMORY));
        }
        return TIKA.detect(data, name);
    }

    @Override
    public String checkContentType(File file) {
        if (file == null) {
            throw new IOFaultException("File must not be null", metadataFor(IOOperation.DETECT_CONTENT_TYPE, IOStorageType.LOCAL));
        }
        try {
            return TIKA.detect(file);
        } catch (Exception ex) {
            printLog(ex);
            throw new IOFaultException("Failed to detect content type", metadataFor(IOOperation.DETECT_CONTENT_TYPE, IOStorageType.LOCAL).toBuilder()
                    .fileName(file.getName())
                    .filePath(file.getPath())
                    .uri(file.toURI())
                    .build(), ex);
        }
    }

    @Override
    public String checkContentType(Path path) {
        if (path == null) {
            throw new IOFaultException("Path must not be null", metadataFor(IOOperation.DETECT_CONTENT_TYPE, IOStorageType.LOCAL));
        }
        try {
            return TIKA.detect(path);
        } catch (Exception ex) {
            printLog(ex);
            throw new IOFaultException("Failed to detect content type", metadataFor(IOOperation.DETECT_CONTENT_TYPE, IOStorageType.LOCAL).toBuilder()
                    .fileName(path.getFileName() != null ? path.getFileName().toString() : null)
                    .filePath(path.toString())
                    .uri(path.toUri())
                    .build(), ex);
        }
    }

    @Override
    public String checkContentType(String name) {
        if (name == null || name.isBlank()) {
            throw new IOFaultException("Name must not be null or blank", metadataFor(IOOperation.DETECT_CONTENT_TYPE, IOStorageType.MEMORY));
        }
        return TIKA.detect(name);
    }

    @Override
    public String checkContentType(URL url) {
        if (url == null) {
            throw new IOFaultException("URL must not be null", metadataFor(IOOperation.DETECT_CONTENT_TYPE, storageTypeForUrl(url)));
        }
        try {
            return TIKA.detect(url);
        } catch (Exception ex) {
            printLog(ex);
            throw new IOFaultException("Failed to detect content type", metadataFor(IOOperation.DETECT_CONTENT_TYPE, storageTypeForUrl(url)).toBuilder()
                    .uri(toUriSafe(url))
                    .build(), ex);
        }
    }

    @Override
    public String extractContent(InputStream inputStream) {
        if (inputStream == null) {
            throw new IOFaultException("InputStream must not be null", metadataFor(IOOperation.PARSE_TO_STRING, IOStorageType.MEMORY));
        }
        try {
            return TIKA.parseToString(inputStream);
        } catch (Exception ex) {
            printLog(ex);
            throw new IOFaultException("Failed to extract content", metadataFor(IOOperation.PARSE_TO_STRING, IOStorageType.MEMORY), ex);
        }
    }

    @Override
    public String extractContent(InputStream inputStream, Metadata metadata) {
        if (inputStream == null) {
            throw new IOFaultException("InputStream must not be null", metadataFor(IOOperation.PARSE_TO_STRING, IOStorageType.MEMORY));
        }
        try {
            return TIKA.parseToString(inputStream, metadata);
        } catch (Exception ex) {
            printLog(ex);
            throw new IOFaultException("Failed to extract content", metadataFor(IOOperation.PARSE_TO_STRING, IOStorageType.MEMORY), ex);
        }
    }

    @Override
    public String extractContent(InputStream inputStream, Metadata metadata, int maxLength) {
        if (inputStream == null) {
            throw new IOFaultException("InputStream must not be null", metadataFor(IOOperation.PARSE_TO_STRING, IOStorageType.MEMORY));
        }
        try {
            return TIKA.parseToString(inputStream, metadata, maxLength);
        } catch (Exception ex) {
            printLog(ex);
            throw new IOFaultException("Failed to extract content", metadataFor(IOOperation.PARSE_TO_STRING, IOStorageType.MEMORY), ex);
        }
    }

    @Override
    public String extractContent(URL url) {
        if (url == null) {
            throw new IOFaultException("URL must not be null", metadataFor(IOOperation.PARSE_TO_STRING, storageTypeForUrl(url)));
        }
        try {
            return TIKA.parseToString(url);
        } catch (Exception ex) {
            printLog(ex);
            throw new IOFaultException("Failed to extract content", metadataFor(IOOperation.PARSE_TO_STRING, storageTypeForUrl(url)).toBuilder()
                    .uri(toUriSafe(url))
                    .build(), ex);
        }
    }

    @Override
    public String extractContent(File file) {
        if (file == null) {
            throw new IOFaultException("File must not be null", metadataFor(IOOperation.PARSE_TO_STRING, IOStorageType.LOCAL));
        }
        try {
            return TIKA.parseToString(file);
        } catch (Exception ex) {
            printLog(ex);
            throw new IOFaultException("Failed to extract content", metadataFor(IOOperation.PARSE_TO_STRING, IOStorageType.LOCAL).toBuilder()
                    .fileName(file.getName())
                    .filePath(file.getPath())
                    .uri(file.toURI())
                    .build(), ex);
        }
    }

    @Override
    public String extractContent(Path path) {
        if (path == null) {
            throw new IOFaultException("Path must not be null", metadataFor(IOOperation.PARSE_TO_STRING, IOStorageType.LOCAL));
        }
        try {
            return TIKA.parseToString(path);
        } catch (Exception ex) {
            printLog(ex);
            throw new IOFaultException("Failed to extract content", metadataFor(IOOperation.PARSE_TO_STRING, IOStorageType.LOCAL).toBuilder()
                    .fileName(path.getFileName() != null ? path.getFileName().toString() : null)
                    .filePath(path.toString())
                    .uri(path.toUri())
                    .build(), ex);
        }
    }

    @Override
    public Reader extract(InputStream inputStream) {
        if (inputStream == null) {
            throw new IOFaultException("InputStream must not be null", metadataFor(IOOperation.PARSE, IOStorageType.MEMORY));
        }
        try {
            return TIKA.parse(inputStream);
        } catch (Exception ex) {
            printLog(ex);
            throw new IOFaultException("Failed to parse content", metadataFor(IOOperation.PARSE, IOStorageType.MEMORY), ex);
        }
    }

    @Override
    public Reader extract(InputStream inputStream, Metadata metadata) {
        if (inputStream == null) {
            throw new IOFaultException("InputStream must not be null", metadataFor(IOOperation.PARSE, IOStorageType.MEMORY));
        }
        try {
            return TIKA.parse(inputStream, metadata);
        } catch (Exception ex) {
            printLog(ex);
            throw new IOFaultException("Failed to parse content", metadataFor(IOOperation.PARSE, IOStorageType.MEMORY), ex);
        }
    }

    @Override
    public Reader extract(URL url) {
        if (url == null) {
            throw new IOFaultException("URL must not be null", metadataFor(IOOperation.PARSE, storageTypeForUrl(url)));
        }
        try {
            return TIKA.parse(url);
        } catch (Exception ex) {
            printLog(ex);
            throw new IOFaultException("Failed to parse content", metadataFor(IOOperation.PARSE, storageTypeForUrl(url)).toBuilder()
                    .uri(toUriSafe(url))
                    .build(), ex);
        }
    }

    @Override
    public Reader extract(File file) {
        if (file == null) {
            throw new IOFaultException("File must not be null", metadataFor(IOOperation.PARSE, IOStorageType.LOCAL));
        }
        try {
            return TIKA.parse(file);
        } catch (Exception ex) {
            printLog(ex);
            throw new IOFaultException("Failed to parse content", metadataFor(IOOperation.PARSE, IOStorageType.LOCAL).toBuilder()
                    .fileName(file.getName())
                    .filePath(file.getPath())
                    .uri(file.toURI())
                    .build(), ex);
        }
    }

    @Override
    public Reader extract(File file, Metadata metadata) {
        if (file == null) {
            throw new IOFaultException("File must not be null", metadataFor(IOOperation.PARSE, IOStorageType.LOCAL));
        }
        try {
            return TIKA.parse(file, metadata);
        } catch (Exception ex) {
            printLog(ex);
            throw new IOFaultException("Failed to parse content", metadataFor(IOOperation.PARSE, IOStorageType.LOCAL).toBuilder()
                    .fileName(file.getName())
                    .filePath(file.getPath())
                    .uri(file.toURI())
                    .build(), ex);
        }
    }

    @Override
    public Reader extract(Path path) {
        if (path == null) {
            throw new IOFaultException("Path must not be null", metadataFor(IOOperation.PARSE, IOStorageType.LOCAL));
        }
        try {
            return TIKA.parse(path);
        } catch (Exception ex) {
            printLog(ex);
            throw new IOFaultException("Failed to parse content", metadataFor(IOOperation.PARSE, IOStorageType.LOCAL).toBuilder()
                    .fileName(path.getFileName() != null ? path.getFileName().toString() : null)
                    .filePath(path.toString())
                    .uri(path.toUri())
                    .build(), ex);
        }
    }

    @Override
    public Reader extract(Path path, Metadata metadata) {
        if (path == null) {
            throw new IOFaultException("Path must not be null", metadataFor(IOOperation.PARSE, IOStorageType.LOCAL));
        }
        try {
            return TIKA.parse(path, metadata);
        } catch (Exception ex) {
            printLog(ex);
            throw new IOFaultException("Failed to parse content", metadataFor(IOOperation.PARSE, IOStorageType.LOCAL).toBuilder()
                    .fileName(path.getFileName() != null ? path.getFileName().toString() : null)
                    .filePath(path.toString())
                    .uri(path.toUri())
                    .build(), ex);
        }
    }

    @Override
    public String translate(String text, String targetLanguage) {
        if (text == null) {
            throw new IOFaultException("Text must not be null", metadataFor(IOOperation.TRANSLATE, IOStorageType.MEMORY));
        }
        try {
            return TIKA.translate(text, targetLanguage);
        } catch (Exception ex) {
            printLog(ex);
            throw new IOFaultException("Failed to translate content", metadataFor(IOOperation.TRANSLATE, IOStorageType.MEMORY), ex);
        }
    }

    @Override
    public String translate(String text, String sourceLanguage, String targetLanguage) {
        if (text == null) {
            throw new IOFaultException("Text must not be null", metadataFor(IOOperation.TRANSLATE, IOStorageType.MEMORY));
        }
        try {
            return TIKA.translate(text, sourceLanguage, targetLanguage);
        } catch (Exception ex) {
            printLog(ex);
            throw new IOFaultException("Failed to translate content", metadataFor(IOOperation.TRANSLATE, IOStorageType.MEMORY), ex);
        }
    }

    private IOFaultMetadata metadataFor(IOOperation operation, IOStorageType storageType) {
        return IOFaultMetadata.builder()
                .operation(operation)
                .permissions(IOPermissions.READ_ONLY)
                .storageType(storageType)
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
        if (logger != null) {
            logger.warn("Error Message :", e.getMessage());
        }
    }
}
