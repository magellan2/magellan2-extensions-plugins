package magellan.plugin.statistics.torque;

import java.io.PrintWriter;

import org.apache.torque.om.Persistent;

/**
 * Contains informations about turn specific region prices
 */
public class RegionStatisticsPricesData extends BaseRegionStatisticsPricesData implements Persistent {
  /**
   * Saves the data of this object into a XML file.
   */
  public void save(PrintWriter pw) throws Exception {
    pw.println("<price itemType=\""+getLuxuryItem()+"\" value=\""+getPrice()+"\"/>");
  }
}
