package magellan.plugin.statistics;

import java.awt.Component;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.JMenuItem;

import magellan.client.Client;
import magellan.client.extern.MagellanPlugIn;
import magellan.client.swing.preferences.PreferencesFactory;
import magellan.library.GameData;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;

/**
 * This is the entry class for this plugin
 *
 * @author Thoralf Rickert
 * @version 1.0, 03.05.2008
 */
public class StatisticsPlugIn implements MagellanPlugIn {
  private static Logger log = null;
  private Client client = null;
  private Properties settings = null;
  private Statistics statistics = null;
  private StatisticDock dock = null;

  /**
   * @see magellan.client.extern.MagellanPlugIn#init(magellan.client.Client, java.util.Properties)
   */
  public void init(Client client, Properties properties) {
    // init the plugin
    log = Logger.getInstance(StatisticsPlugIn.class);
    Resources.getInstance().initialize(Client.getSettingsDirectory(),"statisticsplugin_");
    this.client = client;
    this.settings = properties;
    log.info(getName()+" initialized...(Client)");
  }


  /**
   * @see magellan.client.extern.MagellanPlugIn#init(magellan.library.GameData)
   */
  public void init(GameData data) {
    // init the report
    LoadThread thread = new LoadThread();
    thread.data = data;
    thread.start();
  }
  
  /**
   * @see magellan.client.extern.MagellanPlugIn#getDocks()
   */
  public Map<String, Component> getDocks() {
    dock = new StatisticDock(client,settings,this);
    
    Map<String, Component> components = new HashMap<String, Component>();
    components.put("Statistics", dock);
    return components;
  }

  /**
   * @see magellan.client.extern.MagellanPlugIn#getMenuItems()
   */
  public List<JMenuItem> getMenuItems() {
    return null;
  }

  /**
   * @see magellan.client.extern.MagellanPlugIn#getName()
   */
  public String getName() {
    return "Statistics Plugin";
  }

  /**
   * @see magellan.client.extern.MagellanPlugIn#getPreferencesProvider()
   */
  public PreferencesFactory getPreferencesProvider() {
    return null;
  }

  /**
   * @see magellan.client.extern.MagellanPlugIn#quit(boolean)
   */
  public void quit(boolean storeSettings) {
    // do nothing (it's only a read process)
  }
  
  class LoadThread extends Thread {
    protected GameData data = null;
    
    /**
     * @see java.lang.Thread#run()
     */
    public void run() {
      log.info(getName()+" initializing...(GameData)");
      try {
        // check if we have a filetype
        if (data.getFileType() == null || data.getFileType().getName() == null || data.getFileType().getFile() == null) return;
        
        File dataFile = data.getFileType().getFile();
        if (!dataFile.exists()) return;
        
        // build filename for statistics...
        String fileName = dataFile.getName();
        if (fileName.indexOf(".") > 0) {
          // remove file extension
          fileName = fileName.substring(0,fileName.lastIndexOf("."));
        }
        fileName = fileName+".statistic.xml.bz2";
        
        File statFile = new File(dataFile.getParentFile(),fileName);
        
        log.info("GameData file is "+dataFile);
        log.info("StatData file is "+statFile);
        
        // load the statistics file
        statistics = new Statistics(statFile);
        
        // add current file
        statistics.add(data);
        
        // view dock
        if (dock != null) {
          dock.show(data.getActiveRegion());
        }
        
        // save statistics
        statistics.save();
        
      } catch (Exception exception) {
        log.error(exception);
        return;
      }
    }
  }
  
  public Statistics getStatistics() {
    return statistics;
  }
}
