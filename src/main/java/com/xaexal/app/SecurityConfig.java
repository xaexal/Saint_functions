package com.xaexal.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import com.xaexal.app.Common.OAuth2SuccessHandler;
import com.xaexal.app.Common.RememberMeFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    @Autowired
    private RememberMeFilter rememberMeFilter;

    @Bean
    public FilterRegistrationBean<RememberMeFilter> rememberMeFilterRegistration() {
        FilterRegistrationBean<RememberMeFilter> registration = new FilterRegistrationBean<>(rememberMeFilter);
        registration.setOrder(-200); // Spring Security(-100)보다 먼저 실행
        return registration;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/**").permitAll()
            )
            .oauth2Login(oauth2 -> oauth2
                .successHandler(oAuth2SuccessHandler)
            );

        return http.build();
    }
}
