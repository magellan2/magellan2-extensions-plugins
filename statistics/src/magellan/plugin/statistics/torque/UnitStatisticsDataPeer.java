package magellan.plugin.statistics.torque;

import java.util.ArrayList;
import java.util.List;

import magellan.library.utils.logging.Logger;

import org.apache.torque.TorqueException;
import org.apache.torque.util.Criteria;

/**
 * Contains informations about turn specific unit data
 */
public class UnitStatisticsDataPeer extends BaseUnitStatisticsDataPeer {
  private static Logger log = Logger.getInstance(UnitStatisticsDataPeer.class);
  
  /**
   * Returns the data for the given unit.
   */
  public static List<UnitStatisticsData> get(UnitStatistics statistics) {
    try {
      Criteria criteria = new Criteria();
      criteria.addAscendingOrderByColumn(TURN);
      return statistics.getUnitStatisticsDatas(criteria);
    } catch (Exception exception) {
      log.error(exception);
      return new ArrayList<UnitStatisticsData>();
    }
  }

  /**
   * Returns the data for the given unit in the given turn.
   */
  public static UnitStatisticsData get(UnitStatistics statistics, int turn) throws TorqueException {
    Criteria criteria = new Criteria();
    criteria.add(UNIT_ID,statistics.getID());
    criteria.add(TURN,turn);
    criteria.setLimit(1);
    List<UnitStatisticsData> data = doSelect(criteria);
    if (data != null && data.size()>0) return data.get(0);
    return null;
  }
}
