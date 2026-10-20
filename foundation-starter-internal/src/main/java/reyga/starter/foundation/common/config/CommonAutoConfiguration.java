package reyga.starter.foundation.common.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * Registers {@link CommonConfig} through Boot auto-configuration without changing its bean factories.
 * The legacy configuration remains available for explicit imports and direct factory calls.
 */
@AutoConfiguration
@Import(CommonConfig.class)
public class CommonAutoConfiguration {

    /** Creates this feature entry point. */
    public CommonAutoConfiguration() {
    }
}
