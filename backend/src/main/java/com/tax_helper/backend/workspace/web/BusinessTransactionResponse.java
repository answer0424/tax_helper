package com.tax_helper.backend.workspace.web;

import com.tax_helper.backend.workspace.domain.BusinessTransaction;
import com.tax_helper.backend.workspace.domain.ReviewStatus;
import com.tax_helper.backend.workspace.domain.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record BusinessTransactionResponse(
        Long id,
        Long hospitalId,
        Long taxYearWorkspaceId,
        LocalDate transactionDate,
        String counterpartyName,
        String counterpartyBusinessNumber,
        BigDecimal amount,
        BigDecimal supplyAmount,
        BigDecimal vatAmount,
        TransactionType transactionType,
        String accountTitle,
        ReviewStatus reviewStatus,
        String memo,
        LocalDateTime createdAt
) {
    public static BusinessTransactionResponse from(BusinessTransaction transaction) {
        return new BusinessTransactionResponse(
                transaction.getId(),
                transaction.getHospitalId(),
                transaction.getTaxYearWorkspaceId(),
                transaction.getTransactionDate(),
                transaction.getCounterpartyName(),
                transaction.getCounterpartyBusinessNumber(),
                transaction.getAmount(),
                transaction.getSupplyAmount(),
                transaction.getVatAmount(),
                transaction.getTransactionType(),
                transaction.getAccountTitle(),
                transaction.getReviewStatus(),
                transaction.getMemo(),
                transaction.getCreatedAt()
        );
    }
}
