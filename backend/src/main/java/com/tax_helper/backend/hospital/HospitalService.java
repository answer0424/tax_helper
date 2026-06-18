package com.tax_helper.backend.hospital;

import com.tax_helper.backend.hospital.domain.Hospital;
import com.tax_helper.backend.hospital.domain.TaxYearWorkspace;
import com.tax_helper.backend.hospital.repository.HospitalRepository;
import com.tax_helper.backend.hospital.repository.TaxYearWorkspaceRepository;
import com.tax_helper.backend.hospital.web.HospitalCreateRequest;
import com.tax_helper.backend.hospital.web.HospitalResponse;
import com.tax_helper.backend.hospital.web.TaxYearResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HospitalService {

    private final HospitalRepository hospitalRepository;
    private final TaxYearWorkspaceRepository taxYearWorkspaceRepository;

    public HospitalService(
            HospitalRepository hospitalRepository,
            TaxYearWorkspaceRepository taxYearWorkspaceRepository
    ) {
        this.hospitalRepository = hospitalRepository;
        this.taxYearWorkspaceRepository = taxYearWorkspaceRepository;
    }

    @Transactional(readOnly = true)
    public List<HospitalResponse> findAll() {
        return hospitalRepository.findAll().stream()
                .map(HospitalResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public HospitalResponse findById(Long id) {
        return HospitalResponse.from(getHospital(id));
    }

    @Transactional
    public HospitalResponse create(HospitalCreateRequest request) {
        hospitalRepository.findByBusinessRegistrationNumber(request.businessRegistrationNumber())
                .ifPresent(hospital -> {
                    throw new IllegalArgumentException("DUPLICATED_BUSINESS_REGISTRATION_NUMBER");
                });

        Hospital hospital = new Hospital(
                request.name(),
                request.ownerName(),
                request.businessRegistrationNumber(),
                request.medicalDepartment(),
                request.openedOn(),
                request.taxationType(),
                request.jointBusiness(),
                request.previousYearRevenue(),
                request.diligentFilingManuallyChecked()
        );

        return HospitalResponse.from(hospitalRepository.save(hospital));
    }

    @Transactional
    public HospitalResponse update(Long id, HospitalCreateRequest request) {
        Hospital hospital = getHospital(id);
        hospital.update(
                request.name(),
                request.ownerName(),
                request.medicalDepartment(),
                request.openedOn(),
                request.taxationType(),
                request.jointBusiness(),
                request.previousYearRevenue(),
                request.diligentFilingManuallyChecked()
        );
        return HospitalResponse.from(hospital);
    }

    @Transactional(readOnly = true)
    public List<TaxYearResponse> findTaxYears(Long hospitalId) {
        return taxYearWorkspaceRepository.findByHospital_IdOrderByTaxYearDesc(hospitalId).stream()
                .map(TaxYearResponse::from)
                .toList();
    }

    @Transactional
    public TaxYearResponse createTaxYear(Long hospitalId, int taxYear) {
        Hospital hospital = getHospital(hospitalId);
        TaxYearWorkspace workspace = taxYearWorkspaceRepository.findByHospital_IdAndTaxYear(hospitalId, taxYear)
                .orElseGet(() -> taxYearWorkspaceRepository.save(new TaxYearWorkspace(
                        hospital,
                        taxYear,
                        createStoragePath(hospitalId, taxYear)
                )));

        return TaxYearResponse.from(workspace);
    }

    private Hospital getHospital(Long id) {
        return hospitalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("HOSPITAL_NOT_FOUND"));
    }

    private String createStoragePath(Long hospitalId, int taxYear) {
        Path storagePath = Path.of("data", "hospitals", String.valueOf(hospitalId), String.valueOf(taxYear));
        try {
            Files.createDirectories(storagePath);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to create local storage directory", exception);
        }
        return storagePath.toString();
    }
}
