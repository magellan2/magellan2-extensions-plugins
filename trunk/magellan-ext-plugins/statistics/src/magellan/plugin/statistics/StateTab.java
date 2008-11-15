// class magellan.plugin.statistics.StateTab
// created on 03.11.2008
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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import magellan.library.utils.Resources;

/**
 * This tab shows some informations about the current process
 *
 * @author Thoralf Rickert
 * @version 1.0, 03.11.2008
 */
public class StateTab extends JPanel {
  
  protected StatisticsPlugIn plugin = null;
  
  protected JLabel gameDataFileLabel = null;
  protected JProgressBar completeProgressBar = null;
  protected JProgressBar partProgressBar = null;
  protected JLabel currentStateLabel = null;
  protected JLabel currentObjectLabel = null;
  protected JLabel runTimeLabel = null;
  
  public StateTab(StatisticsPlugIn plugin) {
    
    this.plugin = plugin;
    
    setLayout(new BorderLayout());
    

    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(2, 2, 2, 2);
    
    int line = 0;
    
    // GameData
    c.gridx = 0;
    c.gridy = line;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.EAST;
    c.weightx = 0.0;
    panel.add(new JLabel(Resources.get("statisticsplugin.state.file")),c);
    c.gridx = 1;
    c.gridy = line;
    c.fill = GridBagConstraints.NONE;
    c.anchor = GridBagConstraints.WEST;
    c.weightx = 0.1;
    gameDataFileLabel = new JLabel("-");
    panel.add(gameDataFileLabel, c);
    line++;

    // Percentage
    c.gridx = 0;
    c.gridy = line;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.EAST;
    c.weightx = 0.0;
    panel.add(new JLabel(Resources.get("statisticsplugin.state.complete_percentage")),c);
    c.gridx = 1;
    c.gridy = line;
    c.fill = GridBagConstraints.NONE;
    c.anchor = GridBagConstraints.WEST;
    c.weightx = 0.1;
    completeProgressBar = new JProgressBar(0,100);
    panel.add(completeProgressBar, c);
    line++;

    // Percentage
    c.gridx = 0;
    c.gridy = line;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.EAST;
    c.weightx = 0.0;
    panel.add(new JLabel(Resources.get("statisticsplugin.state.part_percentage")),c);
    c.gridx = 1;
    c.gridy = line;
    c.fill = GridBagConstraints.NONE;
    c.anchor = GridBagConstraints.WEST;
    c.weightx = 0.1;
    partProgressBar = new JProgressBar(0,100);
    panel.add(partProgressBar, c);
    line++;
    
    // CurrentState
    c.gridx = 0;
    c.gridy = line;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.EAST;
    c.weightx = 0.0;
    panel.add(new JLabel(Resources.get("statisticsplugin.state.state")),c);
    c.gridx = 1;
    c.gridy = line;
    c.fill = GridBagConstraints.NONE;
    c.anchor = GridBagConstraints.WEST;
    c.weightx = 0.1;
    currentStateLabel = new JLabel("-");
    panel.add(currentStateLabel, c);
    line++;
    
    // Current Object
    c.gridx = 0;
    c.gridy = line;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.EAST;
    c.weightx = 0.0;
    panel.add(new JLabel(Resources.get("statisticsplugin.state.current_object")),c);
    c.gridx = 1;
    c.gridy = line;
    c.fill = GridBagConstraints.NONE;
    c.anchor = GridBagConstraints.WEST;
    c.weightx = 0.1;
    currentObjectLabel = new JLabel("-");
    panel.add(currentObjectLabel, c);
    line++;
    
    // Runtime
    c.gridx = 0;
    c.gridy = line;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.EAST;
    c.weightx = 0.0;
    panel.add(new JLabel(Resources.get("statisticsplugin.state.runtime")),c);
    c.gridx = 1;
    c.gridy = line;
    c.fill = GridBagConstraints.NONE;
    c.anchor = GridBagConstraints.WEST;
    c.weightx = 0.1;
    runTimeLabel = new JLabel("-");
    panel.add(runTimeLabel, c);
    line++;
    
    add(panel,BorderLayout.CENTER);
    
    Runner runner = new Runner();
    runner.start();
  }
  
  protected class Runner extends Thread {
    public void run() {
      try {
        while (true) {
          sleep(1000);
          if (plugin != null) {
            if (plugin.getStatistics() != null) {
              
              Statistics statistics = plugin.getStatistics();
              
              if (statistics.getState().equals(AnalyzeState.FINISHED)) {
                // reset all values
                currentStateLabel.setText(Resources.get("statisticsplugin.AnalyzeState."+AnalyzeState.FINISHED.toString()));
                completeProgressBar.setValue(100);
                partProgressBar.setValue(100);
                currentObjectLabel.setText("-");
              } else {
                if (plugin.getWorld().getFileType() != null) {
                  gameDataFileLabel.setText(plugin.getWorld().getFileType().getFile().getAbsolutePath());
                } else {
                  gameDataFileLabel.setText("-");
                }
                
                completeProgressBar.setValue(statistics.getCompleteProgress());
                partProgressBar.setValue(statistics.getPartProgress());
                currentStateLabel.setText(Resources.get("statisticsplugin.AnalyzeState."+statistics.getState().toString()));
                
                if (statistics.getCurrentObject() != null) {
                  currentObjectLabel.setText(statistics.getCurrentObject().getName() + " (" + statistics.getCurrentObject().getID()+")");
                } else {
                  currentObjectLabel.setText("-");
                }
                
                int runtime = (int)(statistics.getRuntime() / 1000);
                runTimeLabel.setText(runtime+" sec");
              }
            }
          }
        }
      } catch (Exception exception) {
        exception.printStackTrace(System.err);
      }
    }
  }
}
