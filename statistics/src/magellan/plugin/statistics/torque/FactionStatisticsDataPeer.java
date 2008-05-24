package magellan.plugin.statistics.torque;

import java.util.ArrayList;
import java.util.List;

import magellan.library.utils.logging.Logger;

import org.apache.torque.TorqueException;
import org.apache.torque.util.Criteria;

/**
 * Contains informations about turn specific faction data
 */
public class FactionStatisticsDataPeer extends BaseFactionStatisticsDataPeer {
  private static Logger log = Logger.getInstance(FactionStatisticsDataPeer.class);
  
  /**
   * Returns the data for the given faction in the given turn.
   */
  public static List<FactionStatisticsData> get(FactionStatistics statistics) {
    try {
      Criteria criteria = new Criteria();
      criteria.addAscendingOrderByColumn(TURN);
      return statistics.getFactionStatisticsDatas(criteria);
    } catch (Exception exception) {
      log.error(exception);
      return new ArrayList<FactionStatisticsData>();
    }
  }

  /**
   * Returns the data for the given faction in the given turn.
   */
  public static FactionStatisticsData get(FactionStatistics factionStatistics, int turn) throws TorqueException {
    Criteria criteria = new Criteria();
    criteria.add(FACTION_ID,factionStatistics.getID());
    criteria.add(TURN,turn);
    criteria.setLimit(1);
    List<FactionStatisticsData> data = doSelect(criteria);
    if (data != null && data.size()>0) return data.get(0);
    return null;
  }
}
