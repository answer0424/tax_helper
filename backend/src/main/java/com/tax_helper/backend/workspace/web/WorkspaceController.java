package com.tax_helper.backend.workspace.web;

import com.tax_helper.backend.workspace.AccountTitleRecommendationService;
import com.tax_helper.backend.workspace.EvidenceUploadService;
import com.tax_helper.backend.workspace.OcrService;
import com.tax_helper.backend.workspace.WorkspaceService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class WorkspaceController {

    private final WorkspaceService workspaceService;
    private final EvidenceUploadService evidenceUploadService;
    private final OcrService ocrService;
    private final AccountTitleRecommendationService accountTitleRecommendationService;

    public WorkspaceController(
            WorkspaceService workspaceService,
            EvidenceUploadService evidenceUploadService,
            OcrService ocrService,
            AccountTitleRecommendationService accountTitleRecommendationService
    ) {
        this.workspaceService = workspaceService;
        this.evidenceUploadService = evidenceUploadService;
        this.ocrService = ocrService;
        this.accountTitleRecommendationService = accountTitleRecommendationService;
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

    @PutMapping("/transactions/{transactionId}")
    public BusinessTransactionResponse updateTransaction(
            @PathVariable Long transactionId,
            @Valid @RequestBody BusinessTransactionUpdateRequest request
    ) {
        return workspaceService.updateTransaction(transactionId, request);
    }

    @GetMapping("/hospitals/{hospitalId}/account-title-suggestions")
    public List<AccountTitleSuggestionResponse> suggestAccountTitles(
            @PathVariable Long hospitalId,
            @RequestParam(required = false) String counterpartyName,
            @RequestParam(required = false) String itemName,
            @RequestParam(required = false) BigDecimal amount
    ) {
        return accountTitleRecommendationService.suggest(hospitalId, counterpartyName, itemName, amount);
    }

    @GetMapping("/hospitals/{hospitalId}/counterparty-account-rules")
    public List<CounterpartyAccountRuleResponse> findCounterpartyAccountRules(@PathVariable Long hospitalId) {
        return accountTitleRecommendationService.findRules(hospitalId);
    }

    @PostMapping("/hospitals/{hospitalId}/counterparty-account-rules")
    @ResponseStatus(HttpStatus.CREATED)
    public CounterpartyAccountRuleResponse saveCounterpartyAccountRule(
            @PathVariable Long hospitalId,
            @Valid @RequestBody CounterpartyAccountRuleRequest request
    ) {
        return accountTitleRecommendationService.learn(hospitalId, request.counterpartyName(), request.accountTitle());
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

    @PostMapping(
            value = "/hospitals/{hospitalId}/tax-years/{taxYear}/evidences/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    public EvidenceUploadResponse uploadEvidences(
            @PathVariable Long hospitalId,
            @PathVariable int taxYear,
            @RequestParam("files") MultipartFile[] files
    ) {
        return evidenceUploadService.upload(hospitalId, taxYear, files);
    }

    @GetMapping("/evidences/{evidenceId}/file")
    public ResponseEntity<Resource> previewEvidence(@PathVariable Long evidenceId) {
        Resource resource = evidenceUploadService.loadEvidenceFile(evidenceId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .contentType(MediaTypeFactory.getMediaType(resource).orElse(MediaType.APPLICATION_OCTET_STREAM))
                .body(resource);
    }

    @PostMapping("/evidences/{evidenceId}/transaction-review")
    public BusinessTransactionResponse saveEvidenceTransaction(
            @PathVariable Long evidenceId,
            @Valid @RequestBody BusinessTransactionUpdateRequest request
    ) {
        return workspaceService.saveEvidenceTransaction(evidenceId, request);
    }

    @PostMapping("/evidences/{evidenceId}/ocr")
    public EvidenceResponse runOcr(@PathVariable Long evidenceId) {
        return ocrService.runOcr(evidenceId);
    }

    @PutMapping("/evidences/{evidenceId}/ocr")
    public EvidenceResponse updateOcr(
            @PathVariable Long evidenceId,
            @RequestBody OcrUpdateRequest request
    ) {
        return ocrService.updateOcrResult(evidenceId, request);
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
