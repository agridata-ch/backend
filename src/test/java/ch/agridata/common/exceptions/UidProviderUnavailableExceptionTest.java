package ch.agridata.common.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests for UidProviderUnavailableException constructors and inheritance.
 *
 * @CommentLastReviewed 2026-04-15
 */
class UidProviderUnavailableExceptionTest {

  @Test
  void givenMessageAndCause_whenCreateException_thenKeepMessageAndCause() {
    RuntimeException cause = new RuntimeException("timeout");

    UidProviderUnavailableException exception =
        new UidProviderUnavailableException("AGIS unavailable", cause);

    assertThat(exception).isInstanceOf(ExternalWebServiceException.class);
    assertThat(exception.getMessage()).isEqualTo("AGIS unavailable");
    assertThat(exception.getCause()).isSameAs(cause);
  }

  @Test
  void givenMessageOnly_whenCreateException_thenKeepMessageWithoutCause() {
    UidProviderUnavailableException exception =
        new UidProviderUnavailableException("TVD unavailable");

    assertThat(exception).isInstanceOf(ExternalWebServiceException.class);
    assertThat(exception.getMessage()).isEqualTo("TVD unavailable");
    assertThat(exception.getCause()).isNull();
  }
}

