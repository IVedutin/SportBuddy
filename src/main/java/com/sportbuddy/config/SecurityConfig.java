package com.sportbuddy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@EnableMethodSecurity
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Статические ресурсы
                        .requestMatchers("/static/**", "/style/**", "/icons/**", "/css/**", "/js/**", "/fonts/**").permitAll()
                        // Публичные страницы
                        .requestMatchers("/", "/home", "/register", "/login", "/auth.html", "/signup.html", "/homer.html").permitAll()
                        // API эндпоинты для регистрации и авторизации - ВАЖНО!
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/register/**").permitAll()
                        .requestMatchers("/api/register/send-email-code").permitAll()
                        .requestMatchers("/api/register/verify-email-code").permitAll()
                        .requestMatchers("/api/register").permitAll()
                        // Другие публичные эндпоинты
                        .requestMatchers("/api/hello", "/api/greet").permitAll()
                        .requestMatchers("/court/ranked-matches", "/court/api/ranked-matches").permitAll()
                        // Все остальные запросы требуют аутентификации
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .permitAll()
                )
                .csrf(csrf -> csrf.disable());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}