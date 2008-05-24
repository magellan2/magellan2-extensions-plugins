package magellan.plugin.statistics.torque;


import java.util.Collection;
import java.util.List;

import magellan.library.LuxuryPrice;
import magellan.library.Region;
import magellan.library.RegionResource;
import magellan.library.Ship;
import magellan.library.Unit;
import magellan.library.utils.logging.Logger;

import org.apache.torque.om.Persistent;

/**
 * Contains informations about regions
 */
public class RegionStatistics extends BaseRegionStatistics implements Persistent {
  private static Logger log = Logger.getInstance(RegionStatistics.class);
  
  /**
   * This method checks, if there is data for the given region and the given
   * turn in the database. If it is, it updates this data otherwise this data
   * is created.
   */
  public void add(int turn, Region region) {
    try {
      // 1. get informations about this region in this turn
      RegionStatisticsData data = RegionStatisticsDataPeer.get(this,turn);
      
      // 2. if data doesn't exist, create a skeleton
      if (data == null) {
        data = new RegionStatisticsData();
        data.setRegionStatistics(this);
        data.setTurn(turn);
      }
      
      // 3. update data.
      data.setType(region.getRegionType().toString());
      data.setName(region.getName());
      data.setDescription(region.getDescription());
      data.setLaen(region.getLaen());
      data.setIron(region.getIron());
      data.setPeasants(region.getPeasants());
      data.setInhabitants(count(region.units()));
      data.setSilver(region.getSilver());
      data.setSprouts(region.getSprouts());
      data.setTrees(region.getTrees());
      data.setStones(region.getStones());
      data.setMaxEntertain(region.maxEntertain());
      data.setMaxLuxuries(region.maxLuxuries());
      data.setMaxRecruits(region.maxRecruit());
      
      // 4. save data to database.
      data.save();
      
      // 5. add additional informations
      if (region.getPrices() != null) {
        for (LuxuryPrice prices : region.getPrices().values()) {
          String type = prices.getItemType().toString();
          RegionStatisticsPricesData price = RegionStatisticsPricesDataPeer.get(this,data,type);
          if (price == null) {
            price = new RegionStatisticsPricesData();
            price.setRegionStatistics(this);
            price.setRegionStatisticsData(data);
            price.setLuxuryItem(type);
          }
          
          price.setPrice(prices.getPrice());
          price.save();
        }
      }
      
      for (RegionResource resource : region.resources()) {
        String type = resource.getType().toString();
        RegionStatisticsResourcesData res = RegionStatisticsResourcesDataPeer.get(this,data,type);
        if (res == null) {
          res = new RegionStatisticsResourcesData();
          res.setRegionStatistics(this);
          res.setRegionStatisticsData(data);
          res.setItemType(type);
        }
        
        res.setSkillLevel(resource.getSkillLevel());
        res.setAmount(resource.getAmount());
        res.save();
      }
      
      for (Ship ship : region.ships()) {
        String id = ship.getID().toString();
        RegionStatisticsShipData shipdata = RegionStatisticsShipDataPeer.get(this,data,id);
        if (shipdata == null) {
          shipdata = new RegionStatisticsShipData();
          shipdata.setRegionStatisticsData(data);
          shipdata.setShipNumber(id);
          shipdata.save();
        }
      }
      
    } catch (Exception exception) {
      log.error(exception);
    }
  }
  
  /**
   * Returns cached data for this object
   */
  public List<RegionStatisticsData> getData() {
    return RegionStatisticsDataPeer.get(this);
  }
  
  protected int count(Collection<Unit> units) {
    int counter = 0;
    for (Unit unit : units) counter += unit.getPersons();
    return counter;
  }
}
