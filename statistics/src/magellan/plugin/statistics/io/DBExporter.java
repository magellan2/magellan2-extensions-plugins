// class magellan.plugin.statistics.io.DBExporter
// created on 30.01.2009
//
// Copyright 2003-2009 by Thoralf Rickert
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
package magellan.plugin.statistics.io;

import java.io.File;
import java.io.PrintStream;
import java.sql.Connection;
import java.util.Enumeration;
import java.util.List;

import org.apache.torque.TorqueException;
import org.apache.torque.util.Criteria;

import magellan.plugin.statistics.db.CriteriaEnumeration;
import magellan.plugin.statistics.db.CriteriaPopulator;
import magellan.plugin.statistics.db.DerbyConnector;
import magellan.plugin.statistics.torque.BuildingStatistics;
import magellan.plugin.statistics.torque.BuildingStatisticsData;
import magellan.plugin.statistics.torque.BuildingStatisticsDataPeer;
import magellan.plugin.statistics.torque.BuildingStatisticsPeer;
import magellan.plugin.statistics.torque.FactionStatistics;
import magellan.plugin.statistics.torque.FactionStatisticsData;
import magellan.plugin.statistics.torque.FactionStatisticsDataPeer;
import magellan.plugin.statistics.torque.FactionStatisticsPeer;
import magellan.plugin.statistics.torque.RegionStatistics;
import magellan.plugin.statistics.torque.RegionStatisticsData;
import magellan.plugin.statistics.torque.RegionStatisticsDataPeer;
import magellan.plugin.statistics.torque.RegionStatisticsPeer;
import magellan.plugin.statistics.torque.RegionStatisticsPricesData;
import magellan.plugin.statistics.torque.RegionStatisticsPricesDataPeer;
import magellan.plugin.statistics.torque.RegionStatisticsResourcesData;
import magellan.plugin.statistics.torque.RegionStatisticsResourcesDataPeer;
import magellan.plugin.statistics.torque.RegionStatisticsShipData;
import magellan.plugin.statistics.torque.RegionStatisticsShipDataPeer;
import magellan.plugin.statistics.torque.Report;
import magellan.plugin.statistics.torque.ReportPeer;
import magellan.plugin.statistics.torque.ShipStatistics;
import magellan.plugin.statistics.torque.ShipStatisticsData;
import magellan.plugin.statistics.torque.ShipStatisticsDataPeer;
import magellan.plugin.statistics.torque.ShipStatisticsPeer;
import magellan.plugin.statistics.torque.UnitStatistics;
import magellan.plugin.statistics.torque.UnitStatisticsData;
import magellan.plugin.statistics.torque.UnitStatisticsDataPeer;
import magellan.plugin.statistics.torque.UnitStatisticsItemData;
import magellan.plugin.statistics.torque.UnitStatisticsItemDataPeer;
import magellan.plugin.statistics.torque.UnitStatisticsPeer;
import magellan.plugin.statistics.torque.UnitStatisticsSkillData;
import magellan.plugin.statistics.torque.UnitStatisticsSkillDataPeer;

/**
 * This class exports all data from the given database
 * to a file, that can be inserted in another database.
 *
 * @author Thoralf Rickert
 * @version 1.0, 30.01.2009
 */
public class DBExporter {

