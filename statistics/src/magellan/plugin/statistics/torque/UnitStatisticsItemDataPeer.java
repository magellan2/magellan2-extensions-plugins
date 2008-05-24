package magellan.plugin.statistics.torque;

import java.util.List;

import magellan.library.utils.logging.Logger;

import org.apache.torque.util.Criteria;

/**
 * Contains informations about turn specific unit items
 */
public class UnitStatisticsItemDataPeer extends BaseUnitStatisticsItemDataPeer {
  private static Logger log = Logger.getInstance(UnitStatisticsSkillDataPeer.class);

  /**
   * Returns the data for the given unit.
   */
  public static UnitStatisticsItemData get(UnitStatistics statistics, UnitStatisticsData data, String type) {
    try {
      Criteria criteria = new Criteria();
      criteria.add(UNIT_ID,statistics.getID());
      criteria.add(TURN_ID, data.getID());
      criteria.add(ITEM_TYPE, type);
      criteria.setLimit(1);
      List<UnitStatisticsItemData> itemdata = doSelect(criteria);
      if (itemdata != null && itemdata.size()>0) return itemdata.get(0);
    } catch (Exception exception) {
      log.error(exception);
    }
    return null;
  }
}
