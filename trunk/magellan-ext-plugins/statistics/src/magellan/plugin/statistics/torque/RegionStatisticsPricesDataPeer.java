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
    try {
      Criteria criteria = new Criteria();
      criteria.add(REGION_ID,statistics.getID());
      criteria.add(TURN_ID, data.getID());
      criteria.add(PRICE, type);
      criteria.setLimit(1);
      List<RegionStatisticsPricesData> prices = doSelect(criteria);
      if (prices != null && prices.size()>0) return prices.get(0);
    } catch (Exception exception) {
      log.error(exception);
    }
    return null;
  }
}
