package magellan.plugin.statistics.torque;

import java.util.ArrayList;
import java.util.List;

import magellan.library.utils.logging.Logger;

import org.apache.torque.TorqueException;
import org.apache.torque.util.Criteria;

/**
 * Contains informations about turn specific building data
 */
public class BuildingStatisticsDataPeer extends BaseBuildingStatisticsDataPeer {
  private static Logger log = Logger.getInstance(BuildingStatisticsDataPeer.class);
  
  /**
   * Returns the data for the given building.
   */
  public static List<BuildingStatisticsData> get(BuildingStatistics statistics) {
    try {
      Criteria criteria = new Criteria();
      criteria.addAscendingOrderByColumn(TURN);
      return statistics.getBuildingStatisticsDatas(criteria);
    } catch (Exception exception) {
      log.error(exception);
      return new ArrayList<BuildingStatisticsData>();
    }
  }

  /**
   * Returns the data for the given building in the given turn.
   */
  public static BuildingStatisticsData get(BuildingStatistics statistics, int turn) throws TorqueException {
    Criteria criteria = new Criteria();
    criteria.add(BUILDING_ID,statistics.getID());
    criteria.add(TURN,turn);
    criteria.setLimit(1);
    List<BuildingStatisticsData> data = doSelect(criteria);
    if (data != null && data.size()>0) return data.get(0);
    return null;
  }
}
