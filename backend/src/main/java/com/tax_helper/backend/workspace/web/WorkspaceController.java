package com.tax_helper.backend.workspace.web;

import com.tax_helper.backend.workspace.WorkspaceService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    public WorkspaceController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @GetMapping("/hospitals/{hospitalId}/tax-years/{taxYear}/transactions")
    public List<BusinessTransactionResponse> findTransactions(
            @PathVariable Long hospitalId,
            @PathVariable int taxYear
    ) {
        return workspaceService.findTransactions(hospitalId, taxYear);
    }

    @PostMapping("/hospitals/{hospitalId}/tax-years/{taxYear}/transactions")
    @ResponseStatus(HttpStatus.CREATED)
    public BusinessTransactionResponse createTransaction(
            @PathVariable Long hospitalId,
            @PathVariable int taxYear,
            @Valid @RequestBody BusinessTransactionCreateRequest request
    ) {
        return workspaceService.createTransaction(hospitalId, taxYear, request);
    }

    @GetMapping("/hospitals/{hospitalId}/tax-years/{taxYear}/evidences")
    public List<EvidenceResponse> findEvidences(
            @PathVariable Long hospitalId,
            @PathVariable int taxYear
    ) {
        return workspaceService.findEvidences(hospitalId, taxYear);
    }

    @PostMapping("/hospitals/{hospitalId}/tax-years/{taxYear}/evidences")
    @ResponseStatus(HttpStatus.CREATED)
    public EvidenceResponse createEvidence(
            @PathVariable Long hospitalId,
            @PathVariable int taxYear,
            @Valid @RequestBody EvidenceCreateRequest request
    ) {
        return workspaceService.createEvidence(hospitalId, taxYear, request);
    }

    @GetMapping("/rules")
    public List<TaxRuleResponse> findRules() {
        return workspaceService.findRules();
    }

    @PostMapping("/rules")
    @ResponseStatus(HttpStatus.CREATED)
    public TaxRuleResponse createRule(@Valid @RequestBody TaxRuleCreateRequest request) {
        return workspaceService.createRule(request);
    }
}
