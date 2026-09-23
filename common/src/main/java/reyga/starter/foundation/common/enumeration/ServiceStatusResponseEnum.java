package reyga.starter.foundation.common.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ServiceStatusResponseEnum {
    SUCCESS("SUCCESS"), FAILED("FAILED");

    private final String label;
}
