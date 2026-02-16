package ch.agridata.common.openapi;

import lombok.NoArgsConstructor;

/**
 * Constants for OpenAPI subset filtering via the {@code x-api-subset} extension.
 *
 * @CommentLastReviewed 2026-02-16
 */

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class ApiSubsetConstants {
  public static final String X_API_SUBSET = "x-api-subset";
  public static final String MOBILE_APP = "Mobile App";
  public static final String WEB_APP = "agridata.ch Web App";
  public static final String DATA_CONSUMER = "Data Consumer";
  public static final String DATA_PROVIDER = "Data Provider";
}
