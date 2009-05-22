package magellan.plugin.shiploader;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import magellan.client.Client;
import magellan.client.event.EventDispatcher;
import magellan.client.extern.MagellanPlugIn;
import magellan.client.swing.context.UnitContainerContextMenuProvider;
import magellan.client.swing.context.UnitContextMenuProvider;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.client.swing.preferences.PreferencesFactory;
import magellan.library.GameData;
import magellan.library.Ship;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;

/**
 * This plugin helps to load ships.
 * 
 * @author stm
 */
public class ShipLoaderPlugin implements MagellanPlugIn, UnitContainerContextMenuProvider,
		UnitContextMenuProvider, ActionListener {
	// public static final String NAMESPACE_PROPERTY = "plugins.shiploader.namespace";


	static Logger log = null;

	private Client client = null;

	private Properties properties = null;
	private GameData gd = null;

	private Loader loader;

	/**
	 * An enum for all action types in this plugin.
	 * 
	 * @author stm
	 */
	public enum PlugInAction {
		EXECUTE("mainmenu.execute"), DISTRIBUTESILVER("mainmenu.distribute"), SHOW("mainmenu.show"), CLEAR(
				"mainmenu.clear"), CLEARORDERS("mainmenu.clearorders"), UNKNOWN("");

		private String id;

		private PlugInAction(String id) {
			this.id = id;
		}

		public String getID() {
			return id;
		}

		public static PlugInAction getAction(ActionEvent e) {
			if (e == null)
				return UNKNOWN;
			for (PlugInAction action : values()) {
				if (action.id.equalsIgnoreCase(e.getActionCommand()))
					return action;
			}
			return UNKNOWN;
		}

	}

	/**
	 * @see magellan.client.extern.MagellanPlugIn#init(magellan.client.Client, java.util.Properties)
	 */
	public void init(Client _client, Properties _properties) {
		// init the plugin
		this.client = _client;
		this.properties = _properties;
		Resources.getInstance().initialize(Client.getSettingsDirectory(), "shiploaderplugin_");

		loader = new Loader(this, client);

		initProperties();

		log = Logger.getInstance(ShipLoaderPlugin.class);
		log.info(getName() + " initialized...");
	}

	private void initProperties() {
		// setNamespace(properties.getProperty(NAMESPACE_PROPERTY, getNamespace()));
	}

	/**
	 * @see magellan.client.extern.MagellanPlugIn#init(magellan.library.GameData)
	 */
	public void init(GameData data) {
		// init the report
		this.gd = data;
	}

	/**
	 * @see magellan.client.extern.MagellanPlugIn#getMenuItems()
	 */
	public List<JMenuItem> getMenuItems() {
		List<JMenuItem> items = new ArrayList<JMenuItem>();

		JMenu menu = new JMenu(getString("plugin.shiploader.mainmenu.title"));
		items.add(menu);

		JMenuItem executeMenu = new JMenuItem(getString("plugin.shiploader.mainmenu.execute.title"));
		executeMenu.setActionCommand(PlugInAction.EXECUTE.getID());
		executeMenu.addActionListener(this);
		menu.add(executeMenu);

		JMenuItem distributeMenu = new JMenuItem(getString("plugin.shiploader.mainmenu.distribute.title"));
		distributeMenu.setActionCommand(PlugInAction.DISTRIBUTESILVER.getID());
		distributeMenu.addActionListener(this);
		menu.add(distributeMenu);

		JMenuItem showMenu = new JMenuItem(getString("plugin.shiploader.mainmenu.show.title"));
		showMenu.setActionCommand(PlugInAction.SHOW.getID());
		showMenu.addActionListener(this);
		menu.add(showMenu);

		JMenuItem clearMenu = new JMenuItem(getString("plugin.shiploader.mainmenu.clear.title"));
		clearMenu.setActionCommand(PlugInAction.CLEAR.getID());
		clearMenu.addActionListener(this);
		menu.add(clearMenu);

		JMenuItem clearOrdersMenu = new JMenuItem(
				getString("plugin.shiploader.mainmenu.clearorders.title"));
		clearOrdersMenu.setActionCommand(PlugInAction.CLEARORDERS.getID());
		clearOrdersMenu.addActionListener(this);
		menu.add(clearOrdersMenu);

		return items;
	}

	/**
	 * @see magellan.client.extern.MagellanPlugIn#getName()
	 */
	public String getName() {
		return getString("plugin.shiploader.name");
	}

	/**
	 * @see magellan.client.swing.context.UnitContainerContextMenuProvider#createContextMenu(magellan.client.event.EventDispatcher,
	 *      magellan.library.GameData, magellan.library.UnitContainer, Collection)
	 */
	public JMenuItem createContextMenu(final EventDispatcher dispatcher, final GameData data,
			final UnitContainer container, final Collection selectedObjects) {
		JMenu menu = new JMenu(getString("plugin.shiploader.contextmenu.title"));

		if (selectedObjects.contains(container) && container instanceof Ship) {

			JMenuItem addMenu = new JMenuItem(getString("plugin.shiploader.contextmenu.addships.title"));
			addMenu.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					loader.add(selectedObjects);
				}
			});
			menu.add(addMenu);

			JMenuItem removeMenu = new JMenuItem(getString("plugin.shiploader.contextmenu.removeships.title"));
			removeMenu.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					loader.remove(selectedObjects);
				}
			});
			menu.add(removeMenu);
		} else if (selectedObjects.contains(container)){
			JMenuItem addMenu = new JMenuItem(getString("plugin.shiploader.contextmenu.addunits.title"));
			addMenu.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					loader.add(selectedObjects);
				}
			});
			menu.add(addMenu);

			JMenuItem removeMenu = new JMenuItem(getString("plugin.shiploader.contextmenu.removeunits.title"));
			removeMenu.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					loader.remove(selectedObjects);
				}
			});
			menu.add(removeMenu);
		}

		return menu;
	}

	/**
	 * 
	 * 
	 * @see magellan.client.swing.context.UnitContextMenuProvider#createContextMenu(magellan.client.event.EventDispatcher,
	 *      magellan.library.GameData, magellan.library.Unit, java.util.Collection)
	 */
	@SuppressWarnings("unchecked")
	public JMenuItem createContextMenu(EventDispatcher dispatcher, GameData data, final Unit unit,
			final Collection selectedObjects) {
		JMenu menu = new JMenu(getString("plugin.shiploader.contextmenu.title"));

		if (selectedObjects.contains(unit)) {
			JMenuItem addMenu = new JMenuItem(getString("plugin.shiploader.contextmenu.addunits.title"));
			addMenu.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					loader.add(selectedObjects);
				}
			});
			menu.add(addMenu);

			JMenuItem removeMenu = new JMenuItem(getString("plugin.shiploader.contextmenu.removeunits.title"));
			removeMenu.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					loader.remove(selectedObjects);
				}
			});
			menu.add(removeMenu);

			JMenuItem distributeMenu = new JMenuItem(
					getString("plugin.shiploader.contextmenu.distribute.title"));
			distributeMenu.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					loader.distribute(selectedObjects);
				}
			});
			menu.add(distributeMenu);
		}

		return menu;

	}

	public void actionPerformed(ActionEvent e) {
		log.info(e.getActionCommand());
		switch (PlugInAction.getAction(e)) {
		case EXECUTE: {
			execute();
			break;
		}
		case DISTRIBUTESILVER: {
			distribute();
			break;
		}
		case CLEAR: {
			clear();
			break;
		}
		case CLEARORDERS: {
			clearOrders();
			break;
		}
		case SHOW: {
			show();
			break;
		}
		}

	}

	private void distribute() {
		loader.distribute();
	}

	protected void show() {
		log.info("============ ships =============");
		for (Ship s : loader.ships) {
			log.info(s);
		}
		log.info("============ units =============");
		for (Unit u : loader.units) {
			log.info(u);
		}
	}

	protected void clear() {
		loader.clear();
	}

	protected void clearOrders() {
		loader.clearOrders();
	}

	protected void execute() {
		loader.execute();
	}

	/**
	 * @see magellan.client.extern.MagellanPlugIn#quit(boolean)
	 */
	public void quit(boolean storeSettings) {
		// do nothing
	}

	/**
	 * @see magellan.client.extern.MagellanPlugIn#getDocks()
	 */
	public Map<String, Component> getDocks() {
		return null;
	}

	/**
	 * 
	 */
	public PreferencesFactory getPreferencesProvider() {
		return new PreferencesFactory() {

			public PreferencesAdapter createPreferencesAdapter() {
				return new ShipLoaderPreferences();
			}

		};
	}

	class ShipLoaderPreferences implements PreferencesAdapter {

		private JPanel mainPanel;

		// private JTextArea txtNamespace;
		// private JCheckBox chkConfirmFullTeachers;

		public ShipLoaderPreferences() {
			initGUI();
		}

		protected void initGUI() {
			mainPanel = new JPanel();
		}

		public void applyPreferences() {
			// setNamespace(txtNamespace.getText());
		}

		public Component getComponent() {
			return mainPanel;
		}

		public String getTitle() {
			return getString("plugin.shiploader.preferences.title");
		}

		public void initPreferences() {
		}

	}

	protected static String getString(String key) {
		return Resources.get(key);
	}

	protected static String getString(String key, Object[] args) {
		String value = getString(key);
		if (value != null) {
			value = new MessageFormat(value).format(args);
		}
		return value;
	}

}
