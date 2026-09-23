package reyga.starter.foundation.common.util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class FileUtility {

    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private FileUtility() {
    }

    public static Path toPath(String path) {
        Objects.requireNonNull(path, "path must not be null");
        return Paths.get(path);
    }

    public static String normalize(String path) {
        Objects.requireNonNull(path, "path must not be null");
        return Paths.get(path).normalize().toString();
    }

    public static String fileName(Path path) {
        Objects.requireNonNull(path, "path must not be null");
        Path fileName = path.getFileName();
        return fileName != null ? fileName.toString() : null;
    }

    public static String fileName(String path) {
        return fileName(toPath(path));
    }

    public static String extension(String fileName) {
        Objects.requireNonNull(fileName, "fileName must not be null");
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex <= 0 || dotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dotIndex + 1);
    }

    public static String extension(Path path) {
        return extension(fileName(path));
    }

    public static String baseName(String fileName) {
        Objects.requireNonNull(fileName, "fileName must not be null");
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex <= 0) {
            return fileName;
        }
        return fileName.substring(0, dotIndex);
    }

    public static String baseName(Path path) {
        return baseName(fileName(path));
    }

    public static boolean exists(Path path) {
        Objects.requireNonNull(path, "path must not be null");
        return Files.exists(path);
    }

    public static boolean isDirectory(Path path) {
        Objects.requireNonNull(path, "path must not be null");
        return Files.isDirectory(path);
    }

    public static boolean isRegularFile(Path path) {
        Objects.requireNonNull(path, "path must not be null");
        return Files.isRegularFile(path);
    }

    public static long size(Path path) throws IOException {
        Objects.requireNonNull(path, "path must not be null");
        return Files.size(path);
    }

    public static byte[] readBytes(Path path) throws IOException {
        Objects.requireNonNull(path, "path must not be null");
        return Files.readAllBytes(path);
    }

    public static String readString(Path path) throws IOException {
        return readString(path, DEFAULT_CHARSET);
    }

    public static String readString(Path path, Charset charset) throws IOException {
        Objects.requireNonNull(path, "path must not be null");
        Charset resolvedCharset = charset != null ? charset : DEFAULT_CHARSET;
        return Files.readString(path, resolvedCharset);
    }

    public static Path writeBytes(Path path, byte[] data, OpenOption... options) throws IOException {
        Objects.requireNonNull(path, "path must not be null");
        Objects.requireNonNull(data, "data must not be null");
        return Files.write(path, data, options);
    }

    public static Path writeString(Path path, String content, OpenOption... options) throws IOException {
        return writeString(path, content, DEFAULT_CHARSET, options);
    }

    public static Path writeString(Path path, String content, Charset charset, OpenOption... options) throws IOException {
        Objects.requireNonNull(path, "path must not be null");
        Objects.requireNonNull(content, "content must not be null");
        Charset resolvedCharset = charset != null ? charset : DEFAULT_CHARSET;
        return Files.writeString(path, content, resolvedCharset, options);
    }

    public static Path copy(Path source, Path target, CopyOption... options) throws IOException {
        Objects.requireNonNull(source, "source must not be null");
        Objects.requireNonNull(target, "target must not be null");
        return Files.copy(source, target, options);
    }

    public static Path move(Path source, Path target, CopyOption... options) throws IOException {
        Objects.requireNonNull(source, "source must not be null");
        Objects.requireNonNull(target, "target must not be null");
        return Files.move(source, target, options);
    }

    public static boolean deleteIfExists(Path path) throws IOException {
        Objects.requireNonNull(path, "path must not be null");
        return Files.deleteIfExists(path);
    }

    public static void ensureDirectory(Path path) throws IOException {
        Objects.requireNonNull(path, "path must not be null");
        Files.createDirectories(path);
    }

    public static void ensureParentDirectory(Path path) throws IOException {
        Objects.requireNonNull(path, "path must not be null");
        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
    }

    public static List<Path> list(Path directory) throws IOException {
        Objects.requireNonNull(directory, "directory must not be null");
        try (Stream<Path> stream = Files.list(directory)) {
            return stream.collect(Collectors.toList());
        }
    }

    public static String maskFileName(String fileName) {
        return maskFileName(fileName, 3, 3, true, '*');
    }

    public static String maskFileName(String fileName, int prefix, int suffix, boolean keepExtension) {
        return maskFileName(fileName, prefix, suffix, keepExtension, '*');
    }

    public static String maskFileName(String fileName, int prefix, int suffix, boolean keepExtension, char maskChar) {
        Objects.requireNonNull(fileName, "fileName must not be null");
        if (fileName.isBlank()) {
            return fileName;
        }
        int safePrefix = Math.max(prefix, 0);
        int safeSuffix = Math.max(suffix, 0);

        String namePart = fileName;
        String extensionPart = "";
        if (keepExtension) {
            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
                namePart = fileName.substring(0, dotIndex);
                extensionPart = fileName.substring(dotIndex);
            }
        }

        if (namePart.length() <= safePrefix + safeSuffix) {
            return namePart + extensionPart;
        }

        int maskLength = namePart.length() - safePrefix - safeSuffix;
        String masked = namePart.substring(0, safePrefix)
                + String.valueOf(maskChar).repeat(maskLength)
                + namePart.substring(namePart.length() - safeSuffix);
        return masked + extensionPart;
    }
}
