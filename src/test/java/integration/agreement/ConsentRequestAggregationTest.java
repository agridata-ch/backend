package integration.agreement;

import static integration.testutils.TestUserEnum.PRODUCER_A;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

import ch.agridata.agreement.controller.ConsentRequestAggregationController;
import ch.agridata.agreement.dto.ConsentRequestAggregationDto;
import ch.agridata.agreement.dto.ConsentRequestAggregationStateEnum;
import ch.agridata.agreement.dto.ConsentRequestAggregationSummaryDto;
import ch.agridata.agreement.dto.ConsentRequestProducerViewV2Dto;
import ch.agridata.agreement.dto.ConsentRequestStateEnum;
import integration.testutils.AuthTestUtils;
import integration.testutils.TestDataIdentifiers;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RequiredArgsConstructor
class ConsentRequestAggregationTest {
  private final EntityManager entityManager;
  private final Flyway flyway;

  @BeforeEach
  void setUp() {
    flyway.migrate();
  }

  @Test
  void givenProducer_whenGetConsentRequestAggregationsWithoutUid_thenReturn400() {
    AuthTestUtils.requestAs(PRODUCER_A)
        .when().get(ConsentRequestAggregationController.PATH)
        .then().statusCode(400)
        .extract().as(new TypeRef<>() {
        });
  }

  @Test
  void givenProducer_whenGetConsentRequestAggregationsWithUnauthorizedUid_thenReturn200AndEmptyList() {
    AuthTestUtils.requestAs(PRODUCER_A)
        .when().get(ConsentRequestAggregationController.PATH + "?dataProducerUid=" + TestDataIdentifiers.Uid.CHE102000001)
        .then().statusCode(200)
        .body("size()", equalTo(0))
        .extract().as(new TypeRef<>() {
        });
  }

  @Test
  void givenProducer_whenGetConsentRequestAggregationsWithAuthorizedUid_thenReturn200AndAggregations() {
    AuthTestUtils.requestAs(PRODUCER_A)
        .when().get(ConsentRequestAggregationController.PATH + "?dataProducerUid=" + TestDataIdentifiers.Uid.CHE101000001)
        .then().statusCode(200)
        .body("size()", equalTo(7))
        .extract().as(new TypeRef<>() {
        });
  }

  @Test
  void givenProducer_whenGetConsentRequestAggregations_thenSummaryHasAggregatedFieldsAndOrderedConsentRequests() {
    List<ConsentRequestAggregationSummaryDto> aggregations = AuthTestUtils.requestAs(PRODUCER_A)
        .when().get(ConsentRequestAggregationController.PATH + "?dataProducerUid=" + TestDataIdentifiers.Uid.CHE101000001)
        .then().statusCode(200)
        .extract().as(new TypeRef<>() {
        });

    var bioSuisse01 = aggregations.stream()
        .filter(aggregation -> aggregation.id().equals(TestDataIdentifiers.DataRequest.BIO_SUISSE_01.uuid()))
        .findFirst()
        .orElseThrow();

    assertThat(bioSuisse01.stateCode()).isEqualTo(ConsentRequestAggregationStateEnum.DECLINED);
    assertThat(bioSuisse01.requestDate()).isEqualTo(LocalDate.of(2025, 3, 14));
    assertThat(bioSuisse01.showStateAsMigrated()).isTrue();

    assertThat(bioSuisse01.consentRequests())
        .extracting(ConsentRequestAggregationSummaryDto.ConsentRequestStateDto::id)
        .containsExactly(
            TestDataIdentifiers.ConsentRequest.BIO_SUISSE_01_CHE101000001.uuid(),
            TestDataIdentifiers.ConsentRequest.BIO_SUISSE_01_CHE101000001_99910003.uuid()
        );
  }

