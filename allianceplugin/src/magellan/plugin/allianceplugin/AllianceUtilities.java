// Created on 25.08.2011
//
package magellan.plugin.allianceplugin;

import java.text.SimpleDateFormat;
import java.util.Date;

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
  
  public static String toString(Date date) {
    if(date == null) return "-";
    SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm");
    return format.format(date);
  }
}