  /**
   * @param args
   */
  public static void main(String[] args) throws Exception {
    File magellanDir = new File("D:/Eressea/Magellan/");
    File settingsDir = magellanDir;
    
    System.getProperties().setProperty("user", "statistics");
    System.getProperties().setProperty("password", "statistics");
    System.getProperties().setProperty("derby.system.home", magellanDir.getAbsolutePath());

    
    DerbyConnector.getInstance().init(null, magellanDir, settingsDir);
    
    System.out.println("Database connection established...");
    PrintStream out = new PrintStream(new File("D:/Eressea/statistics.sql"));
    int counter = 0;
    
    System.out.println("Exporting Report...");
    Enumeration<Report> reports = getReportEnumeration();
    counter = 0;
    while (reports.hasMoreElements()) {
      Report report = reports.nextElement();
      out.println("INSERT INTO report (id,filename,lastsave) VALUES ("+report.getID()+",\""+report.getFilename()+"\",\""+report.getLastSave()+"\");");
      counter++;
      if ((counter % 80) == 0) System.out.println();
      System.out.print(".");
    }
    System.out.println();System.gc();
    out.flush();
    
    System.out.println("Exporting FactionStatistics...");
    Enumeration<FactionStatistics> factions = getFactionEnumeration();
    counter = 0;
    while (factions.hasMoreElements()) {
      FactionStatistics faction = factions.nextElement();
      out.println("INSERT INTO faction_statistics (id,report_id,faction_number) VALUES ("+faction.getID()+","+faction.getReportId()+",\""+faction.getFactionNumber()+"\");");
      counter++;
      if ((counter % 80) == 0) System.out.println();
      System.out.print(".");
    }
    
    System.out.println();System.gc();
    out.flush();
    
    System.out.println("Exporting FactionStatisticsData...");
    Enumeration<FactionStatisticsData> factionsData = getFactionDataEnumeration();
    counter = 0;
    while (factionsData.hasMoreElements()) {
      FactionStatisticsData data = factionsData.nextElement();
      String description = data.getDescription();
      if (description == null) description = "(null)";
      if (description.equalsIgnoreCase("(null)")) description = "NULL"; else description = "\""+description.replaceAll("\"", "'")+"\"";
      out.println("INSERT INTO faction_statistics_data (id,faction_id,turn,name,description,persons,units,race,heroes,max_heroes,max_migrants,average_score,score) VALUES ("+data.getID()+","+data.getFactionId()+","+data.getTurn()+",\""+data.getName()+"\","+description+","+data.getPersons()+","+data.getUnits()+",\""+data.getRace()+"\","+data.getHeroes()+","+data.getMaxHeroes()+","+data.getMaxMigrants()+","+data.getAverageScore()+","+data.getScore()+");");
      counter++;
      if ((counter % 80) == 0) System.out.println();
      System.out.print(".");
    }
    
    System.out.println();System.gc();
    out.flush();
    
    System.out.println("Exporting RegionStatistics...");
    Enumeration<RegionStatistics> regions = getRegionEnumeration();
    counter = 0;
    while (regions.hasMoreElements()) {
      RegionStatistics region = regions.nextElement();
      out.println("INSERT INTO region_statistics (id,report_id,region_number) VALUES ("+region.getID()+","+region.getReportId()+",\""+region.getRegionNumber()+"\");");
      counter++;
      if ((counter % 80) == 0) System.out.println();
      System.out.print(".");
    }
    
    System.out.println();System.gc();
    out.flush();
    
    System.out.println("Exporting RegionStatisticsData...");
    Enumeration<RegionStatisticsData> regionsData = getRegionDataEnumeration();
    counter = 0;
    while (regionsData.hasMoreElements()) {
      RegionStatisticsData data = regionsData.nextElement();
      String description = data.getDescription();
      if (description == null) description = "(null)";
      if (description.equalsIgnoreCase("(null)")) description = "NULL"; else description = "\""+description.replaceAll("\"", "'")+"\"";
      out.println("INSERT INTO region_statistics_data (id,region_id,turn,type,name,description,max_recruits,max_luxuries,max_entertain,stones,trees,sprouts,silver,peasants,inhabitants,iron,laen,herb) VALUES ("+data.getID()+","+data.getRegionId()+","+data.getTurn()+",\""+data.getType()+"\",\""+data.getName()+"\","+description+","+data.getMaxRecruits()+","+data.getMaxLuxuries()+","+data.getMaxEntertain()+","+data.getStones()+","+data.getTrees()+","+data.getSprouts()+","+data.getSilver()+","+data.getPeasants()+","+data.getInhabitants()+","+data.getIron()+","+data.getLaen()+",\""+data.getHerb()+"\");");
      counter++;
      if ((counter % 80) == 0) System.out.println();
      System.out.print(".");
    }
    
    System.out.println();System.gc();
    out.flush();
    
    System.out.println("Exporting RegionStatisticsResourcesData...");
    Enumeration<RegionStatisticsResourcesData> regionsDataResources = getRegionDataResourcesEnumeration();
    counter = 0;
    while (regionsDataResources.hasMoreElements()) {
      RegionStatisticsResourcesData data = regionsDataResources.nextElement();
      out.println("INSERT INTO region_statistics_resources_data (id,turn_id,region_id,item_type,skill_level,amount) VALUES ("+data.getID()+","+data.getTurnId()+","+data.getRegionId()+",\""+data.getItemType()+"\","+data.getSkillLevel()+","+data.getAmount()+");");
      counter++;
      if ((counter % 80) == 0) System.out.println();
      System.out.print(".");
    }
    
    System.out.println();System.gc();
    out.flush();
    
    System.out.println("Exporting RegionStatisticsPricesData...");
    Enumeration<RegionStatisticsPricesData> regionsDataPrices = getRegionDataPricesEnumeration();
    counter = 0;
    while (regionsDataPrices.hasMoreElements()) {
      RegionStatisticsPricesData data = regionsDataPrices.nextElement();
      out.println("INSERT INTO region_statistics_prices_data (id,turn_id,region_id,luxury_item,price) VALUES ("+data.getID()+","+data.getTurnId()+","+data.getRegionId()+",\""+data.getLuxuryItem()+"\","+data.getPrice()+");");
      counter++;
      if ((counter % 80) == 0) System.out.println();
      System.out.print(".");
    }
    
    System.out.println();System.gc();
    out.flush();
    
    System.out.println("Exporting RegionStatisticsShipData...");
    Enumeration<RegionStatisticsShipData> regionsDataShips = getRegionDataShipEnumeration();
    counter = 0;
    while (regionsDataShips.hasMoreElements()) {
      RegionStatisticsShipData data = regionsDataShips.nextElement();
      out.println("INSERT INTO region_statistics_ship_data (id,turn_id,ship_number) VALUES ("+data.getID()+","+data.getTurnId()+",\""+data.getShipNumber()+"\""+");");
      counter++;
      if ((counter % 80) == 0) System.out.println();
      System.out.print(".");
    }

    System.out.println();System.gc();
    out.flush();
    
    System.out.println("Exporting BuildingStatistics...");
    Enumeration<BuildingStatistics> buildings = getBuildingsEnumeration();
    counter = 0;
    while (buildings.hasMoreElements()) {
      BuildingStatistics data = buildings.nextElement();
      out.println("INSERT INTO building_statistics (id,report_id,building_number,type) VALUES ("+data.getID()+","+data.getReportId()+",\""+data.getBuildingNumber()+"\",\""+data.getType()+"\""+");");
      counter++;
      if ((counter % 80) == 0) System.out.println();
      System.out.print(".");
    }

    System.out.println();System.gc();
    out.flush();
    
    System.out.println("Exporting BuildingStatisticsData...");
    Enumeration<BuildingStatisticsData> buildingsData = getBuildingsDataEnumeration();
    counter = 0;
    while (buildingsData.hasMoreElements()) {
      BuildingStatisticsData data = buildingsData.nextElement();
      String description = data.getDescription();
      if (description == null) description = "(null)";
      if (description.equalsIgnoreCase("(null)")) description = "NULL"; else description = "\""+description.replaceAll("\"", "'")+"\"";
      out.println("INSERT INTO building_statistics_data (id,building_id,turn,name,description,size,owner,inmates) VALUES ("+data.getID()+","+data.getBuildingId()+","+data.getTurn()+",\""+data.getName()+"\","+description+","+data.getSize()+",\""+data.getOwner()+"\","+data.getInmates()+");");
      counter++;
      if ((counter % 80) == 0) System.out.println();
      System.out.print(".");
    }

    System.out.println();System.gc();
    out.flush();
    
    System.out.println("Exporting UnitStatistics...");
    Enumeration<UnitStatistics> units = getUnitsEnumeration();
    counter = 0;
    while (units.hasMoreElements()) {
      UnitStatistics data = units.nextElement();
      out.println("INSERT INTO unit_statistics (id,report_id,unit_number) VALUES ("+data.getID()+","+data.getReportId()+",\""+data.getUnitNumber()+"\""+");");
      counter++;
      if ((counter % 80) == 0) System.out.println();
      System.out.print(".");
    }

    System.out.println();System.gc();
    out.flush();
    
    System.out.println("Exporting UnitStatisticsData...");
    Enumeration<UnitStatisticsData> unitsData = getUnitsDataEnumeration();
    counter = 0;
    while (unitsData.hasMoreElements()) {
      UnitStatisticsData data = unitsData.nextElement();
      String description = data.getDescription();
      if (description == null) description = "(null)";
      if (description.equalsIgnoreCase("(null)")) description = "NULL"; else description = "\""+description.replaceAll("\"", "'")+"\"";
      out.println("INSERT INTO unit_statistics_data (id,unit_id,turn,name,description,persons,faction,region,building,ship,race,weight,aura,health,hero,guard) VALUES ("+data.getID()+","+data.getUnitId()+","+data.getTurn()+",\""+data.getName()+"\","+description+","+data.getPersons()+",\""+data.getFaction()+"\",\""+data.getRegion()+"\",\""+data.getBuilding()+"\",\""+data.getShip()+"\",\""+data.getRace()+"\","+data.getWeight()+","+data.getAura()+",\""+data.getHealth()+"\","+data.getHero()+","+data.getGuard()+");");
      counter++;
      if ((counter % 80) == 0) System.out.println();
      System.out.print(".");
    }

    System.out.println();System.gc();
    out.flush();
    
    System.out.println("Exporting UnitStatisticsItemData...");
    Enumeration<UnitStatisticsItemData> unitsDataItems = getUnitsDataItemEnumeration();
    counter = 0;
    while (unitsDataItems.hasMoreElements()) {
      UnitStatisticsItemData data = unitsDataItems.nextElement();
      out.println("INSERT INTO unit_statistics_item_data (id,turn_id,unit_id,item_type,amount) VALUES ("+data.getID()+","+data.getTurnId()+","+data.getUnitId()+",\""+data.getItemType()+"\","+data.getAmount()+");");
      counter++;
      if ((counter % 80) == 0) System.out.println();
      System.out.print(".");
    }

    System.out.println();System.gc();
    out.flush();
    
    System.out.println("Exporting UnitStatisticsSkillData...");
    Enumeration<UnitStatisticsSkillData> unitsDataSkills = getUnitsDataSkillEnumeration();
    counter = 0;
    while (unitsDataSkills.hasMoreElements()) {
      UnitStatisticsSkillData data = unitsDataSkills.nextElement();
      out.println("INSERT INTO unit_statistics_skill_data (id,turn_id,unit_id,skill,level) VALUES ("+data.getID()+","+data.getTurnId()+","+data.getUnitId()+",\""+data.getSkill()+"\","+data.getLevel()+");");
      counter++;
      if ((counter % 80) == 0) System.out.println();
      System.out.print(".");
    }

    System.out.println();System.gc();
    out.flush();
    
    System.out.println("Exporting ShipStatistics...");
    Enumeration<ShipStatistics> ships = getShipsEnumeration();
    counter = 0;
    while (ships.hasMoreElements()) {
      ShipStatistics data = ships.nextElement();
      out.println("INSERT INTO ship_statistics (id,report_id,ship_number,type) VALUES ("+data.getID()+","+data.getReportId()+",\""+data.getShipNumber()+"\",\""+data.getType()+"\""+");");
      counter++;
      if ((counter % 80) == 0) System.out.println();
      System.out.print(".");
    }

    System.out.println();System.gc();
    out.flush();
    
    System.out.println("Exporting ShipStatisticsData...");
    Enumeration<ShipStatisticsData> shipsData = getShipsDataEnumeration();
    counter = 0;
    while (shipsData.hasMoreElements()) {
      ShipStatisticsData data = shipsData.nextElement();
      String description = data.getDescription();
      if (description == null) description = "(null)";
      if (description.equalsIgnoreCase("(null)")) description = "NULL"; else description = "\""+description.replaceAll("\"", "'")+"\"";
      out.println("INSERT INTO ship_statistics_data (id,ship_id,turn,name,description,size,owner,region,passengers,max_cargo,cargo,capacity,damage_ratio) VALUES ("+data.getID()+","+data.getShipId()+","+data.getTurn()+",\""+data.getName()+"\","+description+","+data.getSize()+",\""+data.getOwner()+"\",\""+data.getRegion()+"\","+data.getPassengers()+","+data.getMaxCargo()+","+data.getCargo()+","+data.getCapacity()+","+data.getDamageRatio()+");");
      counter++;
      if ((counter % 80) == 0) System.out.println();
      System.out.print(".");
    }
    System.out.println();

    out.flush();
    out.close();
    
    
    System.out.println("System is shutting down...");
    
    DerbyConnector.getInstance().shutdown();
  }

