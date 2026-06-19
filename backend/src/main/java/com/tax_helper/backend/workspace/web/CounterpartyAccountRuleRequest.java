package com.tax_helper.backend.workspace.web;

import jakarta.validation.constraints.NotBlank;

public record CounterpartyAccountRuleRequest(
        @NotBlank String counterpartyName,
        @NotBlank String accountTitle
) {
}
