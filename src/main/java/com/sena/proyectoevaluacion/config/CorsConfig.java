package com.sena.proyectoevaluacion.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class CorsConfig {

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();

		// Permitir todos los orígenes (para desarrollo)
		configuration.setAllowedOriginPatterns(Arrays.asList("*"));

		// Métodos HTTP permitidos
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"));

		// Headers permitidos
		configuration.setAllowedHeaders(Arrays.asList("*"));

		// Permitir credenciales
		configuration.setAllowCredentials(true);

		// Tiempo de caché
		configuration.setMaxAge(3600L);

		// Aplicar configuración a todas las rutas
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);

		return source;
	}
}