package magellan.plugin.statistics.torque;

import java.util.List;

import magellan.library.utils.logging.Logger;

import org.apache.torque.util.Criteria;

/**
 * Contains informations about turn specific region resources
 */
public class RegionStatisticsResourcesDataPeer extends BaseRegionStatisticsResourcesDataPeer {
  private static Logger log = Logger.getInstance(RegionStatisticsResourcesDataPeer.class);

  /**
   * Returns the data for the given region.
   */
  public static RegionStatisticsResourcesData get(RegionStatistics statistics, RegionStatisticsData data, String type) {
    try {
      Criteria criteria = new Criteria();
      criteria.add(REGION_ID,statistics.getID());
      criteria.add(TURN_ID, data.getID());
      criteria.add(ITEM_TYPE, type);
      criteria.setLimit(1);
      List<RegionStatisticsResourcesData> resources = doSelect(criteria);
      if (resources != null && resources.size()>0) return resources.get(0);
    } catch (Exception exception) {
      log.error(exception);
    }
    return null;
  }
}
