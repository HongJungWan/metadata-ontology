package com.hris.metadata;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 경량 온톨로지 기반 메타데이터 레이어 애플리케이션 진입점.
 * <p>
 * 도메인 용어와 물리 스키마를 잇는 공유 메타데이터 계층(정산 도메인)을 제공한다.
 * 자연어 질의를 물리 컬럼·코드값으로 매핑하고, LLM에 줄 스키마 설명을 생성한다.
 */
@SpringBootApplication
public class MetadataOntologyApplication {

    public static void main(String[] args) {
        SpringApplication.run(MetadataOntologyApplication.class, args);
    }

}
