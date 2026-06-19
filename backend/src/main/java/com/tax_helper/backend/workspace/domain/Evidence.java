package com.tax_helper.backend.workspace.domain;

import com.tax_helper.backend.hospital.domain.Hospital;
import com.tax_helper.backend.hospital.domain.TaxYearWorkspace;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "evidences")
public class Evidence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hospital_id")
    private Hospital hospital;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tax_year_workspace_id")
    private TaxYearWorkspace taxYearWorkspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id")
    private BusinessTransaction transaction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EvidenceType evidenceType;

    @Column(nullable = false, length = 500)
    private String filePath;

    @Column(length = 255)
    private String originalFileName;

    @Column(length = 255)
    private String storedFileName;

    @Column(length = 120)
    private String contentType;

    @Column
    private Long fileSize;

    @Column(length = 128)
    private String fileHash;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private EvidenceUploadStatus uploadStatus = EvidenceUploadStatus.UPLOADED;

    @Column
    private Boolean duplicateSuspected;

    private Long duplicateOfEvidenceId;

    @Column(length = 2000)
    private String ocrRawText;

    private Integer ocrConfidence;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private OcrStatus ocrStatus = OcrStatus.NOT_STARTED;

    @Column(length = 500)
    private String ocrErrorMessage;

    @Column
    private Boolean ocrReviewRequired;

    private LocalDate extractedTransactionDate;

    @Column(length = 120)
    private String extractedSupplierName;

    @Column(length = 10)
    private String extractedBusinessRegistrationNumber;

    @Column(precision = 15, scale = 0)
    private BigDecimal extractedTotalAmount;

    @Column(precision = 15, scale = 0)
    private BigDecimal extractedSupplyAmount;

    @Column(precision = 15, scale = 0)
    private BigDecimal extractedVatAmount;

    @Column(length = 300)
    private String extractedItemName;

    @Column(length = 80)
    private String extractedPaymentMethod;

    @Column(length = 80)
    private String extractedApprovalNumber;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected Evidence() {
    }

    public Evidence(
            Hospital hospital,
            TaxYearWorkspace taxYearWorkspace,
            BusinessTransaction transaction,
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
            String ocrRawText,
            Integer ocrConfidence
    ) {
        this.hospital = hospital;
        this.taxYearWorkspace = taxYearWorkspace;
        this.transaction = transaction;
        this.evidenceType = evidenceType;
        this.filePath = filePath;
        this.originalFileName = originalFileName;
        this.storedFileName = storedFileName;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.fileHash = fileHash;
        this.uploadStatus = uploadStatus == null ? EvidenceUploadStatus.UPLOADED : uploadStatus;
        this.duplicateSuspected = duplicateSuspected;
        this.duplicateOfEvidenceId = duplicateOfEvidenceId;
        this.ocrRawText = ocrRawText;
        this.ocrConfidence = ocrConfidence;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getHospitalId() {
        return hospital.getId();
    }

    public Hospital getHospital() {
        return hospital;
    }

    public Long getTaxYearWorkspaceId() {
        return taxYearWorkspace.getId();
    }

    public TaxYearWorkspace getTaxYearWorkspace() {
        return taxYearWorkspace;
    }

    public Long getTransactionId() {
        return transaction == null ? null : transaction.getId();
    }

    public EvidenceType getEvidenceType() {
        return evidenceType;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public String getStoredFileName() {
        return storedFileName;
    }

    public String getContentType() {
        return contentType;
    }

    public long getFileSize() {
        return fileSize == null ? 0 : fileSize;
    }

    public String getFileHash() {
        return fileHash;
    }

    public EvidenceUploadStatus getUploadStatus() {
        return uploadStatus;
    }

    public boolean isDuplicateSuspected() {
        return Boolean.TRUE.equals(duplicateSuspected);
    }

    public Long getDuplicateOfEvidenceId() {
        return duplicateOfEvidenceId;
    }

    public String getOcrRawText() {
        return ocrRawText;
    }

    public Integer getOcrConfidence() {
        return ocrConfidence;
    }

    public OcrStatus getOcrStatus() {
        return ocrStatus == null ? OcrStatus.NOT_STARTED : ocrStatus;
    }

    public String getOcrErrorMessage() {
        return ocrErrorMessage;
    }

    public boolean isOcrReviewRequired() {
        return Boolean.TRUE.equals(ocrReviewRequired);
    }

    public LocalDate getExtractedTransactionDate() {
        return extractedTransactionDate;
    }

    public String getExtractedSupplierName() {
        return extractedSupplierName;
    }

    public String getExtractedBusinessRegistrationNumber() {
        return extractedBusinessRegistrationNumber;
    }

    public BigDecimal getExtractedTotalAmount() {
        return extractedTotalAmount;
    }

    public BigDecimal getExtractedSupplyAmount() {
        return extractedSupplyAmount;
    }

    public BigDecimal getExtractedVatAmount() {
        return extractedVatAmount;
    }

    public String getExtractedItemName() {
        return extractedItemName;
    }

    public String getExtractedPaymentMethod() {
        return extractedPaymentMethod;
    }

    public String getExtractedApprovalNumber() {
        return extractedApprovalNumber;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void markOcrProcessing() {
        this.ocrStatus = OcrStatus.PROCESSING;
        this.ocrErrorMessage = null;
    }

    public void updateOcrResult(
            OcrStatus ocrStatus,
            String ocrRawText,
            Integer ocrConfidence,
            boolean ocrReviewRequired,
            String ocrErrorMessage,
            LocalDate extractedTransactionDate,
            String extractedSupplierName,
            String extractedBusinessRegistrationNumber,
            BigDecimal extractedTotalAmount,
            BigDecimal extractedSupplyAmount,
            BigDecimal extractedVatAmount,
            String extractedItemName,
            String extractedPaymentMethod,
            String extractedApprovalNumber
    ) {
        this.ocrStatus = ocrStatus;
        this.ocrRawText = ocrRawText;
        this.ocrConfidence = ocrConfidence;
        this.ocrReviewRequired = ocrReviewRequired;
        this.ocrErrorMessage = ocrErrorMessage;
        this.extractedTransactionDate = extractedTransactionDate;
        this.extractedSupplierName = extractedSupplierName;
        this.extractedBusinessRegistrationNumber = extractedBusinessRegistrationNumber;
        this.extractedTotalAmount = extractedTotalAmount;
        this.extractedSupplyAmount = extractedSupplyAmount;
        this.extractedVatAmount = extractedVatAmount;
        this.extractedItemName = extractedItemName;
        this.extractedPaymentMethod = extractedPaymentMethod;
        this.extractedApprovalNumber = extractedApprovalNumber;
    }

    public void linkTransaction(BusinessTransaction transaction) {
        this.transaction = transaction;
    }
}
