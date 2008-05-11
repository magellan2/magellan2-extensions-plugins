// class magellan.plugin.statistics.StatisticCharts
// created on 11.05.2008
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

import java.awt.BasicStroke;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JScrollPane;

import magellan.library.*;
import magellan.library.utils.Resources;
import magellan.plugin.statistics.data.FactionStatistics;
import magellan.plugin.statistics.data.RegionStatistics;
import magellan.plugin.statistics.data.UnitStatistics;
import magellan.plugin.statistics.data.FactionStatistics.FactionStatisticsData;
import magellan.plugin.statistics.data.RegionStatistics.RegionStatisticsData;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYStepRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;

public class StatisticCharts {

  /**
   * Creates a chart of all known skills of a unit for the known period of time.
   */
  public static JComponent createSkillChart(StatisticsPlugIn plugin, Unit unit) {
    UnitStatistics stats = plugin.getStatistics().getStatistics(unit);
    if (stats != null) {
      String title = Resources.get("statisticsplugin.unit.skills");
      // time
      String xAxisTitle = Resources.get("statisticsplugin.unit.skills.xAxis");
      // skilllevel
      String yAxisTitle = Resources.get("statisticsplugin.unit.skills.yAxis");
      
      // for each skill one series
      XYSeriesCollection dataset = new XYSeriesCollection();
      Map<String,XYSeries> series = new HashMap<String, XYSeries>();
      
      // sort the turn data
      List<Integer> turns = new ArrayList<Integer>(stats.turnData.keySet());
      Collections.sort(turns);
      
      // for every turn...
      for (Integer turn : turns) {
        Map<String,Integer> skills = stats.turnData.get(turn).skills;
        
        // ...get the skills...
        for (String skill : skills.keySet()) {
          XYSeries serie = null;
          
          // ...check, if we have a known skill...
          if (series.containsKey(skill)) {
            // ...yes, get it out of the map...
            serie = series.get(skill);
          } else {
            // ...no, create a new series for a skill...
            serie = new XYSeries(skill);
            series.put(skill, serie);
          }
          
          // ...and add to this series the new data pair of turn and skill level.
          serie.add(turn.doubleValue(), skills.get(skill));
        }
      }
      
      for (XYSeries serie : series.values()) {
        dataset.addSeries(serie);
      }
      
      JFreeChart chart = ChartFactory.createXYLineChart(title, xAxisTitle, yAxisTitle, dataset, PlotOrientation.VERTICAL, true, true, false);
      XYPlot plot = (XYPlot)chart.getPlot();
      
      XYStepRenderer renderer = new XYStepRenderer();
      renderer.setBaseShapesVisible(true);
      renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
      renderer.setDefaultEntityRadius(6);
      plot.setRenderer(renderer);

      for (int i=0; i<series.size(); i++) {
        renderer.setSeriesShapesVisible(i,true);
        renderer.setSeriesShapesFilled(i,true);
        renderer.setSeriesStroke(i, new BasicStroke(2.0f));
      }

      plot.setAxisOffset(new RectangleInsets(1.0, 1.0, 1.0, 1.0));
      
      plot.getRangeAxis().setStandardTickUnits(NumberAxis.createIntegerTickUnits());
      plot.getDomainAxis().setStandardTickUnits(NumberAxis.createIntegerTickUnits());
      
      return new ChartPanel(chart);
    } else {
      return new JScrollPane();
    }
  }
  
