package com.restaurant.ordering.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Logs where the API documentation lives once the web server is up.
 * Reads the effective port and paths from the environment so the URL stays
 * correct even with a custom {@code server.port} (including a random port).
 */
@Component
class SwaggerUrlLogger implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger log = LoggerFactory.getLogger(SwaggerUrlLogger.class);

    private final Environment environment;

    SwaggerUrlLogger(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        String port = environment.getProperty("local.server.port",
                environment.getProperty("server.port", "8080"));
        String contextPath = environment.getProperty("server.servlet.context-path", "");
        String swaggerPath = environment.getProperty("springdoc.swagger-ui.path", "/swagger-ui.html");
        String apiDocsPath = environment.getProperty("springdoc.api-docs.path", "/v3/api-docs");

        String base = "http://localhost:" + port + contextPath;
        log.info("Swagger UI available at {}{} (OpenAPI: {}{})", base, swaggerPath, base, apiDocsPath);
    }
}
