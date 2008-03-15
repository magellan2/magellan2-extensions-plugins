package magellan.plugin.memorywatch;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import magellan.client.Client;
import magellan.client.extern.MagellanPlugIn;
import magellan.client.swing.preferences.PreferencesFactory;
import magellan.library.GameData;
import magellan.library.utils.logging.Logger;

public class ConnectorPlugin implements MagellanPlugIn, ActionListener{
	  private static final Logger log = Logger.getInstance(ConnectorPlugin.class);
	  
	  /**
	   * List of all known Actions
	   */
	  private ArrayList<MemoryWatchAction> memoryActions = null; 
	
	  /**
	   * our Client
	   */
	  private Client client=null;
	  
	  /**
	   * Returns the Name of the PlugIn. This name will
	   * be presented to the user in the options panel.
	   */
	  public String getName(){
		  return "MemoryWatch";
	  }
	  
	  /**
	   * This method is called during client start up
	   * procedure. You can use this method to initialize
	   * your PlugIn (load preferences and so on...)
	   * 
	   * @param client     the main application
	   * @param properties the already loaded configuration
	   */
	  public void init(Client client, Properties properties){
		log.info("pluginInit (client):" + getName());
		this.client = client;
		
		
		this.memoryActions = new ArrayList<MemoryWatchAction>();
		
		this.memoryActions.add(new ShowMemory(client));
		
	  }
	  
	  /**
	   * This method is called everytime the user has load a
	   * file into Magellan (open or add). You should use
	   * this method to load report specific informations.
	   * 
	   * @param data the loaded and merged gamedata
	   */
	  public void init(GameData data){
		  log.info("pluginInit (GameData):" + getName());   
	  }
	  
	  /**
	   * Returns the menu items that should be added to the
	   * Magellan PlugIn menu. You can return multiple menu
	   * items for every kind of action that is available
	   * in your PlugIn.
	   */
	  public List<JMenuItem> getMenuItems(){
		  
		  List<JMenuItem> erg = new ArrayList<JMenuItem>();
		  
		  JMenu menuTop = new JMenu(getName());
		  
		  // Submenus
		  for (Iterator<MemoryWatchAction> iter = this.memoryActions.iterator();iter.hasNext();){
			  MemoryWatchAction actAction = (MemoryWatchAction)iter.next();
			  
			  JMenuItem actionItem = new JMenuItem(actAction.getName());
			  actionItem.setActionCommand(actAction.getName());
			  actionItem.addActionListener(this);
			  menuTop.add(actionItem);
		  }
		  
		  erg.add(menuTop);
		  
		  return erg;
	  }
	  
	  /**
	   * This method is called whenever the application
	   * stops.
	   */
	  public void quit(boolean storeSettings){
		  log.info("pluginQuit (client):" + getName());   
	  }

	  /**
	   * handels the event that one of our Items was selected
	   * @param e the event
	   */
	  public void actionPerformed(ActionEvent e) {
	    String actionCommand = e.getActionCommand();
	    for (Iterator<MemoryWatchAction> iter = this.memoryActions.iterator();iter.hasNext();){
			  MemoryWatchAction actAction = (MemoryWatchAction)iter.next();
			  if (actionCommand.equals(actAction.getName())){
				  actAction.activate(this.client);
			  }
		  }
	  }

		/**
		 * 
		 */
		public PreferencesFactory getPreferencesProvider() {
			return null;
		}

	  /**
	   * @see magellan.client.extern.MagellanPlugIn#getDocks()
	   */
	  public Map<String, Component> getDocks() {
	    return null;
	  }
}