  /**
   * Creates a chart of all known region resources of a region for the known period of time.
   */
  public static JComponent createResourcesChart(StatisticsPlugIn plugin, Region region) {
    RegionStatistics stats = plugin.getStatistics().getStatistics(region);
    if (stats != null) {
      String title = Resources.get("statisticsplugin.region.resources");
      // time
      String xAxisTitle = Resources.get("statisticsplugin.region.resources.xAxis");
      // skilllevel
      String yAxisTitle = Resources.get("statisticsplugin.region.resources.yAxis");
      
      // for each skill one series
      XYSeriesCollection dataset = new XYSeriesCollection();
      Map<String,XYSeries> series = new HashMap<String, XYSeries>();
      series.put("peasants",     new XYSeries(Resources.get("statisticsplugin.region.peasants")));
      series.put("maxrecruits",  new XYSeries(Resources.get("statisticsplugin.region.maxRecruits")));
    //  series.put("silver",       new XYSeries(Resources.get("statisticsplugin.region.silver")));
      series.put("maxluxuries",  new XYSeries(Resources.get("statisticsplugin.region.maxLuxuries")));
      series.put("maxentertain", new XYSeries(Resources.get("statisticsplugin.region.maxEntertain") +"*100"));
      series.put("stones",       new XYSeries(Resources.get("statisticsplugin.region.stones")));
      series.put("trees",        new XYSeries(Resources.get("statisticsplugin.region.trees")));
      series.put("sprouts",      new XYSeries(Resources.get("statisticsplugin.region.sprouts")));
      series.put("iron",         new XYSeries(Resources.get("statisticsplugin.region.iron")));
      series.put("laen",         new XYSeries(Resources.get("statisticsplugin.region.laen")));
      
      // sort the turn data
      List<Integer> turns = new ArrayList<Integer>(stats.turnData.keySet());
      Collections.sort(turns);
      
      // for every turn...
      for (Integer turn : turns) {
        RegionStatisticsData data = stats.turnData.get(turn);
        
        if (data.peasants>=0)     series.get("peasants").add(turn,new Integer(data.peasants)); 
        if (data.maxRecruits>=0)  series.get("maxrecruits").add(turn,new Integer(data.maxRecruits)); 
      //  if (data.silver>=0)       series.get("silver").add(turn,new Integer(data.silver)); 
        if (data.maxLuxuries>=0)  series.get("maxluxuries").add(turn,new Integer(data.maxLuxuries)); 
        if (data.maxEntertain>=0) series.get("maxentertain").add(turn,new Integer(data.maxEntertain/1000)); 
        if (data.stones>=0)       series.get("stones").add(turn,new Integer(data.stones)); 
        if (data.trees>=0)        series.get("trees").add(turn,new Integer(data.trees)); 
        if (data.sprouts>=0)      series.get("sprouts").add(turn,new Integer(data.sprouts)); 
        if (data.iron>=0)         series.get("iron").add(turn,new Integer(data.iron)); 
        if (data.laen>=0)         series.get("laen").add(turn,new Integer(data.laen)); 
        
      }
      
      for (XYSeries serie : series.values()) {
        dataset.addSeries(serie);
      }
      
      JFreeChart chart = ChartFactory.createXYLineChart(title, xAxisTitle, yAxisTitle, dataset, PlotOrientation.VERTICAL, true, true, false);
      XYPlot plot = (XYPlot)chart.getPlot();
      
      XYStepRenderer renderer = new XYStepRenderer();
      renderer.setBaseShapesVisible(true);
      renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
      renderer.setDefaultEntityRadius(6);
      plot.setRenderer(renderer);

      for (int i=0; i<series.size(); i++) {
        renderer.setSeriesShapesVisible(i,true);
        renderer.setSeriesShapesFilled(i,true);
        renderer.setSeriesStroke(i, new BasicStroke(2.0f));
      }

      plot.setAxisOffset(new RectangleInsets(1.0, 1.0, 1.0, 1.0));
      
      plot.getRangeAxis().setStandardTickUnits(NumberAxis.createIntegerTickUnits());
      plot.getDomainAxis().setStandardTickUnits(NumberAxis.createIntegerTickUnits());
      
      return new ChartPanel(chart);
    } else {
      return new JScrollPane();
    }
  }
  
