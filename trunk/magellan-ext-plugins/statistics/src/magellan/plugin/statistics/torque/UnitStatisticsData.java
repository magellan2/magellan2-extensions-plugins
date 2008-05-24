package magellan.plugin.statistics.torque;


import java.util.ArrayList;
import java.util.List;

import magellan.library.utils.logging.Logger;

import org.apache.torque.om.Persistent;

/**
 * Contains informations about turn specific unit data
 */
public class UnitStatisticsData extends BaseUnitStatisticsData implements Persistent {
  private static Logger log = Logger.getInstance(UnitStatisticsData.class);
  
  /**
   * 
   */
  public List<UnitStatisticsSkillData> getSkillData() {
    try {
      return getUnitStatisticsSkillDatas();
    } catch (Exception exception) {
      log.error(exception);
    }
    return new ArrayList<UnitStatisticsSkillData>();
  }

  /**
   * 
   */
  public List<UnitStatisticsItemData> getItemData() {
    try {
      return getUnitStatisticsItemDatas();
    } catch (Exception exception) {
      log.error(exception);
    }
    return new ArrayList<UnitStatisticsItemData>();
  }

}
