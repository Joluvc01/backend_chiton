package com.chiton.api.config;

import com.chiton.api.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authProvider;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sessionManager ->
                        sessionManager
                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authRequest ->
                        authRequest
                                .requestMatchers("/auth/**").permitAll()
                                .requestMatchers("/categories/**").hasAnyAuthority("ALMACEN","GERENCIA")
                                .requestMatchers("/products/**").hasAnyAuthority("ALMACEN","GERENCIA","PRODUCCION","DISENIO")
                                .requestMatchers("/users/**").hasAuthority("GERENCIA")
                                .requestMatchers("/customers/**").hasAnyAuthority("GERENCIA")
                                .requestMatchers("/references/**").hasAnyAuthority("GERENCIA","DISENIO","PRODUCCION")
                                .requestMatchers("/purchaseOrders/**").hasAnyAuthority("GERENCIA","ALMACEN")
                                .requestMatchers("/productionOrders/**").hasAnyAuthority("GERENCIA","PRODUCCION")
                                .requestMatchers("/translateOrders/**").hasAnyAuthority("GERENCIA","PRODUCCION")
                                .anyRequest().authenticated()
                )
                .authenticationProvider(authProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

}
