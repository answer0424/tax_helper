package com.tax_helper.backend.workspace;

import com.tax_helper.backend.workspace.domain.DuplicateStatus;
import com.tax_helper.backend.workspace.domain.Evidence;
import com.tax_helper.backend.workspace.repository.EvidenceRepository;
import com.tax_helper.backend.workspace.web.DuplicateReviewRequest;
import com.tax_helper.backend.workspace.web.EvidenceResponse;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import javax.imageio.ImageIO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DuplicateDetectionService {

    private static final int IMAGE_SIMILARITY_MAX_DISTANCE = 8;

    private final EvidenceRepository evidenceRepository;

    public DuplicateDetectionService(EvidenceRepository evidenceRepository) {
        this.evidenceRepository = evidenceRepository;
    }

    @Transactional
    public EvidenceResponse detect(Long evidenceId) {
        Evidence evidence = getEvidence(evidenceId);
        detectAndApply(evidence);
        return EvidenceResponse.from(evidence);
    }

    @Transactional
    public EvidenceResponse review(Long evidenceId, DuplicateReviewRequest request) {
        Evidence evidence = getEvidence(evidenceId);
        evidence.reviewDuplicateStatus(
                request.duplicateStatus(),
                request.duplicateCandidateEvidenceId(),
                request.reason()
        );
        return EvidenceResponse.from(evidence);
    }

    public void detectAndApply(Evidence evidence) {
        if (evidence.isDuplicateManuallyReviewed()) {
            return;
        }

        Optional<DuplicateCandidate> bestCandidate = findBestCandidate(evidence);
        if (bestCandidate.isPresent()) {
            DuplicateCandidate candidate = bestCandidate.get();
            evidence.markDuplicateCandidate(candidate.evidenceId(), candidate.reason(), candidate.score());
            return;
        }

        evidence.clearDuplicateCandidate();
    }

    private Optional<DuplicateCandidate> findBestCandidate(Evidence evidence) {
        List<Evidence> candidates = evidenceRepository.findByHospital_IdAndTaxYearWorkspace_TaxYearAndIdNot(
                evidence.getHospitalId(),
                evidence.getTaxYearWorkspace().getTaxYear(),
                evidence.getId()
        );

        return candidates.stream()
                .map(candidate -> score(evidence, candidate))
                .flatMap(Optional::stream)
                .max(Comparator.comparingInt(DuplicateCandidate::score));
    }

    private Optional<DuplicateCandidate> score(Evidence evidence, Evidence candidate) {
        if (sameText(evidence.getFileHash(), candidate.getFileHash())) {
            return Optional.of(new DuplicateCandidate(
                    candidate.getId(),
                    "파일 해시가 같은 증빙입니다.",
                    100
            ));
        }

        if (!isBlank(evidence.getExtractedApprovalNumber())
                && sameText(evidence.getExtractedApprovalNumber(), candidate.getExtractedApprovalNumber())) {
            return Optional.of(new DuplicateCandidate(
                    candidate.getId(),
                    "승인번호가 같은 증빙입니다.",
                    95
            ));
        }

        if (sameTransactionShape(evidence, candidate)) {
            return Optional.of(new DuplicateCandidate(
                    candidate.getId(),
                    "거래일, 금액, 거래처가 같은 증빙입니다.",
                    88
            ));
        }

        Optional<Integer> imageDistance = imageDistance(evidence, candidate);
        if (imageDistance.isPresent() && imageDistance.get() <= IMAGE_SIMILARITY_MAX_DISTANCE) {
            int score = Math.max(70, 90 - imageDistance.get());
            return Optional.of(new DuplicateCandidate(
                    candidate.getId(),
                    "이미지 구성이 유사한 증빙입니다.",
                    score
            ));
        }

        return Optional.empty();
    }

    private boolean sameTransactionShape(Evidence evidence, Evidence candidate) {
        LocalDate transactionDate = evidence.getExtractedTransactionDate();
        BigDecimal totalAmount = evidence.getExtractedTotalAmount();
        String supplier = normalize(evidence.getExtractedSupplierName());

        return transactionDate != null
                && totalAmount != null
                && !supplier.isBlank()
                && transactionDate.equals(candidate.getExtractedTransactionDate())
                && totalAmount.compareTo(candidate.getExtractedTotalAmount() == null ? BigDecimal.valueOf(-1) : candidate.getExtractedTotalAmount()) == 0
                && supplier.equals(normalize(candidate.getExtractedSupplierName()));
    }

    private Optional<Integer> imageDistance(Evidence evidence, Evidence candidate) {
        if (!isImage(evidence) || !isImage(candidate)) {
            return Optional.empty();
        }

        try {
            Optional<Long> hash = averageImageHash(evidence.getFilePath());
            Optional<Long> candidateHash = averageImageHash(candidate.getFilePath());
            if (hash.isEmpty() || candidateHash.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(Long.bitCount(hash.get() ^ candidateHash.get()));
        } catch (IOException exception) {
            return Optional.empty();
        }
    }

    private Optional<Long> averageImageHash(String filePath) throws IOException {
        Path path = Path.of(filePath).normalize();
        if (!Files.isRegularFile(path)) {
            return Optional.empty();
        }

        BufferedImage source = ImageIO.read(path.toFile());
        if (source == null) {
            return Optional.empty();
        }

        Image scaled = source.getScaledInstance(8, 8, Image.SCALE_AREA_AVERAGING);
        BufferedImage grayscale = new BufferedImage(8, 8, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D graphics = grayscale.createGraphics();
        graphics.drawImage(scaled, 0, 0, null);
        graphics.dispose();

        int[] values = new int[64];
        int sum = 0;
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                int value = grayscale.getRaster().getSample(x, y, 0);
                values[y * 8 + x] = value;
                sum += value;
            }
        }

        int average = sum / values.length;
        long hash = 0L;
        for (int i = 0; i < values.length; i++) {
            if (values[i] >= average) {
                hash |= 1L << i;
            }
        }
        return Optional.of(hash);
    }

    private boolean isImage(Evidence evidence) {
        String contentType = evidence.getContentType() == null ? "" : evidence.getContentType().toLowerCase(Locale.ROOT);
        return contentType.startsWith("image/");
    }

    private boolean sameText(String left, String right) {
        return !isBlank(left) && left.equals(right);
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFKC)
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", "")
                .trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private Evidence getEvidence(Long evidenceId) {
        return evidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new IllegalArgumentException("EVIDENCE_NOT_FOUND"));
    }

    private record DuplicateCandidate(Long evidenceId, String reason, int score) {
    }
}
