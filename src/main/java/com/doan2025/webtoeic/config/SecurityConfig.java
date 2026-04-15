package com.doan2025.webtoeic.config;

import com.doan2025.webtoeic.security.CustomerJwtDecoder;
import com.doan2025.webtoeic.security.JwtAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    // Thêm mảng PUBLIC_ENDPOINTS_OPTIONS
    private final String[] PUBLIC_ENDPOINTS_OPTIONS = {
            "/**" // Cho phép tất cả OPTIONS requests
    };
    private final String[] PUBLIC_ENDPOINTS_POST = {
            "/user",
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/verify-email",
            "/api/v1/auth/verify-otp",
            "/api/v1/auth/reset-password",
            "/api/v1/cloud/upload",
            "/api/v1/cloud/delete",
            "/api/v1/post/get-posts",
            "/api/v1/course/get-courses",
    };
    private final String[] PUBLIC_ENDPOINTS_GET = {
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/api/v1/post",
            "/api/v1/post/{id}",
            "/api/v1/course",
            "/api/v1/course/{id}",
            "/api/v1/payment/return",
            "/api/v1/payment/order-status",
            "/api/v1/category/post",
            "/api/v1/category/role",
            "/api/v1/category/gender",
            "/api/v1/category/course",
            "/api/v1/category/status-order",
            "/api/v1/category/status-schedule",
            "/api/v1/category/status-class",
            "/api/v1/category/status-attendance",
            "/api/v1/category/type-class-notification",
            "/api/v1/category/join-class-status",
    };

    private final CustomerJwtDecoder customerJwtDecoder;

    public SecurityConfig(@Lazy CustomerJwtDecoder customerJwtDecoder) {
        this.customerJwtDecoder = customerJwtDecoder;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.cors(); // Thêm dòng này để bật CORS
        httpSecurity.authorizeHttpRequests(
                request -> request
                        .requestMatchers(HttpMethod.OPTIONS, PUBLIC_ENDPOINTS_OPTIONS).permitAll() // Thêm dòng này
                        .requestMatchers(HttpMethod.POST, PUBLIC_ENDPOINTS_POST)
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, PUBLIC_ENDPOINTS_GET)
                        .permitAll()
                        .anyRequest()
                        .authenticated());

        httpSecurity.oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwtConfigurer -> jwtConfigurer
                        .decoder(customerJwtDecoder)
                        .jwtAuthenticationConverter(jwtAuthenticationConverter()))
                .authenticationEntryPoint(new JwtAuthenticationEntryPoint()));
        httpSecurity.csrf(AbstractHttpConfigurer::disable);

        return httpSecurity.build();
    }

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();

        // Danh sách IP được phép
        // Sửa danh sách allowedOrigins
        corsConfiguration.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",
                "https://fe-q0rg.onrender.com",
                "https://fe-learnez.vercel.app",
                "https://learn-ez-fe-local.vercel.app/"
        ));

        // Thêm pattern cho IP (nếu cần)
        corsConfiguration.setAllowedOriginPatterns(Arrays.asList(
                "http://192.168.*.*",
                "http://10.10.*.*",
                "http://172.17.*.*",
                "http://116.111.*.*" // Sử dụng pattern thay vì fixed IP
        ));

        corsConfiguration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        corsConfiguration.setAllowedHeaders(List.of("*"));
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setMaxAge(3600L); // Thêm max age

        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);

        return new CorsFilter(urlBasedCorsConfigurationSource);
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);

        return jwtAuthenticationConverter;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}
