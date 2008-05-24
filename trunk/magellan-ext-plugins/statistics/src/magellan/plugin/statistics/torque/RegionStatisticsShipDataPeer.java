package magellan.plugin.statistics.torque;

import java.util.List;

import magellan.library.utils.logging.Logger;

import org.apache.torque.util.Criteria;

/**
 * Contains informations about turn specific region ship data
 */
public class RegionStatisticsShipDataPeer extends BaseRegionStatisticsShipDataPeer {
  private static Logger log = Logger.getInstance(RegionStatisticsShipDataPeer.class);

  /**
   * Returns the data for the given region.
   */
  public static RegionStatisticsShipData get(RegionStatistics statistics, RegionStatisticsData data, String ship) {
    try {
      Criteria criteria = new Criteria();
      criteria.add(TURN_ID, data.getID());
      criteria.add(SHIP_NUMBER, ship);
      criteria.setLimit(1);
      List<RegionStatisticsShipData> ships = doSelect(criteria);
      if (ships != null && ships.size()>0) return ships.get(0);
    } catch (Exception exception) {
      log.error(exception);
    }
    return null;
  }
}
