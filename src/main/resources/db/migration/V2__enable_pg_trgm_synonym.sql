-- ===================================================================
-- V2 — pg_trgm 확장 + 동의어 표면형 트라이그램 GIN 인덱스 (postgres 프로파일 전용)
-- 퍼지 매칭(PostgresSynonymRepositoryImpl.findBySurfaceFuzzy)의 similarity() 검색을 가속한다.
-- 임베딩(벡터) 대신 어휘 유사도를 택한 비대칭 설계의 MO 측 인프라 —
--   짧은 surface 토큰의 오타·띄어쓰기·근접 OOV 를 설명 가능하게 회복(의미 패러프레이즈는 KS 본문 벡터 담당).
-- 주의(db-migration 규칙): 적용된 이 파일은 수정 금지 — 변경은 새 V{n}__ 로 추가한다.
-- ===================================================================

CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX idx_synonym_surface_trgm ON meta.synonym USING gin (surface gin_trgm_ops);
