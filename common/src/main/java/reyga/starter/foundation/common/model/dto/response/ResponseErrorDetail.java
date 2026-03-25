package reyga.starter.foundation.common.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Getter @Setter
@Builder(toBuilder = true)
@NoArgsConstructor @AllArgsConstructor
public class ResponseErrorDetail {
    private String business;
    private String additionalInfo;
    private Timestamp timestamp;
}
