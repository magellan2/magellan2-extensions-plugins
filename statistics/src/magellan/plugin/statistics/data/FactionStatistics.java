// class magellan.plugin.statistics.data.FactionStatistics
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

import magellan.library.Faction;
import magellan.library.utils.Utils;
import magellan.plugin.statistics.IdentifiableType;

import org.w3c.dom.Element;

/**
 * This container holds all data of a faction.
 *
 * @author Thoralf Rickert
 * @version 1.0, 03.05.2008
 */
public class FactionStatistics {
  protected String id = null;
  protected Map<Integer,FactionStatisticsData> turnData = new HashMap<Integer,FactionStatisticsData>();
  
  /**
   * Creates a faction statistics based on the given XML data.
   */
  public FactionStatistics(Element root) {
    this.id = root.getAttribute("id");
    for (Element turnElement : Utils.getChildNodes(root, "turn")) {
      FactionStatisticsData data = new FactionStatisticsData();
      data.turn = Utils.getIntValue(turnElement.getAttribute("number"));
      data.name = Utils.getCData(Utils.getChildNode(turnElement, "name"));
      data.description = Utils.getCData(Utils.getChildNode(turnElement, "description"));
      data.persons = Utils.getIntValue(Utils.getCData(Utils.getChildNode(turnElement, "persons")));
      data.race = Utils.getCData(Utils.getChildNode(turnElement, "race"));
      data.heroes = Utils.getIntValue(Utils.getCData(Utils.getChildNode(turnElement, "heroes")));
      data.maxHeroes = Utils.getIntValue(Utils.getCData(Utils.getChildNode(turnElement, "maxheroes")));
      data.maxMigrants = Utils.getIntValue(Utils.getCData(Utils.getChildNode(turnElement, "maxmigrants")));
      data.averageScore = Utils.getIntValue(Utils.getCData(Utils.getChildNode(turnElement, "averagescore")));
      data.score = Utils.getIntValue(Utils.getCData(Utils.getChildNode(turnElement, "score")));
      turnData.put(data.turn, data);
    }
  }
  
  /**
   * Creates a faction statistics based on the given details.
   */
  public FactionStatistics(int turn, Faction faction) {
    this.id = faction.getID().toString();
    add(turn,faction);
  }
  
  /**
   * Adds the details of the given faction to this statistics
   */
  public void add(int turn, Faction faction) {
    FactionStatisticsData data = null;
    
    if (turnData.containsKey(turn)) {
      data = turnData.get(turn);
    } else {
      data = new FactionStatisticsData();
      turnData.put(turn, data);
    }
    
    data.turn = turn;
    data.name = faction.getName();
    data.description = faction.getDescription();
    data.persons = faction.getPersons();
    if (faction.getRace() != null) data.race = faction.getRace().getID().toString();
    data.heroes = faction.getHeroes();
    data.maxHeroes = faction.getMaxHeroes();
    data.maxMigrants = faction.getMaxMigrants();
    data.averageScore = faction.getAverageScore();
    data.score = faction.getScore();
  }

  /**
   * Saves the data to the given stream
   */
  public void save(PrintStream ps) {
    ps.println(" <identifiable id=\""+id+"\" type=\""+IdentifiableType.FACTION+"\">");
    for (FactionStatisticsData data : turnData.values()) {
      ps.println("  <turn number=\""+data.turn+"\">");
      ps.println("   <name>"+Utils.escapeXML(data.name)+"</name>");
      ps.println("   <description>"+Utils.escapeXML(data.description)+"</description>");
      ps.println("   <persons>"+Utils.escapeXML(data.persons)+"</persons>");
      ps.println("   <race>"+Utils.escapeXML(data.race)+"</race>");
      ps.println("   <heroes>"+Utils.escapeXML(data.heroes)+"</heroes>");
      ps.println("   <maxheroes>"+Utils.escapeXML(data.maxHeroes)+"</maxheroes>");
      ps.println("   <maxmigrants>"+Utils.escapeXML(data.maxMigrants)+"</maxmigrants>");
      ps.println("   <averagescore>"+Utils.escapeXML(data.averageScore)+"</averagescore>");
      ps.println("   <score>"+Utils.escapeXML(data.score)+"</score>");
      ps.println("  </turn>");
    }
    ps.println(" </identifiable>");
  }
}

class FactionStatisticsData {
  public int turn;
  public String name;
  public String description;
  public int persons;
  public String race;
  public int heroes;
  public int maxHeroes;
  public int maxMigrants;
  public int averageScore;
  public int score;
}