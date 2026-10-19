package ch.agridata.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.agridata.common.dto.PageResponseDto;
import ch.agridata.common.dto.ResourceQueryDto;
import ch.agridata.user.dto.AgbRevisionDto;
import ch.agridata.user.dto.AgbRevisionOverviewDto;
import ch.agridata.user.mapper.AgbRevisionMapper;
import ch.agridata.user.persistence.AgbRevisionEntity;
import ch.agridata.user.persistence.AgbRevisionRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

  @Test
  void givenRevisionsExist_whenGetAllRevisions_thenReturnMappedPage() {
    AgbRevisionEntity agbRevisionEntity = mock(AgbRevisionEntity.class);
    AgbRevisionOverviewDto overviewDto = mock(AgbRevisionOverviewDto.class);
    var query = ResourceQueryDto.builder().page(0).size(20).sortParams(List.of("version")).build();
    when(agbRevisionRepository.findPaged(query)).thenReturn(new PageResponseDto<>(List.of(agbRevisionEntity), 1L, 1, 0, 20));
    when(agbRevisionMapper.toOverviewDto(agbRevisionEntity)).thenReturn(overviewDto);

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
