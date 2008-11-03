// class magellan.plugin.statistics.Statistics
// created on 03.05.2008
//
// Copyright 2003-2008 by Thoralf Rickert
//
// Author : $Author: $
// $Id: $
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program (see doc/LICENCE.txt); if not, write to the
// Free Software Foundation, Inc., 
// 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
// 
package magellan.plugin.statistics;

import java.util.Collection;

import magellan.library.Building;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Named;
import magellan.library.Region;
import magellan.library.Ship;
import magellan.library.Unit;
import magellan.library.utils.logging.Logger;
import magellan.plugin.statistics.torque.BuildingStatistics;
import magellan.plugin.statistics.torque.BuildingStatisticsPeer;
import magellan.plugin.statistics.torque.FactionStatistics;
import magellan.plugin.statistics.torque.FactionStatisticsPeer;
import magellan.plugin.statistics.torque.RegionStatistics;
import magellan.plugin.statistics.torque.RegionStatisticsPeer;
import magellan.plugin.statistics.torque.Report;
import magellan.plugin.statistics.torque.ReportPeer;
import magellan.plugin.statistics.torque.ShipStatistics;
import magellan.plugin.statistics.torque.ShipStatisticsPeer;
import magellan.plugin.statistics.torque.UnitStatistics;
import magellan.plugin.statistics.torque.UnitStatisticsPeer;

/**
 * This container holds all useful data of a report.
 *
 * @author Thoralf Rickert
 * @version 1.0, 03.05.2008
 */
public class Statistics {
  private static Logger log = Logger.getInstance(Statistics.class);
  protected Report report = null;
  protected boolean shutdown = false;
  protected int percentage = 0;
  protected AnalyzeState state = AnalyzeState.INITIALIZED;
  protected Named currentObject = null;
  protected long runtime = 0;
  
  public Statistics(String reportFile) {
    report = ReportPeer.getReport(reportFile,true);
    
    log.info("Loaded report "+report.getID());
  }
  
  /**
   * Adds a report to the current known statistics.
   */
  protected void add(GameData world) {
    int turn = world.getDate().getDate();
    long timestamp = world.getTimestamp();
    
    if (report.getLastSave() == timestamp) {
      // we do not compare anything...it's the same report
      log.info("Abort statistics merge process. Report is the same");
      return;
    }
    
    long startTime = System.currentTimeMillis();
    
    runtime = System.currentTimeMillis() - startTime; 
    
    Collection<Faction> factions = world.factions().values(); 
    Collection<Region> regions = world.regions().values(); 
    Collection<Unit> units = world.units().values(); 
    Collection<Building> buildings = world.buildings().values(); 
    Collection<Ship> ships = world.ships().values(); 
    
    
    percentage = 0;
    state = AnalyzeState.INITIALIZED;
    int counter = 0;
    int max = 0;
    max += factions.size();
    max += regions.size();
    max += units.size();
    max += buildings.size();
    max += ships.size();
    
    state = AnalyzeState.FACTION;
    for (Faction faction : factions) {
      if (shutdown) break;
      currentObject = faction;
      String factionId = faction.getID().toString();
      FactionStatistics statistics = FactionStatisticsPeer.get(report,factionId,true);
      if (statistics != null) statistics.add(turn,faction);
      runtime = System.currentTimeMillis() - startTime; 
      percentage = counter * 100 / max;
      counter++;
    }
    if (shutdown) return;
    
    state = AnalyzeState.REGION;
    for (Region region : regions) {
      if (shutdown) break;
      currentObject = region;
      String regionId = region.getID().toString();
      RegionStatistics statistics = RegionStatisticsPeer.get(report,regionId,true);
      if (statistics != null) statistics.add(turn,region);
      runtime = System.currentTimeMillis() - startTime; 
      percentage = counter * 100 / max;
      counter++;
    }
    if (shutdown) return;
    
    state = AnalyzeState.UNIT;
    for (Unit unit : units) {
      if (shutdown) break;
      currentObject = unit;
      String unitId = unit.getID().toString();
      UnitStatistics statistics = UnitStatisticsPeer.get(report,unitId,true);
      if (statistics != null) statistics.add(turn,unit);
      runtime = System.currentTimeMillis() - startTime; 
      percentage = counter * 100 / max;
      counter++;
    }
    if (shutdown) return;
    
    state = AnalyzeState.BUILDING;
    for (Building building : buildings) {
      if (shutdown) break;
      currentObject = building;
      String buildingId = building.getID().toString();
      BuildingStatistics statistics = BuildingStatisticsPeer.get(report,buildingId,building.getType().getName(),true);
      if (statistics != null) statistics.add(turn,building);
      runtime = System.currentTimeMillis() - startTime; 
      percentage = counter * 100 / max;
      counter++;
    }
    if (shutdown) return;

    state = AnalyzeState.SHIP;
    for (Ship ship : ships) {
      if (shutdown) break;
      currentObject = ship;
      String shipId = ship.getID().toString();
      ShipStatistics statistics = ShipStatisticsPeer.get(report,shipId,ship.getType().getName(),true);
      if (statistics != null) statistics.add(turn,ship);
      runtime = System.currentTimeMillis() - startTime; 
      percentage = counter * 100 / max;
      counter++;
    }
    if (shutdown) return;
    
    state = AnalyzeState.SAVE;

    try {
      if (report.getLastSave() == 0) {
        report.setLastSave(timestamp);
        report.save();
      }
    } catch (Exception exception) {
      log.error(exception);
    }
    
    runtime = System.currentTimeMillis() - startTime; 
    state = AnalyzeState.FINISHED;
    percentage = 100;
  }
  
