// Created on 25.08.2011
//
package magellan.plugin.allianceplugin;

public class AllianceUtilities {
  public static Integer getIntValue(String value, Integer defaultValue) {
    if (value == null)
      return defaultValue;
    try {
      return Integer.parseInt(value);
    } catch (Exception exception) {
      return defaultValue;
    }
  }

  public static Long getLongValue(String value, Long defaultValue) {
    if (value == null)
      return defaultValue;
    try {
      return Long.parseLong(value);
    } catch (Exception exception) {
      return defaultValue;
    }
  }
}
