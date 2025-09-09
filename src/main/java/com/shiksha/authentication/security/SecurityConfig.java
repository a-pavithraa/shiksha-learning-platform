package com.shiksha.authentication.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
                         JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.exceptionHandling((exception)-> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .authorizeHttpRequests(authz -> authz
                        // Public endpoints
                        .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()

                        // Admin and Teacher only endpoints for user management
                        .requestMatchers(HttpMethod.POST, "/api/users").hasAnyRole("ADMIN", "TEACHER")
                        .requestMatchers(HttpMethod.PUT, "/api/users/**").hasAnyRole("ADMIN", "TEACHER")
                        .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasAnyRole("ADMIN", "TEACHER")

                        // Profile endpoints - authenticated users can access their own profile
                        .requestMatchers(HttpMethod.GET, "/api/auth/profile").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/auth/profile").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/auth/change-password").authenticated()

                        // Assignment management - Teachers can create/update/delete, Students can read
                        .requestMatchers(HttpMethod.POST, "/api/assignments").hasRole("TEACHER")
                        .requestMatchers(HttpMethod.PUT, "/api/assignments/**").hasRole("TEACHER")
                        .requestMatchers(HttpMethod.DELETE, "/api/assignments/**").hasRole("TEACHER")
                        .requestMatchers(HttpMethod.GET, "/api/assignments/**").authenticated()

                        // Exam management - Teachers only
                        .requestMatchers(HttpMethod.POST, "/api/exams").hasRole("TEACHER")
                        .requestMatchers(HttpMethod.PUT, "/api/exams/**").hasRole("TEACHER")
                        .requestMatchers(HttpMethod.DELETE, "/api/exams/**").hasRole("TEACHER")
                        .requestMatchers(HttpMethod.GET, "/api/exams/**").authenticated()

                        // Grade management - Teachers only
                        .requestMatchers(HttpMethod.POST, "/api/grades").hasRole("TEACHER")
                        .requestMatchers(HttpMethod.GET, "/api/exams/*/grades").hasRole("TEACHER")

                        // Dashboard endpoints
                        .requestMatchers("/api/dashboard/teacher/**").hasRole("TEACHER")
                        .requestMatchers("/api/dashboard/student/**").hasRole("STUDENT")

                        // All other requests require authentication
                        .anyRequest().authenticated()
                ).cors(CorsConfigurer::disable)
                .csrf(CsrfConfigurer::disable);

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}