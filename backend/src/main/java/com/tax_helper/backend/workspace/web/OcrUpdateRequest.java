package com.tax_helper.backend.workspace.web;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OcrUpdateRequest(
        LocalDate transactionDate,
        String supplierName,
        String businessRegistrationNumber,
        BigDecimal totalAmount,
        BigDecimal supplyAmount,
        BigDecimal vatAmount,
        String itemName,
        String paymentMethod,
        String approvalNumber,
        String rawText,
        Integer confidence,
        boolean reviewRequired
) {
}
