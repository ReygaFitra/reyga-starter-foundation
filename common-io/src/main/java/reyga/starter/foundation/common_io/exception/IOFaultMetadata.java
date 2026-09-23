package reyga.starter.foundation.common_io.exception;

import lombok.*;
import org.springframework.http.MediaType;
import reyga.starter.foundation.common_io.enumeration.IOOperation;
import reyga.starter.foundation.common_io.enumeration.IOPermissions;
import reyga.starter.foundation.common_io.enumeration.IOStorageType;

import java.net.URI;

@Getter @Setter
@Builder(toBuilder = true)
@NoArgsConstructor @AllArgsConstructor
public class IOFaultMetadata {
    private String fileName;
    private String filePath;
    private URI uri;
    private MediaType mimeType;
    private String charset;
    private IOOperation operation;
    private IOPermissions permissions;
    private IOStorageType storageType;
    private String checksum;
    private boolean directory;
    private long sizeBytes;
    private long lastModifiedEpochMillis;
    private Long offset;
    private Long length;
}
