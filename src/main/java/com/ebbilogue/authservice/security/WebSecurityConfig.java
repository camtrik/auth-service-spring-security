package com.ebbilogue.authservice.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.SecurityFilterChain;

import com.ebbilogue.authservice.security.jwt.AuthEntryPointJwt;
import com.ebbilogue.authservice.security.jwt.AuthTokenFilter;
import com.ebbilogue.authservice.security.services.UserDetailsServiceImpl;

/**
 * 配置类，负责设置整个应用程序的安全配置
 */
@Configuration
@EnableMethodSecurity 
public class WebSecurityConfig {
    @Autowired
    UserDetailsServiceImpl userDetailsService; 

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler; 
    
    // 注册过滤器, from AuthTokenFilter.java
    @Bean
    public AuthTokenFilter authenticationJwAuthTokenFilter() {
        return new AuthTokenFilter(); 
    }

    @Bean 
    public PasswordEncoder passwordEncoder() { 
        return new BCryptPasswordEncoder();
    }

    @Bean 
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(); 

        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    @Bean 
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception { 
        return authConfig.getAuthenticationManager();
    } 

    @Bean 
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception { 
        // 禁止跨站请求伪造（CSRF）
        http.csrf(csrf -> csrf.disable()) 
            // 异常处理
            .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
            // 禁用session
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 授权请求
            .authorizeHttpRequests(auth -> 
                auth.requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/test/**").permitAll()
                .anyRequest().authenticated()
            );

        http.authenticationProvider(authenticationProvider());
        
        http.addFilterBefore(authenticationJwAuthTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
