package com.restaurant.ordering.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
class AppConfig {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
