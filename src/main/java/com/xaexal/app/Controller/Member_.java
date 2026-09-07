package com.xaexal.app.Controller;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.xaexal.app.Common.ChurchInitService;
import com.xaexal.app.Common.LogRequest;
import com.xaexal.app.Common.LoginCheck;
import com.xaexal.app.Common.Result;
import com.xaexal.app.DTO.MemberBaptismSaint;
import com.xaexal.app.DTO.SaintMember;
import com.xaexal.app.Entity.Member;
import com.xaexal.app.Repository.MemberRep;
import com.xaexal.app.Service.S3Service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@RestController
@RequestMapping("/member")
public class Member_ {
	@Autowired private MemberRep _member;
	@Autowired private ChurchInitService _churchInit;
	@Autowired private S3Client _s3Client;
	@Autowired private S3Service _s3Service;

	@LoginCheck
	@PostMapping("/checkLogin")
	public ResponseEntity<?> checkLogin() {
		System.out.println("/member/checkLogin POST");
		return ResponseEntity.ok(new Result<Integer>(1,""));
	}

	@GetMapping("/sessionInfo")
	public ResponseEntity<?> sessionInfo(HttpSession s) {
		System.out.println("/member/sessionInfo GET");
		Integer memberId = (Integer) s.getAttribute("member_id");
		if (memberId == null) {
			return ResponseEntity.ok(new Result<>(0, "로그인되지 않았습니다"));
		}
		Map<String, Object> info = new HashMap<>();
		info.put("memberId", memberId);
		info.put("memberName", s.getAttribute("name"));
		info.put("churchId", s.getAttribute("church_id"));
		info.put("churchName", s.getAttribute("church_name"));
		info.put("roleId", s.getAttribute("role_id"));
		info.put("roleName", s.getAttribute("role_name"));
		return ResponseEntity.ok(new Result<>(1, "OK", info));
	}

//	@LoginCheck
	@PostMapping("/logout")
	public ResponseEntity<?> logout(HttpSession s) {
		System.out.println("/member/logout POST");
		if(s.getAttribute("member_id")!=null) {
			Member member = _member.findById((Integer)s.getAttribute("member_id")).orElse(null);
			if(member!=null) {
				this._member.setLogout(member.getId());
			}
			s.invalidate();
		}
		return ResponseEntity.ok(new Result<>(1,"로그아웃됐습니다"));
	}
	@PostMapping("/session-expire")
	public ResponseEntity<?> sessionExpire(HttpSession s) {
		if (s.getAttribute("member_id") != null) {
			Member member = _member.findById((Integer) s.getAttribute("member_id")).orElse(null);
			if (member != null) this._member.setLogout(member.getId());
			s.invalidate();
		}
		return ResponseEntity.ok(new Result<>(1, "세션만료"));
	}

	@PostMapping("/doLogin")
	public ResponseEntity<?> doLogin(@RequestBody Map<String, String> req, HttpSession s) {
		System.out.println("/member/doLogin POST");
		String mobile = req.get("mobile");
		String passcode = req.get("passcode");
		SaintMember member = this._member.findByMobileAndPasscode(mobile, passcode);
		if(member==null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당하는 회원을 찾을 수 없습니다.\n모바일번호와 비밀번호를 확인하고 다시 입력하십시오.");
		}
		// 교적관리최고책임자(roleId=1)는 특정 교회에 속하지 않으므로 church_id를 0으로 강제 설정
		boolean isTopAdmin = member.getRoleId() != null && member.getRoleId() == 1;
		Integer churchId = isTopAdmin ? 0 : member.getChurchId();
		String churchName = isTopAdmin ? null : member.getChurchName();

		s.setAttribute("title","교적관리");
		s.setAttribute("member_id", member.getMemberId());
		s.setAttribute("mobile", mobile);
		s.setAttribute("name", member.getMemberName());
		s.setAttribute("church_id", churchId);
		s.setAttribute("church_name", churchName);
		s.setAttribute("role_id", member.getRoleId());
		s.setAttribute("role_name", member.getRoleName() != null ? member.getRoleName() : "");
		_churchInit.initChurchTables(member.getChurchId());
		_member.setLogin(member.getMemberId());

		Map<String, Object> loginResult = new HashMap<>();
		loginResult.put("memberId", member.getMemberId());
		loginResult.put("memberName", member.getMemberName());
		loginResult.put("churchId", churchId);
		loginResult.put("churchName", churchName);
		loginResult.put("mobile", member.getMobile());
		loginResult.put("roleId", member.getRoleId());
		loginResult.put("roleName", member.getRoleName() != null ? member.getRoleName() : "");
		return ResponseEntity.ok(new Result<Map<String,Object>>(1,"success",loginResult));
	}
	@LogRequest
	@LoginCheck
	@PostMapping("/")  // 관리자가 수정
	public ResponseEntity<?> doPost(@RequestBody Member member,HttpSession s){
		System.out.println("/member/ POST");

		// 신규 등록(id 없음)인 경우, 입력된 항목(name/birthday/gender/mobile)만으로 기존 회원과 일치하는지 먼저 확인해서
		// 일치하는 회원이 있으면 새로 만들지 않고 그 회원을 그대로 돌려준다.
		if (member.getId() == null) {
			String name = blankToNull(member.getName());
			String birthday = blankToNull(member.getBirthday());
			String gender = blankToNull(member.getGender());
			String mobile = blankToNull(member.getMobile());
			if (name != null || birthday != null || gender != null || mobile != null) {
				List<Member> matches = this._member.findMatching(name, birthday, gender, mobile);
				if (!matches.isEmpty()) {
					return ResponseEntity.ok(new Result<Member>(1,"success",matches.get(0)));
				}
			}
		}

		Member savedMember = this._member.save(member);
		return ResponseEntity.ok(new Result<Member>(1,"success",savedMember));
	}

