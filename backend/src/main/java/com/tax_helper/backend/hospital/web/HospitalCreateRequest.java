package com.tax_helper.backend.hospital.web;

import com.tax_helper.backend.hospital.domain.TaxationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.LocalDate;

public record HospitalCreateRequest(
        @NotBlank String name,
        @NotBlank String ownerName,
        @NotBlank
        @Pattern(regexp = "\\d{10}", message = "사업자등록번호는 숫자 10자리여야 합니다.")
        String businessRegistrationNumber,
        @NotBlank String medicalDepartment,
        LocalDate openedOn,
        @NotNull TaxationType taxationType,
        boolean jointBusiness,
        BigDecimal previousYearRevenue,
        boolean diligentFilingManuallyChecked
) {
}
