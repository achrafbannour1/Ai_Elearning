package com.example.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {

    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {

            // ✅ CORS configuration
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**") // allow all endpoints
                        .allowedOrigins("http://localhost:4200") // Angular frontend
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }

            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                // Serve exams from local folder
                registry.addResourceHandler("/exams/**")
                        .addResourceLocations("file:///C:/exams/")
                        .setCachePeriod(0); // optional: disable caching

                // Serve static images
                registry.addResourceHandler("/images/**")
                        .addResourceLocations(
                                "classpath:/static/images/",
                                "file:src/main/resources/static/images/"
                        )
                        .setCachePeriod(0); // Disable caching for development
            }

        };
    }

}