  /**
   * 
   */
  private static Enumeration<Report> getReportEnumeration() {
    CriteriaEnumeration<Report> enumeration = new CriteriaEnumeration<Report>();
    enumeration.setLimit(1000);
    Criteria criteria = new Criteria();
    criteria.addAscendingOrderByColumn(ReportPeer.FILENAME);
    criteria.addAscendingOrderByColumn(ReportPeer.LASTSAVE);
    enumeration.setCriteria(criteria);
    enumeration.setPopulator(new CriteriaPopulator<Report>() {
      public List<Report> getRows(Connection connection, Criteria criteria) {
        try {
          return ReportPeer.doSelect(criteria, connection);
        } catch (TorqueException e) {
          e.printStackTrace();
          return null;
        }
      }
    });
    return enumeration;
  }

  /**
   * 
   */
  private static Enumeration<FactionStatistics> getFactionEnumeration() {
    CriteriaEnumeration<FactionStatistics> enumeration = new CriteriaEnumeration<FactionStatistics>();
    enumeration.setLimit(1000);
    Criteria criteria = new Criteria();
    criteria.addAscendingOrderByColumn(FactionStatisticsPeer.REPORT_ID);
    criteria.addAscendingOrderByColumn(FactionStatisticsPeer.FACTION_NUMBER);
    enumeration.setCriteria(criteria);
    enumeration.setPopulator(new CriteriaPopulator<FactionStatistics>() {
      public List<FactionStatistics> getRows(Connection connection, Criteria criteria) {
        try {
          return FactionStatisticsPeer.doSelect(criteria, connection);
        } catch (TorqueException e) {
          e.printStackTrace();
          return null;
        }
      }
    });
    return enumeration;
  }

