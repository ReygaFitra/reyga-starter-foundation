package reyga.starter.foundation.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Options for the YAML-created validation utility. Manual beans keep their own options.
 * @param mapDetails whether errors include field-detail maps; default true
 */
@ConfigurationProperties("reyga.custom.validation")
public record ValidationProperties(@DefaultValue("true") boolean mapDetails) {}
