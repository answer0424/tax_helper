package com.tax_helper.backend.hospital.web;

import com.tax_helper.backend.hospital.domain.Hospital;
import com.tax_helper.backend.hospital.domain.TaxationType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record HospitalResponse(
        Long id,
        String name,
        String ownerName,
        String businessRegistrationNumber,
        String medicalDepartment,
        LocalDate openedOn,
        TaxationType taxationType,
        boolean jointBusiness,
        BigDecimal previousYearRevenue,
        boolean diligentFilingManuallyChecked,
        boolean diligentFilingWarning,
        boolean diligentFilingCandidate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    private static final BigDecimal WARNING_REVENUE = new BigDecimal("450000000");
    private static final BigDecimal CANDIDATE_REVENUE = new BigDecimal("500000000");

    public static HospitalResponse from(Hospital hospital) {
        BigDecimal revenue = hospital.getPreviousYearRevenue();
        boolean warning = revenue != null && revenue.compareTo(WARNING_REVENUE) >= 0;
        boolean candidate = hospital.isDiligentFilingManuallyChecked()
                || (revenue != null && revenue.compareTo(CANDIDATE_REVENUE) >= 0);

        return new HospitalResponse(
                hospital.getId(),
                hospital.getName(),
                hospital.getOwnerName(),
                hospital.getBusinessRegistrationNumber(),
                hospital.getMedicalDepartment(),
                hospital.getOpenedOn(),
                hospital.getTaxationType(),
                hospital.isJointBusiness(),
                revenue,
                hospital.isDiligentFilingManuallyChecked(),
                warning,
                candidate,
                hospital.getCreatedAt(),
                hospital.getUpdatedAt()
        );
    }
}
