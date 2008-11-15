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
import java.util.HashMap;
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
import magellan.plugin.statistics.torque.FactionStatistics;
import magellan.plugin.statistics.torque.FactionStatisticsData;
import magellan.plugin.statistics.torque.RegionStatistics;
import magellan.plugin.statistics.torque.RegionStatisticsData;
import magellan.plugin.statistics.torque.RegionStatisticsPricesData;
import magellan.plugin.statistics.torque.UnitStatistics;
import magellan.plugin.statistics.torque.UnitStatisticsData;
import magellan.plugin.statistics.torque.UnitStatisticsItemData;
import magellan.plugin.statistics.torque.UnitStatisticsSkillData;
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
  protected StateTab stateTab = null;
  protected Object activeObject = null;
  protected JLabel waitLabel = null;
  protected JLabel notImplementedLabel = null;
  
  protected JComponent component = null;
  
  protected boolean isShown = false;
  
  protected HashMap<VisibleStatisticType, Integer> activeTab = new HashMap<VisibleStatisticType, Integer>();
  protected VisibleStatisticType currentStatistic = null;

  /**
   * 
   */
  public StatisticDock(Client client, Properties settings, StatisticsPlugIn statisticsPlugIn) {
    this.plugin = statisticsPlugIn;
    setLayout(new BorderLayout());
    
    client.getDispatcher().addSelectionListener(this);
    
    ImageIcon wait = client.getMagellanContext().getImageFactory().loadImageIcon("wait30trans");
    
    waitLabel = new JLabel(Resources.get("statisticsplugin.dock.wait"),wait,JLabel.HORIZONTAL);
    
    notImplementedLabel = new JLabel(Resources.get("statisticsplugin.dock.notimplemented"));
    notImplementedLabel.setHorizontalAlignment(JLabel.HORIZONTAL);
    
    component = new JPanel(new BorderLayout());
    
    stateTab = new StateTab(plugin);
    component.add(stateTab,BorderLayout.CENTER);
    
    
    add(component,BorderLayout.CENTER);
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
      new StatisticDockSelectionChangedThread(activeObject).start();
    }
  }
  
  /**
   * Shows the given region.
   * Normally used after a loading/merging process in the client.
   */
  public void show(Region region) {
    if (region == null) return;
    new StatisticDockSelectionChangedThread(region).start();
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
    public synchronized void run() {
      if (activeObject == null) return;

      log.info("Selection Changed. "+activeObject);
      
      if (activeObject instanceof Unit) {
        component = showStatistics((Unit)activeObject);
        
      } else if (activeObject instanceof Region) {
        component = showStatistics((Region)activeObject);
        
      } else if (activeObject instanceof Faction) {
        component = showStatistics((Faction)activeObject);
        
      } else if (activeObject instanceof Building) {
        component = showStatistics((Building)activeObject);
        
      } else if (activeObject instanceof Ship) {
        component = showStatistics((Ship)activeObject);
        
      }
      
      if (component != null) {
        removeAll();
        add(component,BorderLayout.CENTER);
        repaint();
      }
    }
  }
  
  /**
   * 
   */
  protected JComponent showStatistics(Unit unit) {
    if (plugin.getStatistics() != null) {
      log.info("Showing statistics for unit "+unit.getID().toString()+".");
      UnitTableModel model = new UnitTableModel(plugin.getStatistics(),unit);
      
      table = new JTable(model);
      table.setAutoCreateRowSorter(true);
      tableTab = new JScrollPane(table);
      
      skillsTab = StatisticCharts.createSkillChart(plugin,unit);
      
      itemsTab = new JScrollPane();
      
      if (stateTab == null) {
        stateTab = new StateTab(plugin);
      }
      
      // save last tab position
      if (tabbedPane != null) {
        if (currentStatistic != null) {
          activeTab.put(currentStatistic,tabbedPane.getSelectedIndex());
        } 
      }
      
      tabbedPane = new JTabbedPane();
      tabbedPane.addTab(Resources.get("statisticsplugin.state.title"), stateTab);
      tabbedPane.addTab(Resources.get("statisticsplugin.unit.table"), tableTab);
      tabbedPane.addTab(Resources.get("statisticsplugin.unit.skills"), skillsTab);
      tabbedPane.addTab(Resources.get("statisticsplugin.unit.items"), itemsTab);
      
      // reload last tab position
      currentStatistic = VisibleStatisticType.UNIT;
      if (activeTab.containsKey(currentStatistic)) {
        int currentTab = activeTab.get(currentStatistic);
        if (currentTab >= 0) tabbedPane.setSelectedIndex(currentTab);
      }
      
      return tabbedPane;
    }
    
    return null;
  }
  
  /**
   * 
   */
  protected JComponent showStatistics(Region region) {
    if (plugin.getStatistics() != null) {
      log.info("Showing statistics for region "+region.getID().toString()+".");
      RegionTableModel model = new RegionTableModel(plugin.getStatistics(),region);
      
      table = new JTable(model);
      table.setAutoCreateRowSorter(true);
      tableTab = new JScrollPane(table);
      
      resourcesTab = StatisticCharts.createResourcesChart(plugin,region);
      tradesTab = StatisticCharts.createTradeChart(plugin,region);
      
      if (stateTab == null) {
        stateTab = new StateTab(plugin);
      }
      
      // save last tab position
      if (tabbedPane != null) {
        if (currentStatistic != null) {
          activeTab.put(currentStatistic,tabbedPane.getSelectedIndex());
        } 
      }
      
      tabbedPane = new JTabbedPane();
      tabbedPane.addTab(Resources.get("statisticsplugin.state.title"), stateTab);
      tabbedPane.addTab(Resources.get("statisticsplugin.region.table"), tableTab);
      tabbedPane.addTab(Resources.get("statisticsplugin.region.resources"), resourcesTab);
      tabbedPane.addTab(Resources.get("statisticsplugin.region.trades"), tradesTab);

      // reload last tab position
      currentStatistic = VisibleStatisticType.REGION;
      if (activeTab.containsKey(currentStatistic)) {
        int currentTab = activeTab.get(currentStatistic);
        if (currentTab >= 0) tabbedPane.setSelectedIndex(currentTab);
      }
      
      return tabbedPane;
    }
    
    return null;
  }
  
  /**
   * 
   */
  protected JComponent showStatistics(Faction faction) {
    if (plugin.getStatistics() != null) {
      log.info("Showing statistics for faction "+faction.getID().toString()+".");
      FactionTableModel model = new FactionTableModel(plugin.getStatistics(),faction);
      
      table = new JTable(model);
      table.setAutoCreateRowSorter(true);
      tableTab = new JScrollPane(table);
      
      pointsTab = StatisticCharts.createPointsChart(plugin,faction);
      unitsTab = StatisticCharts.createUnitsChart(plugin,faction);
      
      if (stateTab == null) {
        stateTab = new StateTab(plugin);
      }
      
      
      // save last tab position
      if (tabbedPane != null) {
        if (currentStatistic != null) {
          activeTab.put(currentStatistic,tabbedPane.getSelectedIndex());
        } 
      }

      tabbedPane = new JTabbedPane();
      tabbedPane.addTab(Resources.get("statisticsplugin.state.title"), stateTab);
      tabbedPane.addTab(Resources.get("statisticsplugin.faction.table"), tableTab);
      tabbedPane.addTab(Resources.get("statisticsplugin.faction.points"), pointsTab);
      tabbedPane.addTab(Resources.get("statisticsplugin.faction.units"), unitsTab);
      
      // reload last tab position
      currentStatistic = VisibleStatisticType.FACTION;
      if (activeTab.containsKey(currentStatistic)) {
        int currentTab = activeTab.get(currentStatistic);
        if (currentTab >= 0) tabbedPane.setSelectedIndex(currentTab);
      }
      
      return tabbedPane;
    }
    
    return null;
  }
  
  /**
   * 
   */
  protected JComponent showStatistics(Building building) {
    log.info("Showing statistics for building "+building.getID().toString()+".");
    return notImplementedLabel;
  }
  
  /**
   * 
   */
  protected JComponent showStatistics(Ship ship) {
    log.info("Showing statistics for ship "+ship.getID().toString()+".");
    return notImplementedLabel;
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
  protected List<UnitStatisticsData> turns = new ArrayList<UnitStatisticsData>();
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
    
    turns = statistics.getData();
    
    // 1. columns for all skills
    for (UnitStatisticsData turn : turns) {
      List<UnitStatisticsSkillData> skills = turn.getSkillData();
      for (UnitStatisticsSkillData skill : skills) {
        if (!columnNames.contains(skill.getSkill())) columnNames.add(skill.getSkill());
      }
    }
    
    // 2. columns for all items
    for (UnitStatisticsData turn : turns) {
      List<UnitStatisticsItemData> items = turn.getItemData();
      for (UnitStatisticsItemData item : items) {
        if (!columnNames.contains(item.getItemType())) columnNames.add(item.getItemType());
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
    UnitStatisticsData turn = turns.get(rowIndex);
    switch (columnIndex) {
      case 0: return turn.getTurn();
      case 1: if (turn.getName() != null)     return turn.getName();  else return "";
      case 2: return turn.getRegion();
      case 3: return turn.getPersons();
      case 4: if (turn.getShip() != null)     return turn.getShip(); else return "";
      case 5: if (turn.getBuilding() != null) return turn.getBuilding(); else return "";
      case 6: if (turn.getRace() != null)     return turn.getRace(); else return "";
      case 7: return new BigDecimal((double)(turn.getWeight() / 100)).setScale(2);
      default: {
        String columnName = getColumnName(columnIndex);
        List<UnitStatisticsSkillData> skills = turn.getSkillData();
        for (UnitStatisticsSkillData skill : skills) {
          if (skill.getSkill().equals(columnName)) return skill.getLevel();
        }
        List<UnitStatisticsItemData> itemdata = turn.getItemData();
        for (UnitStatisticsItemData item : itemdata) {
          if (item.getItemType().equals(columnName)) return item.getAmount();
        }
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
  protected List<RegionStatisticsData> turns = new ArrayList<RegionStatisticsData>();
  protected List<String> columnNames = new ArrayList<String>();
  protected RegionStatistics statistics = null;
  
  public RegionTableModel(Statistics stat, Region region) {
    statistics = stat.getStatistics(region);

    columnNames.add(Resources.get("statisticsplugin.region.turn"));
    columnNames.add(Resources.get("statisticsplugin.region.name"));
    columnNames.add(Resources.get("statisticsplugin.region.type"));
    columnNames.add(Resources.get("statisticsplugin.region.peasants"));
    columnNames.add(Resources.get("statisticsplugin.region.inhabitants"));
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
    
    turns = statistics.getData();
    
    for (RegionStatisticsData turn : turns) {
      List<RegionStatisticsPricesData> prices = turn.getPrices();
      for (RegionStatisticsPricesData price : prices) {
        if (!columnNames.contains(price.getLuxuryItem())) columnNames.add(price.getLuxuryItem());
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
    RegionStatisticsData turn = turns.get(rowIndex);
    switch (columnIndex) {
      case 0: return turn.getTurn();
      case 1: return turn.getName();
      case 2: return turn.getType();
      case 3: return turn.getPeasants();
      case 4: return turn.getInhabitants();
      case 5: return turn.getMaxRecruits();
      case 6: return turn.getSilver();
      case 7: return turn.getMaxEntertain();
      case 8: return turn.getMaxLuxuries();
      case 9: return turn.getTrees();
      case 10: return turn.getSprouts();
      case 11: return turn.getStones();
      case 12: return turn.getIron();
      case 13: return turn.getLaen();
      default: {
        String columnName = getColumnName(columnIndex);
        List<RegionStatisticsPricesData> prices = turn.getPrices();
        for (RegionStatisticsPricesData price : prices) {
          if (price.getLuxuryItem().equals(columnName)) return price.getPrice();
        }
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
  protected List<FactionStatisticsData> turns = new ArrayList<FactionStatisticsData>();
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
    
    turns = statistics.getData();
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
    FactionStatisticsData turn = turns.get(rowIndex);
    switch (columnIndex) {
      case 0: return turn.getTurn();
      case 1: return turn.getName();
      case 2: return turn.getRace();
      case 3: return turn.getPersons();
      case 4: return turn.getHeroes();
      case 5: return turn.getMaxHeroes();
      case 6: return turn.getScore();
      case 7: return turn.getAverageScore();
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