  /**
   * 
   */
  private static Enumeration<FactionStatisticsData> getFactionDataEnumeration() {
    CriteriaEnumeration<FactionStatisticsData> enumeration = new CriteriaEnumeration<FactionStatisticsData>();
    enumeration.setLimit(1000);
    Criteria criteria = new Criteria();
    criteria.addAscendingOrderByColumn(FactionStatisticsDataPeer.FACTION_ID);
    criteria.addAscendingOrderByColumn(FactionStatisticsDataPeer.TURN);
    enumeration.setCriteria(criteria);
    enumeration.setPopulator(new CriteriaPopulator<FactionStatisticsData>() {
      public List<FactionStatisticsData> getRows(Connection connection, Criteria criteria) {
        try {
          return FactionStatisticsDataPeer.doSelect(criteria, connection);
        } catch (TorqueException e) {
          e.printStackTrace();
          return null;
        }
      }
    });
    return enumeration;
  }

  /**
   * 
   */
  private static Enumeration<RegionStatistics> getRegionEnumeration() {
    CriteriaEnumeration<RegionStatistics> enumeration = new CriteriaEnumeration<RegionStatistics>();
    enumeration.setLimit(1000);
    Criteria criteria = new Criteria();
    criteria.addAscendingOrderByColumn(RegionStatisticsPeer.REPORT_ID);
    criteria.addAscendingOrderByColumn(RegionStatisticsPeer.REGION_NUMBER);
    enumeration.setCriteria(criteria);
    enumeration.setPopulator(new CriteriaPopulator<RegionStatistics>() {
      public List<RegionStatistics> getRows(Connection connection, Criteria criteria) {
        try {
          return RegionStatisticsPeer.doSelect(criteria, connection);
        } catch (TorqueException e) {
          e.printStackTrace();
          return null;
        }
      }
    });
    return enumeration;
  }

