package com.tax_helper.backend.hospital.repository;

import com.tax_helper.backend.hospital.domain.TaxYearWorkspace;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaxYearWorkspaceRepository extends JpaRepository<TaxYearWorkspace, Long> {
    List<TaxYearWorkspace> findByHospital_IdOrderByTaxYearDesc(Long hospitalId);

    Optional<TaxYearWorkspace> findByHospital_IdAndTaxYear(Long hospitalId, int taxYear);
}
