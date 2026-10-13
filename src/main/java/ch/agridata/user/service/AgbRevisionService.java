package ch.agridata.user.service;

import ch.agridata.common.dto.PageResponseDto;
import ch.agridata.common.dto.ResourceQueryDto;
import ch.agridata.user.dto.AgbRevisionDto;
import ch.agridata.user.dto.AgbRevisionOverviewDto;
import ch.agridata.user.mapper.AgbRevisionMapper;
import ch.agridata.user.persistence.AgbRevisionRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;

/**
 * Service for the AgbRevisionEntity. It is used to find the current revision of the agb text
 * and to page through all revisions.
 *
 * @CommentLastReviewed 2026-10-01
 */

@ApplicationScoped
@RequiredArgsConstructor
public class AgbRevisionService {
  private final AgbRevisionMapper agbRevisionMapper;
  private final AgbRevisionRepository agbRevisionRepository;
  private final Clock clock;

  public AgbRevisionDto getCurrentRevision() {
    return agbRevisionMapper.toDto(agbRevisionRepository.findValidRevisionAt(LocalDateTime.now(clock))
        .orElseThrow(() -> new IllegalStateException("No AGB revision is currently valid"))
    );
  }

  public PageResponseDto<AgbRevisionOverviewDto> getAllRevisions(ResourceQueryDto query) {
    var effectiveQuery = (query.sortParams() == null || query.sortParams().isEmpty())
        ? query.toBuilder().sortParams(List.of("-validFrom")).build()
        : query;

    var page = agbRevisionRepository.findPaged(effectiveQuery);
    
    return new PageResponseDto<>(
        page.items().stream().map(agbRevisionMapper::toOverviewDto).toList(),
        page.totalItems(),
        page.totalPages(),
        page.currentPage(),
        page.pageSize()
    );
  }
}
