package magellan.plugin.statistics.torque;


import java.io.PrintWriter;
import java.util.List;

import magellan.library.Faction;
import magellan.library.utils.logging.Logger;

import org.apache.torque.om.Persistent;

/**
 * Contains informations about factions
 */
public class FactionStatistics extends BaseFactionStatistics implements Persistent {
  private static Logger log = Logger.getInstance(FactionStatistics.class);
  
  /**
   * This method checks, if there is data for the given faction and the given
   * turn in the database. If it is, it updates this data otherwise this data
   * is created.
   */
  public void add(int turn, Faction faction) {
    try {
      // 1. get informations about this faction in this turn
      FactionStatisticsData data = FactionStatisticsDataPeer.get(this,turn);
      
      // 2. if data doesn't exist, create a skeleton
      if (data == null) {
        data = new FactionStatisticsData();
        data.setFactionStatistics(this);
        data.setTurn(turn);
      }
      
      // 3. update data.
      data.setName(faction.getName());
      data.setDescription(faction.getDescription());
      data.setPersons(faction.getPersons());
      if (faction.getRace() != null) data.setRace(faction.getRace().getID().toString());
      data.setHeroes(faction.getHeroes());
      data.setMaxHeroes(faction.getMaxHeroes());
      data.setMaxMigrants(faction.getMaxMigrants());
      data.setAverageScore(faction.getAverageScore());
      data.setScore(faction.getScore());
      
      // 4. save data to database.
      data.save();
    } catch (Exception exception) {
      log.error(exception);
    }
  }
  
  /**
   * Returns cached data for this object
   */
  public List<FactionStatisticsData> getData() {
    return FactionStatisticsDataPeer.get(this);
  }
  
  public void save(PrintWriter pw) throws Exception {
    pw.println("<faction id=\""+getFactionNumber()+"\">");
    for (FactionStatisticsData data : getFactionStatisticsDatas()) {
      data.save(pw);
    }
    pw.println("</faction>");
  }
}
