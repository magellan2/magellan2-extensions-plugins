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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

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
import magellan.plugin.statistics.data.FactionStatistics;
import magellan.plugin.statistics.data.RegionStatistics;
import magellan.plugin.statistics.data.UnitStatistics;
import magellan.plugin.statistics.data.FactionStatistics.FactionStatisticsData;
import magellan.plugin.statistics.data.RegionStatistics.RegionStatisticsData;
import magellan.plugin.statistics.data.UnitStatistics.UnitStatisticsData;

import net.infonode.docking.DockingWindow;
import net.infonode.docking.DockingWindowListener;
import net.infonode.docking.OperationAbortedException;
import net.infonode.docking.View;

/**
 * This is the dock that represents the statistic data.
 *
 * @author Thoralf Rickert.
 * @version 1.0, 03.05.2008
 */
public class StatisticDock extends JPanel implements SelectionListener<Object>, DockingWindowListener {
  private static Logger log = Logger.getInstance(StatisticDock.class);
  protected StatisticsPlugIn plugin = null;
  protected JTabbedPane tabbedPane = null;
  protected JTable table = null;
  protected JScrollPane tableTab = null;
  protected JComponent skillsTab = null;
  protected JComponent resourcesTab = null;
  protected JComponent tradesTab = null;
  protected JComponent pointsTab = null;
  protected JComponent unitsTab = null;
  protected JScrollPane itemsTab = null;
  protected Object activeObject = null;
  protected JLabel waitLabel = null;
  protected JLabel notImplementedLavel = null;
  
  protected boolean isShown = false;

  /**
   * 
   */
  public StatisticDock(Client client, Properties settings, StatisticsPlugIn statisticsPlugIn) {
    this.plugin = statisticsPlugIn;
    setLayout(new BorderLayout());
    
    client.getDispatcher().addSelectionListener(this);
    
    ImageIcon wait = client.getMagellanContext().getImageFactory().loadImageIcon("wait30trans");
    
    waitLabel = new JLabel(Resources.get("statisticsplugin.dock.wait"),wait,JLabel.HORIZONTAL);
    
    notImplementedLavel = new JLabel(Resources.get("statisticsplugin.dock.notimplemented"));
    notImplementedLavel.setHorizontalAlignment(JLabel.HORIZONTAL);
    
    
    add(waitLabel,BorderLayout.CENTER);
    repaint();
  }

  /**
   * @see magellan.client.event.SelectionListener#selectionChanged(magellan.client.event.SelectionEvent)
   */
  public void selectionChanged(SelectionEvent<Object> e) {
    Object o = e.getActiveObject();
    
    if (o == null) return;
    
    if (activeObject != null ) {
      if (o.equals(activeObject)) {
        return;
      }
    }
    
    activeObject = o;
    
    if (!isShown) return;
    refresh();
  }
  
  /**
   * Refreshes the GUI.
   */
  protected void refresh() {
    if (activeObject == null) return;
    
    if (activeObject instanceof Unit 
        || activeObject instanceof Region 
        || activeObject instanceof Faction 
        || activeObject instanceof Building 
        || activeObject instanceof Ship) {
      removeAll();
      add(waitLabel,BorderLayout.CENTER);
      repaint();
      StatisticDockSelectionChangedThread thread = new StatisticDockSelectionChangedThread(activeObject);
      thread.start();
    }
  }
  
  /**
   * Shows the given region.
   * Normally used after a loading/merging process in the client.
   */
  public void show(Region region) {
    if (region == null) return;
    
    removeAll();
    add(waitLabel,BorderLayout.CENTER);
    repaint();
    StatisticDockSelectionChangedThread thread = new StatisticDockSelectionChangedThread(region);
    thread.start();
  }
  
  /**
   * This thread opens the given object in the dock. This process can take some seconds
   * so we put it into a thread to avoid GUI blocking
   *
   * @author Thoralf Rickert
   * @version 1.0, 11.05.2008
   */
  class StatisticDockSelectionChangedThread extends Thread {
    Object activeObject;
    
    public StatisticDockSelectionChangedThread(Object activeObject) {
      this.activeObject = activeObject;
    }
    
    /**
     * @see java.lang.Thread#run()
     */
    public void run() {
      if (activeObject == null) return;
      
      if (activeObject instanceof Unit) {
        showStatistics((Unit)activeObject);
        
      } else if (activeObject instanceof Region) {
        showStatistics((Region)activeObject);
        
      } else if (activeObject instanceof Faction) {
        showStatistics((Faction)activeObject);
        
      } else if (activeObject instanceof Building) {
        showStatistics((Building)activeObject);
        
      } else if (activeObject instanceof Ship) {
        showStatistics((Ship)activeObject);
        
      }
    }
  }
  
