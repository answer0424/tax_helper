package com.tax_helper.backend.workspace;

import com.tax_helper.backend.hospital.domain.Hospital;
import com.tax_helper.backend.hospital.domain.TaxYearWorkspace;
import com.tax_helper.backend.hospital.repository.HospitalRepository;
import com.tax_helper.backend.hospital.repository.TaxYearWorkspaceRepository;
import com.tax_helper.backend.workspace.domain.BusinessTransaction;
import com.tax_helper.backend.workspace.domain.Evidence;
import com.tax_helper.backend.workspace.domain.TaxRule;
import com.tax_helper.backend.workspace.repository.BusinessTransactionRepository;
import com.tax_helper.backend.workspace.repository.EvidenceRepository;
import com.tax_helper.backend.workspace.repository.TaxRuleRepository;
import com.tax_helper.backend.workspace.web.BusinessTransactionCreateRequest;
import com.tax_helper.backend.workspace.web.BusinessTransactionResponse;
import com.tax_helper.backend.workspace.web.EvidenceCreateRequest;
import com.tax_helper.backend.workspace.web.EvidenceResponse;
import com.tax_helper.backend.workspace.web.TaxRuleCreateRequest;
import com.tax_helper.backend.workspace.web.TaxRuleResponse;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkspaceService {

    private final HospitalRepository hospitalRepository;
    private final TaxYearWorkspaceRepository taxYearWorkspaceRepository;
    private final BusinessTransactionRepository businessTransactionRepository;
    private final EvidenceRepository evidenceRepository;
    private final TaxRuleRepository taxRuleRepository;

    public WorkspaceService(
            HospitalRepository hospitalRepository,
            TaxYearWorkspaceRepository taxYearWorkspaceRepository,
            BusinessTransactionRepository businessTransactionRepository,
            EvidenceRepository evidenceRepository,
            TaxRuleRepository taxRuleRepository
    ) {
        this.hospitalRepository = hospitalRepository;
        this.taxYearWorkspaceRepository = taxYearWorkspaceRepository;
        this.businessTransactionRepository = businessTransactionRepository;
        this.evidenceRepository = evidenceRepository;
        this.taxRuleRepository = taxRuleRepository;
    }

    @Transactional(readOnly = true)
    public List<BusinessTransactionResponse> findTransactions(Long hospitalId, int taxYear) {
        requireWorkspace(hospitalId, taxYear);
        return businessTransactionRepository
                .findByHospitalIdAndTaxYearWorkspaceTaxYearOrderByTransactionDateDesc(hospitalId, taxYear)
                .stream()
                .map(BusinessTransactionResponse::from)
                .toList();
    }

    @Transactional
    public BusinessTransactionResponse createTransaction(
            Long hospitalId,
            int taxYear,
            BusinessTransactionCreateRequest request
    ) {
        Hospital hospital = requireHospital(hospitalId);
        TaxYearWorkspace workspace = requireWorkspace(hospitalId, taxYear);
        BusinessTransaction transaction = new BusinessTransaction(
                hospital,
                workspace,
                request.transactionDate(),
                request.counterpartyName(),
                request.counterpartyBusinessNumber(),
                request.amount(),
                request.supplyAmount(),
                request.vatAmount(),
                request.transactionType(),
                request.accountTitle(),
                request.reviewStatus(),
                request.memo()
        );

        return BusinessTransactionResponse.from(businessTransactionRepository.save(transaction));
    }

    @Transactional(readOnly = true)
    public List<EvidenceResponse> findEvidences(Long hospitalId, int taxYear) {
        requireWorkspace(hospitalId, taxYear);
        return evidenceRepository.findByHospitalIdAndTaxYearWorkspaceTaxYearOrderByCreatedAtDesc(hospitalId, taxYear)
                .stream()
                .map(EvidenceResponse::from)
                .toList();
    }

    @Transactional
    public EvidenceResponse createEvidence(Long hospitalId, int taxYear, EvidenceCreateRequest request) {
        Hospital hospital = requireHospital(hospitalId);
        TaxYearWorkspace workspace = requireWorkspace(hospitalId, taxYear);
        BusinessTransaction transaction = request.transactionId() == null
                ? null
                : businessTransactionRepository.findById(request.transactionId())
                        .orElseThrow(() -> new IllegalArgumentException("TRANSACTION_NOT_FOUND"));

        Evidence evidence = new Evidence(
                hospital,
                workspace,
                transaction,
                request.evidenceType(),
                request.filePath(),
                request.fileHash(),
                request.ocrRawText(),
                request.ocrConfidence()
        );

        return EvidenceResponse.from(evidenceRepository.save(evidence));
    }

    @Transactional(readOnly = true)
    public List<TaxRuleResponse> findRules() {
        return taxRuleRepository.findAll().stream()
                .map(TaxRuleResponse::from)
                .toList();
    }

    @Transactional
    public TaxRuleResponse createRule(TaxRuleCreateRequest request) {
        TaxRule rule = new TaxRule(
                request.name(),
                request.conditionExpression(),
                request.resultTag(),
                request.severity(),
                request.startsOn(),
                request.endsOn(),
                request.sourceUrl(),
                request.reviewedByTaxAgent()
        );

        return TaxRuleResponse.from(taxRuleRepository.save(rule));
    }

    private Hospital requireHospital(Long hospitalId) {
        return hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new IllegalArgumentException("HOSPITAL_NOT_FOUND"));
    }

    private TaxYearWorkspace requireWorkspace(Long hospitalId, int taxYear) {
        return taxYearWorkspaceRepository.findByHospitalIdAndTaxYear(hospitalId, taxYear)
                .orElseThrow(() -> new IllegalArgumentException("TAX_YEAR_WORKSPACE_NOT_FOUND"));
    }
}
