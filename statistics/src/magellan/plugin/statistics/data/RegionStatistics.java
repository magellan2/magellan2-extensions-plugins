// class magellan.plugin.statistics.data.RegionStatistics
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import magellan.library.LuxuryPrice;
import magellan.library.Region;
import magellan.library.RegionResource;
import magellan.library.Ship;
import magellan.library.utils.Utils;
import magellan.plugin.statistics.IdentifiableType;

import org.w3c.dom.Element;

/**
 * This container holds all data of a region.
 *
 * @author Thoralf Rickert
 * @version 1.0, 03.05.2008
 */
public class RegionStatistics {
  protected String id = null;
  public Map<Integer,RegionStatisticsData> turnData = new HashMap<Integer,RegionStatisticsData>();
  
  /**
   * Creates a region statistics based on the given XML data.
   */
  public RegionStatistics(Element root) {
    this.id = root.getAttribute("id");
    for (Element turnElement : Utils.getChildNodes(root, "turn")) {
      RegionStatisticsData data = new RegionStatisticsData();
      data.turn = Utils.getIntValue(turnElement.getAttribute("number"));
      data.type = Utils.getCData(Utils.getChildNode(turnElement, "type"));
      data.name = Utils.getCData(Utils.getChildNode(turnElement, "name"));
      data.description = Utils.getCData(Utils.getChildNode(turnElement, "description"));
      data.maxRecruits = Utils.getIntValue(Utils.getCData(Utils.getChildNode(turnElement, "maxrecruits")));
      data.maxLuxuries = Utils.getIntValue(Utils.getCData(Utils.getChildNode(turnElement, "maxluxuries")));
      data.maxEntertain = Utils.getIntValue(Utils.getCData(Utils.getChildNode(turnElement, "maxentertain")));
      data.stones = Utils.getIntValue(Utils.getCData(Utils.getChildNode(turnElement, "stones")));
      data.trees = Utils.getIntValue(Utils.getCData(Utils.getChildNode(turnElement, "trees")));
      data.silver = Utils.getIntValue(Utils.getCData(Utils.getChildNode(turnElement, "silver")));
      data.sprouts = Utils.getIntValue(Utils.getCData(Utils.getChildNode(turnElement, "sprouts")));
      data.recruits = Utils.getIntValue(Utils.getCData(Utils.getChildNode(turnElement, "recruits")));
      data.peasants = Utils.getIntValue(Utils.getCData(Utils.getChildNode(turnElement, "peasants")));
      data.iron = Utils.getIntValue(Utils.getCData(Utils.getChildNode(turnElement, "iron")));
      data.laen = Utils.getIntValue(Utils.getCData(Utils.getChildNode(turnElement, "laen")));
      if (Utils.getChildNode(turnElement, "ships") != null) {
        for (Element shipElement : Utils.getChildNodes(Utils.getChildNode(turnElement, "ships"),"ship")) {
          data.ships.add(Utils.getCData(shipElement));
        }
      }
      if (Utils.getChildNode(turnElement, "prices") != null) {
        for (Element priceElement : Utils.getChildNodes(Utils.getChildNode(turnElement, "prices"),"price")) {
          String type = priceElement.getAttribute("type");
          int price = Utils.getIntValue(Utils.getCData(priceElement));
          data.prices.put(type, price);
        }
      }
      if (Utils.getChildNode(turnElement, "resources") != null) {
        for (Element resourceElement : Utils.getChildNodes(Utils.getChildNode(turnElement, "resources"),"resource")) {
          RegionResourceStatisticsData d = new RegionResourceStatisticsData();
          d.type = resourceElement.getAttribute("type");
          d.skillevel = Utils.getIntValue(resourceElement.getAttribute("level"));
          d.amount = Utils.getIntValue(resourceElement.getAttribute("amount"));
          data.resources.put(d.type, d);
        }
      }
      turnData.put(data.turn, data);
    }
  }
  
  /**
   * Creates a region statistics based on the given details.
   */
  public RegionStatistics(int turn, Region region) {
    this.id = region.getID().toString();
    add(turn,region);
  }
  
