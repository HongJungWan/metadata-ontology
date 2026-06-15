package com.hris.metadata.application.pattern;

import com.hris.metadata.domain.pattern.SqlPattern;
import com.hris.metadata.domain.pattern.SqlPatternRepository;
import com.hris.metadata.domain.pattern.vo.SqlPatternId;
import com.hris.metadata.global.exception.BusinessException;
import com.hris.metadata.global.exception.ErrorCode;
import com.hris.metadata.application.pattern.command.DefineSqlPatternCommand;
import com.hris.metadata.application.pattern.dto.response.SqlPatternResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * SQL 패턴 관리 서비스 (CRUD + 소프트 삭제, 응용 서비스).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SqlPatternAdminService {

    private final SqlPatternRepository sqlPatternRepository;

    @Transactional
    public SqlPatternResponse create(DefineSqlPatternCommand command) {
        SqlPattern pattern = SqlPattern.create(UUID.randomUUID(), command.triggerKeywords(),
                command.columnTarget(), command.operator(),
                command.valueTemplate(), command.priority());
        sqlPatternRepository.save(pattern);
        return SqlPatternResponse.from(pattern);
    }

    public List<SqlPatternResponse> getAll() {
        return sqlPatternRepository.findAll().stream().map(SqlPatternResponse::from).toList();
    }

    @Transactional
    public SqlPatternResponse update(UUID sqlPatternId, DefineSqlPatternCommand command) {
        SqlPattern pattern = getOrThrow(sqlPatternId);
        pattern.update(command.triggerKeywords(), command.columnTarget(), command.operator(),
                command.valueTemplate(), command.priority());
        return SqlPatternResponse.from(pattern);
    }

    @Transactional
    public void delete(UUID sqlPatternId) {
        getOrThrow(sqlPatternId).softDelete();
    }

    private SqlPattern getOrThrow(UUID sqlPatternId) {
        return sqlPatternRepository.findById(new SqlPatternId(sqlPatternId))
                .orElseThrow(() -> new BusinessException(ErrorCode.SQL_PATTERN_NOT_FOUND));
    }
}