  @Test
  void givenProducer_whenGetConsentRequestAggregationWithUnknownDataProducerUid_thenReturn404() {
    AuthTestUtils.requestAs(PRODUCER_A)
        .when().get(
            ConsentRequestAggregationController.PATH + "/" + TestDataIdentifiers.DataRequest.BIO_SUISSE_01.uuid()
                + "?dataProducerUid=" + TestDataIdentifiers.Uid.CHE102000001
        )
        .then().statusCode(404);
  }

  @Test
  void givenProducer_whenGetConsentRequestAggregation_thenReturn200AndFullDetailsWithDataProducerBurSetForBurAndNullForUid() {
    ConsentRequestAggregationDto bioSuisse01 = AuthTestUtils.requestAs(PRODUCER_A)
        .when().get(
            ConsentRequestAggregationController.PATH + "/" + TestDataIdentifiers.DataRequest.BIO_SUISSE_01.uuid()
                + "?dataProducerUid=" + TestDataIdentifiers.Uid.CHE101000001
        )
        .then().statusCode(200)
        .extract().as(ConsentRequestAggregationDto.class);

    var uidConsentRequest = findConsentRequest(
        bioSuisse01.consentRequests(),
        TestDataIdentifiers.ConsentRequest.BIO_SUISSE_01_CHE101000001.uuid()
    );
    var burConsentRequest = findConsentRequest(
        bioSuisse01.consentRequests(),
        TestDataIdentifiers.ConsentRequest.BIO_SUISSE_01_CHE101000001_99910003.uuid()
    );

    assertThat(uidConsentRequest.dataProducerBur()).isNull();
    assertThat(burConsentRequest.dataProducerBur()).isEqualTo(TestDataIdentifiers.Bur.CODE_99910003.getCode());

    assertThat(bioSuisse01.consentRequests())
        .extracting(ConsentRequestProducerViewV2Dto::id)
        .containsExactlyInAnyOrder(
            TestDataIdentifiers.ConsentRequest.BIO_SUISSE_01_CHE101000001.uuid(),
            TestDataIdentifiers.ConsentRequest.BIO_SUISSE_01_CHE101000001_99910003.uuid()
        );
    assertThat(bioSuisse01.consentRequests())
        .extracting(ConsentRequestProducerViewV2Dto::stateCode)
        .containsOnly(ConsentRequestStateEnum.DECLINED);

    assertThat(bioSuisse01.stateCode()).isEqualTo(ConsentRequestAggregationStateEnum.DECLINED);
    assertThat(bioSuisse01.requestDate()).isEqualTo(LocalDate.of(2025, 3, 14));
    assertThat(bioSuisse01.showStateAsMigrated()).isTrue();
    assertThat(bioSuisse01.lastStateChangeDate()).isEqualTo(LocalDateTime.of(2025, 3, 20, 14, 25));
  }

  @Test
  void givenProducerWithOnlyLegallyPermittedConsentRequests_whenGetConsentRequestAggregations_thenAggregatedStateIsLegallyPermitted() {
    var dataRequestId = insertDataRequestWithLegallyPermittedConsentRequests();

    List<ConsentRequestAggregationSummaryDto> aggregations = AuthTestUtils.requestAs(PRODUCER_A)
        .when().get(ConsentRequestAggregationController.PATH + "?dataProducerUid=" + TestDataIdentifiers.Uid.CHE101000001)
        .then().statusCode(200)
        .extract().as(new TypeRef<>() {
        });

    var aggregation = aggregations.stream()
        .filter(candidate -> candidate.id().equals(dataRequestId))
        .findFirst()
        .orElseThrow();

    assertThat(aggregation.stateCode()).isEqualTo(ConsentRequestAggregationStateEnum.LEGALLY_PERMITTED);
    assertThat(aggregation.consentRequests()).hasSize(2);
  }

