package com.ict.project.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
	@Override
	public void addCorsMappings(CorsRegistry registry) {
	    registry.addMapping("/api/**")
	        .allowedOrigins(
	            "http://localhost:9191",
	            "https://hoppscotch.io"
	        )
	        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
	        .allowedHeaders("*")
	        .exposedHeaders("Authorization")
	        .allowCredentials(true)
	        .maxAge(3600);
	}

}