	private static String blankToNull(String s) {
		return (s == null || s.isBlank()) ? null : s;
	}

	@GetMapping("/socialSignupInfo")
	public ResponseEntity<?> socialSignupInfo(HttpSession s) {
		System.out.println("/member/socialSignupInfo GET");
		String email    = (String) s.getAttribute("social_email");
		String mobile   = (String) s.getAttribute("social_mobile");
		String provider = (String) s.getAttribute("social_provider");
		if (email == null && mobile == null) {
			return ResponseEntity.ok(new Result<>(0, "소셜 가입 정보가 없습니다."));
		}
		Map<String, Object> info = new HashMap<>();
		info.put("email",    email);
		info.put("mobile",   mobile);
		info.put("provider", provider);
		return ResponseEntity.ok(new Result<>(1, "OK", info));
	}

	@PostMapping("/doSocialSignup")
	public ResponseEntity<?> doSocialSignup(@RequestBody Map<String, String> req, HttpSession s) {
		System.out.println("/member/doSocialSignup POST");
		String email    = (String) s.getAttribute("social_email");
		String mobile   = (String) s.getAttribute("social_mobile");
		String provider = (String) s.getAttribute("social_provider");

		// 요청으로 넘어온 mobile이 있으면 우선 사용 (구글/카카오는 직접 입력)
		if (req.get("mobile") != null && !req.get("mobile").isBlank()) {
			mobile = req.get("mobile");
		}
		if (mobile == null || mobile.isBlank()) throw new RuntimeException("모바일번호가 필요합니다.");

		Member existing = _member.findByMobile(mobile);
		if (existing != null) throw new RuntimeException("이미 가입된 모바일번호입니다.");

		Member member = new Member();
		member.setMobile(mobile);
		if (email != null) member.setEmail(email);
		Member m = _member.save(member);
		if (m == null) throw new RuntimeException("회원 가입 실패");

		s.removeAttribute("social_email");
		s.removeAttribute("social_mobile");
		s.removeAttribute("social_provider");

		return ResponseEntity.ok(new Result<>(1, "가입되었습니다."));
	}

