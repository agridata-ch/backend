package ch.agridata.user.dto;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Defines legal forms. It supports mapping external register values into consistent internal categories.
 *
 * @CommentLastReviewed 2025-08-25
 */
@Getter
@Slf4j
public enum LegalFormEnum {

  NATUERLICHE_PERSON("01"),
  EINFACHE_GESELLSCHAFT("02"),
  KOLLEKTIVGESELLSCHAFT("03"),
  KOMMANDITGESELLSCHAFT("04"),
  KOMMANDITAKTIENGESELLSCHAFT("05"),
  AKTIENGESELLSCHAFT("06"),
  GMBH("07"),
  GENOSSENSCHAFT("08"),
  VEREIN("09"),
  STIFTUNG("10"),
  OEFF_RECHT_KOERPERSCHAFT_VERWALTUNG("24"),
  LANDESKIRCHE("25"),
  BUND("30"),
  KANTON("31"),
  BEZIRK("32"),
  GEMEINDE("33"),
  OEFF_RECHT_KOERPERSCHAFT_BETRIEB("34"),
  UNKNOWN("-1");

  private static final Map<String, LegalFormEnum> CODE_MAP = new HashMap<>();

  static {
    for (LegalFormEnum form : values()) {
      CODE_MAP.put(form.code, form);
    }
  }

  private final String code;

  LegalFormEnum(String code) {
    this.code = code;
  }

  public static LegalFormEnum fromNumber(String number) {
    if (!CODE_MAP.containsKey(number)) {
      log.warn("No LegalFormEnum found for number: {}", number);
    }

    return CODE_MAP.getOrDefault(number, UNKNOWN);
  }

}
