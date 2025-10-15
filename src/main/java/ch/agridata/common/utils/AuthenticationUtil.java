package ch.agridata.common.utils;

import lombok.NoArgsConstructor;

/**
 * This class holds constants representing different roles in the application.
 *
 * @CommentLastReviewed 2025-08-25
 */

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class AuthenticationUtil {
  public static final String PRODUCER_ROLE = "agridata.ch.Agridata_Einwilliger";
  public static final String CONSUMER_ROLE = "agridata.ch.Agridata_Datenbezueger";
  public static final String PROVIDER_ROLE = "agridata.ch.Agridata_Datenanbieter";
  public static final String ADMIN_ROLE = "agridata.ch.Agridata_Admin";
  public static final String SUPPORT_ROLE = "agridata.ch.Agridata_Support";
}
