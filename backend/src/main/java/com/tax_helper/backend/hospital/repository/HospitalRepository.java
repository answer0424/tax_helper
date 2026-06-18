package com.tax_helper.backend.hospital.repository;

import com.tax_helper.backend.hospital.domain.Hospital;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HospitalRepository extends JpaRepository<Hospital, Long> {
    Optional<Hospital> findByBusinessRegistrationNumber(String businessRegistrationNumber);
}