  /**
   * 
   */
  private static Enumeration<RegionStatisticsData> getRegionDataEnumeration() {
    CriteriaEnumeration<RegionStatisticsData> enumeration = new CriteriaEnumeration<RegionStatisticsData>();
    enumeration.setLimit(1000);
    Criteria criteria = new Criteria();
    criteria.addAscendingOrderByColumn(RegionStatisticsDataPeer.REGION_ID);
    criteria.addAscendingOrderByColumn(RegionStatisticsDataPeer.TURN);
    enumeration.setCriteria(criteria);
    enumeration.setPopulator(new CriteriaPopulator<RegionStatisticsData>() {
      public List<RegionStatisticsData> getRows(Connection connection, Criteria criteria) {
        try {
          return RegionStatisticsDataPeer.doSelect(criteria, connection);
        } catch (TorqueException e) {
          e.printStackTrace();
          return null;
        }
      }
    });
    return enumeration;
  }

  /**
   * 
   */
  private static Enumeration<RegionStatisticsResourcesData> getRegionDataResourcesEnumeration() {
    CriteriaEnumeration<RegionStatisticsResourcesData> enumeration = new CriteriaEnumeration<RegionStatisticsResourcesData>();
    enumeration.setLimit(1000);
    Criteria criteria = new Criteria();
    criteria.addAscendingOrderByColumn(RegionStatisticsResourcesDataPeer.REGION_ID);
    criteria.addAscendingOrderByColumn(RegionStatisticsResourcesDataPeer.TURN_ID);
    criteria.addAscendingOrderByColumn(RegionStatisticsResourcesDataPeer.ITEM_TYPE);
    enumeration.setCriteria(criteria);
    enumeration.setPopulator(new CriteriaPopulator<RegionStatisticsResourcesData>() {
      public List<RegionStatisticsResourcesData> getRows(Connection connection, Criteria criteria) {
        try {
          return RegionStatisticsResourcesDataPeer.doSelect(criteria, connection);
        } catch (TorqueException e) {
          e.printStackTrace();
          return null;
        }
      }
    });
    return enumeration;
  }
  
