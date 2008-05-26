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

import magellan.library.Building;
import magellan.library.Faction;
import magellan.library.GameData;
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
  
  public Statistics(String reportFile) {
    report = ReportPeer.getReport(reportFile,true);
    
    log.info("Loaded report "+report.getID());
  }
  
  /**
   * Adds a report to the current known statistics.
   */
  protected void add(GameData world) {
    int turn = world.getDate().getDate();
    
    for (Faction faction : world.factions().values()) {
      String factionId = faction.getID().toString();
      FactionStatistics statistics = FactionStatisticsPeer.get(report,factionId,true);
      if (statistics != null) statistics.add(turn,faction);
    }
    
    for (Region region : world.regions().values()) {
      String regionId = region.getID().toString();
      RegionStatistics statistics = RegionStatisticsPeer.get(report,regionId,true);
      if (statistics != null) statistics.add(turn,region);
    }
    
    for (Unit unit : world.units().values()) {
      String unitId = unit.getID().toString();
      UnitStatistics statistics = UnitStatisticsPeer.get(report,unitId,true);
      if (statistics != null) statistics.add(turn,unit);
    }
    
    for (Building building : world.buildings().values()) {
      String buildingId = building.getID().toString();
      BuildingStatistics statistics = BuildingStatisticsPeer.get(report,buildingId,building.getType().getName(),true);
      if (statistics != null) statistics.add(turn,building);
    }

    for (Ship ship : world.ships().values()) {
      String shipId = ship.getID().toString();
      ShipStatistics statistics = ShipStatisticsPeer.get(report,shipId,ship.getType().getName(),true);
      if (statistics != null) statistics.add(turn,ship);
    }

    
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
}
