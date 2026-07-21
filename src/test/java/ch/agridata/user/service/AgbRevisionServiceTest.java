package ch.agridata.user.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ch.agridata.user.dto.AgbRevisionDto;
import ch.agridata.user.mapper.AgbRevisionMapper;
import ch.agridata.user.persistence.AgbRevisionEntity;
import ch.agridata.user.persistence.AgbRevisionRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AgbRevisionServiceTest {
  private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-07-16T00:00:00Z"), ZoneId.of("Europe/Zurich"));

  @Mock
  AgbRevisionMapper agbRevisionMapper;
  @Mock
  AgbRevisionRepository agbRevisionRepository;

  AgbRevisionService agbRevisionService;

  @BeforeEach
  void setUp() {
    agbRevisionService = new AgbRevisionService(agbRevisionMapper, agbRevisionRepository, FIXED_CLOCK);
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
}
