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
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
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
import magellan.client.event.SelectionEvent;
import magellan.client.extern.MagellanPlugIn;
import magellan.client.swing.InternationalizedDialog;
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
import magellan.client.utils.SwingUtils;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.Ship;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.event.GameDataEvent;
import magellan.library.event.GameDataListener;
import magellan.library.event.UnitChangeEvent;
import magellan.library.event.UnitChangeListener;
import magellan.library.gamebinding.MovementEvaluator;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;

/**
 * This plugin helps to load ships.
 * 
 * @author stm
 * @version 0.1.3
 */
public class ShipLoaderPlugin implements MagellanPlugIn, UnitContainerContextMenuProvider,
    UnitContextMenuProvider, ActionListener, GameDataListener {

  public static final String SAFETY_PROPERTY = "plugins.shiploader.safety";
  public static final String SAFETYPERPERSON_PROPERTY = "plugins.shiploader.safetyperperson";
  public static final String CHANGESHIP_PROPERTY = "plugins.shiploader.changeship";

  public static final String KEEPSILVER_PROPERTY = "plugins.shiploader.keepsilver";

  public static final String KEEPSILVERINFACTION_PROPERTY =
      "plugins.shiploader.keepsilverinfaction";

  public static final String MARKER_PROPERTY = "plugins.shiploader.marker";

  static Logger log = null;

  private Client client = null;

  private Properties properties = null;
  private GameData gd = null;

  private ShipLoader loader;
  private MovementEvaluator evaluator;
  private ShowDialog shower;
  private static final String version = "0.1.3";

  /**
   * An enum for all action types in this plugin.
   * 
   * @author stm
   */
  public enum PlugInAction {
    EXECUTE("mainmenu.execute"), DISTRIBUTESILVER("mainmenu.distribute"), SHOW("mainmenu.show"),
    CLEAR("mainmenu.clear"), CLEARORDERS("mainmenu.clearorders"),
    CONFIRMORDERS("mainmenu.confirm"), UNCONFIRMORDERS("mainmenu.unconfirm"),
    HELP("mainmenu.help"), UNKNOWN("");

    private final String id;

    private PlugInAction(String id) {
      this.id = id;
    }

    public String getID() {
      return id;
    }

    public static PlugInAction getAction(ActionEvent e) {
      if (e == null) {
        return UNKNOWN;
      }
      for (final PlugInAction action : values()) {
        if (action.id.equalsIgnoreCase(e.getActionCommand())) {
          return action;
        }
      }
      return UNKNOWN;
    }

  }

  /**
   * @see magellan.client.extern.MagellanPlugIn#init(magellan.client.Client, java.util.Properties)
   */
  public void init(Client _client, Properties _properties) {
    // init the plugin
    client = _client;
    client.getDispatcher().addGameDataListener(this);

    properties = _properties;
    Resources.getInstance().initialize(Client.getResourceDirectory(), "shiploaderplugin_");

    evaluator = client.getData().getGameSpecificStuff().getMovementEvaluator();

    loader = new ShipLoader(this, client);

    initProperties();

    shower = new ShowDialog(client, loader);
    client.getDispatcher().addGameDataListener(shower);
    log = Logger.getInstance(ShipLoaderPlugin.class);
    log.info(getName() + " (" + getVersion() + ")" + " initialized...");
  }

  public String getVersion() {
    return version;
  }

  private void initProperties() {
    // setNamespace(properties.getProperty(NAMESPACE_PROPERTY, getNamespace()));
  }

  /**
   * @see magellan.client.extern.MagellanPlugIn#init(magellan.library.GameData)
   */
  public void init(GameData data) {
    // init the report
    gd = data;
    gd.addUnitChangeListener(shower);
    loader.init(gd);
    evaluator = data.getGameSpecificStuff().getMovementEvaluator();
  }

  /**
   * @see magellan.client.extern.MagellanPlugIn#getMenuItems()
   */
  public List<JMenuItem> getMenuItems() {
    final List<JMenuItem> items = new ArrayList<JMenuItem>();

    final JMenu menu = new JMenu(getString("plugin.shiploader.mainmenu.title"));
    items.add(menu);

    final JMenuItem executeMenu =
        new JMenuItem(getString("plugin.shiploader.mainmenu.execute.title"));
    executeMenu.setActionCommand(PlugInAction.EXECUTE.getID());
    executeMenu.addActionListener(this);
    menu.add(executeMenu);

    final JMenuItem distributeMenu =
        new JMenuItem(getString("plugin.shiploader.mainmenu.distribute.title"));
    distributeMenu.setActionCommand(PlugInAction.DISTRIBUTESILVER.getID());
    distributeMenu.addActionListener(this);
    menu.add(distributeMenu);

    final JMenuItem showMenu = new JMenuItem(getString("plugin.shiploader.mainmenu.show.title"));
    showMenu.setActionCommand(PlugInAction.SHOW.getID());
    showMenu.addActionListener(this);
    menu.add(showMenu);

    final JMenuItem clearMenu = new JMenuItem(getString("plugin.shiploader.mainmenu.clear.title"));
    clearMenu.setActionCommand(PlugInAction.CLEAR.getID());
    clearMenu.addActionListener(this);
    menu.add(clearMenu);

    final JMenuItem clearOrdersMenu =
        new JMenuItem(getString("plugin.shiploader.mainmenu.clearorders.title"));
    clearOrdersMenu.setActionCommand(PlugInAction.CLEARORDERS.getID());
    clearOrdersMenu.addActionListener(this);
    menu.add(clearOrdersMenu);

    final JMenuItem confirmOrdersMenu =
        new JMenuItem(getString("plugin.shiploader.mainmenu.confirmorders.title"));
    confirmOrdersMenu.setActionCommand(PlugInAction.CONFIRMORDERS.getID());
    confirmOrdersMenu.addActionListener(this);
    menu.add(confirmOrdersMenu);

    final JMenuItem unconfirmOrdersMenu =
        new JMenuItem(getString("plugin.shiploader.mainmenu.unconfirmorders.title"));
    unconfirmOrdersMenu.setActionCommand(PlugInAction.UNCONFIRMORDERS.getID());
    unconfirmOrdersMenu.addActionListener(this);
    menu.add(unconfirmOrdersMenu);

    final JMenuItem helpMenu = new JMenuItem(getString("plugin.shiploader.mainmenu.help.title"));
    helpMenu.setActionCommand(PlugInAction.HELP.getID());
    helpMenu.addActionListener(this);
    menu.add(helpMenu);

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
    final JMenu menu = new JMenu(getString("plugin.shiploader.contextmenu.title"));

    if (selectedObjects.contains(container) && container instanceof Ship) {

      final JMenuItem addMenu =
          new JMenuItem(getString("plugin.shiploader.contextmenu.addships.title"));
      addMenu.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          loader.add(selectedObjects);
        }
      });
      menu.add(addMenu);

      final JMenuItem removeMenu =
          new JMenuItem(getString("plugin.shiploader.contextmenu.removeships.title"));
      removeMenu.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          loader.remove(selectedObjects);
        }
      });
      menu.add(removeMenu);
    } else if (selectedObjects.contains(container)) {
      final JMenuItem addMenu =
          new JMenuItem(getString("plugin.shiploader.contextmenu.addunits.title"));
      addMenu.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          loader.add(selectedObjects);
        }
      });
      menu.add(addMenu);

      final JMenuItem removeMenu =
          new JMenuItem(getString("plugin.shiploader.contextmenu.removeunits.title"));
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
   * @see magellan.client.swing.context.UnitContextMenuProvider#createContextMenu(magellan.client.event.EventDispatcher,
   *      magellan.library.GameData, magellan.library.Unit, java.util.Collection)
   */
  @SuppressWarnings("rawtypes")
  public JMenuItem createContextMenu(EventDispatcher dispatcher, GameData data, final Unit unit,
      final Collection selectedObjects) {
    final JMenu menu = new JMenu(getString("plugin.shiploader.contextmenu.title"));

    if (selectedObjects.contains(unit)) {
      final JMenuItem addMenu =
          new JMenuItem(getString("plugin.shiploader.contextmenu.addunits.title"));
      addMenu.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          loader.add(selectedObjects);
        }
      });
      menu.add(addMenu);

      final JMenuItem removeMenu =
          new JMenuItem(getString("plugin.shiploader.contextmenu.removeunits.title"));
      removeMenu.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          loader.remove(selectedObjects);
        }
      });
      menu.add(removeMenu);

      final JMenuItem distributeMenu =
          new JMenuItem(getString("plugin.shiploader.contextmenu.distribute.title"));
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
    case HELP: {
      showHelp();
      break;
    }
    case SHOW: {
      show();
      break;
    }
    }

  }

  private void unconfirmOrders() {
    for (final Unit u : loader.units) {
      u.setOrdersConfirmed(false);
    }
  }

  private void confirmOrders() {
    for (final Unit u : loader.units) {
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

    shower.setPreferredSize(new Dimension(600, 400));
    shower.pack();
    shower.setVisible(true);
  }

  @SuppressWarnings("serial")
  public class ShowDialog extends JDialog implements ShipLoader.InclusionListener,
      GameDataListener, UnitChangeListener {

    private final DefaultMutableTreeNode unitRoot;
    private final DefaultTreeModel unitModel;
    private final JTree unitTree;
    private final DefaultMutableTreeNode shipRoot;
    private final DefaultTreeModel shipModel;
    private final JTree shipTree;
    private final ShipLoader loader;
    private final NodeWrapperFactory factory;
    private final ContextManager unitContextManager;
    private final ContextManager shipContextManager;
    private final HashMap<Region, DefaultMutableTreeNode> shipRegionNodes;
    private final HashMap<Region, DefaultMutableTreeNode> unitRegionNodes;

    // private Map<UnitID, DefaultMutableTreeNode> unitNodes =
    // new HashMap<UnitID, DefaultMutableTreeNode>();
    // private Map<EntityID, DefaultMutableTreeNode> shipNodes =
    // new HashMap<EntityID, DefaultMutableTreeNode>();

    public ShowDialog(JFrame frame, ShipLoader loader) {
      super(frame);
      this.loader = loader;
      loader.addListener(this);

      factory = new NodeWrapperFactory(properties, "ShipLoader", null);

      final SpringLayout layout = new SpringLayout();
      final JPanel mainPanel = new JPanel(layout);

      unitRoot = new DefaultMutableTreeNode(getString("plugin.shiploader.showdialog.units"));
      unitModel = new DefaultTreeModel(unitRoot);
      unitTree = new JTree(unitModel);

      unitContextManager = new ContextManager(unitTree, client.getDispatcher());
      unitContextManager.putSimpleObject(UnitNodeWrapper.class, new UnitContextFactory());
      unitContextManager.putSimpleObject(RegionNodeWrapper.class, new UnitContainerContextFactory(
          properties));
      unitTree.addTreeSelectionListener(new TreeSelectionListener() {

        public void valueChanged(TreeSelectionEvent e) {
          final LinkedList<Unit> mySelectedUnits = new LinkedList<Unit>();
          if (unitTree.getSelectionPaths() != null) {
            for (final TreePath path : unitTree.getSelectionPaths()) {
              final DefaultMutableTreeNode actNode =
                  (DefaultMutableTreeNode) path.getLastPathComponent();
              final Object o = actNode.getUserObject();
              if (o instanceof UnitNodeWrapper) {
                final UnitNodeWrapper nodeWrapper = (UnitNodeWrapper) o;
                final Unit actUnit = nodeWrapper.getUnit();
                mySelectedUnits.add(actUnit);
              }
            }
          }
          if (mySelectedUnits.size() > 0) {
            // newer version:
            unitContextManager.setSelection(SelectionEvent.create(this, null, mySelectedUnits));
            // 2.0.5 version:
            // unitContextManager.setSelection(mySelectedUnits);
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
      shipContextManager.putSimpleObject(RegionNodeWrapper.class, new UnitContainerContextFactory(
          properties));
      shipTree.addTreeSelectionListener(new TreeSelectionListener() {

        public void valueChanged(TreeSelectionEvent e) {
          final LinkedList<Ship> mySelection = new LinkedList<Ship>();
          if (shipTree.getSelectionPaths() != null) {
            for (final TreePath path : shipTree.getSelectionPaths()) {
              final DefaultMutableTreeNode actNode =
                  (DefaultMutableTreeNode) path.getLastPathComponent();
              final Object o = actNode.getUserObject();
              if (o instanceof UnitContainerNodeWrapper) {
                final UnitContainerNodeWrapper nodeWrapper = (UnitContainerNodeWrapper) o;
                final UnitContainer container = nodeWrapper.getUnitContainer();
                if (container instanceof Ship) {
                  mySelection.add((Ship) container);
                }
              }
            }
          }
          if (mySelection.size() > 0) {
            // newer version:
            unitContextManager.setSelection(SelectionEvent.create(this, null, mySelection));
            // 2.0.5 version:
            // shipContextManager.setSelection(mySelection);
          } else {
            shipContextManager.setSelection(null);
          }
        }
      });

      shipRegionNodes = new HashMap<Region, DefaultMutableTreeNode>();
      shipTree.setShowsRootHandles(true);
      shipTree.setRootVisible(true);

      addUnits(loader.getUnits());

      addShips(loader.getShips());

      final JScrollPane unitScroll = new JScrollPane(unitTree);
      unitScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
      unitScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      mainPanel.add(unitScroll);

      final JScrollPane shipScroll = new JScrollPane(shipTree);
      shipScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
      shipScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      mainPanel.add(shipScroll);

      SpringUtilities.makeGrid(mainPanel, 1, 2, 5, 5, 5, 5);

      add(mainPanel);
    }

    /**
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize() throws Throwable {
      loader.removeListener(this);
      super.finalize();
    }

    private void addUnits(Collection<?> selectedObjects) {
      unitRoot.removeAllChildren();
      unitRegionNodes.clear();

      final ArrayList<Unit> units = new ArrayList<Unit>();

      for (final Object o : selectedObjects) {
        if (!(o instanceof Unit)) {
          continue;
        }
        final Unit newUnit = (Unit) o;
        int newPos = 0;
        for (; newPos < units.size(); ++newPos) {
          if (evaluator.getModifiedWeight(units.get(newPos)) > evaluator.getModifiedWeight(newUnit)) {
            break;
          }
        }
        units.add(newPos, newUnit);
      }
      for (final Unit newUnit : units) {
        addUnit(newUnit);
      }

      if (loader.units.size() > 0) {
        unitTree.expandPath(new TreePath(unitRoot));
      }
      unitModel.nodeStructureChanged(unitRoot);
    }

    private void addShips(Collection<?> selectedObjects) {
      shipRoot.removeAllChildren();
      shipRegionNodes.clear();

      for (final Object o : selectedObjects) {
        if (!(o instanceof Ship)) {
          continue;
        }
        final Ship s = (Ship) o;
        addShip(s);
      }

      if (loader.getShips().size() > 0) {
        shipTree.expandPath(new TreePath(new Object[] { shipRoot, shipRoot.getFirstChild() }));
      }
      shipModel.nodeStructureChanged(shipRoot);
    }

    public void gameDataChanged(GameDataEvent e) {
      // newer version:
      // unitContextManager.setGameData(e.getGameData());
      // shipContextManager.setGameData(e.getGameData());
      // 2.0.5 version:
      unitContextManager.gameDataChanged(e);
      shipContextManager.gameDataChanged(e);

      addUnits(loader.getUnits());
      addShips(loader.getShips());
    }

    public void selectionChanged(magellan.plugin.shiploader.ShipLoader.InclusionEvent e) {
      synchronized (this) {
        if (e.getShip() != null) {
          if (e.isAdded()) {
            addShip(e.getShip());
          } else {
            removeShip(e.getShip());
          }
        } else if (e.getUnit() != null) {
          if (e.isAdded()) {
            addUnit(e.getUnit());
          } else {
            removeUnit(e.getUnit());
          }
        }
      }
    }

    public void unitChanged(UnitChangeEvent event) {
      if (loader.getUnits().contains(event.getUnit())) {
        removeUnit(event.getUnit());
        addUnit(event.getUnit());
        if (event.getUnit().getShip() != null) {
          removeShip(event.getUnit().getShip());
          addShip(event.getUnit().getShip());
        }
        if (event.getUnit().getModifiedShip() != null) {
          removeShip(event.getUnit().getModifiedShip());
          addShip(event.getUnit().getModifiedShip());
        }
      }
    }

    protected void removeUnit(Unit unit) {
      final DefaultMutableTreeNode regionNode = unitRegionNodes.get(unit.getRegion());
      if (regionNode != null && regionNode.getChildCount() > 0) {
        int amount = 0;
        for (final Enumeration<?> enumeration = regionNode.children(); enumeration
            .hasMoreElements();) {
          final DefaultMutableTreeNode node = (DefaultMutableTreeNode) enumeration.nextElement();
          Unit unit2 = ((UnitNodeWrapper) node.getUserObject()).getUnit();
          if (unit2.equals(unit)) {
            regionNode.remove(node);
          } else {
            amount += evaluator.getModifiedWeight(unit2);
          }
        }
        unitModel.nodeStructureChanged(regionNode);
        final RegionNodeWrapper rwrapper = (RegionNodeWrapper) regionNode.getUserObject();
        rwrapper.setAmount(amount);
        unitModel.nodeChanged(regionNode);
      }
    }

    protected void removeShip(Ship ship) {
      final DefaultMutableTreeNode regionNode = shipRegionNodes.get(ship.getRegion());
      if (regionNode != null && regionNode.getChildCount() > 0) {
        int amount = 0;
        for (final Enumeration<?> enumeration = regionNode.children(); enumeration
            .hasMoreElements();) {
          final DefaultMutableTreeNode node = (DefaultMutableTreeNode) enumeration.nextElement();
          Ship ship2 = (Ship) ((UnitContainerNodeWrapper) node.getUserObject()).getUnitContainer();
          if (ship2.equals(ship)) {
            regionNode.remove(node);
          } else {
            amount += loader.getSpace(ship2);
          }
        }
        shipModel.nodeStructureChanged(regionNode);
        final RegionNodeWrapper rwrapper = (RegionNodeWrapper) regionNode.getUserObject();
        rwrapper.setAmount(amount);
        unitModel.nodeChanged(regionNode);
      }
    }

    protected void addUnit(Unit unit) {
      DefaultMutableTreeNode regionNode = unitRegionNodes.get(unit.getRegion());
      if (regionNode == null) {
        regionNode =
            new DefaultMutableTreeNode(factory.createRegionNodeWrapper(unit.getRegion(), 0));
        unitRegionNodes.put(unit.getRegion(), regionNode);
        unitRoot.add(regionNode);
        unitModel.nodeStructureChanged(unitRoot);
      }
      final RegionNodeWrapper rwrapper = (RegionNodeWrapper) regionNode.getUserObject();
      rwrapper.setAmount(rwrapper.getAmount() + evaluator.getModifiedWeight(unit));

      regionNode
          .add(new DefaultMutableTreeNode(factory.createUnitNodeWrapper(unit, getText(unit))));
      unitModel.nodeStructureChanged(regionNode);
    }

    protected void addShip(Ship ship) {
      DefaultMutableTreeNode regionNode = shipRegionNodes.get(ship.getRegion());
      if (regionNode == null) {
        regionNode =
            new DefaultMutableTreeNode(factory.createRegionNodeWrapper(ship.getRegion(), 0));
        shipRegionNodes.put(ship.getRegion(), regionNode);
        shipRoot.add(regionNode);
        shipModel.nodeStructureChanged(shipRoot);
      }
      final RegionNodeWrapper rwrapper = (RegionNodeWrapper) regionNode.getUserObject();
      rwrapper.setAmount(rwrapper.getAmount() + loader.getSpace(ship));

      regionNode.add(new DefaultMutableTreeNode(factory.createUnitContainerNodeWrapper(ship)));
      shipModel.nodeStructureChanged(regionNode);
      // nodesWereInserted(regionNode, new int[] {regionNode.getChildCount()-1});
    }

  }

  protected void clear() {
    loader.clear();
  }

  public String getText(Unit unit) {
    return unit.toString() + ": " + evaluator.getWeight(unit) / 100.0 + " ("
        + evaluator.getModifiedWeight(unit) / 100.0 + ") "
        + (unit.isWeightWellKnown() ? "" : "???");
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
      final JPanel panel = new JPanel(new GridBagLayout());

      txtSafety = new JTextArea("" + loader.getSafety());
      txtSafety.setMinimumSize(new Dimension(50, 20));
      txtSafety.setPreferredSize(new java.awt.Dimension(50, 20));
      txtSafety
          .setToolTipText(getString("plugin.shiploader.preferences.label.safetymargin.tooltip"));
      final JLabel lblSafety =
          new JLabel(getString("plugin.shiploader.preferences.label.safetymargin"));
      lblSafety
          .setToolTipText(getString("plugin.shiploader.preferences.label.safetymargin.tooltip"));
      lblSafety.setLabelFor(txtSafety);

      txtSafetyPerPerson = new JTextArea("" + loader.getSafetyPerPerson());
      txtSafetyPerPerson.setMinimumSize(new Dimension(50, 20));
      txtSafetyPerPerson.setPreferredSize(new java.awt.Dimension(50, 20));
      txtSafetyPerPerson
          .setToolTipText(getString("plugin.shiploader.preferences.label.unitsilver.tooltip"));
      final JLabel lblSafetyPerPerson =
          new JLabel(getString("plugin.shiploader.preferences.label.unitsilver"));
      lblSafetyPerPerson
          .setToolTipText(getString("plugin.shiploader.preferences.label.unitsilver.tooltip"));
      lblSafetyPerPerson.setLabelFor(txtSafetyPerPerson);

      chkChangeShip = new JCheckBox(getString("plugin.shiploader.preferences.label.changeship"));
      chkKeepSilver = new JCheckBox(getString("plugin.shiploader.preferences.label.keepsilver"));
      chkKeepSilverInFaction =
          new JCheckBox(getString("plugin.shiploader.preferences.label.keepsilverinfaction"));

      txtMarker = new JTextArea();
      txtMarker.setMinimumSize(new Dimension(100, 20));
      txtMarker.setPreferredSize(new java.awt.Dimension(100, 20));
      // txtMarker.setToolTipText(getString("plugin.shiploader.preferences.label.marker.tooltip"));
      final JLabel lblMarker = new JLabel(getString("plugin.shiploader.preferences.label.marker"));
      // lblMarker.setToolTipText(getString("plugin.shiploader.preferences.label.Markermargin.tooltip"));
      lblMarker.setLabelFor(txtMarker);

      // GridBagConstraints con = new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST,
      // GridBagConstraints.HORIZONTAL, new Insets(3, 3, 3, 3), 0, 0);
      final GridBagConstraints con = new GridBagConstraints();
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

      final JPanel restrictPanel = new JPanel(new BorderLayout());
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
      } catch (final NumberFormatException e) {
      }
      try {
        loader.setSafetyPerPerson(Integer.parseInt(txtSafetyPerPerson.getText()));
        properties.setProperty(SAFETYPERPERSON_PROPERTY, String
            .valueOf(loader.getSafetyPerPerson()));
      } catch (final NumberFormatException e) {
      }
      loader.setChangeShip(chkChangeShip.isSelected());
      properties.setProperty(CHANGESHIP_PROPERTY, chkChangeShip.isSelected() ? "true" : "false");

      loader.setKeepSilver(chkKeepSilver.isSelected());
      properties.setProperty(KEEPSILVER_PROPERTY, chkKeepSilver.isSelected() ? "true" : "false");

      loader.setKeepSilverInFaction(chkKeepSilverInFaction.isSelected());
      properties.setProperty(KEEPSILVERINFACTION_PROPERTY, chkKeepSilverInFaction.isSelected()
          ? "true" : "false");

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
    init(e.getGameData());
  }

  private void showHelp() {
    new HelpDialog(Client.INSTANCE).setVisible(true);
  }

  class HelpDialog extends InternationalizedDialog {

    private JPanel mainPanel;
    private JButton btn_OK;
    private JLabel magellanImage;
    private JEditorPane helpTextArea;

    /**
     * Creates a new InfoDlg object.
     * 
     * @param parent modally stucked frame.
     */
    public HelpDialog(JFrame parent) {
      super(parent, true);
      initComponents();

      SwingUtils.center(this);
    }

    private void initComponents() {
      mainPanel = new JPanel();
      mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

      setModal(false);
      setTitle(getString("plugin.shiploader.helpdialog.title"));

      String text = getString("plugin.shiploader.helpdialog.text");

      helpTextArea = new JEditorPane();
      helpTextArea.setContentType("text/html");
      helpTextArea.setEditable(false);
      helpTextArea.setText(text);
      helpTextArea.setCaretPosition(0);
      helpTextArea.setPreferredSize(new Dimension(800, 400));
      JScrollPane scrollPane = new JScrollPane(helpTextArea);
      scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
      scrollPane.setPreferredSize(new Dimension(800, 400));
      mainPanel.add(scrollPane);

      // OK Button
      btn_OK = new JButton(getString("plugin.shiploader.ok.text"));
      btn_OK.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          quit();
        }
      });
      btn_OK.setAlignmentX(Component.CENTER_ALIGNMENT);
      mainPanel.add(btn_OK);

      getContentPane().add(mainPanel);

      pack();
    }
  }
}
