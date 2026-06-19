package com.tax_helper.backend.workspace.web;

import com.tax_helper.backend.workspace.domain.ReviewStatus;
import com.tax_helper.backend.workspace.domain.RiskTag;
import com.tax_helper.backend.workspace.domain.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

public record BusinessTransactionCreateRequest(
        @NotNull LocalDate transactionDate,
        @NotBlank String counterpartyName,
        String counterpartyBusinessNumber,
        @NotNull BigDecimal amount,
        BigDecimal supplyAmount,
        BigDecimal vatAmount,
        @NotNull TransactionType transactionType,
        String accountTitle,
        ReviewStatus reviewStatus,
        String memo,
        Set<RiskTag> riskTags
) {
}
