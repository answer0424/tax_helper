package com.tax_helper.backend.hospital.web;

import com.tax_helper.backend.hospital.HospitalService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hospitals")
public class HospitalController {

    private final HospitalService hospitalService;

    public HospitalController(HospitalService hospitalService) {
        this.hospitalService = hospitalService;
    }

    @GetMapping
    public List<HospitalResponse> findAll() {
        return hospitalService.findAll();
    }

    @GetMapping("/{id}")
    public HospitalResponse findById(@PathVariable Long id) {
        return hospitalService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HospitalResponse create(@Valid @RequestBody HospitalCreateRequest request) {
        return hospitalService.create(request);
    }

    @PutMapping("/{id}")
    public HospitalResponse update(@PathVariable Long id, @Valid @RequestBody HospitalCreateRequest request) {
        return hospitalService.update(id, request);
    }

    @GetMapping("/{id}/tax-years")
    public List<TaxYearResponse> findTaxYears(@PathVariable Long id) {
        return hospitalService.findTaxYears(id);
    }

    @PostMapping("/{id}/tax-years")
    @ResponseStatus(HttpStatus.CREATED)
    public TaxYearResponse createTaxYear(
            @PathVariable Long id,
            @Valid @RequestBody TaxYearCreateRequest request
    ) {
        return hospitalService.createTaxYear(id, request.taxYear());
    }
}
