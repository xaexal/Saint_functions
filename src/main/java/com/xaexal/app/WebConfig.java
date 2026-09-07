package com.xaexal.app;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	// 환경변수(CORS_ALLOWED_ORIGIN)를 설정하지 않으면 기존과 동일하게 로컬 프론트엔드 주소를 사용
	@Value("${cors.allowed-origin:https://localhost:3000}")
	private String allowedOrigin;

	@Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 전체 API 경로에 대해
                .allowedOrigins(allowedOrigin) // 허용할 클라이언트 주소
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
