package com.tax_helper.backend.hospital.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "tax_year_workspaces",
        uniqueConstraints = @UniqueConstraint(name = "uk_tax_year_hospital_year", columnNames = {"hospital_id", "tax_year"})
)
public class TaxYearWorkspace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hospital_id")
    private Hospital hospital;

    @Column(name = "tax_year", nullable = false)
    private int taxYear;

    @Column(nullable = false, length = 255)
    private String storagePath;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected TaxYearWorkspace() {
    }

    public TaxYearWorkspace(Hospital hospital, int taxYear, String storagePath) {
        this.hospital = hospital;
        this.taxYear = taxYear;
        this.storagePath = storagePath;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getHospitalId() {
        return hospital.getId();
    }

    public int getTaxYear() {
        return taxYear;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
