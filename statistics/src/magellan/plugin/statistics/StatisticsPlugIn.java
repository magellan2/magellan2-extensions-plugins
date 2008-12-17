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
import magellan.plugin.statistics.db.DerbyConnector;

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
  private LoadThread thread = null;
  protected GameData world = null;

  /**
   * @see magellan.client.extern.MagellanPlugIn#init(magellan.client.Client, java.util.Properties)
   */
  public void init(Client client, Properties properties) {
    // init the plugin
    log = Logger.getInstance(StatisticsPlugIn.class);
    Resources.getInstance().initialize(Client.getSettingsDirectory(),"statisticsplugin_");
    this.client = client;
    this.settings = properties;
    
    DerbyConnector.getInstance().init(properties, Client.getMagellanDirectory(), Client.getSettingsDirectory());
    
    log.info(getName()+" initialized...(Client)");
  }


  /**
   * @see magellan.client.extern.MagellanPlugIn#init(magellan.library.GameData)
   */
  public void init(GameData data) {
    world = data;
    
    // init the report
    if (thread != null) {
      // there is already a thread that analyse data
      // we have to shut it down
      // we use a soft shutdown to prevent the database
      // to be corrupt.
      thread.shutdown();
    }
    
    thread = new LoadThread();
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
    // close the database
    DerbyConnector.getInstance().shutdown();
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
        String reportName = dataFile.getName();
        if (reportName.lastIndexOf(".")>0) { 
          reportName = reportName.substring(0,reportName.lastIndexOf("."));
        }
        log.info("GameData file is "+dataFile);
        
        // load the statistics file
        statistics = new Statistics(reportName);
        
        // add current file
        statistics.add(data);
        
        // view dock
        if (dock != null) {
          dock.show(data.getActiveRegion());
        }
        
      } catch (Exception exception) {
        log.error(exception);
        return;
      }
    }
    
    public void shutdown() {
      if (statistics != null) statistics.setShutdown(true);
    }
  }
  
  public Statistics getStatistics() {
    return statistics;
  }


  /**
   * Returns the value of thread.
   * 
   * @return Returns thread.
   */
  public LoadThread getThread() {
    return thread;
  }


  /**
   * Sets the value of thread.
   *
   * @param thread The value for thread.
   */
  public void setThread(LoadThread thread) {
    this.thread = thread;
  }


  /**
   * Returns the value of world.
   * 
   * @return Returns world.
   */
  public GameData getWorld() {
    return world;
  }


  /**
   * Sets the value of world.
   *
   * @param world The value for world.
   */
  public void setWorld(GameData world) {
    this.world = world;
  }
  
  
}