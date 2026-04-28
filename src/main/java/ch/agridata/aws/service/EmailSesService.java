package ch.agridata.aws.service;

import ch.agridata.aws.api.EmailApi;
import ch.agridata.common.exceptions.ExternalWebServiceException;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SendEmailResponse;
import software.amazon.awssdk.services.ses.model.SesException;

/**
 * Sends transactional emails via AWS SES.
 *
 * @CommentLastReviewed 2026-04-28
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class EmailSesService implements EmailApi {

  public static final String UTF_8 = "UTF-8";
  public static final List<String> ALLOWED_EMAIL_DOMAINS_NON_PROD = List.of("agridata.local", "blw.admin.ch");
  public static final List<String> NON_PROD_PROFILES = List.of("local", "test", "develop", "testing", "integration");

  @ConfigProperty(name = "agridata.email.sender-address")
  String senderAddress;

  @ConfigProperty(name = "agridata.email.enabled", defaultValue = "false")
  boolean emailEnabled;

  @ConfigProperty(name = "quarkus.profile")
  String activeProfile;

  private final SesClient sesClient;

  @Override
  public void submitEmail(String to, String subject, String htmlBody) {
    if (emailFeatureDisabled(to) || emailReceiverDisallowedInProfile(to)) {
      return;
    }

    try {
      SendEmailRequest request = SendEmailRequest.builder()
          .source(senderAddress)
          .destination(dest -> dest.toAddresses(to))
          .message(msg -> msg.subject(sub -> sub.data(subject).charset(UTF_8))
              .body(body -> body.html(html -> html.data(htmlBody).charset(UTF_8))))
          .build();

      SendEmailResponse response = sesClient.sendEmail(request);
      log.info("Email sent to {}. MessageId: {}", to, response.messageId());

    } catch (SesException e) {
      log.error("Failed to send email via AWS SES to {}: {}", to, e.awsErrorDetails().errorMessage());
      throw new ExternalWebServiceException("Failed to submit email. Error: " + e.awsErrorDetails(), e);
    }
  }

  // TODO: Remove this feature flag once it’s enabled in all environments.
  private boolean emailFeatureDisabled(String to) {
    if (!emailEnabled) {
      log.debug("Email sending is disabled (agridata.email.enabled=false). Skipping email to {}.", to);
      return true;
    }
    return false;
  }

  private boolean emailReceiverDisallowedInProfile(String to) {
    var toDomain = to.split("@")[1];
    if (NON_PROD_PROFILES.contains(activeProfile) && !ALLOWED_EMAIL_DOMAINS_NON_PROD.contains(toDomain)) {
      log.debug(
          "Non-production profile '{}': suppressing email to '{}'. Only domains '{}' are permitted outside production.",
          activeProfile,
          to,
          ALLOWED_EMAIL_DOMAINS_NON_PROD
      );
      return true;
    }
    return false;
  }
}
