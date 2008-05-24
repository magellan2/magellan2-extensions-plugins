package magellan.plugin.statistics.torque;


import java.util.ArrayList;
import java.util.List;

import magellan.library.utils.logging.Logger;

import org.apache.torque.om.Persistent;

/**
 * Contains informations about turn specific region data
 */
public  class RegionStatisticsData extends BaseRegionStatisticsData implements Persistent {
  private static Logger log = Logger.getInstance(UnitStatisticsData.class);
  
  /**
   * 
   */
  public List<RegionStatisticsPricesData> getPrices() {
    try {
      return getRegionStatisticsPricesDatas();
    } catch (Exception exception) {
      log.error(exception);
    }
    return new ArrayList<RegionStatisticsPricesData>();
  }
}
