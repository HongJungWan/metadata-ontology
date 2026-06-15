package com.hris.metadata.infrastructure.config;

import com.hris.metadata.domain.expand.ExpansionService;
import com.hris.metadata.domain.mapping.MappingComparator;
import com.hris.metadata.domain.normalize.NormalizationService;
import com.hris.metadata.domain.pattern.SqlPatternService;
import com.hris.metadata.domain.pattern.SqlPatternRepository;
import com.hris.metadata.domain.term.SynonymRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 도메인 서비스 빈 등록.
 * <p>
 * 도메인 서비스({@code @DomainService})는 Spring 스테레오타입을 갖지 않으므로(도메인 순수성)
 * 여기서 명시적으로 빈으로 등록한다. 의존 포트(리포지토리)는 infrastructure 어댑터 빈이 주입된다.
 */
@Configuration
public class DomainServiceConfig {

    @Bean
    public NormalizationService normalizationService() {
        return new NormalizationService();
    }

    @Bean
    public ExpansionService expansionService(
            SynonymRepository synonymRepository,
            @Value("${expansion.fuzzy.threshold:0.3}") double fuzzyThreshold) {
        return new ExpansionService(synonymRepository, fuzzyThreshold);
    }

    @Bean
    public SqlPatternService sqlPatternService(SqlPatternRepository sqlPatternRepository) {
        return new SqlPatternService(sqlPatternRepository);
    }

    @Bean
    public MappingComparator mappingComparator() {
        return new MappingComparator();
    }
}
