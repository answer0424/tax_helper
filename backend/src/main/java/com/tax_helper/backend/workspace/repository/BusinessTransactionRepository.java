package com.tax_helper.backend.workspace.repository;

import com.tax_helper.backend.workspace.domain.BusinessTransaction;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusinessTransactionRepository extends JpaRepository<BusinessTransaction, Long> {
    List<BusinessTransaction> findByHospitalIdAndTaxYearWorkspaceTaxYearOrderByTransactionDateDesc(
            Long hospitalId,
            int taxYear
    );
}
