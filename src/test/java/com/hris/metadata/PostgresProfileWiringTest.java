package com.hris.metadata;

import com.hris.metadata.domain.term.SynonymRepository;
import com.hris.metadata.infrastructure.persistence.PostgresSynonymRepositoryImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * postgres 프로파일 빈 와이어링 스모크 (pg_trgm 컨테이너 불필요).
 * <p>
 * 실제 pg_trgm 마이그레이션은 H2 에서 못 돌므로 Flyway 를 끄고 datasource 를 H2(PostgreSQL 모드)로
 * 가장한다 — 이 테스트는 <b>프로파일 배타성</b>만 검증한다: Synonym 포트는 pg_trgm 어댑터가 단일
 * 선택되고 기본(H2 Java 폴백) 어댑터는 비활성. (실제 pg_trgm SQL 은 도커 postgres 로 사용자가 검증)
 */
@SpringBootTest(properties = {
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.url=jdbc:h2:mem:meta-postgres-wiring;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@ActiveProfiles("postgres")
class PostgresProfileWiringTest {

    @Autowired
    private ApplicationContext context;

    @Test
    @DisplayName("Synonym 포트는 pg_trgm 어댑터가 단일 선택된다 (H2 Java 폴백 어댑터는 비활성)")
    void synonymAdapterIsPostgresImplementation() {
        assertThat(context.getBean(SynonymRepository.class))
                .isInstanceOf(PostgresSynonymRepositoryImpl.class);
    }
}
