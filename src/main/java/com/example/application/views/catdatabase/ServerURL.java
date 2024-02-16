package com.example.application.views.catdatabase;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class ServerURL {
    @Component
    public class YourClass {

        @Value("${server.url}")
        private String backendUrl;

        @Bean
        public String getBackendUrl() {
            return backendUrl;
        }
    }
}