  /**
   * 
   */
  protected void showStatistics(Unit unit) {
    log.info("Showing statistics for unit "+unit.getID().toString()+".");
    
    if (plugin.getStatistics() != null) {
      UnitTableModel model = new UnitTableModel(plugin.getStatistics(),unit);
      
      table = new JTable(model);
      table.setAutoCreateRowSorter(true);
      tableTab = new JScrollPane(table);
      
      skillsTab = StatisticCharts.createSkillChart(plugin,unit);
      
      itemsTab = new JScrollPane();
      
      tabbedPane = new JTabbedPane();
      tabbedPane.addTab(Resources.get("statisticsplugin.unit.table"), tableTab);
      tabbedPane.addTab(Resources.get("statisticsplugin.unit.skills"), skillsTab);
      tabbedPane.addTab(Resources.get("statisticsplugin.unit.items"), itemsTab);
      
      removeAll();
      add(tabbedPane,BorderLayout.CENTER);
      repaint();
    }
  }
  
  /**
   * 
   */
  protected void showStatistics(Region region) {
    log.info("Showing statistics for region "+region.getID().toString()+".");
    
    if (plugin.getStatistics() != null) {
      RegionTableModel model = new RegionTableModel(plugin.getStatistics(),region);
      
      table = new JTable(model);
      table.setAutoCreateRowSorter(true);
      tableTab = new JScrollPane(table);
      
      resourcesTab = StatisticCharts.createResourcesChart(plugin,region);
      tradesTab = StatisticCharts.createTradeChart(plugin,region);
      
      tabbedPane = new JTabbedPane();
      tabbedPane.addTab(Resources.get("statisticsplugin.region.table"), tableTab);
      tabbedPane.addTab(Resources.get("statisticsplugin.region.resources"), resourcesTab);
      tabbedPane.addTab(Resources.get("statisticsplugin.region.trades"), tradesTab);
      
      removeAll();
      add(tabbedPane,BorderLayout.CENTER);
      repaint();
    }
  }
  
  /**
   * 
   */
  protected void showStatistics(Faction faction) {
    log.info("Showing statistics for faction "+faction.getID().toString()+".");

    if (plugin.getStatistics() != null) {
      FactionTableModel model = new FactionTableModel(plugin.getStatistics(),faction);
      
      table = new JTable(model);
      table.setAutoCreateRowSorter(true);
      tableTab = new JScrollPane(table);
      
      pointsTab = StatisticCharts.createPointsChart(plugin,faction);
      unitsTab = StatisticCharts.createUnitsChart(plugin,faction);
      
      tabbedPane = new JTabbedPane();
      tabbedPane.addTab(Resources.get("statisticsplugin.faction.table"), tableTab);
      tabbedPane.addTab(Resources.get("statisticsplugin.faction.points"), pointsTab);
      tabbedPane.addTab(Resources.get("statisticsplugin.faction.units"), unitsTab);
      
      removeAll();
      add(tabbedPane,BorderLayout.CENTER);
      repaint();
    }
  }
  
  /**
   * 
   */
  protected void showStatistics(Building building) {
    log.info("Showing statistics for building "+building.getID().toString()+".");
    removeAll();
    add(notImplementedLavel,BorderLayout.CENTER);
    repaint();
  }
  
  /**
   * 
   */
  protected void showStatistics(Ship ship) {
    log.info("Showing statistics for ship "+ship.getID().toString()+".");
    removeAll();
    add(notImplementedLavel,BorderLayout.CENTER);
    repaint();
  }


