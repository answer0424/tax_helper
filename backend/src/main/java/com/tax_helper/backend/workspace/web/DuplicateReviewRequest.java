package com.tax_helper.backend.workspace.web;

import com.tax_helper.backend.workspace.domain.DuplicateStatus;
import jakarta.validation.constraints.NotNull;

public record DuplicateReviewRequest(
        @NotNull DuplicateStatus duplicateStatus,
        Long duplicateCandidateEvidenceId,
        String reason
) {
}