  /**
   * Creates a chart of all known luxury prices of a region for the known period of time.
   */
  public static JComponent createTradeChart(StatisticsPlugIn plugin, Region region) {
    RegionStatistics stats = plugin.getStatistics().getStatistics(region);
    if (stats != null) {
      String title = Resources.get("statisticsplugin.region.prices");
      // time
      String xAxisTitle = Resources.get("statisticsplugin.region.prices.xAxis");
      // skilllevel
      String yAxisTitle = Resources.get("statisticsplugin.region.prices.yAxis");

      // for each skill one series
      XYSeriesCollection dataset = new XYSeriesCollection();
      Map<String,XYSeries> series = new HashMap<String, XYSeries>();

      // sort the turn data
      List<Integer> turns = new ArrayList<Integer>(stats.turnData.keySet());
      Collections.sort(turns);
      
      // for every turn...
      for (Integer turn : turns) {
        RegionStatisticsData data = stats.turnData.get(turn);
        Map<String,Integer> prices = data.prices;
        
        for (String luxury : prices.keySet()) {
          XYSeries serie = null;
          if (series.containsKey(luxury)) {
            serie = series.get(luxury);
          } else {
            serie = new XYSeries(luxury);
            series.put(luxury, serie);
          }
          serie.add(turn,prices.get(luxury));
        }
        
      }
      
      
      for (XYSeries serie : series.values()) {
        dataset.addSeries(serie);
      }
      
      JFreeChart chart = ChartFactory.createXYLineChart(title, xAxisTitle, yAxisTitle, dataset, PlotOrientation.VERTICAL, true, true, false);
      XYPlot plot = (XYPlot)chart.getPlot();
      
      XYStepRenderer renderer = new XYStepRenderer();
      renderer.setBaseShapesVisible(true);
      renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
      renderer.setDefaultEntityRadius(6);
      plot.setRenderer(renderer);

      for (int i=0; i<series.size(); i++) {
        renderer.setSeriesShapesVisible(i,true);
        renderer.setSeriesShapesFilled(i,true);
        renderer.setSeriesStroke(i, new BasicStroke(2.0f));
      }

      plot.setAxisOffset(new RectangleInsets(1.0, 1.0, 1.0, 1.0));
      
      plot.getRangeAxis().setStandardTickUnits(NumberAxis.createIntegerTickUnits());
      plot.getDomainAxis().setStandardTickUnits(NumberAxis.createIntegerTickUnits());
      
      return new ChartPanel(chart);
    } else {
      return new JScrollPane();
    }
  }
  
  /**
   * Creates a chart of all known points of a faction for the known period of time.
   */
  public static JComponent createPointsChart(StatisticsPlugIn plugin, Faction faction) {
    FactionStatistics stats = plugin.getStatistics().getStatistics(faction);
    if (stats != null) {
      String title = Resources.get("statisticsplugin.faction.points");
      // time
      String xAxisTitle = Resources.get("statisticsplugin.faction.points.xAxis");
      // skilllevel
      String yAxisTitle = Resources.get("statisticsplugin.faction.points.yAxis");
      
      // for each skill one series
      XYSeriesCollection dataset = new XYSeriesCollection();
      Map<String,XYSeries> series = new HashMap<String, XYSeries>();
      series.put("points",  new XYSeries(Resources.get("statisticsplugin.faction.points")));
      series.put("average", new XYSeries(Resources.get("statisticsplugin.faction.averagescores")));
      
      // sort the turn data
      List<Integer> turns = new ArrayList<Integer>(stats.turnData.keySet());
      Collections.sort(turns);
      
      // for every turn...
      for (Integer turn : turns) {
        FactionStatisticsData data = stats.turnData.get(turn);
        
        if (data.score>=0)        series.get("points").add(turn,new Integer(data.score)); 
        if (data.averageScore>=0) series.get("average").add(turn,new Integer(data.averageScore)); 
      }
      
      for (XYSeries serie : series.values()) {
        dataset.addSeries(serie);
      }
      
      JFreeChart chart = ChartFactory.createXYLineChart(title, xAxisTitle, yAxisTitle, dataset, PlotOrientation.VERTICAL, true, true, false);
      XYPlot plot = (XYPlot)chart.getPlot();
      
      XYStepRenderer renderer = new XYStepRenderer();
      renderer.setBaseShapesVisible(true);
      renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
      renderer.setDefaultEntityRadius(6);
      plot.setRenderer(renderer);

      for (int i=0; i<series.size(); i++) {
        renderer.setSeriesShapesVisible(i,true);
        renderer.setSeriesShapesFilled(i,true);
        renderer.setSeriesStroke(i, new BasicStroke(2.0f));
      }

      plot.setAxisOffset(new RectangleInsets(1.0, 1.0, 1.0, 1.0));
      
      plot.getRangeAxis().setStandardTickUnits(NumberAxis.createIntegerTickUnits());
      plot.getDomainAxis().setStandardTickUnits(NumberAxis.createIntegerTickUnits());
      
      return new ChartPanel(chart);
    } else {
      return new JScrollPane();
    }
  }
  
