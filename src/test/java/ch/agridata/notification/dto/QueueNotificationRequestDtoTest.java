package ch.agridata.notification.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link QueueNotificationRequestDto}.
 * Verifies the {@code @NotEmpty} constraint on {@code recipients}, the {@code @NotNull} constraint
 * on {@code eventTypeCode}, and that {@code @Valid} cascades validation to each recipient item.
 *
 * @CommentLastReviewed 2026-05-05
 */
class QueueNotificationRequestDtoTest {

  private static ValidatorFactory validatorFactory;
  private static Validator validator;

  @BeforeAll
  static void setUpValidator() {
    validatorFactory = Validation.buildDefaultValidatorFactory();
    validator = validatorFactory.getValidator();
  }

  @AfterAll
  static void tearDownValidator() {
    validatorFactory.close();
  }

  private static final RecipientRequestDto VALID_RECIPIENT = new RecipientRequestDto(UUID.randomUUID(), null);

  // ── valid ────────────────────────────────────────────────────────────────

  @Test
  void givenAllFieldsValid_whenValidate_thenNoViolations() {
    var dto = new QueueNotificationRequestDto(List.of(VALID_RECIPIENT), EventTypeCodeEnum.DATA_REQUEST_READY_FOR_REVIEW, null);

    assertThat(validator.validate(dto)).isEmpty();
  }

  // ── recipients ───────────────────────────────────────────────────────────

  @Test
  void givenNullRecipients_whenValidate_thenRecipientsViolation() {
    var dto = new QueueNotificationRequestDto(null, EventTypeCodeEnum.DATA_REQUEST_READY_FOR_REVIEW, null);

    Set<ConstraintViolation<QueueNotificationRequestDto>> violations = validator.validate(dto);

    assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("recipients"));
  }

  @Test
  void givenEmptyRecipients_whenValidate_thenRecipientsViolation() {
    var dto = new QueueNotificationRequestDto(List.of(), EventTypeCodeEnum.DATA_REQUEST_READY_FOR_REVIEW, null);

    Set<ConstraintViolation<QueueNotificationRequestDto>> violations = validator.validate(dto);

    assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("recipients"));
  }

  @Test
  void givenInvalidRecipientInList_whenValidate_thenCascadedViolation() {
    var invalid = new RecipientRequestDto(null, null); // both null → @AssertTrue on isValid() fails
    var dto = new QueueNotificationRequestDto(List.of(invalid), EventTypeCodeEnum.DATA_REQUEST_READY_FOR_REVIEW, null);

    Set<ConstraintViolation<QueueNotificationRequestDto>> violations = validator.validate(dto);

    assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().startsWith("recipients"));
  }

  // ── eventTypeCode ─────────────────────────────────────────────────────────

  @Test
  void givenNullEventTypeCode_whenValidate_thenEventTypeCodeViolation() {
    var dto = new QueueNotificationRequestDto(List.of(VALID_RECIPIENT), null, null);

    Set<ConstraintViolation<QueueNotificationRequestDto>> violations = validator.validate(dto);

    assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("eventTypeCode"));
  }
}
