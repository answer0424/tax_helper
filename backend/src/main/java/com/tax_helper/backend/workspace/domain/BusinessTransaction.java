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
@Table(name = "business_transactions")
public class BusinessTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hospital_id")
    private Hospital hospital;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tax_year_workspace_id")
    private TaxYearWorkspace taxYearWorkspace;

    @Column(nullable = false)
    private LocalDate transactionDate;

    @Column(nullable = false, length = 120)
    private String counterpartyName;

    @Column(length = 10)
    private String counterpartyBusinessNumber;

    @Column(nullable = false, precision = 15, scale = 0)
    private BigDecimal amount;

    @Column(precision = 15, scale = 0)
    private BigDecimal supplyAmount;

    @Column(precision = 15, scale = 0)
    private BigDecimal vatAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType transactionType;

    @Column(length = 80)
    private String accountTitle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReviewStatus reviewStatus = ReviewStatus.NOT_REVIEWED;

    @Column(length = 500)
    private String memo;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected BusinessTransaction() {
    }

    public BusinessTransaction(
            Hospital hospital,
            TaxYearWorkspace taxYearWorkspace,
            LocalDate transactionDate,
            String counterpartyName,
            String counterpartyBusinessNumber,
            BigDecimal amount,
            BigDecimal supplyAmount,
            BigDecimal vatAmount,
            TransactionType transactionType,
            String accountTitle,
            ReviewStatus reviewStatus,
            String memo
    ) {
        this.hospital = hospital;
        this.taxYearWorkspace = taxYearWorkspace;
        this.transactionDate = transactionDate;
        this.counterpartyName = counterpartyName;
        this.counterpartyBusinessNumber = counterpartyBusinessNumber;
        this.amount = amount;
        this.supplyAmount = supplyAmount;
        this.vatAmount = vatAmount;
        this.transactionType = transactionType;
        this.accountTitle = accountTitle;
        this.reviewStatus = reviewStatus == null ? ReviewStatus.NOT_REVIEWED : reviewStatus;
        this.memo = memo;
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

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public String getCounterpartyName() {
        return counterpartyName;
    }

    public String getCounterpartyBusinessNumber() {
        return counterpartyBusinessNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getSupplyAmount() {
        return supplyAmount;
    }

    public BigDecimal getVatAmount() {
        return vatAmount;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public String getAccountTitle() {
        return accountTitle;
    }

    public ReviewStatus getReviewStatus() {
        return reviewStatus;
    }

    public String getMemo() {
        return memo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