  /**
   * 
   */
  private static Enumeration<RegionStatisticsPricesData> getRegionDataPricesEnumeration() {
    CriteriaEnumeration<RegionStatisticsPricesData> enumeration = new CriteriaEnumeration<RegionStatisticsPricesData>();
    enumeration.setLimit(1000);
    Criteria criteria = new Criteria();
    criteria.addAscendingOrderByColumn(RegionStatisticsPricesDataPeer.REGION_ID);
    criteria.addAscendingOrderByColumn(RegionStatisticsPricesDataPeer.TURN_ID);
    enumeration.setCriteria(criteria);
    enumeration.setPopulator(new CriteriaPopulator<RegionStatisticsPricesData>() {
      public List<RegionStatisticsPricesData> getRows(Connection connection, Criteria criteria) {
        try {
          return RegionStatisticsPricesDataPeer.doSelect(criteria, connection);
        } catch (TorqueException e) {
          e.printStackTrace();
          return null;
        }
      }
    });
    return enumeration;
  }
  
  /**
   * 
   */
  private static Enumeration<RegionStatisticsShipData> getRegionDataShipEnumeration() {
    CriteriaEnumeration<RegionStatisticsShipData> enumeration = new CriteriaEnumeration<RegionStatisticsShipData>();
    enumeration.setLimit(1000);
    Criteria criteria = new Criteria();
    criteria.addAscendingOrderByColumn(RegionStatisticsShipDataPeer.TURN_ID);
    enumeration.setCriteria(criteria);
    enumeration.setPopulator(new CriteriaPopulator<RegionStatisticsShipData>() {
      public List<RegionStatisticsShipData> getRows(Connection connection, Criteria criteria) {
        try {
          return RegionStatisticsShipDataPeer.doSelect(criteria, connection);
        } catch (TorqueException e) {
          e.printStackTrace();
          return null;
        }
      }
    });
    return enumeration;
  }
  
