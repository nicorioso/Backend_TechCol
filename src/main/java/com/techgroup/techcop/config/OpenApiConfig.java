package com.techgroup.techcop.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {

    @Bean
    public OpenAPI techCopOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("TechCop API")
                        .version("v1")
                        .description("Documentacion OpenAPI para autenticacion, clientes, carrito, productos, pedidos y pagos."));
    }
}
