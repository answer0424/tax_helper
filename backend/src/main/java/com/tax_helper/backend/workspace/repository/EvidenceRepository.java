package com.tax_helper.backend.workspace.repository;

import com.tax_helper.backend.workspace.domain.Evidence;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvidenceRepository extends JpaRepository<Evidence, Long> {
    List<Evidence> findByHospitalIdAndTaxYearWorkspaceTaxYearOrderByCreatedAtDesc(Long hospitalId, int taxYear);
}
