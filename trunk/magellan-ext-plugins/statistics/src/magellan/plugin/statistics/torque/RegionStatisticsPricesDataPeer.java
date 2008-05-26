package magellan.plugin.statistics.torque;

import java.util.List;

import magellan.library.utils.logging.Logger;

import org.apache.torque.util.Criteria;

/**
 * Contains informations about turn specific region prices
 */
public class RegionStatisticsPricesDataPeer extends BaseRegionStatisticsPricesDataPeer {
  private static Logger log = Logger.getInstance(RegionStatisticsPricesDataPeer.class);

  /**
   * Returns the data for the given region.
   */
  public static RegionStatisticsPricesData get(RegionStatistics statistics, RegionStatisticsData data, String type) {
    Criteria criteria = new Criteria();
    try {
      criteria.add(REGION_ID,statistics.getID());
      criteria.add(TURN_ID, data.getID());
      criteria.add(LUXURY_ITEM, type);
      criteria.setLimit(1);
      List<RegionStatisticsPricesData> prices = doSelect(criteria);
      if (prices != null && prices.size()>0) return prices.get(0);
    } catch (Exception exception) {
      try {
        log.error(exception +" in "+createQueryString(criteria));
      } catch (Exception e) {
        log.error(exception);
      }
    }
    return null;
  }
}
