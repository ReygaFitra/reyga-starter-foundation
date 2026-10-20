package reyga.starter.foundation.defaults.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;

/**
 * Compatibility entry point for the default foundation feature configurations.
 * The associated imports resource lists feature auto-configurations. Excluding this
 * entry point continues to disable the same group; HTTP and validation utilities
 * remain independently registered in the top-level AutoConfiguration.imports file.
 */
@AutoConfiguration
@ImportAutoConfiguration
public class FoundationDefaultAutoConfiguration {

    /** Creates the default feature group. */
    public FoundationDefaultAutoConfiguration() {
    }
}
