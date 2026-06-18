package com.tax_helper.backend.workspace.web;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record TaxRuleCreateRequest(
        @NotBlank String name,
        @NotBlank String conditionExpression,
        @NotBlank String resultTag,
        @NotBlank String severity,
        LocalDate startsOn,
        LocalDate endsOn,
        String sourceUrl,
        boolean reviewedByTaxAgent
) {
}
