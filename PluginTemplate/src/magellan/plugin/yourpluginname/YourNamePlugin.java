package magellan.plugin.yourpluginname;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import magellan.client.Client;
import magellan.client.desktop.DesktopEnvironment;
import magellan.client.desktop.ShortcutListener;
import magellan.client.extern.MagellanPlugIn;
import magellan.client.swing.map.MarkingsImageCellRenderer;
import magellan.client.swing.preferences.PreferencesFactory;
import magellan.library.Building;
import magellan.library.CoordinateID;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.Unit;
import magellan.library.Region.Visibility;
import magellan.library.event.GameDataEvent;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.rules.BuildingType;
import magellan.library.rules.RegionType;
import magellan.library.utils.Regions;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;


public class YourPlugin implements MagellanPlugIn {

	
	private Client client = null;
	
	private GameData gd = null;
	
	private static Logger log = null;
	

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
		
		JMenuItem showInfo = new JMenuItem(getString("plugin.yourpluginname.menu.showInfo"));
		showInfo.setActionCommand("showInfo");
		showInfo.addActionListener(this);
		menu.add(showInfo);
		
		
		items.add(menu);

		return items;
	}
	
	/* (non-Javadoc)
	 * @see magellan.client.extern.MagellanPlugIn#getName()
	 */
	public String getName() {
		return getString("plugin.yourpluginnamehere.name");
	}
	
	/* (non-Javadoc)
	 * @see magellan.client.extern.MagellanPlugIn#getPreferencesProvider()
	 */
	public PreferencesFactory getPreferencesProvider() {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see magellan.client.extern.MagellanPlugIn#init(magellan.client.Client, java.util.Properties)
	 */
	public void init(Client _client, Properties _properties) {
		client = _client;
		initShortcuts();
		Resources.getInstance().initialize(Client.getResourceDirectory(), "yourpluginname_");

		log = Logger.getInstance(this.getClassName());

		log.info(getName() + " initialized (Client)...");
	}
	
	
	
	
	/* (non-Javadoc)
	 * @see magellan.client.extern.MagellanPlugIn#init(magellan.library.GameData)
	 */
	public void init(GameData data) {
		// init the report
		gd = data;
		log.info(getName() + " initialized with new GameData...");
		processGameData();
		
	}
	
	/* (non-Javadoc)
	 * @see magellan.client.extern.MagellanPlugIn#quit(boolean)
	 */
	public void quit(boolean storeSettings) {
		// 
	}

	

	/**
	 * init the shortcuts
	 */
	private void initShortcuts(){
		shortcuts = new ArrayList<KeyStroke>();
		// 0: toggle Map Icons
	    shortcuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_MASK));
	    
	    DesktopEnvironment.registerShortcutListener(this);
	}
	
	
	/* (non-Javadoc)
	 * @see magellan.client.desktop.ShortcutListener#getListenerDescription()
	 */
	public String getListenerDescription() {
		return getString("plugin.yourpluginname.shortcuts.description");
	}


	/* (non-Javadoc)
	 * @see magellan.client.desktop.ShortcutListener#getShortcutDescription(javax.swing.KeyStroke)
	 */
	public String getShortcutDescription(KeyStroke stroke) {
		int index = shortcuts.indexOf(stroke);
	    return getString("plugin.yourpluginname.shortcuts.description." + String.valueOf(index));
	}


	/* (non-Javadoc)
	 * @see magellan.client.desktop.ShortcutListener#getShortCuts()
	 */
	public Iterator<KeyStroke> getShortCuts() {
		return shortcuts.iterator();
	}


	/**
	   * This method is called when a shortcut from getShortCuts() is recognized.
	   * 
	   * @param shortcut
	   *          DOCUMENT-ME
	   */
	public void shortCut(KeyStroke shortcut) {
		int index = shortcuts.indexOf(shortcut);

	    switch (index) {
	    case -1:
	      break; // unknown shortcut

	    case 0:
	      // Toggle MapIcons
	      
	      break;
	    }
	}
}
