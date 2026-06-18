package com.tax_helper.backend.hospital.web;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record TaxYearCreateRequest(
        @Min(2000) @Max(2100) int taxYear
) {
}