  /**
   * 
   */
  private static Enumeration<BuildingStatistics> getBuildingsEnumeration() {
    CriteriaEnumeration<BuildingStatistics> enumeration = new CriteriaEnumeration<BuildingStatistics>();
    enumeration.setLimit(1000);
    Criteria criteria = new Criteria();
    criteria.addAscendingOrderByColumn(BuildingStatisticsPeer.REPORT_ID);
    criteria.addAscendingOrderByColumn(BuildingStatisticsPeer.BUILDING_NUMBER);
    enumeration.setCriteria(criteria);
    enumeration.setPopulator(new CriteriaPopulator<BuildingStatistics>() {
      public List<BuildingStatistics> getRows(Connection connection, Criteria criteria) {
        try {
          return BuildingStatisticsPeer.doSelect(criteria, connection);
        } catch (TorqueException e) {
          e.printStackTrace();
          return null;
        }
      }
    });
    return enumeration;
  }
  
  /**
   * 
   */
  private static Enumeration<BuildingStatisticsData> getBuildingsDataEnumeration() {
    CriteriaEnumeration<BuildingStatisticsData> enumeration = new CriteriaEnumeration<BuildingStatisticsData>();
    enumeration.setLimit(1000);
    Criteria criteria = new Criteria();
    criteria.addAscendingOrderByColumn(BuildingStatisticsDataPeer.BUILDING_ID);
    criteria.addAscendingOrderByColumn(BuildingStatisticsDataPeer.TURN);
    enumeration.setCriteria(criteria);
    enumeration.setPopulator(new CriteriaPopulator<BuildingStatisticsData>() {
      public List<BuildingStatisticsData> getRows(Connection connection, Criteria criteria) {
        try {
          return BuildingStatisticsDataPeer.doSelect(criteria, connection);
        } catch (TorqueException e) {
          e.printStackTrace();
          return null;
        }
      }
    });
    return enumeration;
  }
  
  /**
   * 
   */
  private static Enumeration<UnitStatistics> getUnitsEnumeration() {
    CriteriaEnumeration<UnitStatistics> enumeration = new CriteriaEnumeration<UnitStatistics>();
    enumeration.setLimit(1000);
    Criteria criteria = new Criteria();
    criteria.addAscendingOrderByColumn(UnitStatisticsPeer.REPORT_ID);
    criteria.addAscendingOrderByColumn(UnitStatisticsPeer.UNIT_NUMBER);
    enumeration.setCriteria(criteria);
    enumeration.setPopulator(new CriteriaPopulator<UnitStatistics>() {
      public List<UnitStatistics> getRows(Connection connection, Criteria criteria) {
        try {
          return UnitStatisticsPeer.doSelect(criteria, connection);
        } catch (TorqueException e) {
          e.printStackTrace();
          return null;
        }
      }
    });
    return enumeration;
  }
  
  /**
   * 
   */
  private static Enumeration<UnitStatisticsData> getUnitsDataEnumeration() {
    CriteriaEnumeration<UnitStatisticsData> enumeration = new CriteriaEnumeration<UnitStatisticsData>();
    enumeration.setLimit(1000);
    Criteria criteria = new Criteria();
    criteria.addAscendingOrderByColumn(UnitStatisticsDataPeer.UNIT_ID);
    criteria.addAscendingOrderByColumn(UnitStatisticsDataPeer.TURN);
    enumeration.setCriteria(criteria);
    enumeration.setPopulator(new CriteriaPopulator<UnitStatisticsData>() {
      public List<UnitStatisticsData> getRows(Connection connection, Criteria criteria) {
        try {
          return UnitStatisticsDataPeer.doSelect(criteria, connection);
        } catch (TorqueException e) {
          e.printStackTrace();
          return null;
        }
      }
    });
    return enumeration;
  }

