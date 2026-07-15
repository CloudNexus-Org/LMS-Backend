package com.lms.admin.seed;

import com.lms.admin.repository.FinancialTransactionRepository;
import com.lms.admin.repository.MentorPayoutRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

//@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class FinancialDirectoryCleanup implements CommandLineRunner {

    private final FinancialTransactionRepository financialTransactionRepository;
    private final MentorPayoutRepository mentorPayoutRepository;

    @Override
    @Transactional
    public void run(String... args) {
        long txCount = financialTransactionRepository.count();
        long payoutCount = mentorPayoutRepository.count();
        if (txCount == 0 && payoutCount == 0) {
            return;
        }
        financialTransactionRepository.deleteAll();
        mentorPayoutRepository.deleteAll();
        log.info("Financial cleanup cleared {} transaction(s) and {} payout record(s)", txCount, payoutCount);
    }
}
