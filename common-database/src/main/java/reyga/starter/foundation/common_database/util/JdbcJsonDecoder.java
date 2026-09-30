package reyga.starter.foundation.common_database.util;

import java.io.IOException;

import org.jspecify.annotations.Nullable;

/**
 * Converts a complete JSON document into an application value.
 *
 * <p>The consumer selects the JSON library, target type, and parsing policies.
 * Implementations must validate JSON syntax and the target shape, including nested
 * objects and arrays, and reject malformed input rather than returning null to hide
 * an error. A decoder may return null for the JSON literal {@code null}.
 * Generic targets can be captured in the decoder without exposing parser-specific
 * type tokens through the foundation API.</p>
 *
 * <p>A decoder reused by multiple row mappers must be thread-safe. It should impose
 * appropriate nesting and token limits, avoid unsafe polymorphic deserialization,
 * and must not log JSON content. Domain validation is separate from decoding.</p>
 *
 * @param <T> decoded value type
 */
@FunctionalInterface
public interface JdbcJsonDecoder<T> {

    /**
     * Decodes one non-blank JSON document, including its nested values.
     *
     * @param json complete JSON text; never null or blank when called by the row mapper
     * @return the decoded value, which may be null according to decoder policy
     * @throws IOException if the document cannot be decoded
     * @throws RuntimeException if the JSON library reports a decoding failure
     */
    @Nullable T decode(String json) throws IOException;
}
