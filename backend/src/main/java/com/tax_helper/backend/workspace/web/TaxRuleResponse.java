package com.tax_helper.backend.workspace.web;

import com.tax_helper.backend.workspace.domain.TaxRule;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TaxRuleResponse(
        Long id,
        String name,
        String conditionExpression,
        String resultTag,
        String severity,
        LocalDate startsOn,
        LocalDate endsOn,
        String sourceUrl,
        boolean reviewedByTaxAgent,
        LocalDateTime createdAt
) {
    public static TaxRuleResponse from(TaxRule rule) {
        return new TaxRuleResponse(
                rule.getId(),
                rule.getName(),
                rule.getConditionExpression(),
                rule.getResultTag(),
                rule.getSeverity(),
                rule.getStartsOn(),
                rule.getEndsOn(),
                rule.getSourceUrl(),
                rule.isReviewedByTaxAgent(),
                rule.getCreatedAt()
        );
    }
}
