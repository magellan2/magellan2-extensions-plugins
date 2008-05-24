package magellan.plugin.statistics.torque;


import java.util.List;

import magellan.library.Ship;
import magellan.library.utils.logging.Logger;

import org.apache.torque.om.Persistent;

/**
 * Contains informations about ships
 */
public class ShipStatistics extends BaseShipStatistics implements Persistent {
  private static Logger log = Logger.getInstance(ShipStatistics.class);
  
  /**
   * This method checks, if there is data for the given ship and the given
   * turn in the database. If it is, it updates this data otherwise this data
   * is created.
   */
  public void add(int turn, Ship ship) {
    try {
      // 1. get informations about this region in this turn
      ShipStatisticsData data = ShipStatisticsDataPeer.get(this,turn);
      
      // 2. if data doesn't exist, create a skeleton
      if (data == null) {
        data = new ShipStatisticsData();
        data.setShipStatistics(this);
        data.setTurn(turn);
      }
      
      // 3. update data.
      if (getType() == null) {
        setType(ship.getShipType().toString());
        save();
      }
        
      data.setName(ship.getName());
      data.setDescription(ship.getDescription());
      data.setSize(ship.getSize());
      if (ship.getOwner() != null) data.setOwner(ship.getOwner().getID().toString());
      data.setRegion(ship.getRegion().getID().toString());
      data.setMaxCargo(ship.getMaxCapacity());
      data.setCargo(ship.getCargo());
      data.setCapacity(ship.getCapacity());
      data.setDamageRatio(ship.getDamageRatio());
      
      // 4. save data to database.
      data.save();
    } catch (Exception exception) {
      log.error(exception);
    }
  }
  
  /**
   * Returns cached data for this object
   */
  public List<ShipStatisticsData> getData() {
    return ShipStatisticsDataPeer.get(this);
  }
}