  /**
   * Returns the statistic data for the given unit.
   */
  public UnitStatistics getStatistics(Unit unit) {
    if (unit == null || unit.getID() == null) return null;
    String unitId = unit.getID().toString();
    return UnitStatisticsPeer.get(report, unitId, false);
  }

  /**
   * Returns the statistic data for the given region.
   */
  public RegionStatistics getStatistics(Region region) {
    if (region == null || region.getID() == null) return null;
    String regionId = region.getID().toString();
    return RegionStatisticsPeer.get(report, regionId, false);
  }

  /**
   * Returns the statistic data for the given faction.
   */
  public FactionStatistics getStatistics(Faction faction) {
    if (faction == null || faction.getID() == null) return null;
    String factionId = faction.getID().toString();
    return FactionStatisticsPeer.get(report, factionId, false);
  }

  /**
   * Returns the value of shutdown.
   * 
   * @return Returns shutdown.
   */
  public boolean isShutdown() {
    return shutdown;
  }

  /**
   * Sets the value of shutdown.
   * 
   * Setting this value to true prevents the thread
   * from iterating thru all objects in the GameData
   * and returns the add() method as fast as possible
   * without killing the process.
   *
   * @param shutdown The value for shutdown.
   */
  public void setShutdown(boolean shutdown) {
    this.shutdown = shutdown;
  }

  /**
   * Returns the value of percentage.
   * 
   * @return Returns percentage.
   */
  public int getPercentage() {
    return percentage;
  }

  /**
   * Sets the value of percentage.
   *
   * @param percentage The value for percentage.
   */
  public void setPercentage(int percentage) {
    this.percentage = percentage;
  }

  /**
   * Returns the value of state.
   * 
   * @return Returns state.
   */
  public AnalyzeState getState() {
    return state;
  }

  /**
   * Sets the value of state.
   *
   * @param state The value for state.
   */
  public void setState(AnalyzeState state) {
    this.state = state;
  }

  /**
   * Returns the value of currentObject.
   * 
   * @return Returns currentObject.
   */
  public Named getCurrentObject() {
    return currentObject;
  }

  /**
   * Sets the value of currentObject.
   *
   * @param currentObject The value for currentObject.
   */
  public void setCurrentObject(Named currentObject) {
    this.currentObject = currentObject;
  }

  /**
   * Returns the value of runtime.
   * 
   * @return Returns runtime.
   */
  public long getRuntime() {
    return runtime;
  }

  /**
   * Sets the value of runtime.
   *
   * @param runtime The value for runtime.
   */
  public void setRuntime(long runtime) {
    this.runtime = runtime;
  }
  
  
}
