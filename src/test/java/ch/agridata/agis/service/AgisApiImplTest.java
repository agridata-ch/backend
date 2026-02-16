package ch.agridata.agis.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.agridata.agis.dto.AgisFarmOwnershipDto;
import ch.agridata.agis.dto.AgisFarmType;
import ch.agridata.agis.dto.AgisPersonFarmResponseType;
import ch.agridata.agis.dto.AgisPersonFarmTreeType;
import ch.agridata.agis.dto.AgisRegisterMutationDataRequest;
import ch.agridata.agis.dto.AgisRelevantFarms;
import ch.agridata.agis.dto.AgisResultOffsetType;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AgisApiImplTest {

  @Mock
  AgisRegisterApiRestClient agisRegisterApiRestClient;

  @InjectMocks
  AgisApiImpl agisApiImpl;

  // -------------------------
  // fetchFarmMutations / chunking
  // -------------------------

  @Test
  void givenTwoChunks_whenFetchFarmMutations_thenAggregatesAllFarmsAndUsesModifiedFarmParametersAndOffsets() {
    // given
    var from = LocalDate.of(2026, 2, 17);
    var to = LocalDate.of(2026, 2, 18);

    // totalHits=700 => first chunk (0..500), second chunk (500..1000) then stop
    var responseChunk1 = response(
        700,
        List.of(farm("BER1", "UID1"), farm("BER2", "UID2"))
    );
    var responseChunk2 = response(
        700,
        List.of(farm("BER3", "UID3"))
    );

    when(agisRegisterApiRestClient.registerMutation(any()))
        .thenReturn(responseChunk1, responseChunk2);

    // when
    var result = agisApiImpl.fetchFarmMutations(from, to);

    // then
    assertThat(result).containsExactly(
        new AgisFarmOwnershipDto("BER1", "UID1"),
        new AgisFarmOwnershipDto("BER2", "UID2"),
        new AgisFarmOwnershipDto("BER3", "UID3")
    );

    var captor = ArgumentCaptor.forClass(AgisRegisterMutationDataRequest.class);
    verify(agisRegisterApiRestClient, times(2)).registerMutation(captor.capture());

    var req1 = captor.getAllValues().getFirst();
    assertThat(req1.getFrom()).isEqualTo(from);
    assertThat(req1.getTo()).isEqualTo(to);
    assertThat(req1.getMutationType()).isEqualTo(AgisRegisterMutationDataRequest.MutationTypeEnum.MODIFIED);
    assertThat(req1.getMutationDataType()).isEqualTo(AgisRegisterMutationDataRequest.MutationDataTypeEnum.FARM);
    assertThat(req1.getResultOffset().getFrom()).isZero();
    assertThat(req1.getResultOffset().getTo()).isEqualTo(500);

    var req2 = captor.getAllValues().get(1);
    assertThat(req2.getMutationType()).isEqualTo(AgisRegisterMutationDataRequest.MutationTypeEnum.MODIFIED);
    assertThat(req2.getMutationDataType()).isEqualTo(AgisRegisterMutationDataRequest.MutationDataTypeEnum.FARM);
    assertThat(req2.getResultOffset().getFrom()).isEqualTo(500);
    assertThat(req2.getResultOffset().getTo()).isEqualTo(1000);
  }

  @Test
  void givenNullResponse_whenFetchFarmMutations_thenReturnsEmptyAndStopsAfterFirstChunk() {
    // given
    when(agisRegisterApiRestClient.registerMutation(any())).thenReturn(null);

    // when
    var result = agisApiImpl.fetchFarmMutations(LocalDate.of(2026, 2, 17), LocalDate.of(2026, 2, 18));

    // then
    assertThat(result).isEmpty();
    verify(agisRegisterApiRestClient, times(1)).registerMutation(any());
  }

  // -------------------------
  // fetchFarmDeletions
  // -------------------------

  @Test
  void givenMultipleChunks_whenFetchFarmDeletions_thenReturnsOnlyBursAndUsesDeletedFarmParameters() {
    // given
    var from = LocalDate.of(2026, 2, 17);
    var to = LocalDate.of(2026, 2, 18);

    var responseChunk1 = response(600, List.of(farm("BUR1", "UID1"), farm("BUR2", "UID2")));
    var responseChunk2 = response(600, List.of(farm("BUR3", "UID3")));

    when(agisRegisterApiRestClient.registerMutation(any()))
        .thenReturn(responseChunk1, responseChunk2);

    // when
    var result = agisApiImpl.fetchFarmDeletions(from, to);

    // then
    assertThat(result).containsExactly("BUR1", "BUR2", "BUR3");

    var captor = ArgumentCaptor.forClass(AgisRegisterMutationDataRequest.class);
    verify(agisRegisterApiRestClient, times(2)).registerMutation(captor.capture());

    assertThat(captor.getAllValues().get(0).getMutationType())
        .isEqualTo(AgisRegisterMutationDataRequest.MutationTypeEnum.DELETED);
    assertThat(captor.getAllValues().get(0).getMutationDataType())
        .isEqualTo(AgisRegisterMutationDataRequest.MutationDataTypeEnum.FARM);

    assertThat(captor.getAllValues().get(1).getMutationType())
        .isEqualTo(AgisRegisterMutationDataRequest.MutationTypeEnum.DELETED);
    assertThat(captor.getAllValues().get(1).getMutationDataType())
        .isEqualTo(AgisRegisterMutationDataRequest.MutationDataTypeEnum.FARM);
  }

  // -------------------------
  // extractAgisFarmTypes (indirectly via fetchFarmMutations/deletions)
  // -------------------------

  @Test
  void givenMissingNestedFields_whenFetchFarmMutations_thenIgnoresChunkAndReturnsEmpty() {
    // given
    var response = new AgisPersonFarmResponseType();
    // personFarmTree is null -> extractAgisFarmTypes() should return empty

    when(agisRegisterApiRestClient.registerMutation(any())).thenReturn(response);

    // when
    var result = agisApiImpl.fetchFarmMutations(LocalDate.of(2026, 2, 17), LocalDate.of(2026, 2, 18));

    // then
    assertThat(result).isEmpty();
    verify(agisRegisterApiRestClient, times(1)).registerMutation(any());
  }

  // -------------------------
  // Test helpers
  // -------------------------

  private static AgisFarmType farm(String ber, String uid) {
    var farm = new AgisFarmType();
    farm.setBer(ber);
    farm.setUid(uid);
    return farm;
  }

  private static AgisPersonFarmResponseType response(int totalHits, List<AgisFarmType> farms) {
    var response = new AgisPersonFarmResponseType();

    var offset = new AgisResultOffsetType();
    offset.setTotalHits(totalHits);
    response.setResultOffset(offset);

    var relevant = new AgisRelevantFarms();
    relevant.setFarm(farms);

    var tree = new AgisPersonFarmTreeType();
    tree.setRelevantFarms(relevant);

    response.setPersonFarmTree(tree);
    return response;
  }
}
