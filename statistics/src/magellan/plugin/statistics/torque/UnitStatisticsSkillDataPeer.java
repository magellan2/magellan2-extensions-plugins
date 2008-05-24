package magellan.plugin.statistics.torque;

import java.util.List;

import magellan.library.utils.logging.Logger;

import org.apache.torque.util.Criteria;

/**
 * Contains informations about turn specific unit skills
 */
public class UnitStatisticsSkillDataPeer extends BaseUnitStatisticsSkillDataPeer {
  private static Logger log = Logger.getInstance(UnitStatisticsSkillDataPeer.class);

  /**
   * Returns the data for the given unit.
   */
  public static UnitStatisticsSkillData get(UnitStatistics statistics, UnitStatisticsData data, String type) {
    try {
      Criteria criteria = new Criteria();
      criteria.add(UNIT_ID,statistics.getID());
      criteria.add(TURN_ID, data.getID());
      criteria.add(SKILL, type);
      criteria.setLimit(1);
      List<UnitStatisticsSkillData> skill = doSelect(criteria);
      if (skill != null && skill.size()>0) return skill.get(0);
    } catch (Exception exception) {
      log.error(exception);
    }
    return null;
  }
}
