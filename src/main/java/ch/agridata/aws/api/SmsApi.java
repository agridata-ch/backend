package ch.agridata.aws.api;

/**
 * Internal interface for sending SMS messages.
 *
 * @CommentLastReviewed 2026-04-20
 */
public interface SmsApi {

  /**
   * Sends an SMS message to the given phone number.
   *
   * @param phoneNumber the recipient's phone number
   * @param message     the text content to send
   */
  void sendSms(String phoneNumber, String message);
}
