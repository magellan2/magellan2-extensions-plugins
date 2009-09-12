package magellan.plugin.shiploader;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpringLayout;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import magellan.client.Client;
import magellan.client.event.EventDispatcher;
import magellan.client.extern.MagellanPlugIn;
import magellan.client.swing.basics.SpringUtilities;
import magellan.client.swing.context.UnitContainerContextFactory;
import magellan.client.swing.context.UnitContainerContextMenuProvider;
import magellan.client.swing.context.UnitContextFactory;
import magellan.client.swing.context.UnitContextMenuProvider;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.client.swing.preferences.PreferencesFactory;
import magellan.client.swing.tree.ContextManager;
import magellan.client.swing.tree.NodeWrapperFactory;
import magellan.client.swing.tree.RegionNodeWrapper;
import magellan.client.swing.tree.UnitContainerNodeWrapper;
import magellan.client.swing.tree.UnitNodeWrapper;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.Ship;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.event.GameDataEvent;
import magellan.library.event.GameDataListener;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;

/**
 * This plugin helps to load ships.
 * 
 * @author stm
 */
public class ShipLoaderPlugin implements MagellanPlugIn, UnitContainerContextMenuProvider,
		UnitContextMenuProvider, ActionListener, GameDataListener {

	public static final String SAFETY_PROPERTY = "plugins.shiploader.safety";
	public static final String SAFETYPERPERSON_PROPERTY = "plugins.shiploader.safetyperperson";
	public static final String CHANGESHIP_PROPERTY = "plugins.shiploader.changeship";

	public static final String KEEPSILVER_PROPERTY = "plugins.shiploader.keepsilver";

	public static final String KEEPSILVERINFACTION_PROPERTY = "plugins.shiploader.keepsilverinfaction";

	public static final String MARKER_PROPERTY = "plugins.shiploader.marker";

	static Logger log = null;

	private Client client = null;

	private Properties properties = null;
	private GameData gd = null;

	private ShipLoader loader;

	/**
	 * An enum for all action types in this plugin.
	 * 
	 * @author stm
	 */
	public enum PlugInAction {
		EXECUTE("mainmenu.execute"), DISTRIBUTESILVER("mainmenu.distribute"), SHOW("mainmenu.show"), CLEAR(
				"mainmenu.clear"), CLEARORDERS("mainmenu.clearorders"), CONFIRMORDERS("mainmenu.confirm"), UNCONFIRMORDERS(
				"mainmenu.unconfirm"), UNKNOWN("");

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
		this.client.getDispatcher().addGameDataListener(this);
		this.properties = _properties;
		Resources.getInstance().initialize(Client.getSettingsDirectory(), "shiploaderplugin_");

		loader = new ShipLoader(this, client);

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

		JMenuItem distributeMenu = new JMenuItem(
				getString("plugin.shiploader.mainmenu.distribute.title"));
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

		JMenuItem confirmOrdersMenu = new JMenuItem(
				getString("plugin.shiploader.mainmenu.confirmorders.title"));
		confirmOrdersMenu.setActionCommand(PlugInAction.CONFIRMORDERS.getID());
		confirmOrdersMenu.addActionListener(this);
		menu.add(confirmOrdersMenu);

		JMenuItem unconfirmOrdersMenu = new JMenuItem(
				getString("plugin.shiploader.mainmenu.unconfirmorders.title"));
		unconfirmOrdersMenu.setActionCommand(PlugInAction.UNCONFIRMORDERS.getID());
		unconfirmOrdersMenu.addActionListener(this);
		menu.add(unconfirmOrdersMenu);

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
			final UnitContainer container, final Collection<?> selectedObjects) {
		JMenu menu = new JMenu(getString("plugin.shiploader.contextmenu.title"));

		if (selectedObjects.contains(container) && container instanceof Ship) {

			JMenuItem addMenu = new JMenuItem(getString("plugin.shiploader.contextmenu.addships.title"));
			addMenu.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					loader.add(selectedObjects);
				}
			});
			menu.add(addMenu);

			JMenuItem removeMenu = new JMenuItem(
					getString("plugin.shiploader.contextmenu.removeships.title"));
			removeMenu.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					loader.remove(selectedObjects);
				}
			});
			menu.add(removeMenu);
		} else if (selectedObjects.contains(container)) {
			JMenuItem addMenu = new JMenuItem(getString("plugin.shiploader.contextmenu.addunits.title"));
			addMenu.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					loader.add(selectedObjects);
				}
			});
			menu.add(addMenu);

			JMenuItem removeMenu = new JMenuItem(
					getString("plugin.shiploader.contextmenu.removeunits.title"));
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

			JMenuItem removeMenu = new JMenuItem(
					getString("plugin.shiploader.contextmenu.removeunits.title"));
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
		case CONFIRMORDERS: {
			confirmOrders();
			break;
		}
		case UNCONFIRMORDERS: {
			unconfirmOrders();
			break;
		}
		case SHOW: {
			show();
			break;
		}
		}

	}

	private void unconfirmOrders() {
		for (Unit u : loader.units) {
			u.setOrdersConfirmed(false);
		}
	}

	private void confirmOrders() {
		for (Unit u : loader.units) {
			u.setOrdersConfirmed(true);
		}
	}

	private void distribute() {
		loader.distribute();
	}

	protected void show() {
		// log.info("============ ships =============");
		// for (Ship s : loader.ships) {
		// log.info(s);
		// }
		// log.info("============ units =============");
		// for (Unit u : loader.units) {
		// log.info(u);
		// }

		ShowDialog shower = new ShowDialog(client, loader);
		shower.setPreferredSize(new Dimension(600, 400));
		shower.pack();
		shower.setVisible(true);
	}

	@SuppressWarnings("serial")
	public class ShowDialog extends JDialog implements ShipLoader.InclusionListener, GameDataListener {

		private DefaultMutableTreeNode unitRoot;
		private DefaultTreeModel unitModel;
		private JTree unitTree;
		private DefaultMutableTreeNode shipRoot;
		private DefaultTreeModel shipModel;
		private JTree shipTree;
		private ShipLoader loader;
		private NodeWrapperFactory factory;
		private ContextManager unitContextManager;
		private ContextManager shipContextManager;
		private HashMap<Region, DefaultMutableTreeNode> shipRegionNodes;
		private HashMap<Region, DefaultMutableTreeNode> unitRegionNodes;

		public ShowDialog(JFrame frame, ShipLoader loader) {
			super(frame);
			this.loader = loader;
			loader.addListener(this);

			factory = new NodeWrapperFactory(properties, "ShipLoader", null);

			SpringLayout layout = new SpringLayout();
			JPanel mainPanel = new JPanel(layout);

			unitRoot = new DefaultMutableTreeNode(getString("plugin.shiploader.showdialog.units"));
			unitModel = new DefaultTreeModel(unitRoot);
			unitTree = new JTree(unitModel);

			unitContextManager = new ContextManager(unitTree, client.getDispatcher());
			unitContextManager.putSimpleObject(UnitNodeWrapper.class, new UnitContextFactory());
			unitTree.addTreeSelectionListener(new TreeSelectionListener() {

				public void valueChanged(TreeSelectionEvent e) {
					LinkedList<Unit> mySelectedUnits = new LinkedList<Unit>();
					if (unitTree.getSelectionPaths() != null) {
						for (TreePath path : unitTree.getSelectionPaths()) {
							DefaultMutableTreeNode actNode = (DefaultMutableTreeNode) path.getLastPathComponent();
							Object o = actNode.getUserObject();
							if (o instanceof UnitNodeWrapper) {
								UnitNodeWrapper nodeWrapper = (UnitNodeWrapper) o;
								Unit actUnit = nodeWrapper.getUnit();
								mySelectedUnits.add(actUnit);
							}
						}
					}
					if (mySelectedUnits.size() > 0) {
						unitContextManager.setSelection(mySelectedUnits);
					} else {
						unitContextManager.setSelection(null);
					}
				}
			});

			unitRegionNodes = new HashMap<Region, DefaultMutableTreeNode>();
			unitTree.setShowsRootHandles(true);
			unitTree.setRootVisible(true);

			shipRoot = new DefaultMutableTreeNode(getString("plugin.shiploader.showdialog.ships"));
			shipModel = new DefaultTreeModel(shipRoot);
			shipTree = new JTree(shipModel);

			shipContextManager = new ContextManager(shipTree, client.getDispatcher());
			shipContextManager.putSimpleObject(UnitContainerNodeWrapper.class,
					new UnitContainerContextFactory(properties));
			shipTree.addTreeSelectionListener(new TreeSelectionListener() {

				public void valueChanged(TreeSelectionEvent e) {
					LinkedList<UnitContainer> mySelection = new LinkedList<UnitContainer>();
					if (shipTree.getSelectionPaths() != null) {
						for (TreePath path : shipTree.getSelectionPaths()) {
							DefaultMutableTreeNode actNode = (DefaultMutableTreeNode) path.getLastPathComponent();
							Object o = actNode.getUserObject();
							if (o instanceof UnitContainerNodeWrapper) {
								UnitContainerNodeWrapper nodeWrapper = (UnitContainerNodeWrapper) o;
								UnitContainer container = nodeWrapper.getUnitContainer();
								mySelection.add(container);
							}
						}
					}
					if (mySelection.size() > 0) {
						shipContextManager.setSelection(mySelection);
					} else {
						shipContextManager.setSelection(null);
					}
				}
			});

			shipRegionNodes = new HashMap<Region, DefaultMutableTreeNode>();
			shipTree.setShowsRootHandles(true);
			shipTree.setRootVisible(true);

			addUnits(loader.units);

			addShips(loader.ships);

			JScrollPane unitScroll = new JScrollPane(unitTree);
			unitScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
			unitScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			mainPanel.add(unitScroll);

			JScrollPane shipScroll = new JScrollPane(shipTree);
			shipScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
			shipScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			mainPanel.add(shipScroll);

			SpringUtilities.makeGrid(mainPanel, 1, 2, 5, 5, 5, 5);

			add(mainPanel);
		}

		/**
		 * 
		 * 
		 * @see java.lang.Object#finalize()
		 */
		@Override
		protected void finalize() throws Throwable {
			loader.removeListener(this);
			super.finalize();
		}

		private void addUnits(Collection<?> selectedObjects) {
			unitRoot.removeAllChildren();

			ArrayList<Unit> units = new ArrayList<Unit>();

			for (Object o : selectedObjects) {
				if (!(o instanceof Unit))
					continue;
				Unit newUnit = (Unit) o;
				int newPos = 0;
				for (; newPos < units.size(); ++newPos) {
					if (units.get(newPos).getModifiedWeight() > newUnit.getModifiedWeight()) {
						break;
					}
				}
				units.add(newPos, newUnit);
			}
			for (Unit newUnit : units)
				addUnit(newUnit);

			if (loader.units.size() > 0)
				unitTree.expandPath(new TreePath(unitRoot));
			unitModel.nodeStructureChanged(unitRoot);
		}

		private void addShips(Collection<?> selectedObjects) {
			shipRoot.removeAllChildren();
			shipRegionNodes.clear();
			for (Object o : selectedObjects) {
				if (!(o instanceof Ship))
					continue;
				Ship s = (Ship) o;
				addShip(s);
			}

			if (loader.ships.size() > 0)
				shipTree.expandPath(new TreePath(new Object[] { shipRoot, shipRoot.getFirstChild() }));
			shipModel.nodeStructureChanged(shipRoot);
		}

		public void gameDataChanged(GameDataEvent e) {
			unitContextManager.setGameData(e.getGameData());
			shipContextManager.setGameData(e.getGameData());
		}

		public void selectionChanged(magellan.plugin.shiploader.ShipLoader.InclusionEvent e) {
			synchronized (this) {
				if (e.getShip() != null) {
					if (e.isAdded())
						addShip(e.getShip());
					else
						removeShip(e.getShip());
				} else if (e.getUnit() != null) {
					if (e.isAdded())
						addUnit(e.getUnit());
					else
						removeUnit(e.getUnit());
				}
			}
		}

		private void removeUnit(Unit unit) {
			DefaultMutableTreeNode regionNode = unitRegionNodes.get(unit.getRegion());
			if (regionNode != null && regionNode.getChildCount() > 0) {
				for (Enumeration<?> enumeration = regionNode.children(); enumeration.hasMoreElements();) {
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) enumeration.nextElement();
					if (((UnitNodeWrapper) node.getUserObject()).getUnit().equals(unit)) {
						regionNode.remove(node);
						RegionNodeWrapper rwrapper = (RegionNodeWrapper) regionNode.getUserObject();
						rwrapper.setAmount(rwrapper.getAmount() - unit.getModifiedWeight());
						unitModel.nodeStructureChanged(regionNode);
						break;
					}
				}
			}
		}

		private void removeShip(Ship ship) {
			DefaultMutableTreeNode regionNode = shipRegionNodes.get(ship.getRegion());
			if (regionNode != null && regionNode.getChildCount() > 0) {
				for (Enumeration<?> enumeration = regionNode.children(); enumeration.hasMoreElements();) {
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) enumeration.nextElement();
					if (((UnitContainerNodeWrapper) node.getUserObject()).getUnitContainer().equals(ship)) {
						regionNode.remove(node);
						RegionNodeWrapper rwrapper = (RegionNodeWrapper) regionNode.getUserObject();
						rwrapper.setAmount(rwrapper.getAmount() - loader.getSpace(ship, null));
						shipModel.nodeStructureChanged(regionNode);
						break;
					}
				}
			}
		}

		private void addUnit(Unit unit) {
			DefaultMutableTreeNode regionNode = unitRegionNodes.get(unit.getRegion());
			if (regionNode == null) {
				regionNode = new DefaultMutableTreeNode(factory
						.createRegionNodeWrapper(unit.getRegion(), 0));
				unitRegionNodes.put(unit.getRegion(), regionNode);
				unitRoot.add(regionNode);
				unitModel.nodeStructureChanged(unitRoot);
			}
			RegionNodeWrapper rwrapper = (RegionNodeWrapper) regionNode.getUserObject();
			rwrapper.setAmount(rwrapper.getAmount() + unit.getModifiedWeight());

			regionNode.add(new DefaultMutableTreeNode(factory.createUnitNodeWrapper(unit, unit.toString()
					+ ": " + unit.getWeight() / 100.0 + " (" + unit.getModifiedWeight() / 100.0 + ") "
					+ (unit.isWeightWellKnown() ? "" : "???"))));
			unitModel.nodeStructureChanged(regionNode);
		}

		private void addShip(Ship ship) {
			DefaultMutableTreeNode regionNode = shipRegionNodes.get(ship.getRegion());
			if (regionNode == null) {
				regionNode = new DefaultMutableTreeNode(factory
						.createRegionNodeWrapper(ship.getRegion(), 0));
				shipRegionNodes.put(ship.getRegion(), regionNode);
				shipRoot.add(regionNode);
				shipModel.nodeStructureChanged(shipRoot);
			}
			RegionNodeWrapper rwrapper = (RegionNodeWrapper) regionNode.getUserObject();
			rwrapper.setAmount(rwrapper.getAmount() + loader.getSpace(ship, null));

			regionNode.add(new DefaultMutableTreeNode(factory.createUnitContainerNodeWrapper(ship)));
			shipModel.nodeStructureChanged(regionNode);// nodesWereInserted(regionNode, new int[]
			// {regionNode.getChildCount()-1});
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
		if (loader.getErrors() > 0) {
			JOptionPane.showMessageDialog(client, getString("plugin.shiploader.message.loaderrors",
					new Integer[] { loader.getErrors() }));
		}
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
		private JTextArea txtSafety;
		private JTextArea txtSafetyPerPerson;
		private JCheckBox chkChangeShip;
		private JCheckBox chkKeepSilver;
		private JCheckBox chkKeepSilverInFaction;
		private JTextArea txtMarker;

		// private JTextArea txtNamespace;
		// private JCheckBox chkConfirmFullTeachers;

		public ShipLoaderPreferences() {
			initGUI();
		}

		protected void initGUI() {
			mainPanel = new JPanel();
			JPanel panel = new JPanel(new GridBagLayout());

			txtSafety = new JTextArea("" + loader.getSafety());
			txtSafety.setMinimumSize(new Dimension(50, 20));
			txtSafety.setPreferredSize(new java.awt.Dimension(50, 20));
			txtSafety
					.setToolTipText(getString("plugin.shiploader.preferences.label.safetymargin.tooltip"));
			JLabel lblSafety = new JLabel(getString("plugin.shiploader.preferences.label.safetymargin"));
			lblSafety
					.setToolTipText(getString("plugin.shiploader.preferences.label.safetymargin.tooltip"));
			lblSafety.setLabelFor(txtSafety);

			txtSafetyPerPerson = new JTextArea("" + loader.getSafetyPerPerson());
			txtSafetyPerPerson.setMinimumSize(new Dimension(50, 20));
			txtSafetyPerPerson.setPreferredSize(new java.awt.Dimension(50, 20));
			txtSafetyPerPerson
					.setToolTipText(getString("plugin.shiploader.preferences.label.unitsilver.tooltip"));
			JLabel lblSafetyPerPerson = new JLabel(
					getString("plugin.shiploader.preferences.label.unitsilver"));
			lblSafetyPerPerson
					.setToolTipText(getString("plugin.shiploader.preferences.label.unitsilver.tooltip"));
			lblSafetyPerPerson.setLabelFor(txtSafetyPerPerson);

			chkChangeShip = new JCheckBox(getString("plugin.shiploader.preferences.label.changeship"));
			chkKeepSilver = new JCheckBox(getString("plugin.shiploader.preferences.label.keepsilver"));
			chkKeepSilverInFaction = new JCheckBox(
					getString("plugin.shiploader.preferences.label.keepsilverinfaction"));

			txtMarker = new JTextArea();
			txtMarker.setMinimumSize(new Dimension(100, 20));
			txtMarker.setPreferredSize(new java.awt.Dimension(100, 20));
			// txtMarker.setToolTipText(getString("plugin.shiploader.preferences.label.marker.tooltip"));
			JLabel lblMarker = new JLabel(getString("plugin.shiploader.preferences.label.marker"));
			// lblMarker.setToolTipText(getString("plugin.shiploader.preferences.label.Markermargin.tooltip"));
			lblMarker.setLabelFor(txtMarker);

			// GridBagConstraints con = new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST,
			// GridBagConstraints.HORIZONTAL, new Insets(3, 3, 3, 3), 0, 0);
			GridBagConstraints con = new GridBagConstraints();
			con.insets = new Insets(2, 2, 2, 2);
			con.fill = GridBagConstraints.HORIZONTAL;
			con.anchor = GridBagConstraints.WEST;

			con.insets.left = 0;
			con.gridx = 0;
			con.gridy = 0;
			panel.add(lblSafety, con);

			con.insets.left = 0;
			con.gridx++;
			panel.add(txtSafety, con);

			con.gridx = 0;
			con.gridy++;
			panel.add(lblSafetyPerPerson, con);

			con.insets.left = 0;
			con.gridx++;
			panel.add(txtSafetyPerPerson, con);

			con.gridy++;
			con.gridx = 0;
			panel.add(chkChangeShip, con);

			con.gridy++;
			panel.add(chkKeepSilver, con);
			con.gridy++;
			panel.add(chkKeepSilverInFaction, con);

			con.gridy++;
			panel.add(lblMarker, con);

			con.gridx++;
			panel.add(txtMarker, con);

			con.gridx = 1;
			con.gridy = 0;
			con.fill = GridBagConstraints.NONE;
			con.anchor = GridBagConstraints.EAST;
			con.weightx = 0.1;
			panel.add(new JLabel(""), con);

			JPanel restrictPanel = new JPanel(new BorderLayout());
			mainPanel = new JPanel(new BorderLayout());
			mainPanel.add(restrictPanel, BorderLayout.CENTER);
			restrictPanel.add(panel, BorderLayout.NORTH);
			restrictPanel.setBorder(new javax.swing.border.TitledBorder(BorderFactory
					.createEtchedBorder(), getString("plugin.teacher.preferences.label.namespace")));

		}

		public void applyPreferences() {
			// plugin.shiploader.preferences.title = ShipLoader
			// plugin.shiploader.preferences.title.options = Optionen
			// plugin.shiploader.preferences.label.safetymargin = Sicherheitsmarge pro Schiff
			// plugin.shiploader.preferences.label.safetymargin.tooltip = So viel Platz (in Silber) wird
			// auf jedem Schiff freigehalten
			// plugin.shiploader.preferences.label.unitsilver = Silber pro Person
			// plugin.shiploader.preferences.label.unitsilver.tooltip = So viel Silber wird an Einheiten
			// verteilt
			// plugin.shiploader.preferences.label.changeship = Einheiten dürfen Schiffe wechseln
			// plugin.shiploader.preferences.label.keepsilver = Einheiten müssen ihr Silber behalten
			// plugin.shiploader.preferences.label.keepsilverinfaction = Einheiten dürfen Silber nur an
			// Einheiten der eigenen Partei weitergeben
			// plugin.shiploader.preferences.label.marker = Befehle werden mit diesem Text markiert
			try {
				loader.setSafety(Integer.parseInt(txtSafety.getText()));
				properties.setProperty(SAFETY_PROPERTY, String.valueOf(loader.getSafety()));
			} catch (NumberFormatException e) {
			}
			try {
				loader.setSafetyPerPerson(Integer.parseInt(txtSafetyPerPerson.getText()));
				properties.setProperty(SAFETYPERPERSON_PROPERTY, String
						.valueOf(loader.getSafetyPerPerson()));
			} catch (NumberFormatException e) {
			}
			loader.setChangeShip(chkChangeShip.isSelected());
			properties.setProperty(CHANGESHIP_PROPERTY, chkChangeShip.isSelected() ? "true" : "false");

			loader.setKeepSilver(chkKeepSilver.isSelected());
			properties.setProperty(KEEPSILVER_PROPERTY, chkKeepSilver.isSelected() ? "true" : "false");

			loader.setKeepSilverInFaction(chkKeepSilverInFaction.isSelected());
			properties.setProperty(KEEPSILVERINFACTION_PROPERTY,
					chkKeepSilverInFaction.isSelected() ? "true" : "false");

			loader.setMarkerName(txtMarker.getText());
			properties.setProperty(MARKER_PROPERTY, txtMarker.getText());

		}

		public Component getComponent() {
			return mainPanel;
		}

		public String getTitle() {
			return getString("plugin.shiploader.preferences.title");
		}

		public void initPreferences() {
			txtSafety.setText(String.valueOf(loader.getSafety()));
			txtSafetyPerPerson.setText(String.valueOf(loader.getSafetyPerPerson()));
			chkChangeShip.setSelected(loader.isChangeShip());
			chkKeepSilver.setSelected(loader.isKeepSilver());
			chkKeepSilverInFaction.setSelected(loader.isKeepSilverInFaction());
			txtMarker.setText(String.valueOf(loader.getMarker()));
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

	public void gameDataChanged(GameDataEvent e) {
		this.gd = e.getGameData();
		loader.init(this.gd);
	}

}
