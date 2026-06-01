package com.example.flightbookingsystem.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    
    private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Flight Booking System API")
                        .version("1.0.0")
                        .description("RESTful API for Flight Booking System with JWT Authentication. \n"
                                + "Use /api/v1/auth/register (email, name, password) or /api/v1/auth/login (email, password) to get your JWT token, "
                                + "then click 'Authorize' button and enter: Bearer <your-token>")
                        .contact(new Contact()
                                .name("API Support")
                                .email("support@example.com")))
                .addSecurityItem(new SecurityRequirement()
                        .addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT token obtained from /api/v1/auth/login or /api/v1/auth/register")));
    }
}
