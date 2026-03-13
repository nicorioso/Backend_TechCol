package com.techgroup.techcop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración centralizada de CORS
 * Define políticas de acceso desde orígenes externos
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // Orígenes permitidos
                .allowedOrigins(
                        "http://localhost:3000",      // React - puerto 3000
                        "http://localhost:5173",      // Vite - puerto 5173
                        "http://127.0.0.1:5173",      // Vite local
                        "https://frontend-techcol.onrender.com"
                )

                // Métodos HTTP permitidos
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD")

                // Headers permitidos
                .allowedHeaders("*")
                .exposedHeaders(
                        "X-Total-Count",              // Para paginación
                        "X-Page-Number",
                        "X-Page-Size",
                        "Authorization"               // Para tokens JWT
                )

                // Permitir credenciales
                .allowCredentials(false)          // Cambiar a true si usas cookies

                // Tiempo máximo de caché para preflight request
                .maxAge(3600);                    // 1 hora
    }
}
