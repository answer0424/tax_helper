package com.tax_helper.backend.workspace;

import com.tax_helper.backend.workspace.domain.Evidence;
import com.tax_helper.backend.workspace.domain.OcrStatus;
import com.tax_helper.backend.workspace.repository.EvidenceRepository;
import com.tax_helper.backend.workspace.web.EvidenceResponse;
import com.tax_helper.backend.workspace.web.OcrUpdateRequest;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OcrService {

    private static final int LOW_CONFIDENCE_THRESHOLD = 70;
    private static final Pattern BUSINESS_NUMBER_PATTERN = Pattern.compile("(\\d{3})[- ]?(\\d{2})[- ]?(\\d{5})");
    private static final Pattern DATE_PATTERN = Pattern.compile("(\\d{2,4})[.\\-/년 ]\\s*(\\d{1,2})[.\\-/월 ]\\s*(\\d{1,2})");
    private static final Pattern AMOUNT_PATTERN = Pattern.compile("(?<!\\d)(\\d{1,3}(?:,\\d{3})+|\\d{4,})(?!\\d)");
    private static final Pattern APPROVAL_PATTERN = Pattern.compile("(?:승인|승인번호|approval)\\D*(\\d{6,})", Pattern.CASE_INSENSITIVE);

    private final EvidenceRepository evidenceRepository;

    public OcrService(EvidenceRepository evidenceRepository) {
        this.evidenceRepository = evidenceRepository;
    }

    @Transactional
    public EvidenceResponse runOcr(Long evidenceId) {
        Evidence evidence = getEvidence(evidenceId);
        evidence.markOcrProcessing();

        OcrExecutionResult executionResult = executeTesseract(evidence);
        if (!executionResult.success()) {
            evidence.updateOcrResult(
                    OcrStatus.FAILED,
                    executionResult.rawText(),
                    executionResult.confidence(),
                    true,
                    executionResult.errorMessage(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
            return EvidenceResponse.from(evidence);
        }

        ParsedReceipt parsedReceipt = parseReceipt(executionResult.rawText());
        boolean reviewRequired = executionResult.confidence() == null
                || executionResult.confidence() < LOW_CONFIDENCE_THRESHOLD
                || parsedReceipt.totalAmount() == null
                || isBlank(parsedReceipt.supplierName());

        evidence.updateOcrResult(
                OcrStatus.COMPLETED,
                executionResult.rawText(),
                executionResult.confidence(),
                reviewRequired,
                null,
                parsedReceipt.transactionDate(),
                parsedReceipt.supplierName(),
                parsedReceipt.businessRegistrationNumber(),
                parsedReceipt.totalAmount(),
                parsedReceipt.supplyAmount(),
                parsedReceipt.vatAmount(),
                parsedReceipt.itemName(),
                parsedReceipt.paymentMethod(),
                parsedReceipt.approvalNumber()
        );

        return EvidenceResponse.from(evidence);
    }

    @Transactional
    public EvidenceResponse updateOcrResult(Long evidenceId, OcrUpdateRequest request) {
        Evidence evidence = getEvidence(evidenceId);
        evidence.updateOcrResult(
                OcrStatus.COMPLETED,
                request.rawText(),
                request.confidence(),
                request.reviewRequired(),
                null,
                request.transactionDate(),
                request.supplierName(),
                normalizeBusinessNumber(request.businessRegistrationNumber()),
                request.totalAmount(),
                request.supplyAmount(),
                request.vatAmount(),
                request.itemName(),
                request.paymentMethod(),
                request.approvalNumber()
        );
        return EvidenceResponse.from(evidence);
    }

    private OcrExecutionResult executeTesseract(Evidence evidence) {
        Path filePath = Path.of(evidence.getFilePath()).normalize();
        if (!Files.exists(filePath)) {
            return OcrExecutionResult.failed("원본 증빙 파일을 찾을 수 없습니다.");
        }

        String contentType = evidence.getContentType() == null ? "" : evidence.getContentType().toLowerCase(Locale.ROOT);
        if (!contentType.startsWith("image/")) {
            return OcrExecutionResult.failed("현재 OCR은 이미지 파일만 지원합니다. PDF OCR은 다음 단계에서 처리합니다.");
        }

        try {
            CommandResult textResult = executeCommand(buildTesseractCommand(filePath, false));
            if (textResult.exitCode() != 0) {
                return OcrExecutionResult.failed(
                        textResult.error().isBlank() ? "OCR 실행에 실패했습니다." : textResult.error().trim()
                );
            }

            CommandResult tsvResult = executeCommand(buildTesseractCommand(filePath, true));
            Integer confidence = tsvResult.exitCode() == 0 ? parseTsvConfidence(tsvResult.output()) : null;
            return OcrExecutionResult.success(textResult.output().trim(), confidence);
        } catch (IOException exception) {
            return OcrExecutionResult.failed("OCR 엔진을 찾을 수 없습니다. Tesseract 설치 후 다시 시도해 주세요.");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return OcrExecutionResult.failed("OCR 실행이 중단되었습니다.");
        }
    }

    private CommandResult executeCommand(List<String> command) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(command).start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        String error = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
        int exitCode = process.waitFor();
        return new CommandResult(exitCode, output, error);
    }

    private List<String> buildTesseractCommand(Path filePath, boolean tsvOutput) {
        List<String> command = new ArrayList<>();
        command.add(resolveTesseractCommand());
        command.add(filePath.toString());
        command.add("stdout");
        command.add("-l");
        command.add("kor+eng");
        command.add("--psm");
        command.add("6");
        resolveTessdataDir().ifPresent(tessdataDir -> {
            command.add("--tessdata-dir");
            command.add(tessdataDir.toString());
        });
        if (tsvOutput) {
            command.add("tsv");
        }
        return command;
    }

    private String resolveTesseractCommand() {
        String configuredPath = System.getenv("TESSERACT_PATH");
        if (!isBlank(configuredPath) && Files.exists(Path.of(configuredPath))) {
            return configuredPath;
        }

        Path defaultWindowsPath = Path.of("C:", "Program Files", "Tesseract-OCR", "tesseract.exe");
        if (Files.exists(defaultWindowsPath)) {
            return defaultWindowsPath.toString();
        }

        return "tesseract";
    }

    private Optional<Path> resolveTessdataDir() {
        String configuredPath = System.getenv("TESSDATA_PREFIX");
        if (!isBlank(configuredPath) && Files.isDirectory(Path.of(configuredPath))) {
            return Optional.of(Path.of(configuredPath));
        }

        Path localAppDataPath = Path.of(
                System.getProperty("user.home"),
                "AppData",
                "Local",
                "Tesseract-OCR",
                "tessdata"
        );
        if (Files.isDirectory(localAppDataPath)) {
            return Optional.of(localAppDataPath);
        }

        Path defaultWindowsPath = Path.of("C:", "Program Files", "Tesseract-OCR", "tessdata");
        if (Files.isDirectory(defaultWindowsPath)) {
            return Optional.of(defaultWindowsPath);
        }

        return Optional.empty();
    }

    private Integer parseTsvConfidence(String tsv) {
        String[] lines = tsv.split("\\R");
        List<Integer> confidences = new ArrayList<>();
        boolean hasTsvColumns = lines.length > 0 && lines[0].contains("\t");

        if (!hasTsvColumns) {
            return null;
        }

        for (int i = 1; i < lines.length; i++) {
            String[] columns = lines[i].split("\\t", -1);
            if (columns.length < 12) {
                continue;
            }
            String confidenceText = columns[10].trim();
            try {
                int confidence = Math.round(Float.parseFloat(confidenceText));
                if (confidence >= 0) {
                    confidences.add(confidence);
                }
            } catch (NumberFormatException ignored) {
                // Tesseract can emit non-numeric confidence for layout rows.
            }
        }

        return confidences.isEmpty()
                ? null
                : Math.round((float) confidences.stream().mapToInt(Integer::intValue).average().orElse(0));
    }

    private ParsedReceipt parseReceipt(String rawText) {
        List<String> lines = rawText.lines()
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .toList();

        return new ParsedReceipt(
                extractDate(rawText),
                extractSupplierName(lines),
                extractBusinessNumber(rawText),
                extractAmountNearKeyword(lines, List.of("합계", "총액", "총 금액", "받을금액", "결제금액"))
                        .or(() -> extractLargestAmount(rawText))
                        .orElse(null),
                extractAmountNearKeyword(lines, List.of("공급가", "공급가액")).orElse(null),
                extractAmountNearKeyword(lines, List.of("부가세", "부가가치세", "세액")).orElse(null),
                extractItemName(lines),
                extractPaymentMethod(rawText),
                extractApprovalNumber(rawText)
        );
    }

    private LocalDate extractDate(String text) {
        Matcher matcher = DATE_PATTERN.matcher(text);
        while (matcher.find()) {
            int year = Integer.parseInt(matcher.group(1));
            int month = Integer.parseInt(matcher.group(2));
            int day = Integer.parseInt(matcher.group(3));
            if (year < 100) {
                year += 2000;
            }
            try {
                return LocalDate.of(year, month, day);
            } catch (RuntimeException ignored) {
                // Try the next date-like value.
            }
        }
        return null;
    }

    private String extractSupplierName(List<String> lines) {
        int receiptTitleIndex = -1;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains("영수증")) {
                receiptTitleIndex = i;
                break;
            }
        }

        List<String> candidateLines = receiptTitleIndex >= 0 && receiptTitleIndex + 1 < lines.size()
                ? lines.subList(receiptTitleIndex + 1, lines.size())
                : lines;

        return candidateLines.stream()
                .filter(line -> !line.matches(".*\\d{3}[- ]?\\d{2}[- ]?\\d{5}.*"))
                .filter(line -> !containsAny(line, List.of("영수증", "사업자", "대표", "전화", "주소", "합계", "금액", "카드")))
                .filter(line -> line.matches(".*[가-힣].*"))
                .filter(line -> line.length() >= 2)
                .findFirst()
                .orElse(null);
    }

    private String extractBusinessNumber(String text) {
        Matcher matcher = BUSINESS_NUMBER_PATTERN.matcher(text);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(1) + matcher.group(2) + matcher.group(3);
    }

    private Optional<BigDecimal> extractAmountNearKeyword(List<String> lines, List<String> keywords) {
        return lines.stream()
                .filter(line -> containsAny(line, keywords))
                .map(this::extractLastAmount)
                .flatMap(Optional::stream)
                .findFirst();
    }

    private Optional<BigDecimal> extractLargestAmount(String text) {
        Matcher matcher = AMOUNT_PATTERN.matcher(text);
        List<BigDecimal> amounts = new ArrayList<>();
        while (matcher.find()) {
            parseAmount(matcher.group(1)).ifPresent(amounts::add);
        }
        return amounts.stream().max(Comparator.naturalOrder());
    }

    private Optional<BigDecimal> extractLastAmount(String text) {
        Matcher matcher = AMOUNT_PATTERN.matcher(text);
        BigDecimal lastAmount = null;
        while (matcher.find()) {
            lastAmount = parseAmount(matcher.group(1)).orElse(lastAmount);
        }
        return Optional.ofNullable(lastAmount);
    }

    private Optional<BigDecimal> parseAmount(String text) {
        try {
            return Optional.of(new BigDecimal(text.replace(",", "")));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    private String extractItemName(List<String> lines) {
        return lines.stream()
                .filter(line -> !containsAny(line, List.of(
                        "영수증", "합계", "총액", "부가세", "공급가", "사업자", "승인", "카드",
                        "거래일", "결제", "대표", "전화", "주소", "받을금액", "카드번호"
                )))
                .filter(line -> extractLastAmount(line).isPresent())
                .filter(line -> line.length() >= 2)
                .map(line -> AMOUNT_PATTERN.matcher(line).replaceAll("").trim())
                .findFirst()
                .orElse(null);
    }

    private String extractPaymentMethod(String text) {
        String lower = text.toLowerCase(Locale.ROOT);
        if (lower.contains("신용") || lower.contains("카드") || lower.contains("card")) {
            return "카드";
        }
        if (lower.contains("현금") || lower.contains("cash")) {
            return "현금";
        }
        if (lower.contains("계좌") || lower.contains("이체")) {
            return "계좌이체";
        }
        return null;
    }

    private String extractApprovalNumber(String text) {
        Matcher matcher = APPROVAL_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String normalizeBusinessNumber(String value) {
        if (value == null) {
            return null;
        }
        return value.replaceAll("\\D", "");
    }

    private boolean containsAny(String text, List<String> keywords) {
        return keywords.stream().anyMatch(text::contains);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private Evidence getEvidence(Long evidenceId) {
        return evidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new IllegalArgumentException("EVIDENCE_NOT_FOUND"));
    }

    private record OcrExecutionResult(
            boolean success,
            String rawText,
            Integer confidence,
            String errorMessage
    ) {
        static OcrExecutionResult success(String rawText, Integer confidence) {
            return new OcrExecutionResult(true, rawText, confidence, null);
        }

        static OcrExecutionResult failed(String errorMessage) {
            return new OcrExecutionResult(false, null, null, errorMessage);
        }
    }

    private record ParsedReceipt(
            LocalDate transactionDate,
            String supplierName,
            String businessRegistrationNumber,
            BigDecimal totalAmount,
            BigDecimal supplyAmount,
            BigDecimal vatAmount,
            String itemName,
            String paymentMethod,
            String approvalNumber
    ) {
    }

    private record CommandResult(
            int exitCode,
            String output,
            String error
    ) {
    }
}
