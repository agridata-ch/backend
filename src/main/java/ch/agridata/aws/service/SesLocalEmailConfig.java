package ch.agridata.aws.service;

import io.quarkus.runtime.LaunchMode;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.VerifyEmailAddressRequest;

/**
 * Verifies the configured SES sender address in the LocalStack dev environment on startup,
 * so that emails can be sent without having to call the AWS SES verification flow manually.
 * Only active in dev/local launch mode.
 *
 * @CommentLastReviewed 2026-05-04
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class SesLocalEmailConfig {

  @ConfigProperty(name = "agridata.email.sender-address")
  String senderAddress;

  private final SesClient sesClient;

  void onStart(@Observes StartupEvent event) {
    if (LaunchMode.current().isDevOrTest()) {
      log.info("Dev mode: verifying SES sender address '{}' in LocalStack", senderAddress);
      sesClient.verifyEmailAddress(VerifyEmailAddressRequest.builder().emailAddress(senderAddress).build());
    }
  }
}