  /**
   * Creates a chart of all known unit amount of a faction for the known period of time.
   */
  public static JComponent createUnitsChart(StatisticsPlugIn plugin, Faction faction) {
    FactionStatistics stats = plugin.getStatistics().getStatistics(faction);
    if (stats != null) {
      String title = Resources.get("statisticsplugin.faction.units");
      // time
      String xAxisTitle = Resources.get("statisticsplugin.faction.units.xAxis");
      // skilllevel
      String yAxisTitle = Resources.get("statisticsplugin.faction.units.yAxis");
      
      // for each skill one series
      XYSeriesCollection dataset = new XYSeriesCollection();
      Map<String,XYSeries> series = new HashMap<String, XYSeries>();
      series.put("persons",   new XYSeries(Resources.get("statisticsplugin.faction.units")));
      series.put("heroes",    new XYSeries(Resources.get("statisticsplugin.faction.heroes")));
      series.put("maxheroes", new XYSeries(Resources.get("statisticsplugin.faction.maxheroes")));
      
      // sort the turn data
      List<Integer> turns = new ArrayList<Integer>(stats.turnData.keySet());
      Collections.sort(turns);
      
      // for every turn...
      for (Integer turn : turns) {
        FactionStatisticsData data = stats.turnData.get(turn);
        
        if (data.persons>=0)   series.get("persons").add(turn,new Integer(data.persons)); 
        if (data.heroes>=0)    series.get("heroes").add(turn,new Integer(data.heroes)); 
        if (data.maxHeroes>=0) series.get("maxheroes").add(turn,new Integer(data.maxHeroes)); 
      }
      
      for (XYSeries serie : series.values()) {
        dataset.addSeries(serie);
      }
      
      JFreeChart chart = ChartFactory.createXYLineChart(title, xAxisTitle, yAxisTitle, dataset, PlotOrientation.VERTICAL, true, true, false);
      XYPlot plot = (XYPlot)chart.getPlot();
      
      XYStepRenderer renderer = new XYStepRenderer();
      renderer.setBaseShapesVisible(true);
      renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
      renderer.setDefaultEntityRadius(6);
      plot.setRenderer(renderer);

      for (int i=0; i<series.size(); i++) {
        renderer.setSeriesShapesVisible(i,true);
        renderer.setSeriesShapesFilled(i,true);
        renderer.setSeriesStroke(i, new BasicStroke(2.0f));
      }

      plot.setAxisOffset(new RectangleInsets(1.0, 1.0, 1.0, 1.0));
      
      plot.getRangeAxis().setStandardTickUnits(NumberAxis.createIntegerTickUnits());
      plot.getDomainAxis().setStandardTickUnits(NumberAxis.createIntegerTickUnits());
      
      return new ChartPanel(chart);
    } else {
      return new JScrollPane();
    }
  }
}
