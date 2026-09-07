package com.xaexal.app.Common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Component("sshTunnelManager")
public class SshTunnelManager {

    private final SshTunnel sshTunnel = new SshTunnel();

    @Value("${app.datasource.mode:direct}")
    private String datasourceMode;

    @Value("${ssh.tunnel.user}")
    private String sshUser;

    @Value("${ssh.tunnel.password}")
    private String sshPassword;

    @PostConstruct
    public void init() throws Exception {
        if (!"tunnel".equalsIgnoreCase(datasourceMode)) {
            return; // direct 모드에서는 SSH 터널을 열지 않음
        }
        sshTunnel.connectWithTunnel(
                "193.123.234.59",    // SSH 서버 주소
                sshUser,             // SSH 사용자
                sshPassword,         // SSH 비밀번호
                22,                  // SSH 포트 (기본: 22)
                3307,                // 로컬 포트 (임의 설정)
                "127.0.0.1",         // 원격 DB 주소
                3306                 // 원격 DB 포트 (MySQL 예시)
        );
    }

    @PreDestroy
    public void shutdown() {
        sshTunnel.disconnect();
    }

    // 네트워크 순단 등으로 SSH 세션이 끊어진 채 방치되는 것을 막기 위한 워치독.
    // 끊어져 있으면 동일 파라미터로 재연결을 시도한다.
    @Scheduled(fixedDelay = 30_000)
    public void checkAndReconnect() {
        if (!"tunnel".equalsIgnoreCase(datasourceMode)) return;
        if (sshTunnel.isConnected()) return;

        System.out.println("⚠️ SSH 터널 끊김 감지, 재연결 시도...");
        try {
            sshTunnel.reconnect();
            System.out.println("✅ SSH 터널 재연결 성공");
        } catch (Exception e) {
            System.out.println("❌ SSH 터널 재연결 실패: " + e.getMessage());
        }
    }
}
