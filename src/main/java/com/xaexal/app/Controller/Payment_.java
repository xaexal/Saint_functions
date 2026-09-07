package com.xaexal.app.Controller;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.xaexal.app.Common.LoginCheck;
import com.xaexal.app.Common.Result;
import com.xaexal.app.DTO.iPayday4Church;
import com.xaexal.app.Entity.Church;
import com.xaexal.app.Entity.PaymentMethod;
import com.xaexal.app.Repository.ChurchRep;
import com.xaexal.app.Repository.PaymentMethodRep;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

@RestController
@RequestMapping("/payment")
public class Payment_ {

    private static final String TOSS_BILLING_URL = "https://api.tosspayments.com/v1/billing/authorizations/issue";

    @Value("${toss.client-key}") private String clientKey;
    @Value("${toss.secret-key}") private String secretKey;

    @Autowired private PaymentMethodRep _paymentMethods;
    @Autowired private ChurchRep _church;

    @GetMapping("/config")
    public ResponseEntity<?> config() {
        return ResponseEntity.ok(new Result<>(1, "", Map.of("clientKey", clientKey)));
    }

    @LoginCheck
    @GetMapping("/payday4church")
    public ResponseEntity<?> payday4church() {
        List<iPayday4Church> list = _paymentMethods.findPayday4Church();
        return ResponseEntity.ok(new Result<>(1, "", list));
    }

    @LoginCheck
    @Transactional
    @PostMapping("/billing-auth")
    public ResponseEntity<?> billingAuth(@RequestBody Map<String, String> req, HttpSession s) {
        String authKey     = req.get("authKey");
        String customerKey = req.get("customerKey");
        int memberId     = (int) s.getAttribute("member_id");
        // 새교회등록 직후 결제(new_church_id)와, 환경설정>결제수단 등록에서 기존 교회 결제수단을 등록/변경하는
        // 경우(church_id) 두 진입 경로가 있다. new_church_id가 있으면 그걸 우선한다.
        Integer churchId = (Integer) s.getAttribute("new_church_id");
        boolean isNewChurch = churchId != null;
        if (churchId == null) churchId = (Integer) s.getAttribute("church_id");
        if (churchId == null) {
            throw new RuntimeException("등록할 교회 정보를 찾을 수 없습니다. 새교회등록부터 다시 진행해 주세요.");
        }

        RestTemplate restTemplate = new RestTemplate();
        String credentials = Base64.getEncoder()
            .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic " + credentials);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(Map.of("authKey", authKey, "customerKey", customerKey), headers);

        ResponseEntity<Map> tossRes;
        try {
            tossRes = restTemplate.postForEntity(TOSS_BILLING_URL, entity, Map.class);
        } catch (HttpClientErrorException e) {
            String tossMsg = e.getResponseBodyAsString();
            System.out.println("Toss 오류: " + tossMsg);
            throw new RuntimeException("토스 결제 오류: " + (tossMsg.isBlank() ? e.getStatusCode() : tossMsg));
        }

        Map<String, Object> tossBody = tossRes.getBody();
        if (tossBody == null || tossBody.get("billingKey") == null) {
            throw new RuntimeException("빌링키 발급에 실패했습니다.");
        }

        String billingKey = (String) tossBody.get("billingKey");
        String method     = (String) tossBody.get("method");

        PaymentMethod pm = new PaymentMethod();
        pm.setMemberId(memberId);
        pm.setChurchId(churchId);
        pm.setBillingKey(billingKey);
        pm.setType("카드".equals(method) ? "card" : "account");
        pm.setStatus("active");
        pm.setIsDefault(true);
        pm.setWriter(memberId);

        Object cardObj = tossBody.get("card");
        if (cardObj instanceof Map) {
            Map<?,?> card = (Map<?,?>) cardObj;
            pm.setCardCompany(str(card.get("company")));
            pm.setCardNumberMasked(str(card.get("number")));
            pm.setCardType(str(card.get("cardType")));
            pm.setCardExpiryYearMonth(str(card.get("cardExpiryYearMonth")));
        }

        Object accountObj = tossBody.get("account");
        if (accountObj instanceof Map) {
            Map<?,?> account = (Map<?,?>) accountObj;
            pm.setBankCode(str(account.get("bankCode")));
            pm.setAccountNumberMasked(str(account.get("accountNumber")));
            pm.setAccountHolderName(str(account.get("holderName")));
            pm.setAccountType(str(account.get("accountType")));
        }

        // 기존 활성 결제수단 삭제 처리
        List<PaymentMethod> existing = _paymentMethods.findByMemberIdAndStatus(memberId, "active");
        for (PaymentMethod e : existing) {
            e.setStatus("deleted");
            e.setIsDefault(false);
        }
        _paymentMethods.saveAll(existing);

        _paymentMethods.save(pm);

        Church church = _church.findById(churchId.intValue());
        if (church != null) {
            church.setPayday(LocalDate.now().getDayOfMonth());
            _church.save(church);
        }

        return ResponseEntity.ok(new Result<>(1, "자동결제 구독 완료", Map.of("isNewChurch", isNewChurch)));
    }

    private String str(Object o) {
        return o instanceof String ? (String) o : null;
    }
}
