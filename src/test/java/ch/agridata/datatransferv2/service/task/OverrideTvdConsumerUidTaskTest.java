package ch.agridata.datatransferv2.service.task;

import ch.agridata.datatransferv2.service.AgridataContext;
import ch.agridata.product.dto.DataProductProviderConfigurationDto;
import ch.agridata.product.dto.RestClientIdentifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link OverrideTvdConsumerUidTask}. Verify that the consumer identity on the context is only overridden for TVD clients on the
 * integration profile when enabled, and that excluded consumer UIDs keep their real UID.
 *
 * @CommentLastReviewed 2026-07-30
 */
class OverrideTvdConsumerUidTaskTest {

  private static final String REAL_UID = "CHE123456789";
  private static final String DUMMY_UID = "ZZZ100000153";

  private OverrideTvdConsumerUidTask override;

  @BeforeEach
  void setUp() {
    override = new OverrideTvdConsumerUidTask();
    override.activeProfile = "integration";
    override.enabled = true;
    override.dummyUid = Optional.of(DUMMY_UID);
    override.excludedConsumerUids = Optional.empty();
  }

  @Test
  void givenActiveOverrideAndAnimalTracingClient_whenApply_thenConsumerIdentityOverriddenWithDummy() {
    var context = contextFor(RestClientIdentifier.TVD_ANIMAL_TRACING_API);

    Assertions.assertThat(context.getConsumerUid()).isEqualTo(REAL_UID);
    Assertions.assertThat(context.getRequestParameters()).containsEntry("recipientUid", REAL_UID);

    override.apply(context);

    Assertions.assertThat(context.getConsumerUid()).isEqualTo(DUMMY_UID);
    Assertions.assertThat(context.getRequestParameters()).containsEntry("recipientUid", DUMMY_UID);
  }

  @Test
  void givenActiveOverrideAndZoClient_whenApply_thenConsumerIdentityOverriddenWithDummy() {
    var context = contextFor(RestClientIdentifier.TVD_ZO_API);

    Assertions.assertThat(context.getConsumerUid()).isEqualTo(REAL_UID);
    Assertions.assertThat(context.getRequestParameters()).containsEntry("recipientUid", REAL_UID);

    override.apply(context);

    Assertions.assertThat(context.getConsumerUid()).isEqualTo(DUMMY_UID);
    Assertions.assertThat(context.getRequestParameters()).containsEntry("recipientUid", DUMMY_UID);
  }

  @Test
  void givenNonTvdClient_whenApply_thenConsumerIdentityUnchanged() {
    var context = contextFor(RestClientIdentifier.AGIS_API);

    override.apply(context);

    Assertions.assertThat(context.getConsumerUid()).isEqualTo(REAL_UID);
    Assertions.assertThat(context.getRequestParameters()).containsEntry("recipientUid", REAL_UID);
  }

  @Test
  void givenExcludedConsumer_whenApply_thenConsumerIdentityUnchanged() {
    override.excludedConsumerUids = Optional.of(List.of("CHE999999999", REAL_UID));
    var context = contextFor(RestClientIdentifier.TVD_ANIMAL_TRACING_API);

    override.apply(context);

    Assertions.assertThat(context.getConsumerUid()).isEqualTo(REAL_UID);
    Assertions.assertThat(context.getRequestParameters()).containsEntry("recipientUid", REAL_UID);
  }

  @Test
  void givenNonIntegrationProfile_whenApply_thenConsumerIdentityUnchanged() {
    override.activeProfile = "production";
    var context = contextFor(RestClientIdentifier.TVD_ANIMAL_TRACING_API);

    override.apply(context);

    Assertions.assertThat(context.getConsumerUid()).isEqualTo(REAL_UID);
  }

  @Test
  void givenOverrideDisabled_whenApply_thenConsumerIdentityUnchanged() {
    override.enabled = false;
    var context = contextFor(RestClientIdentifier.TVD_ANIMAL_TRACING_API);

    override.apply(context);

    Assertions.assertThat(context.getConsumerUid()).isEqualTo(REAL_UID);
  }

  @Test
  void givenNoDummyConfigured_whenApply_thenConsumerIdentityUnchanged() {
    override.dummyUid = Optional.empty();
    var context = contextFor(RestClientIdentifier.TVD_ANIMAL_TRACING_API);

    override.apply(context);

    Assertions.assertThat(context.getConsumerUid()).isEqualTo(REAL_UID);
  }

  @Test
  void givenRecipientUidParamAbsent_whenApply_thenOnlyConsumerUidOverridden() {
    var context = contextFor(RestClientIdentifier.TVD_ANIMAL_TRACING_API);
    context.getRequestParameters().remove("recipientUid");

    override.apply(context);

    Assertions.assertThat(context.getConsumerUid()).isEqualTo(DUMMY_UID);
    Assertions.assertThat(context.getRequestParameters()).doesNotContainKey("recipientUid");
  }

  @Test
  void givenNullConsumerUid_whenApply_thenNothingOverridden() {
    var context = contextFor(RestClientIdentifier.TVD_ANIMAL_TRACING_API);
    context.setConsumerUid(null);

    override.apply(context);

    Assertions.assertThat(context.getConsumerUid()).isNull();
    Assertions.assertThat(context.getRequestParameters()).containsEntry("recipientUid", REAL_UID);
  }

  @Test
  void givenBlankConsumerUid_whenApply_thenNothingOverridden() {
    var context = contextFor(RestClientIdentifier.TVD_ANIMAL_TRACING_API);
    context.setConsumerUid(" ");

    override.apply(context);

    Assertions.assertThat(context.getConsumerUid()).isEqualTo(" ");
    Assertions.assertThat(context.getRequestParameters()).containsEntry("recipientUid", REAL_UID);
  }

  private AgridataContext contextFor(RestClientIdentifier client) {
    Map<String, String> requestParameters = new HashMap<>();
    requestParameters.put("recipientUid", REAL_UID);
    return AgridataContext.builder()
        .consumerUid(REAL_UID)
        .requestParameters(requestParameters)
        .productProviderConfiguration(
            DataProductProviderConfigurationDto.builder().restClientIdentifierCode(client.name()).build())
        .build();
  }
}
