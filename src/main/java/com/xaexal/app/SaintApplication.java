package com.xaexal.app;

import java.util.TimeZone;

import javax.sql.DataSource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class SaintApplication {

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		SpringApplication.run(SaintApplication.class, args);
	}

    // Hibernate/JPA 설정이 이 빈에 의존하도록 설정하여 순서를 보장합니다.
    @Bean
    @DependsOn("sshTunnelManager")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder builder, DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("com.xaexal.app.Entity") // 엔티티 패키지 경로(대소문자 구분하는 Linux에서 실제 이 이름과 정확히 일치해야 함)
                .build();
    }
}