package reyga.starter.foundation.common_io.operations;

import org.apache.tika.metadata.Metadata;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.nio.file.Path;

public interface FileInspector {

    String checkContentType(InputStream inputStream);

    String checkContentType(InputStream inputStream, Metadata metadata);

    String checkContentType(InputStream inputStream, String name);

    String checkContentType(byte[] data);

    String checkContentType(byte[] data, String name);

    String checkContentType(File file);

    String checkContentType(Path path);

    String checkContentType(String name);

    String checkContentType(URL url);

    String extractContent(InputStream inputStream);

    String extractContent(InputStream inputStream, Metadata metadata);

    String extractContent(InputStream inputStream, Metadata metadata, int maxLength);

    String extractContent(URL url);

    String extractContent(File file);

    String extractContent(Path path);

    Reader extract(InputStream inputStream);

    Reader extract(InputStream inputStream, Metadata metadata);

    Reader extract(URL url);

    Reader extract(File file);

    Reader extract(File file, Metadata metadata);

    Reader extract(Path path);

    Reader extract(Path path, Metadata metadata);

    String translate(String text, String targetLanguage);

    String translate(String text, String sourceLanguage, String targetLanguage);

}
