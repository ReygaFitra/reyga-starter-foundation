package reyga.starter.foundation.common_http.client;

import java.io.IOException;
import java.lang.reflect.Type;

/** Thread-safe JSON/XML serialization extension point; implementations must not mutate payloads. */
public interface HttpBodyCodec {
    /**
     * Encodes a structured payload.
     * @param body non-null payload
     * @param mediaType JSON or XML media type
     * @return encoded bytes
     * @throws IOException on serialization failure
     */
    byte[] encode(Object body, String mediaType) throws IOException;

    /**
     * Decodes a payload without assuming ownership of the response.
     * @param body encoded bytes
     * @param mediaType actual response media type
     * @param type target class or parameterized type
     * @return decoded value
     * @throws IOException on malformed or unsupported content
     */
    Object decode(byte[] body, String mediaType, Type type) throws IOException;
}
