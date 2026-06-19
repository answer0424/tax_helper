package com.tax_helper.backend.workspace.web;

public record AccountTitleSuggestionResponse(
        String accountTitle,
        String reason,
        String source,
        int confidence,
        boolean fixedAssetCandidate
) {
}
