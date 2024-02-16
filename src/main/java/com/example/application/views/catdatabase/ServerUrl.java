package com.example.application.views.catdatabase;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class ServerUrl {
    @Value("${server.url}")
    String url;

    @Bean
    public String getUrl() {
        return url;
    }
}
