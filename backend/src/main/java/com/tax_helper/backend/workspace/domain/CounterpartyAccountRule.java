package com.tax_helper.backend.workspace.domain;

import com.tax_helper.backend.hospital.domain.Hospital;
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
        name = "counterparty_account_rules",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_counterparty_account_rule_hospital_key",
                columnNames = {"hospital_id", "counterparty_key"}
        )
)
public class CounterpartyAccountRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hospital_id")
    private Hospital hospital;

    @Column(nullable = false, length = 160)
    private String counterpartyName;

    @Column(nullable = false, length = 160)
    private String counterpartyKey;

    @Column(nullable = false, length = 80)
    private String accountTitle;

    @Column(nullable = false)
    private int learnedCount;

    @Column(nullable = false)
    private LocalDateTime lastLearnedAt;

    protected CounterpartyAccountRule() {
    }

    public CounterpartyAccountRule(
            Hospital hospital,
            String counterpartyName,
            String counterpartyKey,
            String accountTitle
    ) {
        this.hospital = hospital;
        this.counterpartyName = counterpartyName;
        this.counterpartyKey = counterpartyKey;
        this.accountTitle = accountTitle;
        this.learnedCount = 1;
        this.lastLearnedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getHospitalId() {
        return hospital.getId();
    }

    public String getCounterpartyName() {
        return counterpartyName;
    }

    public String getCounterpartyKey() {
        return counterpartyKey;
    }

    public String getAccountTitle() {
        return accountTitle;
    }

    public int getLearnedCount() {
        return learnedCount;
    }

    public LocalDateTime getLastLearnedAt() {
        return lastLearnedAt;
    }

    public void learn(String counterpartyName, String accountTitle) {
        this.counterpartyName = counterpartyName;
        this.accountTitle = accountTitle;
        this.learnedCount += 1;
        this.lastLearnedAt = LocalDateTime.now();
    }
}
