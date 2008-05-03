// class magellan.plugin.statistics.StatisticDock
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

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import magellan.client.Client;
import magellan.client.event.SelectionEvent;
import magellan.client.event.SelectionListener;
import magellan.library.Building;
import magellan.library.Faction;
import magellan.library.Region;
import magellan.library.Ship;
import magellan.library.Unit;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;
import magellan.plugin.statistics.data.RegionStatistics;
import magellan.plugin.statistics.data.UnitStatistics;
import magellan.plugin.statistics.data.RegionStatistics.RegionStatisticsData;
import magellan.plugin.statistics.data.UnitStatistics.UnitStatisticsData;

/**
 * This is the dock that represents the statistic data.
 *
 * @author Thoralf Rickert.
 * @version 1.0, 03.05.2008
 */
public class StatisticDock extends JPanel implements SelectionListener {
  private static Logger log = Logger.getInstance(StatisticDock.class);
  protected StatisticsPlugIn plugin = null;
  protected JTabbedPane tabbedPane = null;
  protected JTable table = null;
  protected JScrollPane tableTab = null;
  protected JComponent skillsTab = null;
  protected JScrollPane itemsTab = null;

  /**
   * 
   */
  public StatisticDock(Client client, Properties settings, StatisticsPlugIn statisticsPlugIn) {
    this.plugin = statisticsPlugIn;
    setLayout(new BorderLayout());
    
    client.getDispatcher().addSelectionListener(this);
  }

  /**
   * @see magellan.client.event.SelectionListener#selectionChanged(magellan.client.event.SelectionEvent)
   */
  public void selectionChanged(SelectionEvent e) {
    if (e.getActiveObject() instanceof Unit) {
      showStatistics((Unit)e.getActiveObject());
      
    } else if (e.getActiveObject() instanceof Region) {
      showStatistics((Region)e.getActiveObject());
      
    } else if (e.getActiveObject() instanceof Faction) {
      showStatistics((Faction)e.getActiveObject());
      
    } else if (e.getActiveObject() instanceof Building) {
      showStatistics((Building)e.getActiveObject());
      
    } else if (e.getActiveObject() instanceof Ship) {
      showStatistics((Ship)e.getActiveObject());
      
    }
  }
  
  /**
   * 
   */
  protected void showStatistics(Unit unit) {
    log.info("Showing statistics for unit "+unit.getID().toString()+".");
    if (tabbedPane != null) remove(tabbedPane);
    
    if (plugin.getStatistics() != null) {
      UnitTableModel model = new UnitTableModel(plugin.getStatistics(),unit);
      
      table = new JTable(model);
      table.setAutoCreateRowSorter(true);
      tableTab = new JScrollPane(table);
      
      skillsTab = createSkillChart(unit);
      
      itemsTab = new JScrollPane();
      
      tabbedPane = new JTabbedPane();
      tabbedPane.addTab(Resources.get("statisticsplugin.unit.table"), tableTab);
      tabbedPane.addTab(Resources.get("statisticsplugin.unit.skills"), skillsTab);
      tabbedPane.addTab(Resources.get("statisticsplugin.unit.items"), itemsTab);
      
      add(tabbedPane,BorderLayout.CENTER);
    }
  }
  
  /**
   * 
   */
  protected void showStatistics(Region region) {
    log.info("Showing statistics for region "+region.getID().toString()+".");
    if (tabbedPane != null) remove(tabbedPane);
    
    if (plugin.getStatistics() != null) {
      RegionTableModel model = new RegionTableModel(plugin.getStatistics(),region);
      
      table = new JTable(model);
      table.setAutoCreateRowSorter(true);
      tableTab = new JScrollPane(table);
      
      tabbedPane = new JTabbedPane();
      tabbedPane.addTab(Resources.get("statisticsplugin.unit.table"), tableTab);
      
      add(tabbedPane,BorderLayout.CENTER);
    }
  }
  
