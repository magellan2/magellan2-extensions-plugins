package magellan.plugin.statistics.torque;


import java.util.List;

import magellan.library.Item;
import magellan.library.Skill;
import magellan.library.Unit;
import magellan.library.utils.logging.Logger;

import org.apache.torque.om.Persistent;

/**
 * Contains informations about units
 */
public class UnitStatistics extends BaseUnitStatistics implements Persistent {
  private static Logger log = Logger.getInstance(UnitStatistics.class);
  
  /**
   * This method checks, if there is data for the given unit and the given
   * turn in the database. If it is, it updates this data otherwise this data
   * is created.
   */
  public void add(int turn, Unit unit) {
    try {
      // 1. get informations about this region in this turn
      UnitStatisticsData data = UnitStatisticsDataPeer.get(this,turn);
      
      // 2. if data doesn't exist, create a skeleton
      if (data == null) {
        data = new UnitStatisticsData();
        data.setUnitStatistics(this);
        data.setTurn(turn);
      }
      
      // 3. update data.
      data.setName(unit.getName());
      data.setDescription(unit.getDescription());
      data.setPersons(unit.getPersons());
      if (unit.getFaction() != null) data.setFaction(unit.getFaction().getID().toString());
      if (unit.getRegion() != null) data.setRegion(unit.getRegion().getID().toString());
      if (unit.getBuilding() != null) data.setBuilding(unit.getBuilding().getID().toString());
      if (unit.getShip() != null) data.setShip(unit.getShip().getID().toString());
      if (unit.getRace() != null) data.setRace(unit.getRace().getID().toString());
      data.setWeight(unit.getWeight());
      data.setAura(unit.getAura());
      data.setHealth(unit.getHealth());
      data.setHero(unit.isHero());
      data.setGuard(unit.getGuard());

      // 4. save data to database.
      data.save();

      // 5. add additional informations
      for (Skill skill : unit.getSkills()) {
        String type = skill.getSkillType().getID().toString();
        UnitStatisticsSkillData skilldata = UnitStatisticsSkillDataPeer.get(this,data,type);
        if (skilldata == null) {
          skilldata = new UnitStatisticsSkillData();
          skilldata.setUnitStatistics(this);
          skilldata.setUnitStatisticsData(data);
          skilldata.setSkill(type);
        }
        skilldata.setLevel(skill.getLevel());
        skilldata.save();
      }
      for (Item item : unit.getItems()) {
        String type = item.getItemType().getID().toString();
        UnitStatisticsItemData itemdata = UnitStatisticsItemDataPeer.get(this,data,type);
        if (itemdata == null) {
          itemdata = new UnitStatisticsItemData();
          itemdata.setUnitStatistics(this);
          itemdata.setUnitStatisticsData(data);
          itemdata.setItemType(type);
        }
        itemdata.setAmount(item.getAmount());
        itemdata.save();
      }
      
      
    } catch (Exception exception) {
      log.error(exception);
    }
  }
  
  /**
   * Returns cached data for this object
   */
  public List<UnitStatisticsData> getData() {
    return UnitStatisticsDataPeer.get(this);
  }
}
