package com.dietary.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Dietiary API")
                        .description("""
                                REST API for Dietiary, a platform that digitizes how dietitians work with their clients.

                                **Try it:** call `POST /api/auth/login` with the public demo account \
                                `{"email": "demo@dietiary.app", "password": "Dietiary-demo-2026"}`, \
                                copy `accessToken` from the response, click **Authorize** and paste it. \
                                The client account `elif.demo@dietiary.app` (same password) works for the `/api/mobile` endpoints.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Source on GitHub")
                                .url("https://github.com/aserhatsahin/Dietiary"))
                        .license(new License()
                                .name("AGPL-3.0")
                                .url("https://www.gnu.org/licenses/agpl-3.0.html")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter your JWT token")));
    }
}
