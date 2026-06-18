package com.tax_helper.backend.workspace.repository;

import com.tax_helper.backend.workspace.domain.Evidence;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvidenceRepository extends JpaRepository<Evidence, Long> {
    List<Evidence> findByHospital_IdAndTaxYearWorkspace_TaxYearOrderByCreatedAtDesc(Long hospitalId, int taxYear);

    Optional<Evidence> findFirstByHospital_IdAndTaxYearWorkspace_TaxYearAndFileHashOrderByCreatedAtAsc(
            Long hospitalId,
            int taxYear,
            String fileHash
    );
}