  /**
   * 
   */
  protected void showStatistics(Faction faction) {
    log.info("Showing statistics for faction "+faction.getID().toString()+".");
  }
  
  /**
   * 
   */
  protected void showStatistics(Building building) {
    log.info("Showing statistics for building "+building.getID().toString()+".");
  }
  
  /**
   * 
   */
  protected void showStatistics(Ship ship) {
    log.info("Showing statistics for ship "+ship.getID().toString()+".");
  }
  
  protected JComponent createSkillChart(Unit unit) {
    UnitStatistics stats = plugin.getStatistics().getStatistics(unit);
    if (stats != null) {
      String title = Resources.get("statisticsplugin.unit.skills");
      String xAxisTitle = Resources.get("statisticsplugin.unit.skills.xAxis");
      String yAxisTitle = Resources.get("statisticsplugin.unit.skills.yAxis");
      
      XYSeriesCollection dataset = new XYSeriesCollection();
      Map<String,XYSeries> series = new HashMap<String, XYSeries>();
      List<Integer> turns = new ArrayList<Integer>(stats.turnData.keySet());
      Collections.sort(turns);
      
      for (Integer turn : turns) {
        Map<String,Integer> skills = stats.turnData.get(turn).skills;
        
        for (String skill : skills.keySet()) {
          XYSeries serie = null;
          if (series.containsKey(skill)) {
            serie = series.get(skill);
          } else {
            serie = new XYSeries(skill);
            series.put(skill, serie);
          }
          serie.add(turn, skills.get(skill));
        }
      }
      
      for (XYSeries serie : series.values()) {
        dataset.addSeries(serie);
      }
      
      JFreeChart chart = ChartFactory.createXYStepChart(title, xAxisTitle, yAxisTitle, dataset, PlotOrientation.VERTICAL, true, true, false);
      return new ChartPanel(chart);
    } else {
      return new JScrollPane();
    }
  }
}

class UnitTableModel extends AbstractTableModel {
  protected List<Integer> turns = new ArrayList<Integer>();
  protected List<String> columnNames = new ArrayList<String>();
  protected UnitStatistics statistics = null;
  
  public UnitTableModel(Statistics stat, Unit unit) {
    statistics = stat.getStatistics(unit);
    
    columnNames.add(Resources.get("statisticsplugin.unit.turn"));
    columnNames.add(Resources.get("statisticsplugin.unit.name"));
    columnNames.add(Resources.get("statisticsplugin.unit.region"));
    columnNames.add(Resources.get("statisticsplugin.unit.persons"));
    columnNames.add(Resources.get("statisticsplugin.unit.ship"));
    columnNames.add(Resources.get("statisticsplugin.unit.building"));
    columnNames.add(Resources.get("statisticsplugin.unit.race"));
    columnNames.add(Resources.get("statisticsplugin.unit.weight"));
    
    if (statistics == null) return;
    
    turns = new ArrayList<Integer>(statistics.turnData.keySet());
    Collections.sort(turns);
    
    for (Integer turn : turns) {
      UnitStatisticsData data = statistics.turnData.get(turn);
      for (String skill : data.skills.keySet()) {
        if (!columnNames.contains(skill)) columnNames.add(skill);
      }
    }
    
    for (Integer turn : turns) {
      UnitStatisticsData data = statistics.turnData.get(turn);
      for (String item : data.items.keySet()) {
        if (!columnNames.contains(item)) columnNames.add(item);
      }
    }
  }

  /**
   * @see javax.swing.table.TableModel#getColumnCount()
   */
  public int getColumnCount() {
    return columnNames.size();
  }
  
  /**
   * @see javax.swing.table.TableModel#getRowCount()
   */
  public int getRowCount() {
    return turns.size();
  }

