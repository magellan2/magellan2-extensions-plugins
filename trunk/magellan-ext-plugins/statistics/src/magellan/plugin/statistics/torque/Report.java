package magellan.plugin.statistics.torque;


import java.io.PrintWriter;
import java.util.List;

import magellan.library.utils.Resources;
import magellan.library.utils.UserInterface;

import org.apache.torque.om.Persistent;

/**
 * Contains informations about a computer reports
 * 
 * @author Thoralf Rickert
 */
public class Report extends BaseReport implements Persistent {
  public void save(PrintWriter pw, UserInterface ui) throws Exception {
    pw.println("<report name=\""+getFilename()+"\">");

    ui.setTitle(Resources.get("statisticsplugin.progressbar.save.title"));
    ui.show();
    
    List<FactionStatistics> factions = getFactionStatisticss();
    ui.setProgress("", 20);
    List<RegionStatistics> regions = getRegionStatisticss();
    ui.setProgress("", 40);
    List<BuildingStatistics> buildings = getBuildingStatisticss();
    ui.setProgress("", 60);
    List<UnitStatistics> units = getUnitStatisticss();
    ui.setProgress("", 80);
    List<ShipStatistics> ships = getShipStatisticss();
    ui.setProgress("", 100);
    
    int max = factions.size()+regions.size()+buildings.size()+units.size()+ships.size();
    int counter = 0;
    ui.setProgress("", 0);
    ui.setMaximum(max);
    
    for (FactionStatistics faction : factions) {
      ui.setProgress(Resources.get("statisticsplugin.progressbar.save.factions"), counter++);
      faction.save(pw);
    }
    pw.flush();
    for (RegionStatistics region : regions) {
      ui.setProgress(Resources.get("statisticsplugin.progressbar.save.regions"), counter++);
      region.save(pw);
    }
    pw.flush();
    for (BuildingStatistics building : buildings) {
      ui.setProgress(Resources.get("statisticsplugin.progressbar.save.buildings"), counter++);
      building.save(pw);
    }
    pw.flush();
    for (UnitStatistics unit : units) {
      ui.setProgress(Resources.get("statisticsplugin.progressbar.save.units"), counter++);
      unit.save(pw);
    }
    pw.flush();
    for (ShipStatistics ship : ships) {
      ui.setProgress(Resources.get("statisticsplugin.progressbar.save.ships"), counter++);
      ship.save(pw);
    }
    pw.println("</report>");

    ui.ready();
  }
}
