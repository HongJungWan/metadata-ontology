package com.hris.metadata.shared.ddd;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Published Language 마커 (전략적 설계 — Open Host Service 계약).
 * <p>
 * metadata-ontology 는 knowledge-search 등 다운스트림에 대해 <b>Open Host Service(OHS)</b>다.
 * 이 마커가 붙은 타입은 그 <b>발행 언어(Published Language)</b> — 즉 외부와 합의된 공개 계약이다.
 * 필드 추가/이름변경/제거는 다운스트림(특히 knowledge-search 의 {@code MetadataResolveResult})과의
 * 협의·동기화가 필요하다. context-map: {@code .claude/docs/context-map.md}.
 * <p>
 * 문서/의도 표기용 마커다(런타임 동작 없음). {@code @AggregateRoot}·{@code @Subdomain} 과 같은 결.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PublishedLanguage {
}
