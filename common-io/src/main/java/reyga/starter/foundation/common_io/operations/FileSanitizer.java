package reyga.starter.foundation.common_io.operations;

import org.owasp.html.PolicyFactory;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

public interface FileSanitizer {

    String sanitizeHtml(String html);

    String sanitizeHtml(String html, PolicyFactory policy);

    String sanitizeHtml(byte[] data);

    String sanitizeHtml(byte[] data, Charset charset);

    String sanitizeHtml(byte[] data, Charset charset, PolicyFactory policy);

    String sanitizeHtml(InputStream inputStream);

    String sanitizeHtml(InputStream inputStream, Charset charset);

    String sanitizeHtml(InputStream inputStream, Charset charset, PolicyFactory policy);

    String sanitizeHtml(Path path);

    String sanitizeHtml(Path path, Charset charset);

    String sanitizeHtml(Path path, Charset charset, PolicyFactory policy);

    String sanitizeHtml(File file);

    String sanitizeHtml(File file, Charset charset);

    String sanitizeHtml(File file, Charset charset, PolicyFactory policy);

    String sanitizeHtml(URL url);

    String sanitizeHtml(URL url, Charset charset);

    String sanitizeHtml(URL url, Charset charset, PolicyFactory policy);

    boolean checkXssPayload(String html);

    boolean checkXssPayload(String html, PolicyFactory policy);

    boolean checkXssPayload(byte[] data);

    boolean checkXssPayload(byte[] data, Charset charset);

    boolean checkXssPayload(byte[] data, Charset charset, PolicyFactory policy);

    boolean checkXssPayload(InputStream inputStream);

    boolean checkXssPayload(InputStream inputStream, Charset charset);

    boolean checkXssPayload(InputStream inputStream, Charset charset, PolicyFactory policy);

    boolean checkXssPayload(Path path);

    boolean checkXssPayload(Path path, Charset charset);

    boolean checkXssPayload(Path path, Charset charset, PolicyFactory policy);

    boolean checkXssPayload(File file);

    boolean checkXssPayload(File file, Charset charset);

    boolean checkXssPayload(File file, Charset charset, PolicyFactory policy);

    boolean checkXssPayload(URL url);

    boolean checkXssPayload(URL url, Charset charset);

    boolean checkXssPayload(URL url, Charset charset, PolicyFactory policy);

    boolean checkSqlInjection(String content);

    boolean checkSqlInjection(String content, Pattern... additionalPatterns);

    boolean checkSqlInjection(String content, List<Pattern> patterns);

    boolean checkSqlInjection(byte[] data);

    boolean checkSqlInjection(byte[] data, Charset charset);

    boolean checkSqlInjection(byte[] data, Charset charset, Pattern... additionalPatterns);

    boolean checkSqlInjection(byte[] data, Charset charset, List<Pattern> patterns);

    boolean checkSqlInjection(InputStream inputStream, Charset charset, List<Pattern> patterns);

    boolean checkSqlInjection(InputStream inputStream, Charset charset, Pattern... additionalPatterns);

    boolean checkSqlInjection(InputStream inputStream);

    boolean checkSqlInjection(InputStream inputStream, Charset charset);

    boolean checkSqlInjection(Path path, Charset charset, Pattern... additionalPatterns);

    boolean checkSqlInjection(Path path);

    boolean checkSqlInjection(Path path, Charset charset);

    boolean checkSqlInjection(File file);

    boolean checkSqlInjection(File file, Charset charset);

    boolean checkSqlInjection(URL url);

    boolean checkSqlInjection(URL url, Charset charset);

    boolean checkSqlInjection(Path path, Charset charset, List<Pattern> patterns);

    boolean checkSqlInjection(File file, Charset charset, Pattern... additionalPatterns);

    boolean checkSqlInjection(File file, Charset charset, List<Pattern> patterns);

    boolean checkSqlInjection(URL url, Charset charset, Pattern... additionalPatterns);

    boolean checkSqlInjection(URL url, Charset charset, List<Pattern> patterns);

}
