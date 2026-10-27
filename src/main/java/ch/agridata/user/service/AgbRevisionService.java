package ch.agridata.user.service;

import static ch.agridata.auditing.api.ActionEnum.AGB_PUBLISHED;
import static ch.agridata.auditing.api.EntityTypeEnum.AGB_REVISION;
import static ch.agridata.common.utils.AuthenticationUtil.CONSUMER_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.PROVIDER_ROLE;

import ch.agridata.auditing.api.AuditingApi;
import ch.agridata.common.dto.PageResponseDto;
import ch.agridata.common.dto.ResourceQueryDto;
import ch.agridata.common.exceptions.AgbVersionAlreadyExistsException;
import ch.agridata.common.exceptions.AgbVersionNotNewerException;
import ch.agridata.user.dto.AgbRevisionDto;
import ch.agridata.user.dto.AgbRevisionSummaryDto;
import ch.agridata.user.dto.CreateAgbRevisionDto;
import ch.agridata.user.mapper.AgbRevisionMapper;
import ch.agridata.user.persistence.AgbRevisionEntity;
import ch.agridata.user.persistence.AgbRevisionRepository;
import ch.agridata.user.persistence.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for the AgbRevisionEntity. It is used to find the current revision of the agb text,
 * to page through all revisions and to publish new revisions.
 *
 * @CommentLastReviewed 2026-10-27
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class AgbRevisionService {
  private final AgbRevisionMapper agbRevisionMapper;
  private final AgbRevisionRepository agbRevisionRepository;
  private final UserRepository userRepository;
  private final AuditingApi auditingApi;
  private final Clock clock;

  public AgbRevisionDto getCurrentRevision() {
    return agbRevisionMapper.toDto(agbRevisionRepository.findValidRevisionAt(LocalDateTime.now(clock))
        .orElseThrow(() -> new IllegalStateException("No AGB revision is currently valid"))
    );
  }

  public Map<UUID, String> getVersionsById() {
    return agbRevisionRepository.listAll().stream()
        .collect(Collectors.toMap(AgbRevisionEntity::getId, AgbRevisionEntity::getVersion));
  }

  public PageResponseDto<AgbRevisionSummaryDto> getAllRevisions(ResourceQueryDto query) {
    var effectiveQuery = (query.sortParams() == null || query.sortParams().isEmpty())
        ? query.toBuilder().sortParams(List.of("-validFrom")).build()
        : query;

    var page = agbRevisionRepository.findPaged(effectiveQuery);

    return new PageResponseDto<>(
        page.items().stream().map(agbRevisionMapper::toSummaryDto).toList(),
        page.totalItems(),
        page.totalPages(),
        page.currentPage(),
        page.pageSize()
    );
  }

  public AgbRevisionDto getRevision(UUID id) {
    return agbRevisionRepository.findByIdOptional(id)
        .map(agbRevisionMapper::toDto)
        .orElseThrow(() -> new NotFoundException("AGB revision not found: " + id));
  }

  /**
   * Publishes a new revision: closes the currently open revision, inserts the new one valid from now and, if consent is required,
   * brings forward the enforcement date of all data providers and data consumers.
   */
  @Transactional
  public AgbRevisionDto createRevision(CreateAgbRevisionDto createDto) {
    if (agbRevisionRepository.existsByVersion(createDto.version())) {
      throw new AgbVersionAlreadyExistsException("AGB version already exists: " + createDto.version());
    }

    var openRevision = agbRevisionRepository.findOpenRevision();
    if (openRevision.isPresent() && compareVersions(createDto.version(), openRevision.get().getVersion()) <= 0) {
      throw new AgbVersionNotNewerException(
          "AGB version %s is not newer than the current version %s".formatted(createDto.version(), openRevision.get().getVersion())
      );
    }

    var now = LocalDateTime.now(clock);
    // validTo is exclusive, so the old revision ends exactly when the new one starts
    openRevision.ifPresent(open -> open.setValidTo(now));
    // Hibernate executes inserts before updates within a flush. Flushing here makes the update run first, otherwise the insert below
    // would violate the non-overlap constraint of the still open old revision.
    agbRevisionRepository.flush();

    var newRevision = agbRevisionMapper.toEntity(createDto);
    var enforceConsentFrom = resolveEnforceConsentFrom(createDto.enforceConsentFrom(), now);
    newRevision.setId(UUID.randomUUID());
    newRevision.setValidFrom(now);
    newRevision.setEnforceConsentFrom(enforceConsentFrom);
    agbRevisionRepository.persist(newRevision);
    log.info(
        "Published new AGB revision {} (version {}, valid from {})",
        newRevision.getId(),
        newRevision.getVersion(),
        newRevision.getValidFrom()
    );

    if (enforceConsentFrom != null) {
      userRepository.bringForwardEnforceAgbAcceptanceFrom(enforceConsentFrom, CONSUMER_ROLE, PROVIDER_ROLE);
      log.info(
          "Brought forward enforceAgbAcceptanceFrom to {} for agb version {} to all data providers and data consumers "
              + "not already having an earlier date",
          enforceConsentFrom,
          newRevision.getVersion()
      );
    }

    auditingApi.logUserAction(AGB_PUBLISHED, AGB_REVISION, newRevision.getId());
    return agbRevisionMapper.toDto(newRevision);
  }

  /**
   * Days in the past are rejected. A time earlier today is clamped to now, so an admin who picked a time while editing the text can
   * still publish it later the same day.
   */
  private static LocalDateTime resolveEnforceConsentFrom(LocalDateTime requested, LocalDateTime now) {
    if (requested == null) {
      return null;
    }
    if (requested.toLocalDate().isBefore(now.toLocalDate())) {
      throw new IllegalArgumentException("enforceConsentFrom must not be before today");
    }
    return requested.isBefore(now) ? now : requested;
  }

  /**
   * Compares dot separated numeric versions segment by segment, so that 1.10 is newer than 1.9. Missing segments count as 0.
   */
  private static int compareVersions(String first, String second) {
    var firstSegments = first.split("\\.");
    var secondSegments = second.split("\\.");
    for (var i = 0; i < Math.max(firstSegments.length, secondSegments.length); i++) {
      var firstNumber = i < firstSegments.length ? Integer.parseInt(firstSegments[i]) : 0;
      var secondNumber = i < secondSegments.length ? Integer.parseInt(secondSegments[i]) : 0;
      if (firstNumber != secondNumber) {
        return Integer.compare(firstNumber, secondNumber);
      }
    }
    return 0;
  }
}
