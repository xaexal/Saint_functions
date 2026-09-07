package com.xaexal.app.Service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xaexal.app.Common.ExpenseCalculator;
import com.xaexal.app.Entity.Church;
import com.xaexal.app.Entity.Midnight;
import com.xaexal.app.Repository.ChurchRep;
import com.xaexal.app.Repository.MidnightRep;
import com.xaexal.app.Repository.SaintRep;


@Service
public class _Church_ {
	@Autowired private ChurchRep _church;
	@Autowired private MidnightRep _midnight;
	@Autowired private SaintRep _saint;

	// 매일 자정에 체크 (또는 1시간 마다)
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void cleanupInactiveChurches() {
        // 1. 등록된 지 7일이 넘었으나 교인이 0명인 교회 조회
        List<Church> inactiveChurches = this._church.findInactiveChurches();
        int deletedCount = inactiveChurches.size();

        // 2. 삭제 처리
        if (deletedCount > 0) this._church.deleteAllInBatch(inactiveChurches);
        System.out.println("정리된 유령 교회 수: " + deletedCount);

        // 3. 결과 로그 저장
        Midnight countLog = new Midnight();
        countLog.setTitle("삭제된 교회수");
        countLog.setContent(String.valueOf(deletedCount));
        this._midnight.save(countLog);

        for (Church church : inactiveChurches) {
            Midnight nameLog = new Midnight();
            nameLog.setTitle("삭제된 교회");
            nameLog.setContent(church.getName());
            this._midnight.save(nameLog);
        }
    }

	// 매일 자정, 교회별 비용(등록교인수 x capita)을 계산해 church.expense 컬럼에 저장
	@Scheduled(cron = "0 0 0 * * *")
	@Transactional
	public void updateExpense() {
		LocalDate today = LocalDate.now();
		List<Church> churches = this._church.findAll();
		for (Church church : churches) {
			int memberCount = this._saint.countRegisteredByChurchIdAndActive(church.getId());
			Integer expense = ExpenseCalculator.calculate(church.getPayday(), church.getCapita(), memberCount, today);
			church.setExpense(expense);
		}
		this._church.saveAll(churches);
	}
}
