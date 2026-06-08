package com.hris.metadata.domain.expand;

import com.hris.metadata.domain.term.SynonymMatch;
import com.hris.metadata.domain.term.SynonymRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;

/**
 * ExpansionService 동의어 확장 단위 테스트 (SynonymRepository 모킹).
 */
@ExtendWith(MockitoExtension.class)
class ExpansionServiceTest {

    @Mock
    private SynonymRepository synonymRepository;

    @InjectMocks
    private ExpansionService expansionService;

    @Test
    @DisplayName("동의어 토큰은 표준 용어로 치환되고, 사전에 없는 토큰은 그대로 유지된다")
    void expandsSynonyms() {
        lenient().when(synonymRepository.findBySurfaceWithTerm("세틀"))
                .thenReturn(Optional.of(new SynonymMatch("세틀", "정산상태")));
        lenient().when(synonymRepository.findBySurfaceWithTerm("머천트"))
                .thenReturn(Optional.of(new SynonymMatch("머천트", "가맹점")));
        lenient().when(synonymRepository.findBySurfaceWithTerm("지난달"))
                .thenReturn(Optional.empty());

        ExpansionResult result = expansionService.expand("세틀 머천트 지난달");

        assertThat(result.getExpandedQuery()).isEqualTo("정산상태 가맹점 지난달");
        assertThat(result.getExpansions()).hasSize(2);
        assertThat(result.getExpansions())
                .extracting(ExpansionResult.TokenExpansion::getSurface)
                .containsExactly("세틀", "머천트");
        assertThat(result.getExpansions())
                .extracting(ExpansionResult.TokenExpansion::getCanonical)
                .containsExactly("정산상태", "가맹점");
    }

    @Test
    @DisplayName("동의어가 하나도 없으면 원본 질의가 그대로 반환된다")
    void noSynonyms() {
        lenient().when(synonymRepository.findBySurfaceWithTerm("정산금액"))
                .thenReturn(Optional.empty());

        ExpansionResult result = expansionService.expand("정산금액");

        assertThat(result.getExpandedQuery()).isEqualTo("정산금액");
        assertThat(result.getExpansions()).isEmpty();
    }

    @Test
    @DisplayName("빈 질의는 빈 확장 결과를 반환한다")
    void blankQuery() {
        ExpansionResult result = expansionService.expand("");

        assertThat(result.getExpandedQuery()).isEmpty();
        assertThat(result.getExpansions()).isEmpty();
    }
}
