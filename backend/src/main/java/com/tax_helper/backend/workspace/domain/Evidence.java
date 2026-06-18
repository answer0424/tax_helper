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
    private long fileSize;

    @Column(length = 128)
    private String fileHash;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private EvidenceUploadStatus uploadStatus = EvidenceUploadStatus.UPLOADED;

    @Column
    private boolean duplicateSuspected;

    private Long duplicateOfEvidenceId;

    @Column(length = 2000)
    private String ocrRawText;

    private Integer ocrConfidence;

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

    public Long getTaxYearWorkspaceId() {
        return taxYearWorkspace.getId();
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
        return fileSize;
    }

    public String getFileHash() {
        return fileHash;
    }

    public EvidenceUploadStatus getUploadStatus() {
        return uploadStatus;
    }

    public boolean isDuplicateSuspected() {
        return duplicateSuspected;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
