package com.tax_helper.backend.hospital.web;

import com.tax_helper.backend.hospital.domain.TaxYearWorkspace;
import java.time.LocalDateTime;

public record TaxYearResponse(
        Long id,
        Long hospitalId,
        int taxYear,
        String storagePath,
        LocalDateTime createdAt
) {
    public static TaxYearResponse from(TaxYearWorkspace workspace) {
        return new TaxYearResponse(
                workspace.getId(),
                workspace.getHospitalId(),
                workspace.getTaxYear(),
                workspace.getStoragePath(),
                workspace.getCreatedAt()
        );
    }
}
