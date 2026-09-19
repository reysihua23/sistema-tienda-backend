package com.reydi.tienda.config;

import com.reydi.tienda.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Endpoints públicos
                        // ✅ 1. Rutas públicas primero
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/uploads/**").permitAll()

                        .requestMatchers("/api/productos/**").permitAll()
                        .requestMatchers("/api/productos/activos").permitAll()
                        .requestMatchers("/api/productos/buscar").permitAll()
                        .requestMatchers("/api/productos/filtrar").permitAll()
                        .requestMatchers("/api/producto-imagenes/**").permitAll()
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers("/api/usuarios").permitAll()
                        .requestMatchers("/api/clientes").permitAll()
                        // ✅ 2. WebSocket (debe ir ANTES de anyRequest)
                        .requestMatchers("/ws/**").permitAll()

                        // usuarios autenticados pueden crear pedidos
                        .requestMatchers(HttpMethod.POST, "/api/pedidos").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/pedidos/**").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/pedidos/**").authenticated()
                        .requestMatchers("/api/pagos/paypal/**").permitAll()

                        .requestMatchers("/api/comprobantes/**").authenticated()
                        //.requestMatchers("/api/comprobante//pedido/{pedidoId}").authenticated()

                        .requestMatchers("/api/servicios-tecnicos/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/servicios-tecnicos/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/servicios-tecnicos/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/servicios-tecnicos/**").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/servicios-tecnicos/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/servicios-tecnicos/**").authenticated()
                        // Endpoints protegidos
                        .requestMatchers("/api/clientes/**").permitAll()
                        .requestMatchers("/api/usuarios/perfil").authenticated()
                        .requestMatchers("/api/usuarios/**").hasRole("ADMIN")

                        .anyRequest().authenticated()

                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}