package com.tax_helper.backend.workspace.repository;

import com.tax_helper.backend.workspace.domain.CounterpartyAccountRule;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CounterpartyAccountRuleRepository extends JpaRepository<CounterpartyAccountRule, Long> {
    Optional<CounterpartyAccountRule> findByHospital_IdAndCounterpartyKey(Long hospitalId, String counterpartyKey);

    List<CounterpartyAccountRule> findByHospital_IdOrderByLastLearnedAtDesc(Long hospitalId);
}
