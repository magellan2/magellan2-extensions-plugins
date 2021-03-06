package magellan.plugin.statistics.torque;

import java.util.List;

import magellan.library.utils.logging.Logger;

import org.apache.torque.util.Criteria;

/**
 * Contains informations about regions
 *
 * The skeleton for this class was autogenerated by Torque on:
 *
 * [Wed May 21 18:19:59 CEST 2008]
 *
 *  You should add additional methods to this class to meet the
 *  application requirements.  This class will only be generated as
 *  long as it does not already exist in the output directory.
 */
public class RegionStatisticsPeer extends BaseRegionStatisticsPeer {
  /** Log instance */
  private static Logger log = Logger.getInstance(FactionStatisticsPeer.class);
  
  /**
   * Search for a region.
   */
  public static RegionStatistics get(Report report, String regionId, boolean create) {
    try {
      Criteria criteria = new Criteria();
      criteria.add(REPORT_ID,report.getID());
      criteria.add(REGION_NUMBER,regionId);
      criteria.setLimit(1);
      List<RegionStatistics> statistics = doSelect(criteria);
      
      if (statistics != null && statistics.size() > 0) return statistics.get(0); 
      
      if (create) {
        RegionStatistics statistic = new RegionStatistics();
        statistic.setReport(report);
        statistic.setRegionNumber(regionId);
        statistic.save();
        return statistic;
      }

    } catch (Exception exception) {
      log.error(exception);
    }
    return null;
  }
}
