package ch.agridata.agreement.dto;


import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Lists the possible states of a consent request.
 *
 * @CommentLastReviewed 2025-08-25
 */

@Schema(
    description = """
        Possible states of a consent request:
        - GRANTED: Consent has been granted
        - OPENED: Consent request is open and awaiting response
        - DECLINED: Consent has been declined
        - NOT_CREATED: Placeholder to indicate that no consent request exists yet for a given UID.
        """
)
public enum ConsentRequestStateEnum {
  GRANTED,
  OPENED,
  DECLINED,
  NOT_CREATED
}
