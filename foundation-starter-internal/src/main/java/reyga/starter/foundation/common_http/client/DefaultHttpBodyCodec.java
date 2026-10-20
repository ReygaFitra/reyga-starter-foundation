package reyga.starter.foundation.common_http.client;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Locale;

import javax.xml.stream.XMLInputFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import okhttp3.MediaType;

/** JSON/XML codec with DTD and external entity resolution disabled. Thread-safe after construction. */
public class DefaultHttpBodyCodec implements HttpBodyCodec {
    private final ObjectMapper json = new ObjectMapper().findAndRegisterModules();
    private final XmlMapper xml;

    /** Creates independent Jackson 2 mappers without changing Spring's application mapper. */
    public DefaultHttpBodyCodec() {
        XMLInputFactory input = XMLInputFactory.newFactory();
        input.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        input.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        xml = new XmlMapper(XmlFactory.builder().xmlInputFactory(input).build());
    }

    /** {@inheritDoc} */
    @Override
    public byte[] encode(Object body, String mediaType) throws IOException {
        return mapper(mediaType).writeValueAsBytes(body);
    }

    /** {@inheritDoc} */
    @Override
    public Object decode(byte[] body, String mediaType, Type type) throws IOException {
        ObjectMapper mapper = mapper(mediaType);
        return mapper.readValue(body, mapper.constructType(type));
    }

    private ObjectMapper mapper(String value) throws IOException {
        MediaType mediaType = MediaType.parse(value);
        if (mediaType != null) {
            String subtype = mediaType.subtype().toLowerCase(Locale.ROOT);
            if (subtype.equals("json") || subtype.endsWith("+json")) {
                return json;
            }
            if (subtype.equals("xml") || subtype.endsWith("+xml")) {
                return xml;
            }
        }
        throw new IOException("HTTP structured bodies require a JSON or XML media type");
    }
}
