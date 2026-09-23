package reyga.starter.foundation.common.model.dto.response;

import lombok.*;

import java.sql.Timestamp;

@Getter @Setter
@Builder(toBuilder = true)
@NoArgsConstructor @AllArgsConstructor
public class FieldErrorDetail {
    private String field;
    private String message;
    private Timestamp timestamp;
}
