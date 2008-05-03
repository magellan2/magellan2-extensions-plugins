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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.tools.bzip2.CBZip2InputStream;
import org.apache.tools.bzip2.CBZip2OutputStream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import magellan.library.Building;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.Ship;
import magellan.library.Unit;
import magellan.library.utils.Utils;
import magellan.library.utils.logging.Logger;
import magellan.plugin.statistics.data.BuildingStatistics;
import magellan.plugin.statistics.data.FactionStatistics;
import magellan.plugin.statistics.data.RegionStatistics;
import magellan.plugin.statistics.data.ShipStatistics;
import magellan.plugin.statistics.data.UnitStatistics;

/**
 * This container holds all useful data of a report.
 *
 * @author Thoralf Rickert
 * @version 1.0, 03.05.2008
 */
public class Statistics {
  private static Logger log = Logger.getInstance(Statistics.class);
  protected File statFile = null;
  
  protected Map<String,UnitStatistics> units = new HashMap<String, UnitStatistics>();
  protected Map<String,RegionStatistics> regions = new HashMap<String, RegionStatistics>();
  protected Map<String,FactionStatistics> factions = new HashMap<String, FactionStatistics>();
  protected Map<String,ShipStatistics> ships = new HashMap<String, ShipStatistics>();
  protected Map<String,BuildingStatistics> buildings = new HashMap<String, BuildingStatistics>();
  
  
  public Statistics(File statFile) {
    this.statFile = statFile;
    
    if (statFile.exists()) {
      load();
    }
  }
  
  /**
   * Load and parse the statistics file.
   */
  protected void load() {
    try {
      DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      FileInputStream fis = new FileInputStream(statFile);
      BufferedInputStream bis = new BufferedInputStream(fis);
      CBZip2InputStream zis = new CBZip2InputStream(bis);
      Document document = builder.parse(zis);
      zis.close();
      bis.close();
      fis.close();
      if (!document.getDocumentElement().getNodeName().equals("statistics")) {
        log.fatal("The file "+statFile+" does NOT contain statistic informations. Missing XML root element 'statistics'");
        return;
      }
      
      List<Element> identifiableElements = Utils.getChildNodes(document.getDocumentElement(), "identifiable");
      for (Element identifiableElement : identifiableElements) {
        loadIdentifiable(identifiableElement);
      }
    } catch (Exception exception) {
      log.error("",exception);
    }
  }
  
  /**
   * Loads an identifable element from the XML tree.
   */
  protected void loadIdentifiable(Element root) {
    IdentifiableType type = IdentifiableType.getType(root.getAttribute("type"));
    
    switch (type) {
      case UNIT: {
        loadUnit(root);
        break;
      }
      case REGION: {
        loadRegion(root);
        break;
      }
      case SHIP: {
        loadShip(root);
        break;
      }
      case BUILDING: {
        loadBuilding(root);
        break;
      }
      case FACTION: {
        loadFaction(root);
        break;
      }
      default: {
        // do nothing
        break;
      }
    }
  }
  
  
  
  protected void loadUnit(Element root) {
    String id = root.getAttribute("id");
    units.put(id,new UnitStatistics(root));
  }
  
  protected void loadRegion(Element root) {
    String id = root.getAttribute("id");
    regions.put(id,new RegionStatistics(root));
  }
  
  protected void loadShip(Element root) {
    String id = root.getAttribute("id");
    ships.put(id,new ShipStatistics(root));
  }
  
  protected void loadBuilding(Element root) {
    String id = root.getAttribute("id");
    buildings.put(id,new BuildingStatistics(root));
  }
  
  protected void loadFaction(Element root) {
    String id = root.getAttribute("id");
    factions.put(id,new FactionStatistics(root));
  }

  /**
   * Adds a report to the current known statistics.
   */
  protected void add(GameData world) {
    int turn = world.getDate().getDate();
    
    for (Faction faction : world.factions().values()) {
      String id = faction.getID().toString();
      if (factions.containsKey(id)) {
        factions.get(id).add(turn,faction);
      } else {
        factions.put(id,new FactionStatistics(turn,faction));
      }
    }
    
    for (Region region : world.regions().values()) {
      String id = region.getID().toString();
      if (regions.containsKey(id)) {
        regions.get(id).add(turn,region);
      } else {
        regions.put(id,new RegionStatistics(turn,region));
      }
    }
    
    for (Unit unit : world.units().values()) {
      String id = unit.getID().toString();
      if (units.containsKey(id)) {
        units.get(id).add(turn,unit);
      } else {
        units.put(id,new UnitStatistics(turn,unit));
      }
    }
    
    for (Building building : world.buildings().values()) {
      String id = building.getID().toString();
      if (buildings.containsKey(id)) {
        buildings.get(id).add(turn,building);
      } else {
        buildings.put(id,new BuildingStatistics(turn,building));
      }
    }

    for (Ship ship : world.ships().values()) {
      String id = ship.getID().toString();
      if (ships.containsKey(id)) {
        ships.get(id).add(turn,ship);
      } else {
        ships.put(id,new ShipStatistics(turn,ship));
      }
    }

    
  }
  
  /**
   * Saves the statistics file.
   */
  public void save() {
    try {
      log.info("Writing Statistics File: "+statFile);
      FileOutputStream fos = new FileOutputStream(statFile);
      CBZip2OutputStream zos = new CBZip2OutputStream(fos);
      BufferedOutputStream bos = new BufferedOutputStream(zos);
      PrintStream ps = new PrintStream(bos,true,"UTF-8");
      ps.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
      ps.println("<statistics>");
      for (FactionStatistics faction : factions.values()) {
        faction.save(ps);
      }
      for (RegionStatistics region : regions.values()) {
        region.save(ps);
      }
      for (BuildingStatistics building : buildings.values()) {
        building.save(ps);
      }
      for (ShipStatistics ship : ships.values()) {
        ship.save(ps);
      }
      for (UnitStatistics unit : units.values()) {
        unit.save(ps);
      }
      ps.println("</statistics>");
      ps.close();
      bos.close();
      zos.close();
      fos.close();
      
    } catch (Exception exception) {
      log.error("",exception);
    }
  }
  
  
  public UnitStatistics getStatistics(Unit unit) {
    if (unit == null || unit.getID() == null) return null;
    String id = unit.getID().toString();
    if (units.containsKey(id)) return units.get(id);
    return null;
  }

  
  public RegionStatistics getStatistics(Region region) {
    if (region == null || region.getID() == null) return null;
    String id = region.getID().toString();
    if (regions.containsKey(id)) return regions.get(id);
    return null;
  }
}
