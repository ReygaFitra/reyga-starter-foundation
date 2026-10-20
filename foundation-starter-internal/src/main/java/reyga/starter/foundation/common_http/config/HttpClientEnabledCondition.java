package reyga.starter.foundation.common_http.config;

import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

class HttpClientEnabledCondition extends AnyNestedCondition {
    HttpClientEnabledCondition() {
        super(ConfigurationPhase.PARSE_CONFIGURATION);
    }

    @ConditionalOnProperty(name = "reyga.config.http-client.enabled", havingValue = "true")
    static class Enabled {
    }

    @ConditionalOnProperty(name = "reyga.config.default-bean.http-client", havingValue = "true")
    static class LegacyEnabled {
    }
}
