package com.tax_helper.backend.workspace.web;

import java.util.List;

public record EvidenceUploadResponse(
        int uploadedCount,
        int duplicateSuspectedCount,
        List<String> unsupportedFiles,
        List<EvidenceResponse> evidences
) {
}
