package com.xaexal.app.Common;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.xaexal.app.DTO.SaintAndChurch;
import com.xaexal.app.DTO.SaintMember;
import com.xaexal.app.Entity.Member;
import com.xaexal.app.Repository.MemberRep;
import com.xaexal.app.Repository.SaintRep;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private MemberRep memberRep;
    @Autowired
    private SaintRep saintRep;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // 1. 어느 서비스인지 확인
        String registrationId = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();
        String email = null;
        String mobile = null;

        try {
            if ("kakao".equals(registrationId)) {
                // [카카오 방식]
                Map<String, Object> kakaoAccount = (Map<String, Object>) oAuth2User.getAttribute("kakao_account");
                if (kakaoAccount != null) {
                    email = (String) kakaoAccount.get("email");
                }
            } else if("naver".equals(registrationId)) {
            	Map<String,Object> naverResponse = (Map<String,Object>) oAuth2User.getAttribute("response");
            	if(naverResponse!=null) {
            		email=(String) naverResponse.get("email");
            		mobile=(String)naverResponse.get("mobile");
            		System.out.println("naver ["+mobile+"] ["+email+"]");
            	}
            } else if ("google".equals(registrationId)) {
                // [구글 방식] - getAttribute("email")이 가끔 작동안할 때를 대비해
                // attributes 맵 전체에서 안전하게 꺼내옵니다.
                Map<String, Object> attributes = oAuth2User.getAttributes();
                email = (String) attributes.get("email");
            }
        } catch (Exception e) {
            System.out.println("데이터 추출 중 에러 발생: " + e.getMessage());
        }

        System.out.println("===============================");
        System.out.println("Provider: " + registrationId);
        System.out.println("Extracted Email: " + email);
        System.out.println("===============================");

        // 2. 공통 DB 조회
        if(mobile!=null && !mobile.isEmpty()) {
        	mobile = mobile.replaceAll("[^0-9]", "");
        	if (mobile.startsWith("010") && mobile.length() > 3) {
                mobile = mobile.substring(3);
            }
        	System.out.println("naver mobile ["+mobile+"]");
        	Member member = memberRep.findByMobile(mobile);
        	HttpSession s = request.getSession();
        	if(member!=null) {
        		System.out.println("member name ["+member.getName()+"]");
        		s.setAttribute("title", "교적관리");
        		s.setAttribute("member_id", member.getId());
        		s.setAttribute("mobile", mobile);
        		s.setAttribute("name", member.getName());
        		SaintAndChurch saint = saintRep.searchByMemberIdAndActive(member.getId(),"1");
        		if(saint!=null) {
        			Integer rid = saint.getRid() != null ? saint.getRid() : 0;
        			// 교적관리최고책임자(roleId=1)는 특정 교회에 속하지 않으므로 church_id/church_name을 비움
        			boolean isTopAdmin = rid == 1;
        			s.setAttribute("level", rid);
        			s.setAttribute("church_id", isTopAdmin ? 0 : saint.getChurchId());
        			s.setAttribute("church_name", isTopAdmin ? null : saint.getChurchName());
        			s.setAttribute("role_id", rid);
        		}
                response.sendRedirect("https://localhost:3000/oauth-callback");
                return;
        	} else {
        		// 미가입 네이버 사용자 → 소셜 가입으로 이동
        		s.setAttribute("social_mobile", mobile);
        		s.setAttribute("social_provider", registrationId);
        		response.sendRedirect("https://localhost:3000/oauth-callback?action=signup");
        		return;
        	}
        } else if (email != null && !email.isEmpty()) {
            SaintMember member = memberRep.searchByEmail(email);
            if (member != null) {
            	HttpSession s = request.getSession();
            	// 교적관리최고책임자(roleId=1)는 특정 교회에 속하지 않으므로 church_id/church_name을 비움
            	boolean isTopAdmin = member.getRoleId() != null && member.getRoleId() == 1;
            	s.setAttribute("title", "교적관리");
            	s.setAttribute("member_id", member.getMemberId());
            	s.setAttribute("mobile", member.getMobile());
            	s.setAttribute("name", member.getMemberName());
            	s.setAttribute("level", member.getRoleId());
            	s.setAttribute("church_id", isTopAdmin ? 0 : member.getChurchId());
            	s.setAttribute("church_name", isTopAdmin ? null : member.getChurchName());
            	s.setAttribute("role_id", member.getRoleId());
                response.sendRedirect("https://localhost:3000/oauth-callback");
                return;
            } else {
            	// 미가입 구글/카카오 사용자 → 소셜 가입으로 이동
                System.out.println("미가입 소셜 사용자: " + email);
                HttpSession s = request.getSession();
                s.setAttribute("social_email", email);
                s.setAttribute("social_provider", registrationId);
                response.sendRedirect("https://localhost:3000/oauth-callback?action=signup");
                return;
            }
        }

        // 이메일/모바일 정보 없음
        response.sendRedirect("https://localhost:3000/login?error=not_found");
    }
    private void loginUser(HttpServletRequest request, SaintMember member) {
    }
}
