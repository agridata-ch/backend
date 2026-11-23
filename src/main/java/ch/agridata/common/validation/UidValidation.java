package ch.agridata.common.validation;

import lombok.NoArgsConstructor;

/**
 * Constants for validating the format of a UID. Besides the "CHE" UIDs of the UID register, it also accepts the "ZZZ" UIDs that Identitas
 * assigns to equid owners.
 *
 * @CommentLastReviewed 2026-11-23
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class UidValidation {
  public static final String UID_REGEX = "^(CHE|ZZZ)\\d{9}$";
  public static final String UID_FORMAT_MESSAGE = "Invalid UID format. Expected 'CHE' or 'ZZZ' followed by 9 digits.";
}
