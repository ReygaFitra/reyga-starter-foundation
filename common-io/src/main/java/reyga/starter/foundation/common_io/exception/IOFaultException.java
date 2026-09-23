package reyga.starter.foundation.common_io.exception;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.util.List;

@Getter @Setter
public class IOFaultException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private IOFaultMetadata metadata;
    private List<IOFaultMetadata> metadataList;

    public IOFaultException(String message) {
        super(message);
    }

    public IOFaultException(String message, Throwable cause) {
        super(message, cause);
    }

    public IOFaultException(String message, IOFaultMetadata metadata) {
        super(message);
        this.metadata = metadata;
    }

    public IOFaultException(String message, IOFaultMetadata metadata, Throwable cause) {
        super(message, cause);
        this.metadata = metadata;
    }

    public IOFaultException(String message, List<IOFaultMetadata> metadataList) {
        super(message);
        this.metadataList = metadataList;
    }

    public IOFaultException(String message, List<IOFaultMetadata> metadataList, Throwable cause) {
        super(message, cause);
        this.metadataList = metadataList;
    }


}
