package ch.agridata.user.service;

import static ch.agridata.auditing.api.ActionEnum.AGB_PUBLISHED;
import static ch.agridata.auditing.api.EntityTypeEnum.AGB_REVISION;
import static ch.agridata.common.utils.AuthenticationUtil.CONSUMER_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.PROVIDER_ROLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import ch.agridata.auditing.api.AuditingApi;
import ch.agridata.common.dto.PageResponseDto;
import ch.agridata.common.dto.ResourceQueryDto;
import ch.agridata.common.exceptions.AgbVersionAlreadyExistsException;
import ch.agridata.common.exceptions.AgbVersionNotNewerException;
import ch.agridata.user.dto.AgbRevisionDto;
import ch.agridata.user.dto.AgbRevisionSummaryDto;
import ch.agridata.user.dto.AgbTextDto;
import ch.agridata.user.dto.CreateAgbRevisionDto;
import ch.agridata.user.mapper.AgbRevisionMapper;
import ch.agridata.user.persistence.AgbRevisionEntity;
import ch.agridata.user.persistence.AgbRevisionRepository;
import ch.agridata.user.persistence.UserRepository;
import jakarta.ws.rs.NotFoundException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AgbRevisionServiceTest {
  private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-07-16T10:00:00Z"), ZoneId.of("Europe/Zurich"));

  @Mock
  AgbRevisionMapper agbRevisionMapper;
  @Mock
  AgbRevisionRepository agbRevisionRepository;
  @Mock
  UserRepository userRepository;
  @Mock
  AuditingApi auditingApi;

  AgbRevisionService agbRevisionService;

  @BeforeEach
  void setUp() {
    agbRevisionService = new AgbRevisionService(agbRevisionMapper, agbRevisionRepository, userRepository, auditingApi, FIXED_CLOCK);
  }

  @Test
  void givenNewVersionWithConsent_whenCreateRevision_thenCloseOpenRevisionInsertNewAndLog() {
    var now = LocalDateTime.now(FIXED_CLOCK);
    var enforceFrom = now.toLocalDate().plusDays(10).atStartOfDay();
    var createDto = new CreateAgbRevisionDto("2.0", enforceFrom, new AgbTextDto("de", "fr", "it"));
    var open = AgbRevisionEntity.builder().version("1.4").build();
    var created = new AgbRevisionEntity();
    var detailDto = mock(AgbRevisionDto.class);
    when(agbRevisionRepository.existsByVersion("2.0")).thenReturn(false);
    when(agbRevisionRepository.findOpenRevision()).thenReturn(Optional.of(open));
    when(agbRevisionMapper.toEntity(createDto)).thenReturn(created);
    when(agbRevisionMapper.toDto(created)).thenReturn(detailDto);

    var result = agbRevisionService.createRevision(createDto);

    assertThat(result).isEqualTo(detailDto);
    assertThat(open.getValidTo()).isEqualTo(now);
    assertThat(created.getValidFrom()).isEqualTo(now);
    assertThat(created.getId()).isNotNull();
    verify(agbRevisionRepository).persist(created);
    verify(userRepository).bringForwardEnforceAgbAcceptanceFrom(enforceFrom, CONSUMER_ROLE, PROVIDER_ROLE);
    verify(auditingApi).logUserAction(AGB_PUBLISHED, AGB_REVISION, created.getId());
  }

  @Test
  void givenNewVersionWithoutConsent_whenCreateRevision_thenUsersAreNotUpdated() {
    var createDto = new CreateAgbRevisionDto("2.0", null, new AgbTextDto("de", "fr", "it"));
    when(agbRevisionRepository.findOpenRevision()).thenReturn(Optional.empty());
    when(agbRevisionMapper.toEntity(createDto)).thenReturn(new AgbRevisionEntity());

    agbRevisionService.createRevision(createDto);

    verifyNoInteractions(userRepository);
  }

  @Test
  void givenExistingVersion_whenCreateRevision_thenThrowAndDoNotPersist() {
    var createDto = new CreateAgbRevisionDto("1.4", null, new AgbTextDto("de", "fr", "it"));
    when(agbRevisionRepository.existsByVersion("1.4")).thenReturn(true);

    assertThatThrownBy(() -> agbRevisionService.createRevision(createDto)).isInstanceOf(AgbVersionAlreadyExistsException.class);
    verify(agbRevisionRepository, never()).persist(any(AgbRevisionEntity.class));
  }

  @ParameterizedTest
  @CsvSource({"1.0, 1.2", "1.2, 2.0", "1.9, 1.10", "1.2, 1.2.0", "2.0, 2.0"})
  void givenVersionNotNewerThanOpenRevision_whenCreateRevision_thenThrowAndDoNotPersist(String publishedVersion, String currentVersion) {
    var createDto = new CreateAgbRevisionDto(publishedVersion, null, new AgbTextDto("de", "fr", "it"));
    var open = AgbRevisionEntity.builder().version(currentVersion).build();
    when(agbRevisionRepository.findOpenRevision()).thenReturn(Optional.of(open));

    assertThatThrownBy(() -> agbRevisionService.createRevision(createDto)).isInstanceOf(AgbVersionNotNewerException.class);
    verify(agbRevisionRepository, never()).persist(any(AgbRevisionEntity.class));
  }

  @Test
  void givenEnforceDateOnEarlierDay_whenCreateRevision_thenThrowIllegalArgument() {
    var lastMinuteOfYesterday = LocalDateTime.now(FIXED_CLOCK).toLocalDate().atStartOfDay().minusNanos(1);
    var createDto = new CreateAgbRevisionDto("2.0", lastMinuteOfYesterday, new AgbTextDto("de", "fr", "it"));

    assertThatThrownBy(() -> agbRevisionService.createRevision(createDto)).isInstanceOf(IllegalArgumentException.class);
    verify(agbRevisionRepository, never()).persist(any(AgbRevisionEntity.class));
  }

  @ParameterizedTest
  @ValueSource(longs = {0, 1, 60})
  void givenEnforceTimeEarlierToday_whenCreateRevision_thenClampToNow(long minutesInThePast) {
    var now = LocalDateTime.now(FIXED_CLOCK);
    var createDto = new CreateAgbRevisionDto("2.0", now.minusMinutes(minutesInThePast), new AgbTextDto("de", "fr", "it"));
    var created = new AgbRevisionEntity();
    when(agbRevisionMapper.toEntity(createDto)).thenReturn(created);

    agbRevisionService.createRevision(createDto);

    assertThat(created.getEnforceConsentFrom()).isEqualTo(now);
    verify(userRepository).bringForwardEnforceAgbAcceptanceFrom(now, CONSUMER_ROLE, PROVIDER_ROLE);
  }

  @Test
  void givenEnforceDateLaterToday_whenCreateRevision_thenKeepRequestedDate() {
    var later = LocalDateTime.now(FIXED_CLOCK).plusMinutes(5);
    var createDto = new CreateAgbRevisionDto("2.0", later, new AgbTextDto("de", "fr", "it"));
    var created = new AgbRevisionEntity();
    when(agbRevisionMapper.toEntity(createDto)).thenReturn(created);

    agbRevisionService.createRevision(createDto);

    assertThat(created.getEnforceConsentFrom()).isEqualTo(later);
    verify(userRepository).bringForwardEnforceAgbAcceptanceFrom(later, CONSUMER_ROLE, PROVIDER_ROLE);
  }

  @Test
  void givenNewerVersionAndNoOpenRevision_whenCreateRevision_thenPersist() {
    var createDto = new CreateAgbRevisionDto("1.0", null, new AgbTextDto("de", "fr", "it"));
    var created = new AgbRevisionEntity();
    when(agbRevisionRepository.findOpenRevision()).thenReturn(Optional.empty());
    when(agbRevisionMapper.toEntity(createDto)).thenReturn(created);

    agbRevisionService.createRevision(createDto);

    verify(agbRevisionRepository).persist(created);
  }

  @ParameterizedTest
  @CsvSource({"1.5, 1.4", "1.10, 1.9", "2.0, 1.99", "1.2.1, 1.2"})
  void givenNewerVersion_whenCreateRevision_thenPersist(String publishedVersion, String currentVersion) {
    var createDto = new CreateAgbRevisionDto(publishedVersion, null, new AgbTextDto("de", "fr", "it"));
    var created = new AgbRevisionEntity();
    when(agbRevisionRepository.findOpenRevision()).thenReturn(Optional.of(AgbRevisionEntity.builder().version(currentVersion).build()));
    when(agbRevisionMapper.toEntity(createDto)).thenReturn(created);

    agbRevisionService.createRevision(createDto);

    verify(agbRevisionRepository).persist(created);
  }

  @Test
  void givenRevisionExists_whenGetRevision_thenReturnMappedDto() {
    var id = UUID.randomUUID();
    var entity = new AgbRevisionEntity();
    var dto = mock(AgbRevisionDto.class);
    when(agbRevisionRepository.findByIdOptional(id)).thenReturn(Optional.of(entity));
    when(agbRevisionMapper.toDto(entity)).thenReturn(dto);

    assertThat(agbRevisionService.getRevision(id)).isEqualTo(dto);
  }

  @Test
  void givenUnknownId_whenGetRevision_thenThrowNotFound() {
    var id = UUID.randomUUID();
    when(agbRevisionRepository.findByIdOptional(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> agbRevisionService.getRevision(id)).isInstanceOf(NotFoundException.class);
  }

  @Test
  void givenCurrentRevisionExists_whenGetCurrentRevision_thenReturnMappedDto() {
    LocalDateTime dateTimeNow = LocalDateTime.now(FIXED_CLOCK);
    AgbRevisionEntity agbRevisionEntity = mock(AgbRevisionEntity.class);
    AgbRevisionDto agbRevisionDto = mock(AgbRevisionDto.class);
    when(agbRevisionMapper.toDto(agbRevisionEntity)).thenReturn(agbRevisionDto);
    when(agbRevisionRepository.findValidRevisionAt(dateTimeNow)).thenReturn(Optional.of(agbRevisionEntity));

    AgbRevisionDto result = agbRevisionService.getCurrentRevision();
    assertThat(result).isEqualTo(agbRevisionDto);
  }

  @Test
  void givenNoCurrentRevision_whenGetCurrentRevision_thenThrowIllegalState() {
    LocalDateTime dateTimeNow = LocalDateTime.now(FIXED_CLOCK);
    when(agbRevisionRepository.findValidRevisionAt(dateTimeNow)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> agbRevisionService.getCurrentRevision()).isInstanceOf(IllegalStateException.class)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("No AGB revision");
  }

  @Test
  void givenRevisionsExist_whenGetAllRevisions_thenReturnMappedPage() {
    AgbRevisionEntity agbRevisionEntity = mock(AgbRevisionEntity.class);
    AgbRevisionSummaryDto overviewDto = mock(AgbRevisionSummaryDto.class);
    var query = ResourceQueryDto.builder().page(0).size(20).sortParams(List.of("version")).build();
    when(agbRevisionRepository.findPaged(query)).thenReturn(new PageResponseDto<>(List.of(agbRevisionEntity), 1L, 1, 0, 20));
    when(agbRevisionMapper.toSummaryDto(agbRevisionEntity)).thenReturn(overviewDto);

    var result = agbRevisionService.getAllRevisions(query);

    assertThat(result.items()).containsExactly(overviewDto);
    assertThat(result.totalItems()).isEqualTo(1L);
  }

  @Test
  void givenNoSortParams_whenGetAllRevisions_thenSortByValidFromDescending() {
    var query = ResourceQueryDto.builder().page(1).size(5).build();
    when(agbRevisionRepository.findPaged(any())).thenReturn(new PageResponseDto<>(List.of(), 0L, 0, 1, 5));

    agbRevisionService.getAllRevisions(query);

    var captor = ArgumentCaptor.forClass(ResourceQueryDto.class);
    verify(agbRevisionRepository).findPaged(captor.capture());
    assertThat(captor.getValue().sortParams()).isEqualTo(List.of("-validFrom"));
    assertThat(captor.getValue().page()).isEqualTo(1);
    assertThat(captor.getValue().size()).isEqualTo(5);
  }
}