  /**
   * 
   */
  private static Enumeration<UnitStatisticsItemData> getUnitsDataItemEnumeration() {
    CriteriaEnumeration<UnitStatisticsItemData> enumeration = new CriteriaEnumeration<UnitStatisticsItemData>();
    enumeration.setLimit(1000);
    Criteria criteria = new Criteria();
    criteria.addAscendingOrderByColumn(UnitStatisticsItemDataPeer.UNIT_ID);
    criteria.addAscendingOrderByColumn(UnitStatisticsItemDataPeer.TURN_ID);
    enumeration.setCriteria(criteria);
    enumeration.setPopulator(new CriteriaPopulator<UnitStatisticsItemData>() {
      public List<UnitStatisticsItemData> getRows(Connection connection, Criteria criteria) {
        try {
          return UnitStatisticsItemDataPeer.doSelect(criteria, connection);
        } catch (TorqueException e) {
          e.printStackTrace();
          return null;
        }
      }
    });
    return enumeration;
  }
  /**
   * 
   */
  private static Enumeration<UnitStatisticsSkillData> getUnitsDataSkillEnumeration() {
    CriteriaEnumeration<UnitStatisticsSkillData> enumeration = new CriteriaEnumeration<UnitStatisticsSkillData>();
    enumeration.setLimit(1000);
    Criteria criteria = new Criteria();
    criteria.addAscendingOrderByColumn(UnitStatisticsSkillDataPeer.UNIT_ID);
    criteria.addAscendingOrderByColumn(UnitStatisticsSkillDataPeer.TURN_ID);
    enumeration.setCriteria(criteria);
    enumeration.setPopulator(new CriteriaPopulator<UnitStatisticsSkillData>() {
      public List<UnitStatisticsSkillData> getRows(Connection connection, Criteria criteria) {
        try {
          return UnitStatisticsSkillDataPeer.doSelect(criteria, connection);
        } catch (TorqueException e) {
          e.printStackTrace();
          return null;
        }
      }
    });
    return enumeration;
  }

  /**
   * 
   */
  private static Enumeration<ShipStatistics> getShipsEnumeration() {
    CriteriaEnumeration<ShipStatistics> enumeration = new CriteriaEnumeration<ShipStatistics>();
    enumeration.setLimit(1000);
    Criteria criteria = new Criteria();
    criteria.addAscendingOrderByColumn(ShipStatisticsPeer.REPORT_ID);
    criteria.addAscendingOrderByColumn(ShipStatisticsPeer.SHIP_NUMBER);
    enumeration.setCriteria(criteria);
    enumeration.setPopulator(new CriteriaPopulator<ShipStatistics>() {
      public List<ShipStatistics> getRows(Connection connection, Criteria criteria) {
        try {
          return ShipStatisticsPeer.doSelect(criteria, connection);
        } catch (TorqueException e) {
          e.printStackTrace();
          return null;
        }
      }
    });
    return enumeration;
  }
  
  /**
   * 
   */
  private static Enumeration<ShipStatisticsData> getShipsDataEnumeration() {
    CriteriaEnumeration<ShipStatisticsData> enumeration = new CriteriaEnumeration<ShipStatisticsData>();
    enumeration.setLimit(1000);
    Criteria criteria = new Criteria();
    criteria.addAscendingOrderByColumn(ShipStatisticsDataPeer.SHIP_ID);
    criteria.addAscendingOrderByColumn(ShipStatisticsDataPeer.TURN);
    enumeration.setCriteria(criteria);
    enumeration.setPopulator(new CriteriaPopulator<ShipStatisticsData>() {
      public List<ShipStatisticsData> getRows(Connection connection, Criteria criteria) {
        try {
          return ShipStatisticsDataPeer.doSelect(criteria, connection);
        } catch (TorqueException e) {
          e.printStackTrace();
          return null;
        }
      }
    });
    return enumeration;
  }
}
