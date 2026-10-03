package com.restaurant.ordering.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class OpenApiConfig {

    @Bean
    OpenAPI restaurantOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Online Restaurant — Order API")
                .version("0.0.1")
                .description("""
                        Order API for pickup / delivery with a fake payment gateway.

                        Payment is simulated and deterministic: any order whose total ends
                        in .13 (e.g. 10.13 €) is declined, every other amount is captured.
                        This lets the failure path be tested reproducibly."""));
    }
}
