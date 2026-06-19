package com.tax_helper.backend.workspace;

import com.tax_helper.backend.workspace.domain.Evidence;
import com.tax_helper.backend.workspace.domain.EvidenceType;
import com.tax_helper.backend.workspace.domain.RiskTag;
import com.tax_helper.backend.workspace.web.BusinessTransactionCreateRequest;
import com.tax_helper.backend.workspace.web.BusinessTransactionUpdateRequest;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class RiskTagService {

    private static final BigDecimal HIGH_VALUE_THRESHOLD = BigDecimal.valueOf(1_000_000);
    private static final Set<EvidenceType> QUALIFIED_EVIDENCE_TYPES = Set.of(
            EvidenceType.TAX_INVOICE,
            EvidenceType.CARD_RECEIPT,
            EvidenceType.CASH_RECEIPT
    );

    public Set<RiskTag> calculate(BusinessTransactionCreateRequest request) {
        return calculate(
                request.counterpartyName(),
                request.amount(),
                request.accountTitle(),
                request.memo(),
                request.riskTags(),
                null
        );
    }

    public Set<RiskTag> calculate(BusinessTransactionUpdateRequest request) {
        return calculate(
                request.counterpartyName(),
                request.amount(),
                request.accountTitle(),
                request.memo(),
                request.riskTags(),
                null
        );
    }

    public Set<RiskTag> calculate(BusinessTransactionUpdateRequest request, Evidence evidence) {
        return calculate(
                request.counterpartyName(),
                request.amount(),
                request.accountTitle(),
                request.memo(),
                request.riskTags(),
                evidence
        );
    }

    private Set<RiskTag> calculate(
            String counterpartyName,
            BigDecimal amount,
            String accountTitle,
            String memo,
            Collection<RiskTag> manualTags,
            Evidence evidence
    ) {
        EnumSet<RiskTag> tags = EnumSet.noneOf(RiskTag.class);
        if (manualTags != null) {
            tags.addAll(manualTags);
        }

        String searchText = normalize(counterpartyName + " " + accountTitle + " " + memo + " " + evidenceText(evidence));
        applyEvidenceTags(tags, evidence, searchText);

        if (amount != null && amount.compareTo(HIGH_VALUE_THRESHOLD) >= 0) {
            tags.add(RiskTag.HIGH_VALUE);
            tags.add(RiskTag.FIXED_ASSET_CANDIDATE);
        }
        if (containsAny(searchText, "장비", "기기", "비품", "컴퓨터", "노트북", "초음파", "레이저", "의료기기", "가구", "설비")) {
            tags.add(RiskTag.FIXED_ASSET_CANDIDATE);
        }
        if (containsAny(searchText, "접대", "거래처식사", "선물", "화환", "골프", "유흥", "주점")) {
            tags.add(RiskTag.ENTERTAINMENT_REVIEW);
        }
        if (containsAny(searchText, "주유", "정비", "타이어", "주차", "하이패스", "세차", "자동차", "차량")) {
            tags.add(RiskTag.VEHICLE_REVIEW);
        }
        if (containsAny(searchText, "대표", "원장", "개인", "가족", "자택", "백화점", "명품", "여행")) {
            tags.add(RiskTag.OWNER_EXPENSE_REVIEW);
        }

        return tags;
    }

    private void applyEvidenceTags(EnumSet<RiskTag> tags, Evidence evidence, String searchText) {
        if (evidence == null) {
            return;
        }

        EvidenceType evidenceType = evidence.getEvidenceType();
        if (QUALIFIED_EVIDENCE_TYPES.contains(evidenceType) || containsAny(searchText, "신용카드", "체크카드", "현금영수증", "세금계산서")) {
            tags.add(RiskTag.QUALIFIED_EVIDENCE);
        } else {
            tags.add(RiskTag.MISSING_EVIDENCE);
        }

        if (evidenceType == EvidenceType.SIMPLE_RECEIPT || containsAny(searchText, "간이영수증", "간이 영수증")) {
            tags.add(RiskTag.SIMPLE_RECEIPT);
        }
        if (evidence.isOcrReviewRequired() || evidence.getOcrStatus().name().equals("FAILED")) {
            tags.add(RiskTag.OCR_REVIEW_REQUIRED);
        }
    }

    private String evidenceText(Evidence evidence) {
        if (evidence == null) {
            return "";
        }
        return String.join(
                " ",
                nullToBlank(evidence.getOriginalFileName()),
                nullToBlank(evidence.getExtractedItemName()),
                nullToBlank(evidence.getExtractedPaymentMethod()),
                nullToBlank(evidence.getOcrRawText())
        );
    }

    private boolean containsAny(String value, String... keywords) {
        for (String keyword : keywords) {
            if (value.contains(keyword.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private String normalize(String value) {
        return Normalizer.normalize(nullToBlank(value), Normalizer.Form.NFKC)
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String nullToBlank(String value) {
        return value == null ? "" : value;
    }
}
