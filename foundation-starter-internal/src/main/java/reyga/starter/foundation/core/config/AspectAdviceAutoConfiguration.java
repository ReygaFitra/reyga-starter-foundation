package reyga.starter.foundation.core.config;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

import reyga.starter.foundation.core.aspect.AspectAfter;
import reyga.starter.foundation.core.aspect.AspectAfterReturning;
import reyga.starter.foundation.core.aspect.AspectAfterThrowing;
import reyga.starter.foundation.core.aspect.AspectAround;
import reyga.starter.foundation.core.aspect.AspectBefore;
import reyga.starter.foundation.core.aspect.AspectProcessor;
import reyga.starter.foundation.core.aspect.BaseAspectAfter;
import reyga.starter.foundation.core.aspect.BaseAspectAfterReturning;
import reyga.starter.foundation.core.aspect.BaseAspectAfterThrowing;
import reyga.starter.foundation.core.aspect.BaseAspectAround;
import reyga.starter.foundation.core.aspect.BaseAspectBefore;
import reyga.starter.foundation.core.config.properties.ConfigProperties;

/**
 * Configures the AspectAdvice feature using existing bean names and properties.
 * Explicit legacy CoreConfig imports retain priority to avoid duplicate bean definitions.
 */
@AutoConfiguration
@ConditionalOnMissingBean(CoreConfig.class)
@EnableConfigurationProperties(ConfigProperties.class)
public class AspectAdviceAutoConfiguration {

    /** Creates this feature configuration. */
    public AspectAdviceAutoConfiguration() {
    }

    /**
     * Creates the existing aspectAround bean with unchanged activation and defaults.
     * @param configProperties bound properties supplying the configured behavior bean name
     * @param beanFactory application bean factory used to resolve and validate the behavior
     * @return the configured bean using the legacy factory behavior
     * @throws IllegalStateException if the behavior name is blank, missing, or has the wrong type
     */
    @Bean
    @ConditionalOnProperty(name = "reyga.config.aspect.around", havingValue = "true")
    public AspectAround aspectAround(ConfigProperties configProperties, ListableBeanFactory beanFactory) {
        Object behavior = resolveAspectBehavior(
                beanFactory, configProperties.aspect().behavior(),
                "reyga.config.aspect.behavior", "reyga.config.aspect.around",
                BaseAspectAround.class
        );
        return new AspectAround((AspectProcessor) behavior);
    }

    /**
     * Creates the existing aspectBefore bean with unchanged activation and defaults.
     * @param configProperties bound properties supplying the configured behavior bean name
     * @param beanFactory application bean factory used to resolve and validate the behavior
     * @return the configured bean using the legacy factory behavior
     * @throws IllegalStateException if the behavior name is blank, missing, or has the wrong type
     */
    @Bean
    @ConditionalOnProperty(name = "reyga.config.aspect.before", havingValue = "true")
    public AspectBefore aspectBefore(ConfigProperties configProperties, ListableBeanFactory beanFactory) {
        Object behavior = resolveAspectBehavior(
                beanFactory, configProperties.aspect().beforeBehavior(),
                "reyga.config.aspect.before-behavior", "reyga.config.aspect.before",
                BaseAspectBefore.class
        );
        return new AspectBefore((BaseAspectBefore) behavior);
    }

    /**
     * Creates the existing aspectAfter bean with unchanged activation and defaults.
     * @param configProperties bound properties supplying the configured behavior bean name
     * @param beanFactory application bean factory used to resolve and validate the behavior
     * @return the configured bean using the legacy factory behavior
     * @throws IllegalStateException if the behavior name is blank, missing, or has the wrong type
     */
    @Bean
    @ConditionalOnProperty(name = "reyga.config.aspect.after", havingValue = "true")
    public AspectAfter aspectAfter(ConfigProperties configProperties, ListableBeanFactory beanFactory) {
        Object behavior = resolveAspectBehavior(
                beanFactory, configProperties.aspect().afterBehavior(),
                "reyga.config.aspect.after-behavior", "reyga.config.aspect.after",
                BaseAspectAfter.class
        );
        return new AspectAfter((BaseAspectAfter) behavior);
    }

    /**
     * Creates the existing aspectAfterReturning bean with unchanged activation and defaults.
     * @param configProperties bound properties supplying the configured behavior bean name
     * @param beanFactory application bean factory used to resolve and validate the behavior
     * @return the configured bean using the legacy factory behavior
     * @throws IllegalStateException if the behavior name is blank, missing, or has the wrong type
     */
    @Bean
    @ConditionalOnProperty(name = "reyga.config.aspect.after-returning", havingValue = "true")
    public AspectAfterReturning aspectAfterReturning(ConfigProperties configProperties, ListableBeanFactory beanFactory) {
        Object behavior = resolveAspectBehavior(
                beanFactory, configProperties.aspect().afterReturningBehavior(),
                "reyga.config.aspect.after-returning-behavior", "reyga.config.aspect.after-returning",
                BaseAspectAfterReturning.class
        );
        return new AspectAfterReturning((BaseAspectAfterReturning) behavior);
    }

    /**
     * Creates the existing aspectAfterThrowing bean with unchanged activation and defaults.
     * @param configProperties bound properties supplying the configured behavior bean name
     * @param beanFactory application bean factory used to resolve and validate the behavior
     * @return the configured bean using the legacy factory behavior
     * @throws IllegalStateException if the behavior name is blank, missing, or has the wrong type
     */
    @Bean
    @ConditionalOnProperty(name = "reyga.config.aspect.after-throwing", havingValue = "true")
    public AspectAfterThrowing aspectAfterThrowing(ConfigProperties configProperties, ListableBeanFactory beanFactory) {
        Object behavior = resolveAspectBehavior(
                beanFactory, configProperties.aspect().afterThrowingBehavior(),
                "reyga.config.aspect.after-throwing-behavior", "reyga.config.aspect.after-throwing",
                BaseAspectAfterThrowing.class
        );
        return new AspectAfterThrowing((BaseAspectAfterThrowing) behavior);
    }

    private Object resolveAspectBehavior(ListableBeanFactory beanFactory, String configuredBehaviorName,
                                         String behaviorProperty, String enabledProperty, Class<?> requiredType) {
        if (!StringUtils.hasText(configuredBehaviorName)) {
            throw new IllegalStateException(
                    behaviorProperty + " must contain the bean name of a "
                            + requiredType.getSimpleName() + " implementation when "
                            + enabledProperty + "=true"
            );
        }
        String behaviorName = configuredBehaviorName.trim();

        if (!beanFactory.containsBean(behaviorName)) {
            throw new IllegalStateException(
                    "No bean named '" + behaviorName + "' was found for " + behaviorProperty + ". "
                            + "Register a @Component that extends " + requiredType.getSimpleName()
            );
        }

        Object behavior = beanFactory.getBean(behaviorName);
        Class<?> behaviorType = AopUtils.getTargetClass(behavior);
        if (!requiredType.isAssignableFrom(behaviorType)) {
            throw new IllegalStateException(
                    "Bean named '" + behaviorName + "' configured by " + behaviorProperty
                            + " must extend " + requiredType.getSimpleName()
                            + " but was " + behaviorType.getName()
            );
        }

        return behavior;
    }
}
