package com.xaexal.app.Common;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
public class DataSourceConfig {

    @Value("${app.datasource.mode:direct}")
    private String mode;

    @Value("${app.datasource.tunnel-local-port:3307}")
    private int tunnelLocalPort;

    @Value("${app.datasource.direct-host}")
    private String directHost;

    @Value("${app.datasource.direct-port:3306}")
    private int directPort;

    @Value("${app.datasource.db-name:saint}")
    private String dbName;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    // sshTunnelManager가 먼저 초기화되어야 tunnel 모드에서 DB 접속이 가능하므로 순서를 강제한다.
    // spring.datasource.hikari.* 프로퍼티를 이 빈에 바인딩하기 위해 @ConfigurationProperties 필요
    // (직접 만든 @Bean이라 spring.datasource.hikari.*가 자동으로 적용되지 않음)
    @Bean
    @DependsOn("sshTunnelManager")
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    public DataSource dataSource() {
        boolean tunnel = "tunnel".equalsIgnoreCase(mode);
        String host = tunnel ? "localhost" : directHost;
        int port = tunnel ? tunnelLocalPort : directPort;

        // SSH 터널 구간은 이미 암호화되어 있으므로 MySQL 자체 TLS 협상은 비활성화하여
        // 커넥션 생성 왕복(RTT)을 줄인다. connectTimeout/socketTimeout으로
        // 터널이 죽었을 때 무한 대기 대신 빠르게 실패하도록 한다.
        return DataSourceBuilder.create()
                .driverClassName("com.mysql.cj.jdbc.Driver")
                .url("jdbc:mysql://" + host + ":" + port + "/" + dbName
                        + "?serverTimezone=UTC&sslMode=DISABLED&connectTimeout=5000&socketTimeout=15000")
                .username(username)
                .password(password)
                .build();
    }
}
