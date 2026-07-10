package com.warmingup.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Value("${app.swagger.server-url:/}")
    private String serverUrl;

    @Bean
    public OpenAPI openAPI() {
        SecurityScheme sessionAuth = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.COOKIE)
                .name("JSESSIONID");

        return new OpenAPI()
                .info(new Info()
                        .title("WarmingUp API")
                        .version("v1.0"))
                .addServersItem(new Server()
                        .url(serverUrl)
                        .description("Current deployment"))
                .components(new Components().addSecuritySchemes("sessionAuth", sessionAuth))
                .addSecurityItem(new SecurityRequirement().addList("sessionAuth"));
    }
}
