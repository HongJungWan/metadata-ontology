package com.hris.metadata.infrastructure.config;

import com.hris.metadata.domain.mapping.SchemaMapping;
import com.hris.metadata.domain.mapping.SchemaMappingRepository;
import com.hris.metadata.domain.pattern.PatternOperator;
import com.hris.metadata.domain.pattern.SqlPattern;
import com.hris.metadata.domain.pattern.SqlPatternRepository;
import com.hris.metadata.domain.schema.CodeValue;
import com.hris.metadata.domain.schema.CodeValueRepository;
import com.hris.metadata.domain.schema.SchemaCatalog;
import com.hris.metadata.domain.schema.SchemaCatalogRepository;
import com.hris.metadata.domain.term.Synonym;
import com.hris.metadata.domain.term.SynonymRepository;
import com.hris.metadata.domain.term.SynonymType;
import com.hris.metadata.domain.term.Term;
import com.hris.metadata.domain.term.TermRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 정산(Settlement) 도메인 사전 시드 데이터 (infrastructure 설정).
 * <p>
 * H2 in-memory(local 프로필) 부팅 시 데모/개발용 사전을 적재한다.
 * 운영 프로필에서는 비활성(@Profile("local")) 이며, 이미 데이터가 있으면 건너뛴다.
 */
