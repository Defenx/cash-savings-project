package com.kavencore.moneyharbor.app.config;

import com.kavencore.moneyharbor.app.entity.RoleName;
import com.kavencore.moneyharbor.app.security.ProblemAccessDeniedHandler;
import com.kavencore.moneyharbor.app.security.ProblemAuthEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String[] SWAGGER = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger/**"
    };

    @Bean
    @Order(1)
    SecurityFilterChain swaggerChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher(SWAGGER)
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(reg -> reg.anyRequest().permitAll())
                .build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain appChain(HttpSecurity http, ProblemAuthEntryPoint authEntryPoint,
                                 ProblemAccessDeniedHandler accessDeniedHandler) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(reg -> reg
                        .requestMatchers("/user/sign-up", "/error").permitAll()
                        .requestMatchers("/accounts/**").hasRole(RoleName.USER.name())
                        .anyRequest().authenticated()
                )
                .httpBasic(b -> b.realmName("api"))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}



