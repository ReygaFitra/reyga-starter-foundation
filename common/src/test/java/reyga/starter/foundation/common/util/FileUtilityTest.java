package reyga.starter.foundation.common.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileUtilityTest {

    @TempDir
    Path tempDirectory;

    @Test
    void should_ReturnPathComponents_When_PathValuesAreValid() {
        // Given
        Path path = tempDirectory.resolve("archive.tar.gz");

        // When
        Path converted = FileUtility.toPath(path.toString());
        String normalized = FileUtility.normalize(tempDirectory.resolve("child").resolve("..").resolve("file.txt").toString());

        // Then
        assertEquals(path, converted);
        assertEquals(tempDirectory.resolve("file.txt").toString(), normalized);
        assertEquals("archive.tar.gz", FileUtility.fileName(path));
        assertEquals("archive.tar.gz", FileUtility.fileName(path.toString()));
        assertEquals("gz", FileUtility.extension(path));
        assertEquals("gz", FileUtility.extension("archive.tar.gz"));
        assertEquals("archive.tar", FileUtility.baseName(path));
        assertEquals("archive.tar", FileUtility.baseName("archive.tar.gz"));
    }

    @Test
    void should_ReturnEmptyExtensionAndOriginalBaseName_When_FileHasNoUsableExtension() {
        // Given
        List<String> fileNames = List.of("README", ".gitignore", "file.");

        // When
        List<String> extensions = fileNames.stream().map(FileUtility::extension).toList();
        List<String> baseNames = fileNames.stream().map(FileUtility::baseName).toList();

        // Then
        assertEquals(List.of("", "", ""), extensions);
        assertEquals(List.of("README", ".gitignore", "file"), baseNames);
    }

    @Test
    void should_WriteReadAndReportFile_When_ValidFileIsProvided() throws IOException {
        // Given
        Path file = tempDirectory.resolve("data.txt");
        byte[] initial = "hello".getBytes(StandardCharsets.UTF_8);

        // When
        Path written = FileUtility.writeBytes(file, initial);
        Path appended = FileUtility.writeString(file, " world", StandardCharsets.UTF_8, StandardOpenOption.APPEND);

        // Then
        assertSame(file, written);
        assertSame(file, appended);
        assertTrue(FileUtility.exists(file));
        assertTrue(FileUtility.isRegularFile(file));
        assertFalse(FileUtility.isDirectory(file));
        assertEquals(11L, FileUtility.size(file));
        assertArrayEquals("hello world".getBytes(StandardCharsets.UTF_8), FileUtility.readBytes(file));
        assertEquals("hello world", FileUtility.readString(file));
        assertEquals("hello world", FileUtility.readString(file, null));
    }

    @Test
    void should_CreateDirectoriesAndListEntries_When_DirectoryPathsAreValid() throws IOException {
        // Given
        Path directory = tempDirectory.resolve("one/two");
        Path file = directory.resolve("data.txt");

        // When
        FileUtility.ensureDirectory(directory);
        FileUtility.ensureParentDirectory(file);
        FileUtility.ensureParentDirectory(Path.of("filename-without-parent"));
        FileUtility.writeString(file, "content");
        List<Path> entries = FileUtility.list(directory);

        // Then
        assertTrue(FileUtility.isDirectory(directory));
        assertEquals(List.of(file), entries);
    }

    @Test
    void should_CopyMoveAndDeleteFile_When_TargetsAreValid() throws IOException {
        // Given
        Path source = FileUtility.writeString(tempDirectory.resolve("source.txt"), "content");
        Path copy = tempDirectory.resolve("copy.txt");
        Path moved = tempDirectory.resolve("moved.txt");

        // When
        Path copied = FileUtility.copy(source, copy);
        Path moveResult = FileUtility.move(copied, moved, StandardCopyOption.REPLACE_EXISTING);
        boolean firstDelete = FileUtility.deleteIfExists(moved);
        boolean secondDelete = FileUtility.deleteIfExists(moved);

        // Then
        assertEquals(copy, copied);
        assertEquals(moved, moveResult);
        assertTrue(firstDelete);
        assertFalse(secondDelete);
        assertTrue(FileUtility.exists(source));
        assertFalse(FileUtility.exists(moved));
    }

    @Test
    void should_MaskExpectedCharacters_When_FileNameOptionsAreProvided() {
        // Given
        String fileName = "confidential-document.pdf";

        // When
        String defaultResult = FileUtility.maskFileName(fileName);
        String customResult = FileUtility.maskFileName(fileName, 4, 4, true, '#');
        String noExtensionResult = FileUtility.maskFileName(fileName, 4, 4, false);
        String negativeResult = FileUtility.maskFileName("secret.txt", -1, -2, true);

        // Then
        assertEquals("con***************ent.pdf", defaultResult);
        assertEquals("conf#############ment.pdf", customResult);
        assertEquals("conf*****************.pdf", noExtensionResult);
        assertEquals("******.txt", negativeResult);
        assertEquals("short.txt", FileUtility.maskFileName("short.txt", 3, 3, true));
        assertEquals("   ", FileUtility.maskFileName("   "));
    }

    @Test
    void should_ThrowNullPointerException_When_RequiredArgumentsAreNull() {
        // Given
        Path path = tempDirectory.resolve("file.txt");

        // When
        NullPointerException pathException = assertThrows(NullPointerException.class, () -> FileUtility.exists(null));
        NullPointerException dataException = assertThrows(NullPointerException.class, () -> FileUtility.writeBytes(path, null));
        NullPointerException contentException = assertThrows(NullPointerException.class, () -> FileUtility.writeString(path, null));
        NullPointerException sourceException = assertThrows(NullPointerException.class, () -> FileUtility.copy(null, path));
        NullPointerException targetException = assertThrows(NullPointerException.class, () -> FileUtility.move(path, null));
        NullPointerException nameException = assertThrows(NullPointerException.class, () -> FileUtility.maskFileName(null));

        // Then
        assertEquals("path must not be null", pathException.getMessage());
        assertEquals("data must not be null", dataException.getMessage());
        assertEquals("content must not be null", contentException.getMessage());
        assertEquals("source must not be null", sourceException.getMessage());
        assertEquals("target must not be null", targetException.getMessage());
        assertEquals("fileName must not be null", nameException.getMessage());
    }

    @Test
    void should_ThrowNoSuchFileException_When_MissingFileIsRead() {
        // Given
        Path missing = tempDirectory.resolve("missing.txt");

        // When
        IOException exception = assertThrows(IOException.class, () -> FileUtility.readString(missing));

        // Then
        assertInstanceOf(NoSuchFileException.class, exception);
        assertFalse(FileUtility.exists(missing));
    }
}
