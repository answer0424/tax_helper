package com.tax_helper.backend.workspace.web;

import com.tax_helper.backend.workspace.domain.Evidence;
import com.tax_helper.backend.workspace.domain.EvidenceType;
import java.time.LocalDateTime;

public record EvidenceResponse(
        Long id,
        Long hospitalId,
        Long taxYearWorkspaceId,
        Long transactionId,
        EvidenceType evidenceType,
        String filePath,
        String fileHash,
        String ocrRawText,
        Integer ocrConfidence,
        LocalDateTime createdAt
) {
    public static EvidenceResponse from(Evidence evidence) {
        return new EvidenceResponse(
                evidence.getId(),
                evidence.getHospitalId(),
                evidence.getTaxYearWorkspaceId(),
                evidence.getTransactionId(),
                evidence.getEvidenceType(),
                evidence.getFilePath(),
                evidence.getFileHash(),
                evidence.getOcrRawText(),
                evidence.getOcrConfidence(),
                evidence.getCreatedAt()
        );
    }
}
