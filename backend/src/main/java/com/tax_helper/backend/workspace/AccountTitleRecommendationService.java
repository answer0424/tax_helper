package com.tax_helper.backend.workspace;

import com.tax_helper.backend.hospital.domain.Hospital;
import com.tax_helper.backend.hospital.repository.HospitalRepository;
import com.tax_helper.backend.workspace.domain.CounterpartyAccountRule;
import com.tax_helper.backend.workspace.repository.CounterpartyAccountRuleRepository;
import com.tax_helper.backend.workspace.web.AccountTitleSuggestionResponse;
import com.tax_helper.backend.workspace.web.CounterpartyAccountRuleResponse;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountTitleRecommendationService {

    private static final BigDecimal FIXED_ASSET_AMOUNT_THRESHOLD = BigDecimal.valueOf(1_000_000);
    private static final List<KeywordRule> KEYWORD_RULES = List.of(
            new KeywordRule("의약품비", List.of("의약품", "약품", "제약", "약국", "pharm", "drug"), "거래처/품목에 의약품 관련 키워드가 있습니다.", 76),
            new KeywordRule("진료재료비", List.of("의료소모품", "진료재료", "멸균", "거즈", "주사", "시린지", "카테터"), "진료재료로 볼 수 있는 키워드가 있습니다.", 74),
            new KeywordRule("소모품비", List.of("소모품", "문구", "사무용품", "다이소", "쿠팡", "토너", "용지"), "반복 소모품성 지출 키워드가 있습니다.", 68),
            new KeywordRule("지급수수료", List.of("수수료", "카드", "pg", "정산", "세무", "법무", "노무", "플랫폼"), "수수료 또는 전문용역 관련 키워드가 있습니다.", 70),
            new KeywordRule("임차료", List.of("임대", "임차", "월세", "렌트", "lease"), "임차/월세 관련 키워드가 있습니다.", 72),
            new KeywordRule("관리비", List.of("관리비", "전기", "수도", "가스", "빌딩관리", "공용관리"), "관리비 또는 공과성 비용 키워드가 있습니다.", 70),
            new KeywordRule("광고선전비", List.of("광고", "마케팅", "홍보", "네이버", "검색광고", "인스타", "블로그"), "광고/홍보 관련 키워드가 있습니다.", 73),
            new KeywordRule("급여", List.of("급여", "월급", "상여", "인건비", "payroll"), "급여성 지출 키워드가 있습니다.", 78),
            new KeywordRule("복리후생비", List.of("식대", "회식", "복지", "간식", "커피", "직원", "건강검진"), "직원 복리후생으로 볼 수 있는 키워드가 있습니다.", 66),
            new KeywordRule("접대비", List.of("접대", "거래처식사", "선물", "화환", "골프"), "접대성 지출 키워드가 있습니다.", 64),
            new KeywordRule("차량유지비", List.of("주유", "정비", "타이어", "주차", "하이패스", "세차", "자동차"), "차량 유지 관련 키워드가 있습니다.", 71),
            new KeywordRule("통신비", List.of("통신", "전화", "인터넷", "kt", "skt", "lg유플러스", "유플러스"), "통신/인터넷 관련 키워드가 있습니다.", 73),
            new KeywordRule("세금과공과", List.of("세금", "공과금", "면허세", "주민세", "국민연금", "건강보험", "고용보험", "산재보험"), "세금 또는 공과금 관련 키워드가 있습니다.", 72),
            new KeywordRule("보험료", List.of("보험", "화재보험", "배상책임", "손해보험", "생명보험"), "보험료 관련 키워드가 있습니다.", 70),
            new KeywordRule("수선비", List.of("수리", "수선", "보수", "as", "교체", "인테리어보수"), "수리/보수 관련 키워드가 있습니다.", 67)
    );
    private static final List<String> FIXED_ASSET_KEYWORDS = List.of(
            "장비", "기기", "비품", "컴퓨터", "노트북", "프린터", "초음파", "레이저", "의료기기", "가구", "인테리어", "설비"
    );

    private final HospitalRepository hospitalRepository;
    private final CounterpartyAccountRuleRepository ruleRepository;

    public AccountTitleRecommendationService(
            HospitalRepository hospitalRepository,
            CounterpartyAccountRuleRepository ruleRepository
    ) {
        this.hospitalRepository = hospitalRepository;
        this.ruleRepository = ruleRepository;
    }

    @Transactional(readOnly = true)
    public List<AccountTitleSuggestionResponse> suggest(
            Long hospitalId,
            String counterpartyName,
            String itemName,
            BigDecimal amount
    ) {
        requireHospital(hospitalId);
        String counterpartyKey = normalizeKey(counterpartyName);
        if (!counterpartyKey.isBlank()) {
            CounterpartyAccountRule learnedRule = ruleRepository
                    .findByHospital_IdAndCounterpartyKey(hospitalId, counterpartyKey)
                    .orElse(null);
            if (learnedRule != null) {
                return List.of(new AccountTitleSuggestionResponse(
                        learnedRule.getAccountTitle(),
                        "이 병원에서 같은 거래처에 사용자가 적용한 계정과목입니다.",
                        "LEARNED_COUNTERPARTY",
                        Math.min(98, 82 + learnedRule.getLearnedCount() * 2),
                        isFixedAssetCandidate(counterpartyName, itemName, amount)
                ));
            }
        }

        String searchText = normalizeSearchText(counterpartyName + " " + itemName);
        boolean fixedAssetCandidate = isFixedAssetCandidate(counterpartyName, itemName, amount);
        if (fixedAssetCandidate) {
            return List.of(new AccountTitleSuggestionResponse(
                    "고정자산 후보",
                    buildFixedAssetReason(amount),
                    "FIXED_ASSET",
                    79,
                    true
            ));
        }

        return KEYWORD_RULES.stream()
                .filter(rule -> rule.matches(searchText))
                .sorted(Comparator.comparingInt(KeywordRule::confidence).reversed())
                .limit(3)
                .map(rule -> new AccountTitleSuggestionResponse(
                        rule.accountTitle(),
                        rule.reason(),
                        "KEYWORD",
                        rule.confidence(),
                        false
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CounterpartyAccountRuleResponse> findRules(Long hospitalId) {
        requireHospital(hospitalId);
        return ruleRepository.findByHospital_IdOrderByLastLearnedAtDesc(hospitalId)
                .stream()
                .map(CounterpartyAccountRuleResponse::from)
                .toList();
    }

    @Transactional
    public CounterpartyAccountRuleResponse learn(Long hospitalId, String counterpartyName, String accountTitle) {
        Hospital hospital = requireHospital(hospitalId);
        CounterpartyAccountRule rule = learnInternal(hospital, counterpartyName, accountTitle);
        return CounterpartyAccountRuleResponse.from(rule);
    }

    @Transactional
    public void learnIfPossible(Hospital hospital, String counterpartyName, String accountTitle) {
        if (hospital == null) {
            return;
        }
        learnInternal(hospital, counterpartyName, accountTitle);
    }

    private CounterpartyAccountRule learnInternal(Hospital hospital, String counterpartyName, String accountTitle) {
        String normalizedCounterparty = normalizeDisplay(counterpartyName);
        String normalizedAccountTitle = normalizeDisplay(accountTitle);
        if (normalizedCounterparty.isBlank() || normalizedAccountTitle.isBlank()) {
            return null;
        }

        String counterpartyKey = normalizeKey(normalizedCounterparty);
        CounterpartyAccountRule rule = ruleRepository
                .findByHospital_IdAndCounterpartyKey(hospital.getId(), counterpartyKey)
                .orElseGet(() -> new CounterpartyAccountRule(
                        hospital,
                        normalizedCounterparty,
                        counterpartyKey,
                        normalizedAccountTitle
                ));
        if (rule.getId() != null) {
            rule.learn(normalizedCounterparty, normalizedAccountTitle);
        }
        return ruleRepository.save(rule);
    }

    private Hospital requireHospital(Long hospitalId) {
        return hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new IllegalArgumentException("HOSPITAL_NOT_FOUND"));
    }

    private boolean isFixedAssetCandidate(String counterpartyName, String itemName, BigDecimal amount) {
        if (amount != null && amount.compareTo(FIXED_ASSET_AMOUNT_THRESHOLD) >= 0) {
            return true;
        }
        String searchText = normalizeSearchText(counterpartyName + " " + itemName);
        return FIXED_ASSET_KEYWORDS.stream().anyMatch(searchText::contains);
    }

    private String buildFixedAssetReason(BigDecimal amount) {
        if (amount != null && amount.compareTo(FIXED_ASSET_AMOUNT_THRESHOLD) >= 0) {
            return "금액이 100만원 이상이므로 고정자산 처리 여부를 검토해야 합니다.";
        }
        return "품목 또는 거래처에 장비/기기/비품성 키워드가 있습니다.";
    }

    private String normalizeDisplay(String value) {
        return value == null ? "" : value.trim();
    }

    public static String normalizeKey(String value) {
        String normalized = normalizeSearchText(value);
        return normalized.replaceAll("[^0-9a-z가-힣]", "");
    }

    private static String normalizeSearchText(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFKC)
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ")
                .trim();
    }

    private record KeywordRule(String accountTitle, List<String> keywords, String reason, int confidence) {
        boolean matches(String searchText) {
            return keywords.stream().anyMatch(keyword -> searchText.contains(keyword.toLowerCase(Locale.ROOT)));
        }
    }
}
