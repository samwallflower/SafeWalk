package com.samwallflower.safewalk.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    private static final String BEARER = "bearerAuth ";

    @Bean
    public OpenAPI safeWalkOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Safe Walk API")
                        .version("v1")
                        .description("API documentation for the Safe Walk application. This API allows users to request safe walking routes, report incidents, and manage their accounts."))
                        .components(new Components()
                                .addSecuritySchemes(BEARER, new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                        .addSecurityItem(new SecurityRequirement().addList(BEARER));
    }
}
