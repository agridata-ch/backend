package ch.agridata.common.validation;

import lombok.NoArgsConstructor;

/**
 * Constants for validating the format of a BUR (local farm unit number).
 *
 * @CommentLastReviewed 2026-11-05
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class BurValidation {
  public static final String BUR_REGEX = "^[A-Za-z0-9]{8,9}$";
  public static final String BUR_FORMAT_MESSAGE = "Invalid BUR format. Expected 8 to 9 alphanumeric characters.";
}
