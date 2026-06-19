package com.tax_helper.backend.workspace.web;

import com.tax_helper.backend.workspace.domain.CounterpartyAccountRule;
import java.time.LocalDateTime;

public record CounterpartyAccountRuleResponse(
        Long id,
        Long hospitalId,
        String counterpartyName,
        String accountTitle,
        int learnedCount,
        LocalDateTime lastLearnedAt
) {
    public static CounterpartyAccountRuleResponse from(CounterpartyAccountRule rule) {
        return new CounterpartyAccountRuleResponse(
                rule.getId(),
                rule.getHospitalId(),
                rule.getCounterpartyName(),
                rule.getAccountTitle(),
                rule.getLearnedCount(),
                rule.getLastLearnedAt()
        );
    }
}
