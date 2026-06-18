package com.tax_helper.backend.workspace.repository;

import com.tax_helper.backend.workspace.domain.TaxRule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaxRuleRepository extends JpaRepository<TaxRule, Long> {
}