  /**
   * Adds the details of the given region to this statistics
   */
  public void add(int turn, Region region) {
    RegionStatisticsData data = null;
    
    if (turnData.containsKey(turn)) {
      data = turnData.get(turn);
    } else {
      data = new RegionStatisticsData();
      turnData.put(turn, data);
    }
    
    data.turn = turn;
    data.type = region.getRegionType().toString();
    data.name = region.getName();
    data.description = region.getDescription();
    data.laen = region.getLaen();
    data.iron = region.getIron();
    data.peasants = region.getPeasants();
    data.recruits = region.getRecruits();
    data.silver = region.getSilver();
    data.sprouts = region.getSprouts();
    data.trees = region.getTrees();
    data.stones = region.getStones();
    data.maxEntertain = region.maxEntertain();
    data.maxLuxuries = region.maxLuxuries();
    data.maxRecruits = region.maxRecruit();
    if (region.getPrices() != null) {
      for (LuxuryPrice prices : region.getPrices().values()) {
        data.prices.put(prices.getItemType().toString(),prices.getPrice());
      }
    }
    for (RegionResource resource : region.resources()) {
      RegionResourceStatisticsData d = new RegionResourceStatisticsData();
      d.skillevel = resource.getSkillLevel();
      d.amount = resource.getAmount();
      d.type = resource.getType().toString();
      data.resources.put(d.type,d);
    }
    for (Ship ship : region.ships()) {
      data.ships.add(ship.getID().toString());
    }
  }

  /**
   * Saves the data to the given stream
   */
  public void save(PrintStream ps) {
    ps.println(" <identifiable id=\""+id+"\" type=\""+IdentifiableType.REGION+"\">");
    for (RegionStatisticsData data : turnData.values()) {
      ps.println("  <turn number=\""+data.turn+"\">");
      ps.println("   <type>"+Utils.escapeXML(data.type)+"</type>");
      ps.println("   <name>"+Utils.escapeXML(data.name)+"</name>");
      ps.println("   <description>"+Utils.escapeXML(data.description)+"</description>");
      ps.println("   <maxrecruits>"+Utils.escapeXML(data.maxRecruits)+"</maxrecruits>");
      ps.println("   <maxluxuries>"+Utils.escapeXML(data.maxLuxuries)+"</maxluxuries>");
      ps.println("   <maxentertain>"+Utils.escapeXML(data.maxEntertain)+"</maxentertain>");
      ps.println("   <stones>"+Utils.escapeXML(data.stones)+"</stones>");
      ps.println("   <trees>"+Utils.escapeXML(data.trees)+"</trees>");
      ps.println("   <silver>"+Utils.escapeXML(data.silver)+"</silver>");
      ps.println("   <sprouts>"+Utils.escapeXML(data.sprouts)+"</sprouts>");
      ps.println("   <recruits>"+Utils.escapeXML(data.recruits)+"</recruits>");
      ps.println("   <peasants>"+Utils.escapeXML(data.peasants)+"</peasants>");
      ps.println("   <iron>"+Utils.escapeXML(data.iron)+"</iron>");
      ps.println("   <laen>"+Utils.escapeXML(data.laen)+"</laen>");
      if (data.ships.size()>0) {
        ps.println("   <ships>");
        for (String ship : data.ships) {
          ps.println("    <ship>"+Utils.escapeXML(ship)+"</ship>");
        }
        ps.println("   </ships>");
      }
      if (data.prices.size()>0) {
        ps.println("   <prices>");
        for (String item : data.prices.keySet()) {
          ps.println("    <price type=\""+Utils.escapeXML(item)+"\">"+data.prices.get(item)+"</price>");
        }
        ps.println("   </prices>");
      }
      if (data.resources.size()>0) {
        ps.println("   <resources>");
        for (String item : data.resources.keySet()) {
          ps.println("    <resource type=\""+Utils.escapeXML(item)+"\" level=\""+data.resources.get(item).skillevel+"\" amount=\""+data.resources.get(item).amount+"\"/>");
        }
        ps.println("   </resources>");
      }
      ps.println("  </turn>");
    }
    ps.println(" </identifiable>");
  }
  

  public class RegionStatisticsData {
    public int turn;
    public String type;
    public String name;
    public String description;
    public int maxRecruits;
    public int maxLuxuries;
    public int maxEntertain;
    public int stones;
    public int trees;
    public int silver;
    public int sprouts;
    public int recruits;
    public int peasants;
    public int iron;
    public int laen;
    public List<String> ships = new ArrayList<String>();
    public Map<String,Integer> prices = new HashMap<String, Integer>();
    public Map<String,RegionResourceStatisticsData> resources = new HashMap<String, RegionResourceStatisticsData>();
  }

  public class RegionResourceStatisticsData {
    public String type;
    public int skillevel;
    public int amount;
  }
}
