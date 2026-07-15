package ch.agridata.user.service;

import ch.agridata.user.dto.AgbRevisionDto;
import ch.agridata.user.mapper.AgbRevisionMapper;
import ch.agridata.user.persistence.AgbRevisionRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;

/**
 * Service for the AgbRevisionEntity. It is used to find the current revision of the agb text.
 *
 * @CommentLastReviewed 2026-07-16
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
}
