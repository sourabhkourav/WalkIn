package com.walkin.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    static final String BEARER_AUTH = "bearerAuth";

    @Bean
    OpenAPI walkInOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("WalkIn API")
                        .version("1.0.0")
                        .description("REST API for recruitment walk-in drives")
                        .contact(new Contact().name("WalkIn API team")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH))
                .components(new Components().addSecuritySchemes(BEARER_AUTH,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer").bearerFormat("JWT")
                                .description("Use the access token returned by POST /api/auth/login")));
    }
}
