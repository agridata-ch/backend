package ch.agridata.aws.api;

/**
 * Internal interface for sending transactional emails.
 *
 * @CommentLastReviewed 2026-04-28
 */
public interface EmailApi {

  /**
   * Sends an email message via the configured email service.
   *
   * @param to       the recipient's email address
   * @param subject  the email subject line
   * @param htmlBody the HTML-formatted body content
   */
  void submitEmail(String to, String subject, String htmlBody);
}
