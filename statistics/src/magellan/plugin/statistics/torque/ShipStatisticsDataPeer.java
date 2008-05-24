package magellan.plugin.statistics.torque;

import java.util.ArrayList;
import java.util.List;

import magellan.library.utils.logging.Logger;

import org.apache.torque.TorqueException;
import org.apache.torque.util.Criteria;

/**
 * Contains informations about turn specific ship data
 */
public class ShipStatisticsDataPeer extends BaseShipStatisticsDataPeer {
  private static Logger log = Logger.getInstance(ShipStatisticsDataPeer.class);
  
  /**
   * Returns the data for the given ship.
   */
  public static List<ShipStatisticsData> get(ShipStatistics statistics) {
    try {
      Criteria criteria = new Criteria();
      criteria.addAscendingOrderByColumn(TURN);
      return statistics.getShipStatisticsDatas(criteria);
    } catch (Exception exception) {
      log.error(exception);
      return new ArrayList<ShipStatisticsData>();
    }
  }

  /**
   * Returns the data for the given ship in the given turn.
   */
  public static ShipStatisticsData get(ShipStatistics statistics, int turn) throws TorqueException {
    Criteria criteria = new Criteria();
    criteria.add(SHIP_ID,statistics.getID());
    criteria.add(TURN,turn);
    criteria.setLimit(1);
    List<ShipStatisticsData> data = doSelect(criteria);
    if (data != null && data.size()>0) return data.get(0);
    return null;
  }
}