	@LogRequest
	@PostMapping("/doSignup") // 신규회원가입
	public ResponseEntity<?> doSignup(@RequestBody Map<String, String> req) throws Exception {
		System.out.println("/member/doSignup POST");

		Member member = this._member.findByMobile(req.get("mobile"));
		if(member!=null) throw new RuntimeException("이미 가입된 모바일번호입니다.");

		member = new Member();
		member.setMobile(req.get("mobile"));
		member.setPasscode(req.get("passcode"));
		Member m = this._member.save(member);
		if(m==null) throw new RuntimeException("회원 가입 실패");
		return ResponseEntity.ok(new Result<>(1, "가입되었습니다."));
	}
	// 여러사람 찾을때(가족전체 본인+가족 검색가능)
	@LogRequest
	@LoginCheck
	@GetMapping("/")
	public ResponseEntity<?> findAll(Member req) {
		System.out.println("/member/ GET");
        List<Member> members = this._member.findAll();
        return ResponseEntity.ok(new Result<List<Member>>(1, "", members));
	}
	@LogRequest
	@LoginCheck
	@GetMapping("/mobile/{mobile}")
	public ResponseEntity<?> searchByMobile(@PathVariable("mobile") String mobile){
		System.out.println("GET /member/mobile/"+mobile);
		Member member = this._member.findByMobile(mobile);
		if(member==null) return ResponseEntity.ok(new Result<Member>(1,"success",null));
		System.out.println("name ["+member.getName()+"]");
		return ResponseEntity.ok(new Result<Member>(1,"success",member));
	}
	@LogRequest
	@LoginCheck
	@GetMapping("/one/{member_id}")
	public ResponseEntity<?> findOne(@PathVariable("member_id") int member_id) {
		System.out.println("/member/ GET");

	    // 2. findById 수행
	    Member member = this._member.findById(member_id)
	            .orElseThrow(() -> new EntityNotFoundException("ID " + member_id + "번에 해당하는 회원을 찾을 수 없습니다."));

	    return ResponseEntity.ok(new Result<Member>(1, "", member));
	}

	@LoginCheck
	@GetMapping("/{id}")
	public ResponseEntity<?> findAll(@PathVariable("id") int id) {
		System.out.println("/member/"+id+" GET");

        Member member = this._member.findById(id).orElseThrow(()->new EntityNotFoundException("해당하는 사람을 찾지 못했습니다."));
        return ResponseEntity.ok(new Result<Member>(1, "", member));
	}

	@GetMapping("/name")
	public ResponseEntity<?> findByNameLike(@RequestParam Map<String,String> req){
		System.out.println("/member/name GET");
		System.out.println("name ["+req.get("name"));
		if(req.get("name")==null || req.get("name").isBlank())
			throw new RuntimeException("이름이 입력되지 않았습니다.");
		List<Member> members = this._member.findByNameLike(req.get("name"));

		return ResponseEntity.ok(new Result<List<Member>>(1,"",members));
	}


	@LoginCheck
	@PostMapping("/image/{member_id}")
	public ResponseEntity<?> uploadImage(@PathVariable("member_id") int member_id,
			@RequestPart("file") MultipartFile file) throws Exception {
		Member member = this._member.findById(member_id)
				.orElseThrow(() -> new EntityNotFoundException("해당하는 회원을 찾지 못했습니다."));

		String originalName = file.getOriginalFilename();
		String ext = "";
		if (originalName != null && originalName.lastIndexOf(".") != -1)
			ext = originalName.substring(originalName.lastIndexOf("."));
		String newFileName = "member_" + member_id + "_"
				+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ext;

		PutObjectRequest putReq = PutObjectRequest.builder()
				.bucket("xaexal").key(newFileName).contentType(file.getContentType()).build();
		_s3Client.putObject(putReq, software.amazon.awssdk.core.sync.RequestBody.fromBytes(file.getBytes()));

		member.setImagefile(newFileName);
		this._member.save(member);

		String url = _s3Service.generatePresignedUrl(newFileName, Duration.ofMinutes(10));
		return ResponseEntity.ok(new Result<>(1, "업로드 완료", url));
	}

	@LoginCheck
	@GetMapping("/image/{member_id}")
	public ResponseEntity<?> getImage(@PathVariable("member_id") int member_id) {
		Member member = this._member.findById(member_id)
				.orElseThrow(() -> new EntityNotFoundException("해당하는 회원을 찾지 못했습니다."));
		if (member.getImagefile() == null || member.getImagefile().isBlank())
			return ResponseEntity.ok(new Result<>(0, "이미지 없음", null));
		String url = _s3Service.generatePresignedUrl(member.getImagefile(), Duration.ofMinutes(10));
		return ResponseEntity.ok(new Result<>(1, "", url));
	}

	@DeleteMapping("/private")
	public ResponseEntity<?> doDelete(@RequestBody Map<String, String> req) {
		System.out.println("/private DELETE");
		req.forEach((paramName, paramValue) -> {
			System.out.println("- " + paramName + " [" + paramValue + "]");
		});
		return ResponseEntity.ok(new Result<>(1, "교적정보의 삭제는 지원하지 않습니다."));
	}

}
