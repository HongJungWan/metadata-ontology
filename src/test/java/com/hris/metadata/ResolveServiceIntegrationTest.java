package com.hris.metadata;

import com.hris.metadata.application.resolve.ResolveService;
import com.hris.metadata.application.resolve.dto.response.ResolveResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * /resolve 엔드투엔드 통합 테스트 (H2 + DataSeeder).
 * <p>
 * SchemaMapping QueryDSL 조인, CodeValue 엔티티-조인 프로젝션(N+1 제거), "미정산"→PENDING 코드값
 * 보강을 시드 데이터에 대해 한 번에 검증한다.
 */
@SpringBootTest
@ActiveProfiles("local")
class ResolveServiceIntegrationTest {

    @Autowired
    private ResolveService resolveService;

    @Test
    @DisplayName("'미정산' → settlement_status 컬럼에 codeValue=PENDING, 기간 없음")
    void resolveCodeValueEnrichment() {
        ResolveResponse response = resolveService.resolve("미정산", LocalDate.of(2026, 6, 7));

        assertThat(response.getTimeRange()).isNull();
        assertThat(response.getColumnMappings())
                .anySatisfy(mapping -> {
                    assertThat(mapping.getPhysicalColumn()).isEqualTo("settlement_status");
                    assertThat(mapping.getCodeValue()).isEqualTo("PENDING");
                });
    }

    @Test
    @DisplayName("'미정산 가맹점 지난달' → 기간 2026-05-01~2026-05-31, 용어 해석·컬럼 매핑 존재")
    void resolveWithTimeRange() {
        ResolveResponse response = resolveService.resolve("미정산 가맹점 지난달", LocalDate.of(2026, 6, 7));

        assertThat(response.getTimeRange()).isNotNull();
        assertThat(response.getTimeRange().from()).isEqualTo(LocalDate.of(2026, 5, 1));
        assertThat(response.getTimeRange().to()).isEqualTo(LocalDate.of(2026, 5, 31));
        assertThat(response.getTerms()).isNotEmpty();
        assertThat(response.getColumnMappings()).isNotEmpty();
    }
}
