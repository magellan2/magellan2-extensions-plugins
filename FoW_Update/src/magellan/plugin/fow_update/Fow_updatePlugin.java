package magellan.plugin.fow_update;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import magellan.client.Client;
import magellan.client.extern.MagellanPlugIn;
import magellan.client.swing.preferences.PreferencesFactory;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.Region.Visibility;
import magellan.library.utils.logging.Logger;



public class Fow_updatePlugin implements MagellanPlugIn, ActionListener {

	public static final String version="0.2";
	
	private GameData gd = null;
	
	private static Logger log = null;
	
	private Client client = null;

	
	
	
	/* (non-Javadoc)
	 * @see magellan.client.extern.MagellanPlugIn#getDocks()
	 */
	public Map<String, Component> getDocks() {
		return null;
	}
	/* (non-Javadoc)
	 * @see magellan.client.extern.MagellanPlugIn#getMenuItems()
	 */
	public List<JMenuItem> getMenuItems() {
		List<JMenuItem> items = new ArrayList<JMenuItem>();

		JMenu menu = new JMenu(getName());
		
		
		JMenuItem toogleIconsMenu = new JMenuItem("About FoW Update");
		toogleIconsMenu.setActionCommand("showInfo");
		toogleIconsMenu.addActionListener(this);
		menu.add(toogleIconsMenu);
		
		
		
		
		
		items.add(menu);

		return items;
	}
	/* (non-Javadoc)
	 * @see magellan.client.extern.MagellanPlugIn#getName()
	 */
	public String getName() {
		return "FoW_Update-PlugIn";
	}
	/* (non-Javadoc)
	 * @see magellan.client.extern.MagellanPlugIn#getPreferencesProvider()
	 */
	public PreferencesFactory getPreferencesProvider() {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see magellan.client.extern.MagellanPlugIn#init(magellan.client.Client, java.util.Properties)
	 */
	public void init(Client _client, Properties _properties) {

		log = Logger.getInstance(Fow_updatePlugin.class);
		client = _client;
		log.info(getName() + " initialized (Client)...");
		
		
		
	}
	
	
	
	
	/* (non-Javadoc)
	 * @see magellan.client.extern.MagellanPlugIn#init(magellan.library.GameData)
	 */
	public void init(GameData data) {
		// init the report
		gd = data;
		log.info(getName() + " initialized with new GameData...");
		adjustFogOfWar2Visibility();
		log.info(getName() + " finished with Fog of War");
	}
	/* (non-Javadoc)
	 * @see magellan.client.extern.MagellanPlugIn#quit(boolean)
	 */
	public void quit(boolean storeSettings) {
		// TODO Auto-generated method stub
		
	}

	/**
	   * Removes the FoW for regions with visibility greaterthan lighthouse
	   * 
	   * @param data World
	   */
	  private void adjustFogOfWar2Visibility() {
	    if (gd.regions()!= null) {
	      for (Region r : gd.regions().values()) {
	        r.setFogOfWar(-1);
	        if (r.getVisibility().greaterEqual(Visibility.LIGHTHOUSE)) {
	          r.setFogOfWar(0);
	        }
	      }
	    }
	  }
	  
	  
	  
	
	  
	  /* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			log.info(getName() + ": action " + e.getActionCommand());

			// showInfo
			if (e.getActionCommand().equalsIgnoreCase("showInfo")){
				new Thread(new Runnable() {
					public void run() {
						new MsgBox(client,"FogOfWar-Update from Fiete, Version " + Fow_updatePlugin.version,"FoW Update",false);
					}
				}).start();
			}
			
			
		}

}
