package reyga.starter.foundation.common_http.enums;

import java.util.Locale;
import java.util.Objects;

/** Common media types for Content-Type and Accept; custom types and charset parameters use String overloads. */
public enum HttpContentType {
    /** JSON documents. */
    APPLICATION_JSON("application/json"),
    /** XML documents. */
    APPLICATION_XML("application/xml"),
    /** XML represented with the text media type. */
    TEXT_XML("text/xml"),
    /** Plain text, requiring raw bodies or a custom structured codec. */
    TEXT_PLAIN("text/plain"),
    /** HTML, requiring raw bodies or a custom structured codec. */
    TEXT_HTML("text/html"),
    /** URL-encoded forms, normally supplied as an OkHttp FormBody. */
    APPLICATION_FORM_URLENCODED("application/x-www-form-urlencoded"),
    /** Multipart forms; the actual RequestBody must supply its boundary. */
    MULTIPART_FORM_DATA("multipart/form-data"),
    /** Binary data. */
    APPLICATION_OCTET_STREAM("application/octet-stream"),
    /** PDF documents. */
    APPLICATION_PDF("application/pdf"),
    /** JSON problem details. */
    APPLICATION_PROBLEM_JSON("application/problem+json"),
    /** JSON Patch documents. */
    APPLICATION_JSON_PATCH("application/json-patch+json"),
    /** JSON Merge Patch documents. */
    APPLICATION_MERGE_PATCH_JSON("application/merge-patch+json");

    private final String value;

    HttpContentType(String value) {
        this.value = value;
    }

    /**
     * Returns the wire media type without charset or multipart boundary parameters.
     * @return immutable MIME type string
     */
    public String value() {
        return value;
    }

    /**
     * Resolves a symbolic enum name or preserves a literal/custom media type.
     * Symbolic names ignore case; literal values are returned unchanged for downstream validation.
     * @param value non-null enum name or media type
     * @return canonical media type for known enum names, otherwise the supplied value
     * @throws NullPointerException if value is null
     */
    public static String resolve(String value) {
        Objects.requireNonNull(value, "media type must not be null");
        try {
            return valueOf(value.toUpperCase(Locale.ROOT)).value();
        } catch (IllegalArgumentException exception) {
            return value;
        }
    }
}