@Slf4j
@Order(1)
@Component
@Profile("local")
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final TermRepository termRepository;
    private final SynonymRepository synonymRepository;
    private final SchemaCatalogRepository schemaCatalogRepository;
    private final CodeValueRepository codeValueRepository;
    private final SchemaMappingRepository schemaMappingRepository;
    private final SqlPatternRepository sqlPatternRepository;

    @Override
    public void run(String... args) {
        if (termRepository.count() > 0) {
            log.info("Settlement dictionary already seeded — skipping.");
            return;
        }
        log.info("Seeding settlement dictionary (H2 local profile)...");

        Map<String, UUID> termIds = seedTerms();
        seedSynonyms(termIds);
        Map<String, UUID> catalogIds = seedCatalog();
        seedCodeValues(catalogIds.get("settlement_status"));
        seedMappings(termIds, catalogIds);
        seedSqlPatterns();

        log.info("Settlement dictionary seeded: {} terms, {} synonyms, {} catalogs, {} patterns.",
                termRepository.count(), synonymRepository.count(),
                schemaCatalogRepository.count(), sqlPatternRepository.count());
    }

    private Map<String, UUID> seedTerms() {
        Map<String, UUID> ids = new HashMap<>();
        ids.put("정산금액", saveTerm("정산금액", "가맹점에 지급될 정산 금액"));
        ids.put("정산상태", saveTerm("정산상태", "정산 처리 상태 (미정산/정산완료 등)"));
        ids.put("수수료", saveTerm("수수료", "정산 시 차감되는 수수료 금액"));
        ids.put("지급일", saveTerm("지급일", "정산 금액이 가맹점에 지급되는 날짜"));
        ids.put("가맹점", saveTerm("가맹점", "정산 대상 가맹점(머천트)"));
        ids.put("정산주기", saveTerm("정산주기", "정산이 이루어지는 주기 (일/주/월 등)"));
        return ids;
    }

    private UUID saveTerm(String canonicalName, String definition) {
        UUID id = UUID.randomUUID();
        termRepository.save(Term.create(id, canonicalName, "settlement", definition));
        return id;
    }

    private void seedSynonyms(Map<String, UUID> termIds) {
        saveSynonym(termIds.get("정산상태"), "세틀", SynonymType.ABBREVIATION);
        saveSynonym(termIds.get("정산상태"), "settle", SynonymType.KOR_ENG);
        saveSynonym(termIds.get("정산상태"), "미정산", SynonymType.COLLOQUIAL);
        saveSynonym(termIds.get("가맹점"), "머천트", SynonymType.COLLOQUIAL);
        saveSynonym(termIds.get("가맹점"), "상점", SynonymType.COLLOQUIAL);
        saveSynonym(termIds.get("수수료"), "fee", SynonymType.KOR_ENG);
        saveSynonym(termIds.get("정산금액"), "정산액", SynonymType.ABBREVIATION);
    }

    private void saveSynonym(UUID termId, String surface, SynonymType type) {
        synonymRepository.save(Synonym.create(UUID.randomUUID(), termId, surface, type));
    }

    private Map<String, UUID> seedCatalog() {
        Map<String, UUID> ids = new HashMap<>();
        ids.put("settlement_amount", saveCatalog("settlement_amount", "numeric", "정산 금액 (원)"));
        ids.put("settlement_status", saveCatalog("settlement_status", "varchar", "정산 상태 코드"));
        ids.put("fee_amount", saveCatalog("fee_amount", "numeric", "수수료 금액 (원)"));
        ids.put("payout_date", saveCatalog("payout_date", "date", "지급일"));
        ids.put("merchant_id", saveCatalog("merchant_id", "varchar", "가맹점 식별자"));
        ids.put("settlement_cycle", saveCatalog("settlement_cycle", "varchar", "정산 주기"));
        return ids;
    }

    private UUID saveCatalog(String column, String dataType, String description) {
        UUID id = UUID.randomUUID();
        schemaCatalogRepository.save(SchemaCatalog.create(
                id, "settlement", column, dataType, description, "redshift"));
        return id;
    }

    private void seedCodeValues(UUID statusCatalogId) {
        saveCodeValue(statusCatalogId, "PENDING", "미정산", "미정산,대기");
        saveCodeValue(statusCatalogId, "SETTLED", "정산완료", "정산완료,완료");
        saveCodeValue(statusCatalogId, "HOLD", "보류", "보류,홀드");
        saveCodeValue(statusCatalogId, "CANCELED", "취소", "취소,캔슬");
    }

    private void saveCodeValue(UUID catalogId, String code, String label, String synonyms) {
        codeValueRepository.save(CodeValue.create(UUID.randomUUID(), catalogId, code, label, synonyms));
    }

    private void seedMappings(Map<String, UUID> termIds, Map<String, UUID> catalogIds) {
        saveMapping(termIds.get("정산금액"), catalogIds.get("settlement_amount"), "DIRECT", null);
        saveMapping(termIds.get("정산상태"), catalogIds.get("settlement_status"), "DIRECT", null);
        saveMapping(termIds.get("수수료"), catalogIds.get("fee_amount"), "DIRECT", null);
        saveMapping(termIds.get("지급일"), catalogIds.get("payout_date"), "DIRECT", null);
        saveMapping(termIds.get("가맹점"), catalogIds.get("merchant_id"), "DIRECT", null);
        saveMapping(termIds.get("정산주기"), catalogIds.get("settlement_cycle"), "DIRECT", null);
    }

    private void saveMapping(UUID termId, UUID catalogId, String mappingType, String codeValueRule) {
        schemaMappingRepository.save(SchemaMapping.create(
                UUID.randomUUID(), termId, catalogId, mappingType, codeValueRule));
    }

    private void seedSqlPatterns() {
        savePattern("미정산", "settlement_status", PatternOperator.EQ, "PENDING", 1);
        savePattern("정산완료", "settlement_status", PatternOperator.EQ, "SETTLED", 1);
        savePattern("수수료 높은", "fee_amount", PatternOperator.GTE, ":feeThreshold", 2);
        savePattern("지난달 정산", "payout_date", PatternOperator.BETWEEN, ":timeRange", 2);
    }

    private void savePattern(String keywords, String column, PatternOperator operator,
                             String valueTemplate, int priority) {
        sqlPatternRepository.save(SqlPattern.create(
                UUID.randomUUID(), keywords, column, operator, valueTemplate, priority));
    }
}
