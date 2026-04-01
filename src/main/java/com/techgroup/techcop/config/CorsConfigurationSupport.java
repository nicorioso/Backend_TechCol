package com.techgroup.techcop.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableConfigurationProperties(CorsProperties.class)
public class CorsConfigurationSupport {

    @Bean
    public CorsConfigurationSource corsConfigurationSource(CorsProperties corsProperties) {
        CorsConfiguration configuration = buildConfiguration(corsProperties);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public WebMvcConfigurer corsWebMvcConfigurer(CorsProperties corsProperties) {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                applyConfiguration(registry.addMapping("/**"), corsProperties);
            }
        };
    }

    private CorsConfiguration buildConfiguration(CorsProperties corsProperties) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(corsProperties.isAllowCredentials());
        configuration.setAllowedOrigins(corsProperties.getAllowedOrigins());
        configuration.setAllowedMethods(corsProperties.getAllowedMethods());
        configuration.setAllowedHeaders(corsProperties.getAllowedHeaders());
        configuration.setExposedHeaders(corsProperties.getExposedHeaders());
        configuration.setMaxAge(corsProperties.getMaxAge());

        if (!CollectionUtils.isEmpty(corsProperties.getAllowedOriginPatterns())) {
            configuration.setAllowedOriginPatterns(corsProperties.getAllowedOriginPatterns());
        }

        return configuration;
    }

    private void applyConfiguration(CorsRegistration registration, CorsProperties corsProperties) {
        registration.allowedOrigins(corsProperties.getAllowedOrigins().toArray(String[]::new));
        registration.allowedMethods(corsProperties.getAllowedMethods().toArray(String[]::new));
        registration.allowedHeaders(corsProperties.getAllowedHeaders().toArray(String[]::new));
        registration.exposedHeaders(corsProperties.getExposedHeaders().toArray(String[]::new));
        registration.allowCredentials(corsProperties.isAllowCredentials());
        registration.maxAge(corsProperties.getMaxAge());

        if (!CollectionUtils.isEmpty(corsProperties.getAllowedOriginPatterns())) {
            registration.allowedOriginPatterns(corsProperties.getAllowedOriginPatterns().toArray(String[]::new));
        }
    }

}