  /**
   * @see net.infonode.docking.DockingWindowListener#viewFocusChanged(net.infonode.docking.View, net.infonode.docking.View)
   */
  public void viewFocusChanged(View arg0, View arg1) {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowAdded(net.infonode.docking.DockingWindow, net.infonode.docking.DockingWindow)
   */
  public void windowAdded(DockingWindow arg0, DockingWindow arg1) {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowClosed(net.infonode.docking.DockingWindow)
   */
  public void windowClosed(DockingWindow arg0) {
    log.info("Closing dock...");
    this.isShown=false;
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowClosing(net.infonode.docking.DockingWindow)
   */
  public void windowClosing(DockingWindow arg0) throws OperationAbortedException {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowDocked(net.infonode.docking.DockingWindow)
   */
  public void windowDocked(DockingWindow arg0) {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowDocking(net.infonode.docking.DockingWindow)
   */
  public void windowDocking(DockingWindow arg0) throws OperationAbortedException {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowHidden(net.infonode.docking.DockingWindow)
   */
  public void windowHidden(DockingWindow arg0) {
    log.info("Hidding dock...");
    this.isShown=false;
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowMaximized(net.infonode.docking.DockingWindow)
   */
  public void windowMaximized(DockingWindow arg0) {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowMaximizing(net.infonode.docking.DockingWindow)
   */
  public void windowMaximizing(DockingWindow arg0) throws OperationAbortedException {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowMinimized(net.infonode.docking.DockingWindow)
   */
  public void windowMinimized(DockingWindow arg0) {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowMinimizing(net.infonode.docking.DockingWindow)
   */
  public void windowMinimizing(DockingWindow arg0) throws OperationAbortedException {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowRemoved(net.infonode.docking.DockingWindow, net.infonode.docking.DockingWindow)
   */
  public void windowRemoved(DockingWindow arg0, DockingWindow arg1) {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowRestored(net.infonode.docking.DockingWindow)
   */
  public void windowRestored(DockingWindow arg0) {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowRestoring(net.infonode.docking.DockingWindow)
   */
  public void windowRestoring(DockingWindow arg0) throws OperationAbortedException {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowShown(net.infonode.docking.DockingWindow)
   */
  public void windowShown(DockingWindow arg0) {
    log.info("Showing dock...");
    this.isShown=true;
    refresh();
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowUndocked(net.infonode.docking.DockingWindow)
   */
  public void windowUndocked(DockingWindow arg0) {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowUndocking(net.infonode.docking.DockingWindow)
   */
  public void windowUndocking(DockingWindow arg0) throws OperationAbortedException {
  }
}

/**
 * A model for statistic unit table informations
 *
 * @author Thoralf Rickert
 * @version 1.0, 11.05.2008
 */
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
      case 1: if (data.name != null)     return data.name;  else return "";
      case 2: return data.region;
      case 3: return data.persons;
      case 4: if (data.ship != null)     return data.ship; else return "";
      case 5: if (data.building != null) return data.building; else return "";
      case 6: if (data.race != null)     return data.race; else return "";
      case 7: return new BigDecimal((double)(data.weight / 100)).setScale(2);
      default: {
        String columnName = getColumnName(columnIndex);
        if (data.skills.containsKey(columnName)) return data.skills.get(columnName);
        if (data.items.containsKey(columnName)) return data.items.get(columnName);
      }
    }
    
    return "";
  }
  
  /**
   * @see javax.swing.table.AbstractTableModel#getColumnName(int)
   */
  @Override
  public String getColumnName(int col) {
    return columnNames.get(col);
  }
  
  /**
   * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
   */
  @Override
  public Class<?> getColumnClass(int c) {
    Object o = getValueAt(0, c);
    if (o == null) return null;
    return getValueAt(0, c).getClass();
  }
}

/**
 * A model for statistic region table informations
 *
 * @author Thoralf Rickert
 * @version 1.0, 11.05.2008
 */
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
      case 4: return data.maxRecruits;
      case 5: return data.silver;
      case 6: return data.maxEntertain;
      case 7: return data.maxLuxuries;
      case 8: return data.trees;
      case 9: return data.sprouts;
      case 10: return data.stones;
      case 11: return data.iron;
      case 12: return data.laen;
      default: {
        String columnName = getColumnName(columnIndex);
        if (data.prices.containsKey(columnName)) return data.prices.get(columnName);
      }
    }
    
    return "";
  }
  
  /**
   * @see javax.swing.table.AbstractTableModel#getColumnName(int)
   */
  @Override
  public String getColumnName(int col) {
    return columnNames.get(col);
  }
  
  /**
   * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
   */
  @Override
  public Class<?> getColumnClass(int c) {
    Object o = getValueAt(0, c);
    if (o == null) return null;
    return getValueAt(0, c).getClass();
  }
}

/**
 * A model for statistic faction table informations
 *
 * @author Thoralf Rickert
 * @version 1.0, 11.05.2008
 */
class FactionTableModel extends AbstractTableModel {
  protected List<Integer> turns = new ArrayList<Integer>();
  protected List<String> columnNames = new ArrayList<String>();
  protected FactionStatistics statistics = null;
  
  /**
   * 
   */
  public FactionTableModel(Statistics stat, Faction region) {
    statistics = stat.getStatistics(region);

    columnNames.add(Resources.get("statisticsplugin.faction.turn"));
    columnNames.add(Resources.get("statisticsplugin.faction.name"));
    columnNames.add(Resources.get("statisticsplugin.faction.race"));
    columnNames.add(Resources.get("statisticsplugin.faction.persons"));
    columnNames.add(Resources.get("statisticsplugin.faction.heroes"));
    columnNames.add(Resources.get("statisticsplugin.faction.maxheroes"));
    columnNames.add(Resources.get("statisticsplugin.faction.score"));
    columnNames.add(Resources.get("statisticsplugin.faction.averagescore"));
    
    if (statistics == null) return;
    
    turns = new ArrayList<Integer>(statistics.turnData.keySet());
    Collections.sort(turns);
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
    FactionStatisticsData data = statistics.turnData.get(turn);
    switch (columnIndex) {
      case 0: return turn;
      case 1: return data.name;
      case 2: return data.race;
      case 3: return data.persons;
      case 4: return data.heroes;
      case 5: return data.maxHeroes;
      case 6: return data.score;
      case 7: return data.averageScore;
    }
    
    return "";
  }
  
  /**
   * @see javax.swing.table.AbstractTableModel#getColumnName(int)
   */
  @Override
  public String getColumnName(int col) {
    return columnNames.get(col);
  }
  
  /**
   * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
   */
  @Override
  public Class<?> getColumnClass(int c) {
    Object o = getValueAt(0, c);
    if (o == null) return null;
    return getValueAt(0, c).getClass();
  }
}