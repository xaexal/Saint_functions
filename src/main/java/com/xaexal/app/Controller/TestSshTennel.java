package com.xaexal.app.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xaexal.app.Common.SshTunnelManager;

@RestController
public class TestSshTennel {

    private final SshTunnelManager tunnelManager;

    // 생성자 주입
    public TestSshTennel(SshTunnelManager tunnelManager) {
        this.tunnelManager = tunnelManager;
    }

    @GetMapping("/test-ssh")
    public String testSsh() {
        return "SSH 터널이 이미 설정되어 있어요!";
    }
}