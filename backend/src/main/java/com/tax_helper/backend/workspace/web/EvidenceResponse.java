package com.tax_helper.backend.workspace.web;

import com.tax_helper.backend.workspace.domain.Evidence;
import com.tax_helper.backend.workspace.domain.EvidenceType;
import com.tax_helper.backend.workspace.domain.EvidenceUploadStatus;
import java.time.LocalDateTime;

public record EvidenceResponse(
        Long id,
        Long hospitalId,
        Long taxYearWorkspaceId,
        Long transactionId,
        EvidenceType evidenceType,
        String filePath,
        String originalFileName,
        String storedFileName,
        String contentType,
        long fileSize,
        String fileHash,
        EvidenceUploadStatus uploadStatus,
        boolean duplicateSuspected,
        Long duplicateOfEvidenceId,
        String previewUrl,
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
                evidence.getOriginalFileName(),
                evidence.getStoredFileName(),
                evidence.getContentType(),
                evidence.getFileSize(),
                evidence.getFileHash(),
                evidence.getUploadStatus(),
                evidence.isDuplicateSuspected(),
                evidence.getDuplicateOfEvidenceId(),
                "/api/evidences/" + evidence.getId() + "/file",
                evidence.getOcrRawText(),
                evidence.getOcrConfidence(),
                evidence.getCreatedAt()
        );
    }
}
