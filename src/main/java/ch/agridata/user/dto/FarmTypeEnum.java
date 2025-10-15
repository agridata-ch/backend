package ch.agridata.user.dto;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Enumerates possible farm types. It provides a standardized vocabulary for interpreting register data.
 *
 * @CommentLastReviewed 2025-08-25
 */
@Getter
@Slf4j
public enum FarmTypeEnum {

  GANZJAHRESBETRIEB("01"),
  PRODUKTIONSSTAETTE("02"),
  GEMEINSCHAFTSWEIDEBETRIEB("04"),
  SOEMMERUNGSBETRIEB("05"),
  BETRIEBSGEMEINSCHAFT("06"),
  VIEHHANDELSUNTERNEHMEN("09"),
  WANDERHERDE("10"),
  TIERKLINIK("11"),
  SCHLACHTBETRIEB("12"),
  VIEHMAERKTE_UND_VERANSTALTUNGEN("13"),
  BETRIEBSZWEIGGEMEINSCHAFT("14"),
  NICHTKOMMERZIELLE_TIERHALTUNG("15"),
  OELN_GEMEINSCHAFT("16"),
  VERGAERUNG_KOMPOSTIERUNG_NAEHRSTOFFPOOL("17"),
  TIERHALTUNG("20"),
  NICHT_ZUGETEILT("99"),
  UNKNOWN("-1");

  private static final Map<String, FarmTypeEnum> CODE_MAP = new HashMap<>();

  static {
    for (FarmTypeEnum type : values()) {
      CODE_MAP.put(type.code, type);
    }
  }

  private final String code;

  FarmTypeEnum(String code) {
    this.code = code;
  }

  public static FarmTypeEnum fromNumber(String number) {
    if (!CODE_MAP.containsKey(number)) {
      log.warn("No FarmTypeEnum found for number: {}", number);
    }

    return CODE_MAP.getOrDefault(number, UNKNOWN);
  }

}
