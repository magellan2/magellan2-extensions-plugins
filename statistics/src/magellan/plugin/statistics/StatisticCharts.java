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

import magellan.library.Unit;
import magellan.library.utils.Resources;
import magellan.plugin.statistics.data.UnitStatistics;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
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
}
