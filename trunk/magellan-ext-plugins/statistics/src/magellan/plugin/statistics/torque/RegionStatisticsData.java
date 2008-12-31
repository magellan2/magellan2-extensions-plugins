package magellan.plugin.statistics.torque;


import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import magellan.library.utils.logging.Logger;

import org.apache.torque.om.Persistent;

/**
 * Contains informations about turn specific region data
 */
public  class RegionStatisticsData extends BaseRegionStatisticsData implements Persistent {
  private static Logger log = Logger.getInstance(UnitStatisticsData.class);
  
  /**
   * 
   */
  public List<RegionStatisticsPricesData> getPrices() {
    try {
      return getRegionStatisticsPricesDatas();
    } catch (Exception exception) {
      log.error(exception);
    }
    return new ArrayList<RegionStatisticsPricesData>();
  }
  
  /**
   * Saves the data of this object into a XML file.
   */
  public void save(PrintWriter pw) throws Exception {
    pw.println("<data turn=\""+getTurn()+"\">");

    pw.println("<type>"+getType()+"</type>");
    pw.println("<name>"+getName()+"</name>");
    pw.println("<description>"+getDescription()+"</description>");
    pw.println("<maxRecruits>"+getMaxRecruits()+"</maxRecruits>");
    pw.println("<maxLuxuries>"+getMaxLuxuries()+"</maxLuxuries>");
    pw.println("<maxEntertain>"+getMaxEntertain()+"</maxEntertain>");
    pw.println("<stones>"+getStones()+"</stones>");
    pw.println("<trees>"+getTrees()+"</trees>");
    pw.println("<sprouts>"+getSprouts()+"</sprouts>");
    pw.println("<silver>"+getSilver()+"</silver>");
    pw.println("<peasants>"+getPeasants()+"</peasants>");
    pw.println("<inhabitants>"+getInhabitants()+"</inhabitants>");
    pw.println("<iron>"+getIron()+"</iron>");
    pw.println("<laen>"+getLaen()+"</laen>");
    pw.println("<herb>"+getHerb()+"</herb>");
    
    pw.println("<resouces>");
    for (RegionStatisticsResourcesData resource : getRegionStatisticsResourcesDatas()) {
      resource.save(pw);
    }
    pw.println("</resouces>");
    
    pw.println("<prices>");
    for (RegionStatisticsPricesData price : getRegionStatisticsPricesDatas()) {
      price.save(pw);
    }
    pw.println("</prices>");
    
    pw.println("<ships>");
    for (RegionStatisticsShipData ship : getRegionStatisticsShipDatas()) {
      ship.save(pw);
    }
    pw.println("</ships>");
    
    pw.println("</data>");
  }

}
