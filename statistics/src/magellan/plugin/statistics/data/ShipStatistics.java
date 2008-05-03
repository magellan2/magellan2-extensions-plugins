// class magellan.plugin.statistics.data.ShipStatistics
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

import magellan.library.Ship;
import magellan.library.utils.Utils;
import magellan.plugin.statistics.IdentifiableType;

import org.w3c.dom.Element;

/**
 * This container holds all data of a ship.
 *
 * @author Thoralf Rickert
 * @version 1.0, 03.05.2008
 */
public class ShipStatistics {
  protected String id = null;
  protected String type = null;
  protected Map<Integer,ShipStatisticsData> turnData = new HashMap<Integer,ShipStatisticsData>();

  /**
   * Creates a ship statistics based on the given XML data.
   */
  public ShipStatistics(Element root) {
    this.id = root.getAttribute("id");
    this.type = Utils.getCData(Utils.getChildNode(root, "buildintype"));
    for (Element turnElement : Utils.getChildNodes(root, "turn")) {
      ShipStatisticsData data = new ShipStatisticsData();
      data.turn = Utils.getIntValue(turnElement.getAttribute("number"));
      data.name = Utils.getCData(Utils.getChildNode(turnElement, "name"));
      data.description = Utils.getCData(Utils.getChildNode(turnElement, "description"));
      data.size = Utils.getIntValue(Utils.getCData(Utils.getChildNode(turnElement, "size")));
      data.owner = Utils.getCData(Utils.getChildNode(turnElement, "owner"));
      data.region = Utils.getCData(Utils.getChildNode(turnElement, "region"));
      data.maxCargo = Utils.getIntValue(Utils.getCData(Utils.getChildNode(turnElement, "maxcargo")));
      data.cargo = Utils.getIntValue(Utils.getCData(Utils.getChildNode(turnElement, "cargo")));
      data.capacity = Utils.getIntValue(Utils.getCData(Utils.getChildNode(turnElement, "capacity")));
      data.damageRatio = Utils.getIntValue(Utils.getCData(Utils.getChildNode(turnElement, "damage")));
      turnData.put(data.turn, data);
    }
  }
  
  /**
   * Creates a ship statistics based on the given details.
   */
  public ShipStatistics(int turn, Ship ship) {
    this.id = ship.getID().toString();
    add(turn,ship);
  }
  
  /**
   * Adds the details of the given ship to this statistics
   */
  public void add(int turn, Ship ship) {
    ShipStatisticsData data = null;
    
    if (turnData.containsKey(turn)) {
      data = turnData.get(turn);
    } else {
      data = new ShipStatisticsData();
      turnData.put(turn, data);
    }
    
    type = ship.getShipType().toString();
    data.turn = turn;
    data.name = ship.getName();
    data.description = ship.getDescription();
    data.size = ship.getSize();
    if (ship.getOwner() != null) data.owner = ship.getOwner().getID().toString();
    data.region = ship.getRegion().getID().toString();
    data.maxCargo = ship.getMaxCapacity();
    data.cargo = ship.getCargo();
    data.capacity = ship.getCapacity();
    data.damageRatio = ship.getDamageRatio();
  }

  /**
   * Saves the data to the given stream
   */
  public void save(PrintStream ps) {
    ps.println(" <identifiable id=\""+id+"\" type=\""+IdentifiableType.SHIP+"\">");
    ps.println("  <shiptype>"+Utils.escapeXML(type)+"</shiptype>");
    for (ShipStatisticsData data : turnData.values()) {
      ps.println("  <turn number=\""+data.turn+"\">");
      ps.println("   <name>"+Utils.escapeXML(data.name)+"</name>");
      ps.println("   <description>"+Utils.escapeXML(data.description)+"</description>");
      ps.println("   <size>"+data.size+"</size>");
      ps.println("   <owner>"+Utils.escapeXML(data.owner)+"</owner>");
      ps.println("   <region>"+Utils.escapeXML(data.region)+"</region>");
      ps.println("   <maxcargo>"+data.maxCargo+"</maxcargo>");
      ps.println("   <cargo>"+data.cargo+"</cargo>");
      ps.println("   <capacity>"+data.capacity+"</capacity>");
      ps.println("   <damage>"+data.damageRatio+"</damage>");
      ps.println("  </turn>");
    }
    ps.println(" </identifiable>");
  }
}

class ShipStatisticsData {
  public int turn;
  public String name;
  public String description;
  public int size;
  public String owner;
  public String region;
  public int maxCargo;
  public int cargo;
  public int capacity;
  public int damageRatio;
  
}