  /**
   * @see javax.swing.table.TableModel#getValueAt(int, int)
   */
  public Object getValueAt(int rowIndex, int columnIndex) {
    Integer turn = turns.get(rowIndex);
    UnitStatisticsData data = statistics.turnData.get(turn);
    switch (columnIndex) {
      case 0: return turn;
      case 1: return data.name;
      case 2: return data.region;
      case 3: return data.persons;
      case 4: return data.ship;
      case 5: return data.building;
      case 6: return data.race;
      case 7: return data.weight;
      default: {
        String columnName = getColumnName(columnIndex);
        if (data.skills.containsKey(columnName)) return data.skills.get(columnName);
        if (data.items.containsKey(columnName)) return data.items.get(columnName);
      }
    }
    
    return null;
  }
  
  /**
   * @see javax.swing.table.AbstractTableModel#getColumnName(int)
   */
  @Override
  public String getColumnName(int col) {
    return columnNames.get(col);
  }
}



class RegionTableModel extends AbstractTableModel {
  protected List<Integer> turns = new ArrayList<Integer>();
  protected List<String> columnNames = new ArrayList<String>();
  protected RegionStatistics statistics = null;
  
  public RegionTableModel(Statistics stat, Region region) {
    statistics = stat.getStatistics(region);

    columnNames.add(Resources.get("statisticsplugin.region.turn"));
    columnNames.add(Resources.get("statisticsplugin.region.name"));
    columnNames.add(Resources.get("statisticsplugin.region.type"));
    columnNames.add(Resources.get("statisticsplugin.region.peasants"));
    columnNames.add(Resources.get("statisticsplugin.region.recruits"));
    columnNames.add(Resources.get("statisticsplugin.region.maxRecruits"));
    columnNames.add(Resources.get("statisticsplugin.region.silver"));
    columnNames.add(Resources.get("statisticsplugin.region.maxEntertain"));
    columnNames.add(Resources.get("statisticsplugin.region.maxLuxuries"));
    columnNames.add(Resources.get("statisticsplugin.region.trees"));
    columnNames.add(Resources.get("statisticsplugin.region.sprouts"));
    columnNames.add(Resources.get("statisticsplugin.region.stones"));
    columnNames.add(Resources.get("statisticsplugin.region.iron"));
    columnNames.add(Resources.get("statisticsplugin.region.laen"));
    
    if (statistics == null) return;
    
    turns = new ArrayList<Integer>(statistics.turnData.keySet());
    Collections.sort(turns);
    
    for (Integer turn : turns) {
      RegionStatisticsData data = statistics.turnData.get(turn);
      for (String price : data.prices.keySet()) {
        if (!columnNames.contains(price)) columnNames.add(price);
      }
    }
  }

  /**
   * @see javax.swing.table.TableModel#getColumnCount()
   */
  public int getColumnCount() {
    return columnNames.size();
  }
  
  /**
   * @see javax.swing.table.TableModel#getRowCount()
   */
  public int getRowCount() {
    return turns.size();
  }

  /**
   * @see javax.swing.table.TableModel#getValueAt(int, int)
   */
  public Object getValueAt(int rowIndex, int columnIndex) {
    Integer turn = turns.get(rowIndex);
    RegionStatisticsData data = statistics.turnData.get(turn);
    switch (columnIndex) {
      case 0: return turn;
      case 1: return data.name;
      case 2: return data.type;
      case 3: return data.peasants;
      case 4: return data.recruits;
      case 5: return data.maxRecruits;
      case 6: return data.silver;
      case 7: return data.maxEntertain;
      case 8: return data.maxLuxuries;
      case 9: return data.trees;
      case 10: return data.sprouts;
      case 11: return data.stones;
      case 12: return data.iron;
      case 13: return data.laen;
      default: {
        String columnName = getColumnName(columnIndex);
        if (data.prices.containsKey(columnName)) return data.prices.get(columnName);
      }
    }
    
    return null;
  }
  
  /**
   * @see javax.swing.table.AbstractTableModel#getColumnName(int)
   */
  @Override
  public String getColumnName(int col) {
    return columnNames.get(col);
  }
}