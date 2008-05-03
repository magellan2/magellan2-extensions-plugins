// class magellan.plugin.statistics.data.BuildingStatistics
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
package magellan.plugin.statistics.data;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import magellan.library.Building;
import magellan.library.utils.Utils;
import magellan.plugin.statistics.IdentifiableType;

import org.w3c.dom.Element;

/**
 * This container holds all data of a building.
 *
 * @author Thoralf Rickert
 * @version 1.0, 03.05.2008
 */
public class BuildingStatistics {
  protected String id = null;
  protected String type = null;
  protected Map<Integer,BuildingStatisticsData> turnData = new HashMap<Integer,BuildingStatisticsData>();
  
  /**
   * Creates a building statistics based on the given XML data.
   */
  public BuildingStatistics(Element root) {
    this.id = root.getAttribute("id");
    this.type = Utils.getCData(Utils.getChildNode(root, "buildintype"));
    for (Element turnElement : Utils.getChildNodes(root, "turn")) {
      BuildingStatisticsData data = new BuildingStatisticsData();
      data.turn = Utils.getIntValue(turnElement.getAttribute("number"));
      data.name = Utils.getCData(Utils.getChildNode(turnElement, "name"));
      data.description = Utils.getCData(Utils.getChildNode(turnElement, "description"));
      data.size = Utils.getIntValue(Utils.getCData(Utils.getChildNode(turnElement, "size")));
      data.owner = Utils.getCData(Utils.getChildNode(turnElement, "owner"));
      turnData.put(data.turn, data);
    }
  }
  
  /**
   * Creates a building statistics based on the given details.
   */
  public BuildingStatistics(int turn, Building building) {
    this.id = building.getID().toString();
    add(turn,building);
  }
  
  /**
   * Adds the details of the given building to this statistics
   */
  public void add(int turn, Building building) {
    BuildingStatisticsData data = null;
    
    if (turnData.containsKey(turn)) {
      data = turnData.get(turn);
    } else {
      data = new BuildingStatisticsData();
      turnData.put(turn, data);
    }
    
    type = building.getBuildingType().toString();
    data.turn = turn;
    data.name = building.getName();
    data.description = building.getDescription();
    data.size = building.getSize();
    if (building.getOwner() != null) data.owner = building.getOwner().getID().toString();  
  }

  /**
   * Saves the data to the given stream
   */
  public void save(PrintStream ps) {
    ps.println(" <identifiable id=\""+id+"\" type=\""+IdentifiableType.BUILDING+"\">");
    ps.println("  <buildingtype>"+Utils.escapeXML(type)+"</buildingtype>");
    for (BuildingStatisticsData data : turnData.values()) {
      ps.println("  <turn number=\""+data.turn+"\">");
      ps.println("   <name>"+Utils.escapeXML(data.name)+"</name>");
      ps.println("   <description>"+Utils.escapeXML(data.description)+"</description>");
      ps.println("   <size>"+data.size+"</size>");
      ps.println("   <owner>"+Utils.escapeXML(data.owner)+"</owner>");
      ps.println("  </turn>");
    }
    ps.println(" </identifiable>");
  }
}

class BuildingStatisticsData {
  public int turn;
  public String name;
  public String description;
  public int size;
  public String owner;
}