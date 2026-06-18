package com.tax_helper.backend.hospital.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "hospitals")
public class Hospital {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 80)
    private String ownerName;

    @Column(nullable = false, unique = true, length = 10)
    private String businessRegistrationNumber;

    @Column(nullable = false, length = 80)
    private String medicalDepartment;

    private LocalDate openedOn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TaxationType taxationType = TaxationType.VAT_EXEMPT;

    @Column(nullable = false)
    private boolean jointBusiness;

    @Column(precision = 15, scale = 0)
    private BigDecimal previousYearRevenue;

    @Column(nullable = false)
    private boolean diligentFilingManuallyChecked;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "hospital", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaxYearWorkspace> taxYears = new ArrayList<>();

    protected Hospital() {
    }

    public Hospital(
            String name,
            String ownerName,
            String businessRegistrationNumber,
            String medicalDepartment,
            LocalDate openedOn,
            TaxationType taxationType,
            boolean jointBusiness,
            BigDecimal previousYearRevenue,
            boolean diligentFilingManuallyChecked
    ) {
        this.name = name;
        this.ownerName = ownerName;
        this.businessRegistrationNumber = businessRegistrationNumber;
        this.medicalDepartment = medicalDepartment;
        this.openedOn = openedOn;
        this.taxationType = taxationType;
        this.jointBusiness = jointBusiness;
        this.previousYearRevenue = previousYearRevenue;
        this.diligentFilingManuallyChecked = diligentFilingManuallyChecked;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void update(
            String name,
            String ownerName,
            String medicalDepartment,
            LocalDate openedOn,
            TaxationType taxationType,
            boolean jointBusiness,
            BigDecimal previousYearRevenue,
            boolean diligentFilingManuallyChecked
    ) {
        this.name = name;
        this.ownerName = ownerName;
        this.medicalDepartment = medicalDepartment;
        this.openedOn = openedOn;
        this.taxationType = taxationType;
        this.jointBusiness = jointBusiness;
        this.previousYearRevenue = previousYearRevenue;
        this.diligentFilingManuallyChecked = diligentFilingManuallyChecked;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getBusinessRegistrationNumber() {
        return businessRegistrationNumber;
    }

    public String getMedicalDepartment() {
        return medicalDepartment;
    }

    public LocalDate getOpenedOn() {
        return openedOn;
    }

    public TaxationType getTaxationType() {
        return taxationType;
    }

    public boolean isJointBusiness() {
        return jointBusiness;
    }

    public BigDecimal getPreviousYearRevenue() {
        return previousYearRevenue;
    }

    public boolean isDiligentFilingManuallyChecked() {
        return diligentFilingManuallyChecked;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
