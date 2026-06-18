package com.tax_helper.backend.workspace.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tax_rules")
public class TaxRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 1000)
    private String conditionExpression;

    @Column(nullable = false, length = 120)
    private String resultTag;

    @Column(nullable = false, length = 20)
    private String severity;

    private LocalDate startsOn;

    private LocalDate endsOn;

    @Column(length = 500)
    private String sourceUrl;

    @Column(nullable = false)
    private boolean reviewedByTaxAgent;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected TaxRule() {
    }

    public TaxRule(
            String name,
            String conditionExpression,
            String resultTag,
            String severity,
            LocalDate startsOn,
            LocalDate endsOn,
            String sourceUrl,
            boolean reviewedByTaxAgent
    ) {
        this.name = name;
        this.conditionExpression = conditionExpression;
        this.resultTag = resultTag;
        this.severity = severity;
        this.startsOn = startsOn;
        this.endsOn = endsOn;
        this.sourceUrl = sourceUrl;
        this.reviewedByTaxAgent = reviewedByTaxAgent;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getConditionExpression() {
        return conditionExpression;
    }

    public String getResultTag() {
        return resultTag;
    }

    public String getSeverity() {
        return severity;
    }

    public LocalDate getStartsOn() {
        return startsOn;
    }

    public LocalDate getEndsOn() {
        return endsOn;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public boolean isReviewedByTaxAgent() {
        return reviewedByTaxAgent;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
