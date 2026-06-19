package com.tax_helper.backend.workspace;

import com.tax_helper.backend.hospital.domain.Hospital;
import com.tax_helper.backend.hospital.domain.TaxYearWorkspace;
import com.tax_helper.backend.hospital.repository.HospitalRepository;
import com.tax_helper.backend.hospital.repository.TaxYearWorkspaceRepository;
import com.tax_helper.backend.workspace.domain.Evidence;
import com.tax_helper.backend.workspace.domain.EvidenceType;
import com.tax_helper.backend.workspace.domain.EvidenceUploadStatus;
import com.tax_helper.backend.workspace.repository.EvidenceRepository;
import com.tax_helper.backend.workspace.web.EvidenceResponse;
import com.tax_helper.backend.workspace.web.EvidenceUploadResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class EvidenceUploadService {

    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final DateTimeFormatter DAY_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;

    private final HospitalRepository hospitalRepository;
    private final TaxYearWorkspaceRepository taxYearWorkspaceRepository;
    private final EvidenceRepository evidenceRepository;
    private final DuplicateDetectionService duplicateDetectionService;

    public EvidenceUploadService(
            HospitalRepository hospitalRepository,
            TaxYearWorkspaceRepository taxYearWorkspaceRepository,
            EvidenceRepository evidenceRepository,
            DuplicateDetectionService duplicateDetectionService
    ) {
        this.hospitalRepository = hospitalRepository;
        this.taxYearWorkspaceRepository = taxYearWorkspaceRepository;
        this.evidenceRepository = evidenceRepository;
        this.duplicateDetectionService = duplicateDetectionService;
    }

    @Transactional
    public EvidenceUploadResponse upload(Long hospitalId, int taxYear, MultipartFile[] files) {
        Hospital hospital = requireHospital(hospitalId);
        TaxYearWorkspace workspace = requireWorkspace(hospitalId, taxYear);
        List<EvidenceResponse> uploaded = new ArrayList<>();
        List<String> unsupportedFiles = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue;
            }

            String originalFileName = sanitizeFileName(file.getOriginalFilename());
            if (isZip(originalFileName, file.getContentType())) {
                uploaded.addAll(uploadZip(hospital, workspace, file, originalFileName, unsupportedFiles));
                continue;
            }

            if (!isSupportedEvidenceFile(originalFileName)) {
                unsupportedFiles.add(originalFileName);
                continue;
            }

            uploaded.add(saveEvidence(hospital, workspace, originalFileName, file.getContentType(), readBytes(file)));
        }

        int duplicateCount = (int) uploaded.stream()
                .filter(EvidenceResponse::duplicateSuspected)
                .count();

        return new EvidenceUploadResponse(uploaded.size(), duplicateCount, unsupportedFiles, uploaded);
    }

    @Transactional(readOnly = true)
    public Resource loadEvidenceFile(Long evidenceId) {
        Evidence evidence = evidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new IllegalArgumentException("EVIDENCE_NOT_FOUND"));
        Path filePath = Path.of(evidence.getFilePath()).normalize();
        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            throw new IllegalArgumentException("EVIDENCE_FILE_NOT_FOUND");
        }
        return new FileSystemResource(filePath);
    }

    private List<EvidenceResponse> uploadZip(
            Hospital hospital,
            TaxYearWorkspace workspace,
            MultipartFile zipFile,
            String zipFileName,
            List<String> unsupportedFiles
    ) {
        List<EvidenceResponse> uploaded = new ArrayList<>();
        try (ZipInputStream zipInputStream = new ZipInputStream(zipFile.getInputStream())) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }

                String entryFileName = sanitizeFileName(entry.getName());
                if (!isSupportedEvidenceFile(entryFileName)) {
                    unsupportedFiles.add(zipFileName + " / " + entryFileName);
                    continue;
                }

                uploaded.add(saveEvidence(
                        hospital,
                        workspace,
                        entryFileName,
                        guessContentType(entryFileName),
                        zipInputStream.readAllBytes()
                ));
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read zip file", exception);
        }
        return uploaded;
    }

    private EvidenceResponse saveEvidence(
            Hospital hospital,
            TaxYearWorkspace workspace,
            String originalFileName,
            String contentType,
            byte[] bytes
    ) {
        String fileHash = sha256(bytes);
        Evidence duplicate = evidenceRepository
                .findFirstByHospital_IdAndTaxYearWorkspace_TaxYearAndFileHashOrderByCreatedAtAsc(
                        hospital.getId(),
                        workspace.getTaxYear(),
                        fileHash
                )
                .orElse(null);
        boolean duplicateSuspected = duplicate != null;
        String storedFileName = buildStoredFileName(originalFileName, fileHash);
        Path storagePath = buildStoragePath(workspace);
        Path targetPath = storagePath.resolve(storedFileName);

        try {
            Files.createDirectories(storagePath);
            Files.write(targetPath, bytes);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to store evidence file", exception);
        }

        Evidence evidence = new Evidence(
                hospital,
                workspace,
                null,
                classifyEvidenceType(originalFileName, contentType),
                targetPath.toString(),
                originalFileName,
                storedFileName,
                contentType == null ? guessContentType(originalFileName) : contentType,
                bytes.length,
                fileHash,
                duplicateSuspected ? EvidenceUploadStatus.DUPLICATE_SUSPECTED : EvidenceUploadStatus.UPLOADED,
                duplicateSuspected,
                duplicate == null ? null : duplicate.getId(),
                null,
                null
        );

        Evidence savedEvidence = evidenceRepository.save(evidence);
        duplicateDetectionService.detectAndApply(savedEvidence);
        return EvidenceResponse.from(savedEvidence);
    }

    private Hospital requireHospital(Long hospitalId) {
        return hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new IllegalArgumentException("HOSPITAL_NOT_FOUND"));
    }

    private TaxYearWorkspace requireWorkspace(Long hospitalId, int taxYear) {
        return taxYearWorkspaceRepository.findByHospital_IdAndTaxYear(hospitalId, taxYear)
                .orElseThrow(() -> new IllegalArgumentException("TAX_YEAR_WORKSPACE_NOT_FOUND"));
    }

    private Path buildStoragePath(TaxYearWorkspace workspace) {
        LocalDate today = LocalDate.now();
        return Path.of(workspace.getStoragePath(), "evidences", MONTH_FORMAT.format(today), DAY_FORMAT.format(today));
    }

    private String buildStoredFileName(String originalFileName, String fileHash) {
        String extension = getExtension(originalFileName);
        return UUID.randomUUID() + "_" + fileHash.substring(0, 12) + extension;
    }

    private boolean isZip(String fileName, String contentType) {
        return fileName.toLowerCase(Locale.ROOT).endsWith(".zip")
                || "application/zip".equalsIgnoreCase(contentType)
                || "application/x-zip-compressed".equalsIgnoreCase(contentType);
    }

    private boolean isSupportedEvidenceFile(String fileName) {
        String lower = fileName.toLowerCase(Locale.ROOT);
        return lower.endsWith(".jpg")
                || lower.endsWith(".jpeg")
                || lower.endsWith(".png")
                || lower.endsWith(".pdf");
    }

    private String guessContentType(String fileName) {
        String lower = fileName.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (lower.endsWith(".png")) {
            return "image/png";
        }
        if (lower.endsWith(".pdf")) {
            return "application/pdf";
        }
        return "application/octet-stream";
    }

    private EvidenceType classifyEvidenceType(String fileName, String contentType) {
        String lower = fileName.toLowerCase(Locale.ROOT);
        if (lower.contains("세금계산서") || lower.contains("tax_invoice")) {
            return EvidenceType.TAX_INVOICE;
        }
        if (lower.contains("현금영수증") || lower.contains("cash_receipt")) {
            return EvidenceType.CASH_RECEIPT;
        }
        if (lower.contains("카드") || lower.contains("card")) {
            return EvidenceType.CARD_RECEIPT;
        }
        if (lower.contains("간이") || lower.contains("simple")) {
            return EvidenceType.SIMPLE_RECEIPT;
        }
        if ("application/pdf".equalsIgnoreCase(contentType) || lower.endsWith(".pdf")) {
            return EvidenceType.INVOICE;
        }
        return EvidenceType.OTHER;
    }

    private String sanitizeFileName(String fileName) {
        String normalized = fileName == null || fileName.isBlank() ? "unnamed" : fileName.replace("\\", "/");
        int lastSlash = normalized.lastIndexOf('/');
        String baseName = lastSlash >= 0 ? normalized.substring(lastSlash + 1) : normalized;
        return baseName.replaceAll("[^a-zA-Z0-9가-힣._ -]", "_");
    }

    private String getExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex < 0 ? "" : fileName.substring(dotIndex).toLowerCase(Locale.ROOT);
    }

    private byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read uploaded file", exception);
        }
    }

    private String sha256(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(bytes));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
