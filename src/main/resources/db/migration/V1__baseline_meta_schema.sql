-- ===================================================================
-- V1 baseline — postgres 프로파일 전용 (운영 PostgreSQL meta 스키마)
-- 6개 애그리거트 엔티티 + BaseEntity 감사필드를 PostgreSQL 타입으로 미러한다.
-- 이 프로파일은 ddl-auto: none — Flyway 가 스키마를 소유한다(Hibernate DDL/검증 불개입).
-- H2(local, create-drop + DataSeeder) 경로와 무관(Flyway 는 postgres 프로파일에서만 활성).
-- PK 는 애플리케이션 생성 UUID. 감사 created_at 은 JPA Auditing 이 채우나 DEFAULT now() 로 안전망.
-- ===================================================================

CREATE SCHEMA IF NOT EXISTS meta;

CREATE TABLE meta.term (
    term_id        uuid          PRIMARY KEY,
    canonical_name varchar(200)  NOT NULL,
    domain         varchar(100),
    definition     varchar(1000),
    status         varchar(20)   NOT NULL,
    created_at     timestamptz   NOT NULL DEFAULT now(),
    updated_at     timestamptz,
    deleted_at     timestamptz
);
CREATE INDEX idx_term_canonical_name ON meta.term (canonical_name);

CREATE TABLE meta.synonym (
    synonym_id uuid         PRIMARY KEY,
    term_id    uuid         NOT NULL,
    surface    varchar(200) NOT NULL,
    type       varchar(20)  NOT NULL,
    created_at timestamptz  NOT NULL DEFAULT now(),
    updated_at timestamptz,
    deleted_at timestamptz
);
CREATE INDEX idx_synonym_surface ON meta.synonym (surface);
CREATE INDEX idx_synonym_term_id ON meta.synonym (term_id);

CREATE TABLE meta.schema_catalog (
    schema_catalog_id uuid         PRIMARY KEY,
    physical_table    varchar(200) NOT NULL,
    physical_column   varchar(200) NOT NULL,
    data_type         varchar(100),
    description       varchar(1000),
    source_system     varchar(100),
    created_at        timestamptz  NOT NULL DEFAULT now(),
    updated_at        timestamptz,
    deleted_at        timestamptz
);
CREATE INDEX idx_schema_catalog_table_column ON meta.schema_catalog (physical_table, physical_column);

CREATE TABLE meta.code_value (
    code_value_id     uuid         PRIMARY KEY,
    schema_catalog_id uuid         NOT NULL,
    code              varchar(100) NOT NULL,
    label             varchar(200),
    synonyms          varchar(500),
    created_at        timestamptz  NOT NULL DEFAULT now(),
    updated_at        timestamptz,
    deleted_at        timestamptz
);
CREATE INDEX idx_code_value_catalog_id ON meta.code_value (schema_catalog_id);

CREATE TABLE meta.schema_mapping (
    schema_mapping_id uuid         PRIMARY KEY,
    term_id           uuid         NOT NULL,
    schema_catalog_id uuid         NOT NULL,
    mapping_type      varchar(50),
    code_value_rule   varchar(200),
    created_at        timestamptz  NOT NULL DEFAULT now(),
    updated_at        timestamptz,
    deleted_at        timestamptz
);
CREATE INDEX idx_schema_mapping_term_id ON meta.schema_mapping (term_id);
CREATE INDEX idx_schema_mapping_catalog_id ON meta.schema_mapping (schema_catalog_id);

CREATE TABLE meta.sql_pattern (
    sql_pattern_id   uuid         PRIMARY KEY,
    trigger_keywords varchar(500) NOT NULL,
    column_target    varchar(200) NOT NULL,
    operator         varchar(20)  NOT NULL,
    value_template   varchar(500),
    priority         integer      NOT NULL,
    created_at       timestamptz  NOT NULL DEFAULT now(),
    updated_at       timestamptz,
    deleted_at       timestamptz
);
