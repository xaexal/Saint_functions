package com.xaexal.app.Repository;

import com.xaexal.app.DTO.iPayday4Church;
import com.xaexal.app.DTO.iPaymentMethod;
import com.xaexal.app.Entity.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PaymentMethodRep extends JpaRepository<PaymentMethod, Integer> {
    List<PaymentMethod> findByMemberIdAndStatus(Integer memberId, String status);

    @Query(value =
        "SELECT c.id AS churchId, c.name AS churchName, c.payday, c.capita, c.expense AS cost, pm.type, " +
        "pm.card_number_masked AS cardNumberMasked, pm.card_type AS cardType, " +
        "m.name AS memberName " +
        "FROM church c " +
        "LEFT JOIN payment_methods pm ON pm.church_id = c.id AND pm.status = 'active' " +
        "LEFT JOIN member m ON m.id = pm.member_id " +
        "ORDER BY c.name",
        nativeQuery = true)
    List<iPayday4Church> findPayday4Church();

    @Query(value =
        "SELECT pm.id, m.name AS memberName, pm.type, pm.card_company AS cardCompany, " +
        "pm.card_number_masked AS cardNumberMasked, pm.card_type AS cardType, " +
        "pm.card_expiry_year_month AS cardExpiryYearMonth, pm.bank_code AS bankCode, " +
        "pm.account_number_masked AS accountNumberMasked, pm.accound_holder_name AS accountHolderName, " +
        "pm.account_type AS accountType, pm.is_default AS isDefault " +
        "FROM payment_methods pm JOIN member m ON m.id = pm.member_id " +
        "WHERE pm.church_id = :churchId AND pm.status = 'active'",
        nativeQuery = true)
    List<iPaymentMethod> findActiveByChurchId(@Param("churchId") int churchId);
}
