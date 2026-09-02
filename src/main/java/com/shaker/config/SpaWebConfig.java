package com.shaker.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

/**
 * Serves the built Vue SPA from {@code classpath:/static/} and forwards unknown, non-API,
 * non-asset paths to {@code index.html} so client-side routing (history mode) works on refresh.
 */
@Configuration
public class SpaWebConfig implements WebMvcConfigurer {

    private static final Resource INDEX = new ClassPathResource("static/index.html");

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(@NonNull String resourcePath, @NonNull Resource location)
                            throws IOException {
                        Resource requested = location.createRelative(resourcePath);
                        if (requested.exists() && requested.isReadable()) {
                            return requested;
                        }
                        // Let the API and Spring's own error handling flow through unchanged.
                        if (resourcePath.startsWith("api/") || resourcePath.startsWith("error")) {
                            return null;
                        }
                        // Anything else is a client-side route -> hand back the SPA shell.
                        return INDEX.exists() ? INDEX : null;
                    }
                });
    }
}
