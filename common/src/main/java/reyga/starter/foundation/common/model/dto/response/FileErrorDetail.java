package reyga.starter.foundation.common.model.dto.response;

import lombok.*;

@Getter @Setter
@Builder(toBuilder = true)
@NoArgsConstructor @AllArgsConstructor
public class FileErrorDetail {
    private String fileName;
    private String operation;
    private String mimeType;
    private String charset;
    private boolean directory;
    private long sizeBytes;
    private long lastModifiedEpochMillis;
    private long offset;
    private long length;
}
