package ch.agridata.common.utils;

import io.quarkiverse.loggingjson.providers.KeyValueStructuredArgument;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.LogRecord;

public class LogUtil {

  /**
   * Extracts the {@code kv(...)} structured arguments of a captured log logRecord into a key/value map
   * by reading the {@code key}/{@code value} fields of each {@code KeyValueStructuredArgument}.
   */
  public static Map<String, Object> structuredFields(LogRecord logRecord) {
    var fields = new LinkedHashMap<String, Object>();
    Object[] params = logRecord.getParameters();
    if (params == null) {
      return fields;
    }
    for (Object param : params) {
      if (!(param instanceof KeyValueStructuredArgument kvArgument)) {
        continue;
      }
      try {
        var keyField = KeyValueStructuredArgument.class.getDeclaredField("key");
        var valueField = KeyValueStructuredArgument.class.getDeclaredField("value");
        keyField.setAccessible(true);
        valueField.setAccessible(true);
        fields.put(String.valueOf(keyField.get(kvArgument)), valueField.get(kvArgument));
      } catch (ReflectiveOperationException e) {
        throw new IllegalStateException("Cannot read structured argument", e);
      }
    }
    return fields;
  }

}
