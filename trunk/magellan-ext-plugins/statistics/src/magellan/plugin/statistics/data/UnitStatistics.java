// class magellan.plugin.statistics.UnitStatistics
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

import magellan.library.Item;
import magellan.library.Skill;
import magellan.library.Unit;
import magellan.library.utils.Utils;
import magellan.plugin.statistics.IdentifiableType;

import org.w3c.dom.Element;

/**
 * This container holds all data of an unit.
 *
 * @author Thoralf Rickert
 * @version 1.0, 03.05.2008
 */
public class UnitStatistics {
  protected String id = null;
  public Map<Integer,UnitStatisticsData> turnData = new HashMap<Integer,UnitStatisticsData>();
  
  /**
   * Creates a unit statistics based on the given XML data.
   */
  public UnitStatistics(Element root) {
    this.id = root.getAttribute("id");
    for (Element turnElement : Utils.getChildNodes(root, "turn")) {
      UnitStatisticsData data = new UnitStatisticsData();
      data.turn = Utils.getIntValue(turnElement.getAttribute("number"));
      data.name = Utils.getCData(Utils.getChildNode(turnElement, "name"));
      data.description = Utils.getCData(Utils.getChildNode(turnElement, "description"));
      data.persons = Utils.getIntValue(Utils.getCData(Utils.getChildNode(turnElement, "persons")));
      data.faction = Utils.getCData(Utils.getChildNode(turnElement, "faction"));
      data.region = Utils.getCData(Utils.getChildNode(turnElement, "region"));
      data.building = Utils.getCData(Utils.getChildNode(turnElement, "building"));
      data.ship = Utils.getCData(Utils.getChildNode(turnElement, "ship"));
      data.race = Utils.getCData(Utils.getChildNode(turnElement, "race"));
      data.weight = Utils.getIntValue(Utils.getCData(Utils.getChildNode(turnElement, "weight")));
      data.aura = Utils.getIntValue(Utils.getCData(Utils.getChildNode(turnElement, "aura")));
      data.health = Utils.getCData(Utils.getChildNode(turnElement, "health"));
      data.hero = Utils.getBoolValue(Utils.getCData(Utils.getChildNode(turnElement, "hero")),false);
      data.guard = Utils.getIntValue(Utils.getCData(Utils.getChildNode(turnElement, "guard")));
      
      if (Utils.getChildNode(turnElement,"skills") != null) {
        for (Element skillElement : Utils.getChildNodes(Utils.getChildNode(turnElement,"skills"), "skill")) {
          String type = skillElement.getAttribute("type");
          int level = Utils.getIntValue(skillElement.getAttribute("level"));
          data.skills.put(type, level);
        }
      }
      if (Utils.getChildNode(turnElement,"items") != null) {
        for (Element itemElement : Utils.getChildNodes(Utils.getChildNode(turnElement,"items"), "item")) {
          String type = itemElement.getAttribute("type");
          int amount = Utils.getIntValue(itemElement.getAttribute("amount"));
          data.items.put(type, amount);
        }
      }
      turnData.put(data.turn, data);
    }
  }
  
  /**
   * Creates a unit statistics based on the given details.
   */
  public UnitStatistics(int turn, Unit unit) {
    this.id = unit.getID().toString();
    add(turn,unit);
  }
  
  /**
   * Adds the details of the given unit to this statistics
   */
  public void add(int turn, Unit unit) {
    UnitStatisticsData data = null;
    
    if (turnData.containsKey(turn)) {
      data = turnData.get(turn);
    } else {
      data = new UnitStatisticsData();
      turnData.put(turn, data);
    }
    
    data.turn = turn;
    data.name = unit.getName();
    data.description = unit.getDescription();
    data.persons = unit.getPersons();
    if (unit.getFaction() != null) data.faction = unit.getFaction().getID().toString();
    if (unit.getRegion() != null) data.region = unit.getRegion().getID().toString();
    if (unit.getBuilding() != null) data.building = unit.getBuilding().getID().toString();
    if (unit.getShip() != null) data.ship = unit.getShip().getID().toString();
    data.race = unit.getRace().getID().toString();
    data.weight = unit.getWeight();
    data.aura = unit.getAura();
    data.health = unit.getHealth();
    data.hero = unit.isHero();
    data.guard = unit.getGuard();
    
    for (Skill skill : unit.getSkills()) {
      data.skills.put(skill.getSkillType().getID().toString(), skill.getLevel());
    }
    for (Item item : unit.getItems()) {
      data.items.put(item.getItemType().getID().toString(), item.getAmount());
    }
  }

  /**
   * Saves the data to the given stream
   */
  public void save(PrintStream ps) {
    ps.println(" <identifiable id=\""+id+"\" type=\""+IdentifiableType.UNIT+"\">");
    for (UnitStatisticsData data : turnData.values()) {
      ps.println("  <turn number=\""+data.turn+"\">");
      ps.println("   <name>"+Utils.escapeXML(data.name)+"</name>");
      ps.println("   <description>"+Utils.escapeXML(data.description)+"</description>");
      ps.println("   <persons>"+data.persons+"</persons>");
      ps.println("   <faction>"+Utils.escapeXML(data.faction)+"</faction>");
      ps.println("   <region>"+Utils.escapeXML(data.region)+"</region>");
      ps.println("   <building>"+Utils.escapeXML(data.building)+"</building>");
      ps.println("   <ship>"+Utils.escapeXML(data.ship)+"</ship>");
      ps.println("   <race>"+Utils.escapeXML(data.race)+"</race>");
      ps.println("   <weight>"+data.weight+"</weight>");
      ps.println("   <aura>"+data.aura+"</aura>");
      ps.println("   <health>"+data.health+"</health>");
      ps.println("   <hero>"+data.hero+"</hero>");
      ps.println("   <guard>"+data.guard+"</guard>");
      if (data.skills.size()>0) {
        ps.println("   <skills>");
        for (String skill : data.skills.keySet()) {
          ps.println("    <skill type=\""+Utils.escapeXML(skill)+"\" level=\""+data.skills.get(skill)+"\"/>");
        }
        ps.println("   </skills>");
      }
      if (data.items.size()>0) {
        ps.println("   <items>");
        for (String item : data.items.keySet()) {
          ps.println("    <item type=\""+Utils.escapeXML(item)+"\" amount=\""+data.items.get(item)+"\"/>");
        }
        ps.println("   </items>");
      }
      ps.println("  </turn>");
    }
    ps.println(" </identifiable>");
  }

  public class UnitStatisticsData {
    public int turn;
    public String name;
    public String description;
    public int persons;
    public String faction;
    public String region;
    public String building;
    public String ship;
    public String race;
    public int weight;
    public Map<String,Integer> skills = new HashMap<String,Integer>();
    public Map<String,Integer> items = new HashMap<String,Integer>();
    public int aura;
    public String health;
    public boolean hero;
    public int guard;
  }
}

