package com.lms.admin.repository;

import com.lms.admin.model.FinancialTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FinancialTransactionRepository extends JpaRepository<FinancialTransaction, String>,
        JpaSpecificationExecutor<FinancialTransaction> {
}
