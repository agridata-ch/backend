package integration.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.when;

import ch.agridata.common.persistence.TranslationPersistenceDto;
import ch.agridata.common.security.AgridataSecurityIdentity;
import ch.agridata.product.persistence.DataProductEntity;
import ch.agridata.product.persistence.DataProductRepository;
import ch.agridata.product.persistence.DataProductStateEnum;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.validation.ConstraintViolationException;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Verifies that the size validation of multilingual text also takes effect when the API DTO validation is bypassed — here by writing
 * straight through the repository, as an import job or a service-internal call would.
 *
 * <p>This is the runtime counterpart to {@code ValidationArchitectureTest}: the architecture rule guarantees the {@code @Valid} cascade is
 * declared everywhere, this test proves the cascade actually rejects an over-long value at flush time.
 *
 * @CommentLastReviewed 2026-09-02
 */
@QuarkusTest
@RequiredArgsConstructor
class DataProductPersistenceValidationTest {

  private final DataProductRepository dataProductRepository;
  private final Flyway flyway;

  @InjectMock
  AgridataSecurityIdentity securityIdentity;

  private static final UUID TEST_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000099");

  @BeforeEach
  void setUp() {
    flyway.migrate();
    when(securityIdentity.getUserId()).thenReturn(TEST_USER_ID);
  }

  @Test
  void persisting_an_over_long_translation_fails_even_when_dto_validation_is_bypassed() {
    var tooLong = "a".repeat(TranslationPersistenceDto.MAX_LENGTH + 1);

    var thrown = catchThrowable(() -> QuarkusTransaction.requiringNew()
        .run(() -> dataProductRepository.persistAndFlush(dataProduct(tooLong))));

    assertThat(constraintViolationIn(thrown))
        .as("an over-long translation must be rejected at flush time, however the exception is wrapped")
        .isPresent()
        .get()
        .satisfies(violation -> assertThat(violation.getConstraintViolations())
            .anyMatch(v -> "name.de".equals(v.getPropertyPath().toString())));
  }

  @Test
  void persisting_a_translation_at_the_limit_still_succeeds() {
    var atLimit = "a".repeat(TranslationPersistenceDto.MAX_LENGTH);
    var entity = dataProduct("Valid name");
    entity.setExtendedDescription(TranslationPersistenceDto.builder().de(atLimit).build());

    assertThatCode(() -> QuarkusTransaction.requiringNew()
        .run(() -> dataProductRepository.persistAndFlush(entity)))
        .doesNotThrowAnyException();

    var reloaded = QuarkusTransaction.requiringNew()
        .call(() -> dataProductRepository.findById(entity.getId()));
    assertThat(reloaded.getExtendedDescription().de()).hasSize(TranslationPersistenceDto.MAX_LENGTH);
  }

  /**
   * Unwraps the exception chain, since a flush-time violation may surface directly or wrapped by the transaction layer.
   */
  private static Optional<ConstraintViolationException> constraintViolationIn(Throwable thrown) {
    for (Throwable current = thrown; current != null; current = current.getCause()) {
      if (current instanceof ConstraintViolationException violation) {
        return Optional.of(violation);
      }
    }
    return Optional.empty();
  }

  private static DataProductEntity dataProduct(String germanName) {
    return DataProductEntity.builder()
        .name(TranslationPersistenceDto.builder().de(germanName).build())
        .stateCode(DataProductStateEnum.DRAFT)
        .build();
  }
}
