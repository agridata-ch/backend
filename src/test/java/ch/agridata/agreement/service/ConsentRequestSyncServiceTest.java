package ch.agridata.agreement.service;

import static ch.agridata.agreement.persistence.ConsentRequestEntity.StateEnum.DECLINED;
import static ch.agridata.agreement.persistence.ConsentRequestEntity.StateEnum.GRANTED;
import static ch.agridata.agreement.persistence.ConsentRequestEntity.StateEnum.OPENED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.agridata.agreement.persistence.ConsentRequestEntity;
import ch.agridata.agreement.persistence.ConsentRequestRepository;
import ch.agridata.agreement.persistence.DataRequestEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for the derivation of the UID consent request state from its active BUR consent requests
 * (precedence {@code GRANTED > DECLINED > OPENED}).
 */
@ExtendWith(MockitoExtension.class)
class ConsentRequestSyncServiceTest {

  private static final UUID DATA_REQUEST_ID = UUID.randomUUID();
  private static final String UID = "CHE123456789";
  private static final String BUR1 = "99910001";
  private static final String BUR2 = "99910002";

  @Mock
  private ConsentRequestRepository consentRequestRepository;
  @Mock
  private AuditingService auditingService;
  @InjectMocks
  private ConsentRequestSyncService service;

  private ConsentRequestEntity row(String bur, ConsentRequestEntity.StateEnum state) {
    return ConsentRequestEntity.builder()
        .id(UUID.randomUUID())
        .dataRequest(DataRequestEntity.builder().id(DATA_REQUEST_ID).build())
        .dataProducerUid(UID)
        .dataProducerBur(bur)
        .stateCode(state)
        .build();
  }

  private void givenUidRowAndBurRows(ConsentRequestEntity uidRow, ConsentRequestEntity... burRows) {
    lenient().when(consentRequestRepository.findAndLockUidBasedByDataRequestIdAndDataProducerUid(DATA_REQUEST_ID, UID))
        .thenReturn(Optional.ofNullable(uidRow));
    var active = new java.util.ArrayList<ConsentRequestEntity>();
    if (uidRow != null) {
      active.add(uidRow);
    }
    active.addAll(List.of(burRows));
    lenient().when(consentRequestRepository.findActiveUidAndBurBasedByDataRequestIdAndDataProducerUid(DATA_REQUEST_ID, UID))
        .thenReturn(active);
  }

  private void sync() {
    service.syncUidConsentRequestWithBurConsentRequests(DATA_REQUEST_ID, UID);
  }

  @Test
  void throwsWhenNoUidConsentRequestExists() {
    when(consentRequestRepository.findAndLockUidBasedByDataRequestIdAndDataProducerUid(DATA_REQUEST_ID, UID))
        .thenReturn(Optional.empty());

    assertThatThrownBy(this::sync).isInstanceOf(IllegalStateException.class);
    verify(auditingService, never()).logConsentRequestStateChange(any());
  }

  @Test
  void withoutBurConsentRequests_isNoOp() {
    var uidRow = row(null, OPENED);
    givenUidRowAndBurRows(uidRow); // only the UID row is active, no BUR rows

    sync();

    assertThat(uidRow.getStateCode()).isEqualTo(OPENED);
    verify(auditingService, never()).logConsentRequestStateChange(any());
  }

  @Test
  void anyGrantedBur_rollsUpToGranted() {
    var uidRow = row(null, OPENED);
    givenUidRowAndBurRows(uidRow, row(BUR1, GRANTED), row(BUR2, DECLINED));

    sync();

    assertThat(uidRow.getStateCode()).isEqualTo(GRANTED);
    verify(auditingService).logConsentRequestStateChange(uidRow);
  }

  @Test
  void noGrantedButDeclinedBur_rollsUpToDeclined() {
    var uidRow = row(null, GRANTED);
    givenUidRowAndBurRows(uidRow, row(BUR1, DECLINED), row(BUR2, OPENED));

    sync();

    assertThat(uidRow.getStateCode()).isEqualTo(DECLINED);
    verify(auditingService).logConsentRequestStateChange(uidRow);
  }

  @Test
  void allBursOpened_rollsUpToOpened() {
    var uidRow = row(null, GRANTED);
    givenUidRowAndBurRows(uidRow, row(BUR1, OPENED), row(BUR2, OPENED));

    sync();

    assertThat(uidRow.getStateCode()).isEqualTo(OPENED);
    verify(auditingService).logConsentRequestStateChange(uidRow);
  }

  @Test
  void targetStateEqualsCurrent_isNoOp() {
    var uidRow = row(null, GRANTED);
    givenUidRowAndBurRows(uidRow, row(BUR1, GRANTED));

    sync();

    assertThat(uidRow.getStateCode()).isEqualTo(GRANTED);
    verify(auditingService, never()).logConsentRequestStateChange(any());
  }
}
