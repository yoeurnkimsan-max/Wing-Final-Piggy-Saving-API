package com.example.piggy_saving.security;
import com.example.piggy_saving.exception.JwtAccessDeniedHandler;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF for stateless APIs
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // No sessions
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/api/v1/**", "/public/**").permitAll() // Public endpoints
                        .requestMatchers("/api/v1/email-otp/**").permitAll()
//                        .requestMatchers("/api/v1/accounts","/api/v1/accounts/**").permitAll()
                        .anyRequest().authenticated() // All other endpoints require authentication
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class); // Add JWT filter

        return http.build();
    }
}