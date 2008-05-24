package magellan.plugin.statistics.torque;


import java.util.List;

import magellan.library.Building;
import magellan.library.utils.logging.Logger;

import org.apache.torque.om.Persistent;

/**
 * Contains informations about buildings
 */
public class BuildingStatistics extends BaseBuildingStatistics implements Persistent {
  private static Logger log = Logger.getInstance(BuildingStatistics.class);
  
  /**
   * This method checks, if there is data for the given building and the given
   * turn in the database. If it is, it updates this data otherwise this data
   * is created.
   */
  public void add(int turn, Building building) {
    try {
      // 1. get informations about this region in this turn
      BuildingStatisticsData data = BuildingStatisticsDataPeer.get(this,turn);
      
      // 2. if data doesn't exist, create a skeleton
      if (data == null) {
        data = new BuildingStatisticsData();
        data.setBuildingStatistics(this);
        data.setTurn(turn);
      }
      
      // 3. update data.
      data.setName(building.getName());
      data.setDescription(building.getDescription());
      data.setSize(building.getSize());
      if (building.getOwner() != null) data.setOwner(building.getOwner().getID().toString());
      
      // 4. save data to database.
      data.save();
    } catch (Exception exception) {
      log.error(exception);
    }
  }
  
  /**
   * Returns cached data for this object
   */
  public List<BuildingStatisticsData> getData() {
    return BuildingStatisticsDataPeer.get(this);
  }
}
