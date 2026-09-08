package com.evrental.common.config;

import com.evrental.common.security.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
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

/**
 * Spring Security配置（Spring Boot 2.7 版本）
 * 基于JWT的无状态认证，RBAC权限控制
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(Arrays.asList("*"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors().and()
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
                // 公开接口
                .antMatchers("/api/auth/**").permitAll()
                .antMatchers("/api/vehicle/list", "/api/vehicle/detail/**", "/api/vehicle/hot", "/api/vehicle/brands").permitAll()
                .antMatchers("/api/store/list").permitAll()
                .antMatchers("/api/notice/list", "/api/notice/detail/**").permitAll()
                .antMatchers("/api/coupon/list").permitAll()
                .antMatchers("/api/member/**").permitAll()
                .antMatchers("/api/pay/qrcode/**", "/api/pay/order/**").permitAll()
                .antMatchers("/api/upload", "/api/file/upload").permitAll()
                .antMatchers("/api/image/show/**").permitAll()
                .antMatchers("/upload/**").permitAll()
                .antMatchers("/ws/**").permitAll()
                // Swagger
                .antMatchers("/doc.html", "/webjars/**", "/v2/api-docs/**", "/swagger-resources/**").permitAll()
                // 管理员
                .antMatchers("/api/admin/**").hasRole("ADMIN")
                // 运营
                .antMatchers("/api/operation/**").hasAnyRole("ADMIN", "OPERATOR")
                // 其余认证
                .anyRequest().authenticated()
            .and()
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
