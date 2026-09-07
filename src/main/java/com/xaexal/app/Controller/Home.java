package com.xaexal.app.Controller;

import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Home {
	@GetMapping("/")
	public String home() {
		return "home";
	}
    @GetMapping("/user/info")
    public Map<String, Object> getUser(@AuthenticationPrincipal OAuth2User principal) {
    	// 1. 전체 속성(Attributes)을 Map으로 가져옵니다.
        Map<String, Object> attributes = principal.getAttributes();
        
        // 2. Map에서 "email" 키에 해당하는 값을 추출합니다.
        // get()은 Object를 반환하므로 String으로 형변환(Casting)이 필요합니다.
        String email = (String) attributes.get("email");
        
        // 3. 콘솔에 출력합니다.
        System.out.println("===============================");
        System.out.println("로그인한 사용자 이메일: " + email);
        System.out.println("===============================");

        return attributes;
    }
}