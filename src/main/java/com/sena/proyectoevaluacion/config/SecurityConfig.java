package com.sena.proyectoevaluacion.config;

import com.sena.proyectoevaluacion.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Autowired
	private CustomUserDetailsService customUserDetailsService;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				// ✅ HABILITAR CSRF CON CONFIGURACIÓN POR DEFECTO
				.csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
				.authorizeHttpRequests(authz -> authz
						// ✅ PERMITIR ACCESO A LAS APIs SIN AUTENTICACIÓN
						.requestMatchers("/api/**").permitAll()

						// Permisos públicos
						.requestMatchers("/", "/usuarios/login", "/usuarios/registro", "/css/**", "/js/**",
								"/assets/**", "/webjars/**", "/images/**", "/error/**")
						.permitAll()

						// ✅ PERMITIR TODOS LOS MÉTODOS HTTP PARA PROFESIONALES
						.requestMatchers("/profesional/**").hasRole("PROFESIONAL")

						// ✅ RUTAS EXCLUSIVAS PARA USUARIOS NORMALES
						.requestMatchers("/usuarios/home", "/usuarios/reservarcita", "/usuarios/citas_usuario",
								"/usuarios/perfilusuario", "/usuarios/actualizar-perfil-ajax", "/citas/**")
						.hasRole("USER")

						.anyRequest().authenticated())
				.formLogin(form -> form.loginPage("/usuarios/login").loginProcessingUrl("/usuarios/login")
						.successHandler(authenticationSuccessHandler()).failureUrl("/usuarios/login?error=true")
						.permitAll())
				.logout(logout -> logout.logoutRequestMatcher(new AntPathRequestMatcher("/usuarios/logout"))
						.logoutSuccessUrl("/usuarios/login?logout=true").invalidateHttpSession(true)
						.deleteCookies("JSESSIONID").permitAll())
				.exceptionHandling(exception -> exception.accessDeniedPage("/acceso-denegado"))
				.sessionManagement(session -> session.maximumSessions(1).expiredUrl("/usuarios/login?expired=true"));

		return http.build();
	}

	// ✅ MANEJADOR PERSONALIZADO PARA REDIRECCIÓN BASADA EN ROLES
	@Bean
	public AuthenticationSuccessHandler authenticationSuccessHandler() {
		return new AuthenticationSuccessHandler() {
			@Override
			public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
					Authentication authentication) throws IOException, ServletException {

				System.out.println("DEBUG: Authentication Success - User: " + authentication.getName());
				System.out.println("DEBUG: Authorities: " + authentication.getAuthorities());

				boolean isProfesional = authentication.getAuthorities().stream()
						.anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_PROFESIONAL"));

				System.out.println("DEBUG: Is Profesional: " + isProfesional);

				if (isProfesional) {
					System.out.println("DEBUG: Redirecting to /profesional/dashboard");
					response.sendRedirect("/profesional/dashboard");
				} else {
					System.out.println("DEBUG: Redirecting to /usuarios/home");
					response.sendRedirect("/usuarios/home");
				}
			}
		};
	}

	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		authProvider.setUserDetailsService(customUserDetailsService);
		authProvider.setPasswordEncoder(passwordEncoder());
		return authProvider;
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
		return authConfig.getAuthenticationManager();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}