  /**
   * Inserts, in its own committed transaction, a legally permitted data request from the federal authority
   * {@code CHE403244345} (BLV) together with two {@code LEGALLY_PERMITTED} consent requests for {@code CHE101000001}, so the
   * running endpoint sees them. Uses native SQL to bypass the auditing listener, which requires an authenticated user. Returns
   * the data request id.
   */
  private UUID insertDataRequestWithLegallyPermittedConsentRequests() {
    var dataRequestId = UUID.randomUUID();
    QuarkusTransaction.requiringNew().run(() -> {
      entityManager.createNativeQuery("""
              INSERT INTO data_request
                (id, human_friendly_id, archived, created_at, modified_at, data_consumer_uid, title, description, purpose,
                 state_code, submission_date, data_consumer_city, data_consumer_country, data_consumer_legal_name,
                 data_consumer_display_name, data_consumer_street, data_consumer_zip, contact_phone_number,
                 contact_email_address, target_group, consumer_signature_type, provider_signature_type, advantages)
              VALUES
                (:id, 'LP01', false, NOW(), NOW(), 'CHE403244345',
                 jsonb_build_object('de', 'Tiergesundheitsdaten zur Tierseuchenüberwachung',
                                    'fr', 'Données de santé animale pour la surveillance des épizooties',
                                    'it', 'Dati sulla salute degli animali per la sorveglianza delle epizoozie'),
                 jsonb_build_object('de', 'Übermittlung von Betriebs- und Tierdaten an das BLV zur Erfüllung der gesetzlichen Aufgaben der Tierseuchenüberwachung gemäss Tierseuchengesetz.',
                                    'fr', 'Transmission des données d''exploitation et animales à l''OSAV pour l''accomplissement des tâches légales de surveillance des épizooties selon la loi sur les épizooties.',
                                    'it', 'Trasmissione dei dati aziendali e animali all''USAV per l''adempimento dei compiti legali di sorveglianza delle epizoozie secondo la legge sulle epizoozie.'),
                 jsonb_build_object('de', 'Gesetzlich vorgeschriebene Überwachung der Tiergesundheit und Prävention von Tierseuchen.',
                                    'fr', 'Surveillance légalement prescrite de la santé animale et prévention des épizooties.',
                                    'it', 'Sorveglianza legalmente prescritta della salute degli animali e prevenzione delle epizoozie.'),
                 'ACTIVE', '2025-03-11 09:27:55'::timestamp, 'Bern', 'CH',
                 'Bundesamt für Lebensmittelsicherheit und Veterinärwesen BLV', 'BLV', 'Schwarzenburgstrasse 155', '3003',
                 '+41 58 463 30 33', 'info@blv.admin.ch', 'Alle Tierhalter',
                 'COLLECTIVE_SIGNATURE', 'COLLECTIVE_SIGNATURE', '[]'::jsonb)
              """)
          .setParameter("id", dataRequestId)
          .executeUpdate();

      entityManager.createNativeQuery("""
              INSERT INTO consent_request
                (id, archived, created_at, modified_at, data_producer_uid, data_producer_bur, state_code, data_request_id)
              VALUES
                (:uidBasedId, false, NOW(), NOW(), :uid, NULL, 'LEGALLY_PERMITTED', :dataRequestId),
                (:burBasedId, false, NOW(), NOW(), :uid, '99910099', 'LEGALLY_PERMITTED', :dataRequestId)
              """)
          .setParameter("uidBasedId", UUID.randomUUID())
          .setParameter("burBasedId", UUID.randomUUID())
          .setParameter("uid", TestDataIdentifiers.Uid.CHE101000001.name())
          .setParameter("dataRequestId", dataRequestId)
          .executeUpdate();
    });
    return dataRequestId;
  }

  private static ConsentRequestProducerViewV2Dto findConsentRequest(List<ConsentRequestProducerViewV2Dto> consentRequests, UUID id) {
    return consentRequests.stream()
        .filter(consentRequest -> consentRequest.id().equals(id))
        .findFirst()
        .orElseThrow();
  }
}
