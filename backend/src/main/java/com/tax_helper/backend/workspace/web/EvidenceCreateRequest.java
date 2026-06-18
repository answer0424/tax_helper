package com.tax_helper.backend.workspace.web;

import com.tax_helper.backend.workspace.domain.EvidenceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EvidenceCreateRequest(
        Long transactionId,
        @NotNull EvidenceType evidenceType,
        @NotBlank String filePath,
        String fileHash,
        String ocrRawText,
        Integer ocrConfidence
) {
}
