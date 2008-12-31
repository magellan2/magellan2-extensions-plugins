package magellan.plugin.statistics.torque;


import java.io.PrintWriter;
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

  /**
   * Saves the data of this object into a XML file.
   */
  public void save(PrintWriter pw) throws Exception {
    pw.println("<data unitid=\""+getUnitId()+"\" turn=\""+getTurn()+"\">");

    pw.println("<name>"+getName()+"</name>");
    pw.println("<description>"+getDescription()+"</description>");
    pw.println("<persons>"+getPersons()+"</persons>");
    pw.println("<faction>"+getFaction()+"</faction>");
    pw.println("<region>"+getRegion()+"</region>");
    pw.println("<building>"+getBuilding()+"</building>");
    pw.println("<ship>"+getShip()+"</ship>");
    pw.println("<race>"+getRace()+"</race>");
    pw.println("<weight>"+getWeight()+"</weight>");
    pw.println("<aura>"+getAura()+"</aura>");
    pw.println("<health>"+getHealth()+"</health>");
    pw.println("<hero>"+getHero()+"</hero>");
    pw.println("<guard>"+getGuard()+"</guard>");
    
    pw.println("<items>");
    for (UnitStatisticsItemData item : getUnitStatisticsItemDatas()) {
      item.save(pw);
    }
    pw.println("</items>");
    
    pw.println("<skills>");
    for (UnitStatisticsSkillData skill : getUnitStatisticsSkillDatas()) {
      skill.save(pw);
    }
    pw.println("</skills>");

    pw.println("</data>");
  }

}
