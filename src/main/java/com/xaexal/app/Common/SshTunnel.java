package com.xaexal.app.Common;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class SshTunnel {

    private Session session;

    // 워치독이 세션 재연결 시 동일한 파라미터로 다시 붙을 수 있도록 보관
    private String sshHost, sshUser, sshPassword, remoteHost;
    private int sshPort, localPort, remotePort;

    public void connectWithTunnel(String sshHost, String sshUser, String sshPassword,
                                   int sshPort, int localPort,
                                   String remoteHost, int remotePort) throws Exception {
        this.sshHost = sshHost;
        this.sshUser = sshUser;
        this.sshPassword = sshPassword;
        this.sshPort = sshPort;
        this.localPort = localPort;
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;

        JSch jsch = new JSch();
        session = jsch.getSession(sshUser, sshHost, sshPort);
        session.setPassword(sshPassword);
        session.setConfig("StrictHostKeyChecking", "no");

        session.setServerAliveInterval(60_000);
        session.setServerAliveCountMax(10);

        session.connect();
        System.out.println("✅ SSH 연결 성공");

        int maxRetry = 5;
        for (int i = 1; i <= maxRetry; i++) {
            try {
                int assignedPort = session.setPortForwardingL(localPort, remoteHost, remotePort);
                System.out.println("✅ 포트 포워딩 설정됨: localhost:" + assignedPort + " → " + remoteHost + ":" + remotePort);
                return;
            } catch (com.jcraft.jsch.JSchException e) {
                if (i == maxRetry) throw e;
                System.out.println("⚠️ 포트 " + localPort + " 바인딩 실패 (" + i + "/" + maxRetry + "), 3초 후 재시도...");
                Thread.sleep(3000);
            }
        }
    }

    public boolean isConnected() {
        return session != null && session.isConnected();
    }

    // 세션이 죽어있으면(네트워크 순단 등) 동일 파라미터로 재연결한다.
    // 워치독(SshTunnelManager)에서 주기적으로 호출.
    public synchronized void reconnect() throws Exception {
        if (isConnected()) return;
        if (session != null) {
            try { session.disconnect(); } catch (Exception ignore) {}
        }
        connectWithTunnel(sshHost, sshUser, sshPassword, sshPort, localPort, remoteHost, remotePort);
    }

    public void disconnect() {
        if (session != null && session.isConnected()) {
            session.disconnect();
            System.out.println("🔌 SSH 연결 종료");
        }
    }
}
