package magellan.plugin.mapicons;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import magellan.client.Client;
import magellan.client.desktop.DesktopEnvironment;
import magellan.client.desktop.ShortcutListener;
import magellan.client.extern.MagellanPlugIn;
import magellan.client.swing.map.MarkingsImageCellRenderer;
import magellan.client.swing.preferences.PreferencesFactory;
import magellan.library.Alliance;
import magellan.library.Battle;
import magellan.library.Building;
import magellan.library.CoordinateID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Item;
import magellan.library.LuxuryPrice;
import magellan.library.Message;
import magellan.library.Order;
import magellan.library.Region;
import magellan.library.Region.Visibility;
import magellan.library.Skill;
import magellan.library.StringID;
import magellan.library.Unit;
import magellan.library.event.GameDataEvent;
import magellan.library.impl.MagellanMessageImpl;
import magellan.library.rules.CastleType;
import magellan.library.rules.ItemType;
import magellan.library.rules.Race;
import magellan.library.rules.SkillType;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;

public class MapiconsPlugin implements MagellanPlugIn, ActionListener, ShortcutListener {

	// TH: Increased version to 0.8 when adding the "show regions with enemies" feature
	public static final String version = "0.93";

	private Client client = null;

	private GameData gd = null;

	private static Logger log = Logger.getInstance(MapiconsPlugin.class);

	private static final String myIconPREFIX = "MIplugin_";

	private static final String MAPICON_BATTLE = "battle.gif";
	private static final String MAPICON_MONSTER = "monster.gif";
	private static final String MAPICON_BADMONSTER = "badmonster.gif";
	private static final String MAPICON_HUNGER = "hunger.gif";
	private static final String MAPICON_SPECIALEVENT = "specialevents.gif";
	private static final String MAPICON_THIEF = "dieb.gif";
	private static final String MAPICON_NOTRADE_CASTLE = "notrade_castle.gif";
	private static final String MAPICON_NOTRADE_TRADER = "notrade_trader.gif";
	private static final String MAPICON_NOTRADE_ITEMS = "notrade_items.gif";

	private static final String MAPICON_GUARD_FRIEND = "guard_friend.gif";
	private static final String MAPICON_GUARD_ENEMY = "guard_enemy.gif";

	private static final String MAPICON_EMPTY_TOWER = "empty_tower.gif";

	private static final String MAPICON_ENEMY_PRESENCE = "enemy_present.gif";
	private static final String ENEMY_FACTION_LIST_IDENTIFIER = "// EnemyFaction=";
	private static final String MAPICON_MESSAGE = "message.gif";

	private static final String MONSTER_FACTION = "ii";

	private boolean mapIcons_showing_all = true;
	private boolean mapIcons_showing_Guarding = false;
	private boolean mapIcons_showing_Empty_Towers = false;
	private boolean mapIcons_showing_enemyPresence = false;
	private boolean mapIcons_showing_noTrade = false;

	private boolean enemy_faction_list_exists = false;

	private JCheckBoxMenuItem showGuardMenu;
	private JCheckBoxMenuItem showEmptyTowersMenu;
	private JCheckBoxMenuItem showTradeWarningsMenu;
	// showEnemyPresenceMenu
	private JCheckBoxMenuItem showEnemyPresenceMenu;

	// shortcuts
	private List<KeyStroke> shortcuts;

	final private static List<String> monsterTypeList = new ArrayList<String>() {
		private static final long serialVersionUID = 4711L;
		{
			add("Drachen");
			add("Dracoide");
			add("Ghaste");
			add("Ghoule");
			add("Hirntöter");
			add("Jungdrachen");
			add("Seeschlangen");
			add("Skelette");
			add("Skelettherren");
			add("Untote");
			add("Wyrme");
			add("Zombies");
			add("Juju-Zombies"); // pbem-spiele, 21.11.20010
			add("Juju-Ghaste"); // unbestätigt...
			add("Juju-Drachen"); // unbestätigt...
		}
	};

	// Mail Thoralf vom 31.10.2011
	// wirklich böse Monster müssen auch auf der normalen Monsterliste stehen (monsterTypeList) !
	final private static List<String> realBadMonsterTypeList = new ArrayList<String>() {
		private static final long serialVersionUID = 4711L;
		{
			add("Wyrme");
			add("Juju-Drachen"); // unbestätigt...
			add("Drachen");
			add("Hirntöter");
		}
	};

	private List<String> enemyFactionList = new ArrayList<String>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see magellan.client.desktop.ShortcutListener#getListenerDescription()
	 */
	public String getListenerDescription() {
		return getString("plugin.mapicons.shortcuts.description");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see magellan.client.desktop.ShortcutListener#getShortcutDescription(javax.swing.KeyStroke)
	 */
	public String getShortcutDescription(KeyStroke stroke) {
		int index = shortcuts.indexOf(stroke);
		return getString("plugin.mapicons.shortcuts.description." + String.valueOf(index));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see magellan.client.desktop.ShortcutListener#getShortCuts()
	 */
	public Iterator<KeyStroke> getShortCuts() {
		return shortcuts.iterator();
	}

	/**
	 * This method is called when a shortcut from getShortCuts() is recognized.
	 * 
	 * @see magellan.client.desktop.ShortcutListener#shortCut(javax.swing.KeyStroke)
	 */
	public void shortCut(KeyStroke shortcut) {
		int index = shortcuts.indexOf(shortcut);

		switch (index) {
		case -1:
			break; // unknown shortcut

		case 0:
			// Toggle MapIcons
			if (mapIcons_showing_all) {
				// ausschalten
				new Thread(new Runnable() {
					public void run() {
						removeMyRegionIcons();
						mapIcons_showing_all = false;
						client.getDispatcher().fire(new GameDataEvent(client, client.getData()));
					}
				}).start();
			} else {
				// anschalten
				new Thread(new Runnable() {
					public void run() {
						processGameData();
						mapIcons_showing_all = true;
						client.getDispatcher().fire(new GameDataEvent(client, client.getData()));
					}
				}).start();
			}

			break;
		case 1:
			// toggle display of enemy presence
			toogleEnemyPresence();
		}
	}

	private void toogleEnemyPresence() {
		if (mapIcons_showing_enemyPresence) {
			mapIcons_showing_enemyPresence = false;
			removeMyRegionIcons();
		} else {
			// Only ever activate if a list of enemy factions exists
			if (enemy_faction_list_exists) {
				mapIcons_showing_enemyPresence = true;
			} else {
				// Info Msg wen keine Feinde angezeigt werden können

				String m = "No enemies known! (No ini-File, no info in orders)";
				mapIcons_showing_enemyPresence = false;
				new MsgBox(client, m, "Impossible", false);
			}
		}
		processGameData();
		client.getDispatcher().fire(new GameDataEvent(client, client.getData()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		log.info(getName() + ": action " + e.getActionCommand());
		if (e.getActionCommand().equalsIgnoreCase("removeAllIcons")) {
			new Thread(new Runnable() {
				public void run() {
					removeMyRegionIcons();
					mapIcons_showing_all = false;
					client.getDispatcher().fire(new GameDataEvent(client, client.getData()));
				}
			}).start();
		}

		// recreateAllIcons
		if (e.getActionCommand().equalsIgnoreCase("recreateAllIcons")) {
			new Thread(new Runnable() {
				public void run() {
					processGameData();
					mapIcons_showing_all = true;
					client.getDispatcher().fire(new GameDataEvent(client, client.getData()));
				}
			}).start();
		}

		// showGuard
		if (e.getActionCommand().equalsIgnoreCase("showGuard")) {
			new Thread(new Runnable() {
				public void run() {
					mapIcons_showing_Guarding = !mapIcons_showing_Guarding;
					showGuardMenu.setSelected(mapIcons_showing_Guarding);
					log.info(getName() + ": switching showing guard info to " + mapIcons_showing_Guarding);
					if (mapIcons_showing_all) {
						if (!mapIcons_showing_Guarding) {
							removeMyRegionIcons();
						}
						processGameData();
						client.getDispatcher().fire(new GameDataEvent(client, client.getData()));
					}
				}
			}).start();
		}

		// empty_towers
		if (e.getActionCommand().equalsIgnoreCase("emptyTower")) {
			new Thread(new Runnable() {
				public void run() {
					mapIcons_showing_Empty_Towers = !mapIcons_showing_Empty_Towers;
					showEmptyTowersMenu.setSelected(mapIcons_showing_Empty_Towers);
					log.info(getName() + ": switching showing empty towers info to "
							+ mapIcons_showing_Empty_Towers);
					if (mapIcons_showing_all) {
						if (!mapIcons_showing_Empty_Towers) {
							removeMyRegionIcons();
						}
						processGameData();
						client.getDispatcher().fire(new GameDataEvent(client, client.getData()));
					}
				}
			}).start();
		}

		// TradeWarnings
		if (e.getActionCommand().equalsIgnoreCase("tradeWarnings")) {
			new Thread(new Runnable() {
				public void run() {
					mapIcons_showing_noTrade = !mapIcons_showing_noTrade;
					showTradeWarningsMenu.setSelected(mapIcons_showing_noTrade);
					log.info(getName() + ": switching showing tradewarnings info to "
							+ mapIcons_showing_noTrade);
					if (mapIcons_showing_all) {
						if (!mapIcons_showing_noTrade) {
							removeMyRegionIcons();
						}
						processGameData();
						client.getDispatcher().fire(new GameDataEvent(client, client.getData()));
					}
				}
			}).start();
		}

		// toogleEnemyPresence
		if (e.getActionCommand().equalsIgnoreCase("EnemeyPresence")) {
			new Thread(new Runnable() {
				public void run() {
					toogleEnemyPresence();
				}
			}).start();
		}

		// showInfo
		if (e.getActionCommand().equalsIgnoreCase("showInfo")) {
			new Thread(new Runnable() {
				public void run() {
					new MsgBox(client, getString("plugin.mapicons.menu.showInfoText") + " "
							+ MapiconsPlugin.version, getString("plugin.mapicons.menu.showInfo"), false);
				}
			}).start();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see magellan.client.extern.MagellanPlugIn#getDocks()
	 */
	public Map<String, Component> getDocks() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see magellan.client.extern.MagellanPlugIn#getMenuItems()
	 */
	public List<JMenuItem> getMenuItems() {
		List<JMenuItem> items = new ArrayList<JMenuItem>();

		JMenu menu = new JMenu(getName());

		JMenuItem removeAllIconsMenu = new JMenuItem(getString("plugin.mapicons.menu.removeAllIcons"));
		removeAllIconsMenu.setActionCommand("removeAllIcons");
		removeAllIconsMenu.addActionListener(this);
		menu.add(removeAllIconsMenu);

		JMenuItem recreateAllIconsMenu = new JMenuItem(
				getString("plugin.mapicons.menu.recreateAllIcons"));
		recreateAllIconsMenu.setActionCommand("recreateAllIcons");
		recreateAllIconsMenu.addActionListener(this);
		menu.add(recreateAllIconsMenu);

		menu.addSeparator();
		showGuardMenu = new JCheckBoxMenuItem(getString("plugin.mapicons.menu.showGuard"));
		showGuardMenu.setActionCommand("showGuard");
		showGuardMenu.setSelected(mapIcons_showing_Guarding);
		showGuardMenu.addActionListener(this);
		menu.add(showGuardMenu);

		showEmptyTowersMenu = new JCheckBoxMenuItem(getString("plugin.mapicons.menu.showEmptyTowers"));
		showEmptyTowersMenu.setActionCommand("emptyTower");
		showEmptyTowersMenu.setSelected(mapIcons_showing_Empty_Towers);
		showEmptyTowersMenu.addActionListener(this);
		menu.add(showEmptyTowersMenu);

		showEnemyPresenceMenu = new JCheckBoxMenuItem(
				getString("plugin.mapicons.menu.showEnemyPresence"));
		showEnemyPresenceMenu.setActionCommand("EnemeyPresence");
		showEnemyPresenceMenu.setSelected(mapIcons_showing_enemyPresence);
		showEnemyPresenceMenu.addActionListener(this);
		menu.add(showEnemyPresenceMenu);

		showTradeWarningsMenu = new JCheckBoxMenuItem(
				getString("plugin.mapicons.menu.showTradeWarnings"));
		showTradeWarningsMenu.setActionCommand("tradeWarnings");
		showTradeWarningsMenu.setSelected(mapIcons_showing_noTrade);
		showTradeWarningsMenu.addActionListener(this);
		menu.add(showTradeWarningsMenu);

		menu.addSeparator();

		JMenuItem showInfo = new JMenuItem(getString("plugin.mapicons.menu.showInfo"));
		showInfo.setActionCommand("showInfo");
		showInfo.addActionListener(this);
		menu.add(showInfo);

		items.add(menu);

		return items;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see magellan.client.extern.MagellanPlugIn#getName()
	 */
	public String getName() {
		return getString("plugin.mapicons.name");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see magellan.client.extern.MagellanPlugIn#getPreferencesProvider()
	 */
	public PreferencesFactory getPreferencesProvider() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see magellan.client.extern.MagellanPlugIn#init(magellan.client.Client, java.util.Properties)
	 */
	public void init(Client _client, Properties _properties) {
		client = _client;

		Resources.getInstance().initialize(Client.getResourceDirectory(), "mapiconsplugin_");

		// initProperties();

		initShortcuts();

		log.info(getName() + " initialized (Client)...");

		// init list of enemies, if .ini file is present
		try {
			loadEnemyFactions(_client);
		} catch (Exception e) {
			// TODO: Machen wir das Feature öffentlich? Falls ja, Logeintrag schreiben
			// System.err.println("Error while reading enemy faction file! Check if MIPlugin_Enemies.ini is present in Magellan directory. \n");
			log.info(getName()
					+ ": No enemy faction file. Check if MIPlugin_Enemies.ini is present in Magellan directory.");
		}

	}

	/**
	 * init the shortcuts
	 */
	private void initShortcuts() {
		shortcuts = new ArrayList<KeyStroke>();
		// 0: toggle Map Icons
		shortcuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_MASK));
		// 1: toggle display of enemy presence
		shortcuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_MASK));

		DesktopEnvironment.registerShortcutListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see magellan.client.extern.MagellanPlugIn#init(magellan.library.GameData)
	 */
	public void init(GameData data) {
		// init the report
		gd = data;
		log.info(getName() + " initialized with new GameData...");
		removeMyRegionIcons();
		processGameData();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see magellan.client.extern.MagellanPlugIn#quit(boolean)
	 */
	public void quit(boolean storeSettings) {
		// nothing to do
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

	/**
	 * Durchsucht die geladenen GameData nach Hinweisen und setzt entsprechende regionicon-tags
	 */
	private void processGameData() {
		if (gd == null) {
			return;
		}

		// init list of enemies from orders from trusted units
		// trusted...from which we know the orders
		getEnemyFactionsFromOrders();

		// die einzelnen Bereiche aufrufen..
		if (mapIcons_showing_enemyPresence) {
			log.info(getName() + " set " + searchEnemyPresence() + " regions with enemies present");
		}
		if (mapIcons_showing_Guarding) {
			log.info(getName() + " set " + setGuarding() + " regions with guard information");
		}
		log.info(getName() + " found " + searchBattles() + " battle-regions");
		log.info(getName() + " found " + searchMonsters() + " regions with monsters");
		log.info(getName() + " found " + searchHunger() + " regions with hunger");
		log.info(getName() + " found " + searchSpecialEvents() + " regions with special events");
		log.info(getName() + " found " + searchThiefs() + " regions with thief-events");
		log.info(getName() + " found " + searchBotschaften() + " regions with Messages(Botschaften)");

		if (mapIcons_showing_Empty_Towers) {
			log.info(getName() + " found " + setEmptyTowers() + " regions with empty towers");
		}

		if (mapIcons_showing_noTrade) {
			//
			log.info(getName() + " found " + searchNoTrade() + " regions with trade warnings");

		}

	}

	/**
	 * Durchsucht alle Regionen nach Feinden
	 * 
	 * @return Anzahl der Regionen mit Feinden
	 */
	private int searchEnemyPresence() {
		int erg = 0;
		for (Region r : gd.getRegions()) {
			erg += searchEnemiesRegion(r);
		}
		return erg;
	}

	/**
	 * Durchsucht die übergebene Region nach Einheiten der Partei aus der Feind-Liste und erzeugt den
	 * entsprechenden Eintrag im regionicon-tag
	 * 
	 * @param r
	 *          = zu durchsuchende Region
	 */
	private int searchEnemiesRegion(Region r) {
		int erg = 0;
		for (Unit u : r.units()) {
			if (u.getFaction() != null) {
				if (enemyFactionList.contains(u.getFaction().getID().toString())) {
					setRegionIcon(MAPICON_ENEMY_PRESENCE, r);
					return 1;
				}
				// Factions that don't show their name and don't have the necessary "HELP" status to us are
				// dubious at best...
				// but: How to figure out if it belongs to an allied faction? Until this is clear, don't
				// count disguised factions as enemies
				/**
				 * if (u.isHideFaction()){ setRegionIcon(MAPICON_ENEMY_PRESENCE,r); return 1; }
				 */
			}
		}
		return erg;
	}

	/**
	 * Durchsucht alle Factions in GameData nach Battles
	 * 
	 * @return
	 */
	private int searchBattles() {

		List<Region> regionsBattles = new ArrayList<Region>(0);
		for (Faction f : gd.getFactions()) {
			if (f.getBattles() != null && f.getBattles().size() > 0) {
				searchBattlesForFaction(f, regionsBattles);
			}
		}
		// Tags setzen
		for (Region r : regionsBattles) {
			setRegionIcon(MAPICON_BATTLE, r);
		}

		return regionsBattles.size();
	}

	/**
	 * durchsucht die Battles einer Faction und ergänzt die Liste der Regionen
	 * 
	 * @param f
	 * @param regionsBattles
	 * @return
	 */
	private void searchBattlesForFaction(Faction f, List<Region> regionsBattles) {

		if (f.getBattles() != null && f.getBattles().size() > 0) {
			for (Battle b : f.getBattles()) {
				Region r = gd.getRegion(b.getID());
				if (r == null) {
					// region with battle disappeared from game data
					log.info(getName() + " cannot display battle (missing region):" + b.toString());
				} else {
					if (!regionsBattles.contains(r)) {
						regionsBattles.add(r);
						// debug
						// log.info(getName() + ": found " + r.getName() + " as battle-region");
					}
				}
			}
		}
	}

	/**
	 * durchsucht alle regionen nach Monstern
	 */
	private int searchMonsters() {
		int erg = 0;
		for (Region r : gd.getRegions()) {
			erg += searchMonstersRegion(r);
		}
		return erg;
	}

	/**
	 * durchsucht die Regionen nach Einheiten der Partei "Monster(ii)" und erzeugt den entsprechenden
	 * Eintrag im regionicon-tag
	 * 
	 * @param r
	 */
	private int searchMonstersRegion(Region r) {
		int erg = 0;
		for (Unit u : r.units()) {
			if (u.getFaction() != null && u.getFaction().getID().toString().equals(MONSTER_FACTION)) {
				if (realBadMonsterTypeList.contains(u.getRace().getName())) {
					setRegionIcon(MAPICON_BADMONSTER, r);
				} else {
					setRegionIcon(MAPICON_MONSTER, r);
				}
				return 1;
			}
			if (u.isHideFaction()) {
				Race race = u.getRace();
				if (monsterTypeList.contains(race.getName())) {
					if (realBadMonsterTypeList.contains(race.getName())) {
						setRegionIcon(MAPICON_BADMONSTER, r);
					} else {
						setRegionIcon(MAPICON_MONSTER, r);
					}
					return 1;
				}
			}
		}
		return erg;
	}

	/**
	 * Setzt freundliche und feindliche Bewachung
	 * 
	 * @return
	 */
	private int setGuarding() {
		int erg = 0;
		for (Region r : gd.getRegions()) {
			erg += setGuardingRegion(r);
		}
		return erg;
	}

	private int setGuardingRegion(Region r) {
		int erg = 0;
		boolean friendly = false;
		boolean enemy = false;
		if (r.getGuards() != null && r.getGuards().size() > 0) {
			for (Unit u : r.getGuards()) {
				boolean actFriendly = false;
				if (u.isWeightWellKnown()) {
					actFriendly = true;
				}
				if (u.getFaction() != null && !u.isSpy() && u.getFaction().isPrivileged()) {
					actFriendly = true;
				}

				if (u.getFaction() != null && !u.isSpy() && hasHelpGuard(u.getFaction())) {
					actFriendly = true;
				}
				if (actFriendly) {
					friendly = true;
				} else {
					enemy = true;
				}
			}
		}

		if (friendly) {
			setRegionIcon(MAPICON_GUARD_FRIEND, r);
			erg = 1;
		}
		if (enemy) {
			setRegionIcon(MAPICON_GUARD_ENEMY, r);
			erg = 1;
		}

		return erg;
	}

	/**
	 * Setzt freundliche und feindliche Bewachung
	 * 
	 * @return
	 */
	private int setEmptyTowers() {
		int erg = 0;
		for (Region r : gd.getRegions()) {
			erg += setEmptyTowersRegion(r);
		}
		return erg;
	}

	private int setEmptyTowersRegion(Region r) {
		int erg = 0;
		if (r.buildings() == null || r.buildings().size() == 0) {
			return erg;
		}

		if (r.getVisibility().lessThan(Visibility.TRAVEL)) {
			return erg;
		}

		for (Building b : r.buildings()) {
			if (b.getBuildingType() != null) {
				if (b.getBuildingType() instanceof CastleType) {
					if (b.getModifiedOwnerUnit() == null) {
						setRegionIcon(MAPICON_EMPTY_TOWER, r);
						return 1;
					}
				}
			}
		}

		return erg;
	}

	/**
	 * Durchsucht alle Regionen auf Handelsbeschränkunge....
	 * 
	 * @return
	 */
	private int searchNoTrade() {
		int erg = 0;
		for (Region r : gd.getRegions()) {
			erg += searchNoTradeRegion(r);
		}
		return erg;
	}

	/**
	 * Durchsucht eine Region auf Handelsbeschränkungen
	 * 
	 * @param r
	 * @return
	 */
	private int searchNoTradeRegion(Region r) {
		int erg = 0;
		// Regionen ohne Bauern raussuchen
		if (r.getModifiedPeasants() < 100) {
			return 0;
		}

		// Nur Regionen, in welcher wir eine Einheit "haben"
		boolean seeRegion = false;
		for (Unit u : r.units()) {
			if (u.getCombatStatus() != -1) {
				seeRegion = true;
				break;
			}
		}
		// Verlassen, wenn wir die Region gerade nicht "sehen"
		if (!seeRegion) {
			return 0;
		}

		// ab hier Probleme
		// Problem 1:keine Burg >=2
		boolean hasCastle = false;
		for (Building b : r.buildings()) {
			if (b.getBuildingType() instanceof CastleType) {
				if (b.getSize() >= 2) {
					hasCastle = true;
					break;
				}
			}
		}
		if (!hasCastle) {
			setRegionIcon(MAPICON_NOTRADE_CASTLE, r);
			return 1;
		}

		// Problem 2: kein Händler
		boolean hasTrader = false;
		SkillType handelsSkillType = gd.rules.getSkillType("Handeln");
		if (handelsSkillType != null) {
			for (Unit u : r.units()) {
				if (u.getCombatStatus() != -1) {
					// Talentcheck
					int actTalent = 0;
					Skill handelsSkill = u.getModifiedSkill(handelsSkillType);
					if (handelsSkill != null) {
						actTalent = handelsSkill.getLevel();
					}
					if (actTalent > 0) {
						hasTrader = true;
						break;
					}
				}
			}
		} else {
			// Keine Handelstalent in den rules...nicht weiter machen
			return 0;
		}

		if (!hasTrader) {
			setRegionIcon(MAPICON_NOTRADE_TRADER, r);
			return 1;
		}

		// Problem 3: nix zu verkaufen
		// Gesamte Region auf Waren checken, die verkauft werden könnten, also die
		// hier nicht *gekauft* werden können

		ArrayList<ItemType> sellItems = new ArrayList<ItemType>();
		Map<StringID, LuxuryPrice> priceMap = r.getPrices();
		for (LuxuryPrice LP : priceMap.values()) {
			if (LP.getPrice() > 0) {
				sellItems.add(LP.getItemType());
			}
		}

		// Sachen suchen
		boolean sellItemExist = false;
		for (Unit u : r.units()) {
			if (u.getCombatStatus() != -1) {
				for (Item i : u.getItems()) {
					if (i.getItemType() != null && sellItems.contains(i.getItemType())) {
						// Treffer
						sellItemExist = true;
						break;
					}
				}
				if (sellItemExist) {
					break;
				}
			}
		}

		if (!sellItemExist) {
			setRegionIcon(MAPICON_NOTRADE_ITEMS, r);
			return 1;
		}

		return erg;
	}

	/**
	 * Durchsucht alle Factions in GameData nach Hunger
	 * 
	 * @return
	 */
	private int searchHunger() {

		List<Region> regionsHunger = new ArrayList<Region>(0);
		for (Faction f : gd.getFactions()) {
			if (f.getMessages() != null && f.getMessages().size() > 0) {
				searchHungerForFaction(f, regionsHunger);
			}
		}
		// Tags setzen

		for (Region r : regionsHunger) {
			setRegionIcon(MAPICON_HUNGER, r);
		}

		return regionsHunger.size();
	}

	/**
	 * durchsucht die Battles einer Faction und ergänzt die Liste der Regionen
	 * 
	 * @param f
	 * @param regionsHunger
	 * @return
	 */
	private void searchHungerForFaction(Faction f, List<Region> regionsHunger) {

		if (f.getMessages() != null && f.getMessages().size() > 0) {
			for (Message m : f.getMessages()) {
				MagellanMessageImpl msg = (MagellanMessageImpl) m;
				if (msg.getAttributes() != null) {
					String regionCoordinate = msg.getAttributes().get("region");

					if (regionCoordinate != null) {
						CoordinateID coordinate = CoordinateID.parse(regionCoordinate, ",");

						if (coordinate == null) {
							coordinate = CoordinateID.parse(regionCoordinate, " ");
						}

						if (coordinate != null) {
							boolean isHungerMessage = false;
							// Unterernährung 1158830147
							if (msg.getMessageType().getID().intValue() == 1158830147) {
								isHungerMessage = true;
							}

							// Schwächung 829394366
							if (msg.getMessageType().getID().intValue() == 829394366) {
								isHungerMessage = true;
							}

							if (isHungerMessage) {
								// yep, dies ist eine Hunger Message und wir
								// haben regionskoords dafür
								Region r = gd.getRegion(coordinate);

								if (r == null) {
									// region with hunger disappeared from game data
									log.info(getName() + " cannot display message (missing region):" + msg.getText());
								} else {
									if (!(regionsHunger.contains(r))) {
										// log.info("Debug: added hunger region: " + r.toString());
										regionsHunger.add(r);
									}
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Durchsucht alle Factions und Regionen in GameData nach Botschaften
	 * 
	 * @return
	 */
	private int searchBotschaften() {

		// in den Messages der Factions suchen
		List<Region> regionsBotschaften = new ArrayList<Region>(0);
		for (Faction f : gd.getFactions()) {
			if (f.getMessages() != null && f.getMessages().size() > 0) {
				searchBotschaftForFaction(f, regionsBotschaften);
			}
		}

		// in den Messages der Regionen suchen

		for (Region r : gd.getRegions()) {
			boolean hasBotschaftsMessage = false;
			if (r.getMessages() != null && r.getMessages().size() > 0) {
				if (r.getMessages() != null && r.getMessages().size() > 0) {
					for (Message m : r.getMessages()) {
						MagellanMessageImpl msg = (MagellanMessageImpl) m;
						if (msg.getAttributes() != null) {
							boolean isBotschaftsMessage = false;
							// Message an Region
							if (msg.getMessageType().getID().intValue() == 2110306401) {
								isBotschaftsMessage = true;
							}
							// Uups, Quack, Quack: 621181552
							if (msg.getMessageType().getID().intValue() == 621181552) {
								isBotschaftsMessage = true;
							}
							if (isBotschaftsMessage) {
								hasBotschaftsMessage = true;
								break;
							}
						}
					}
				}
			}
			if (hasBotschaftsMessage) {
				if (!regionsBotschaften.contains(r)) {
					regionsBotschaften.add(r);
				}
			}

		}

		// Tags setzen

		for (Region r : regionsBotschaften) {
			setRegionIcon(MAPICON_MESSAGE, r);
		}

		return regionsBotschaften.size();
	}

	/**
	 * durchsucht die Msg einer Faction und ergänzt die Liste der Regionen
	 * 
	 * @param f
	 * @param regionsHunger
	 * @return
	 */
	private void searchBotschaftForFaction(Faction f, List<Region> regionsBotschaften) {

		if (f.getMessages() != null && f.getMessages().size() > 0) {
			for (Message m : f.getMessages()) {
				MagellanMessageImpl msg = (MagellanMessageImpl) m;
				if (msg.getAttributes() != null) {
					String regionCoordinate = msg.getAttributes().get("region");

					if (regionCoordinate != null) {
						CoordinateID coordinate = CoordinateID.parse(regionCoordinate, ",");

						if (coordinate == null) {
							coordinate = CoordinateID.parse(regionCoordinate, " ");
						}

						if (coordinate != null) {
							boolean isBotschaftMessage = false;
							// Botschaft an Einheit 424720393
							if (msg.getMessageType().getID().intValue() == 424720393) {
								isBotschaftMessage = true;
							}

							// Botschaft an Partei 1216545701
							if (msg.getMessageType().getID().intValue() == 1216545701) {
								isBotschaftMessage = true;
							}

							if (isBotschaftMessage) {
								// yep, dies ist eine Hunger Message und wir
								// haben regionskoords dafür
								Region r = gd.getRegion(coordinate);

								if (r == null) {
									// region with hunger disappeared from game data
									log.info(getName() + " cannot display message (missing region):" + msg.getText());
								} else {
									if (!(regionsBotschaften.contains(r))) {
										regionsBotschaften.add(r);
									}
								}
							}

						}

					}
				}
			}
		}
	}

	/**
	 * durchsucht die Events einer Faction und ergänzt die Liste der Regionen
	 * 
	 * @param f
	 * @param regionsHunger
	 * @return
	 */
	private void searchVulcanoEventsForFaction(Faction f, List<Region> regionsSE) {

		if (f.getMessages() != null && f.getMessages().size() > 0) {
			for (Message m : f.getMessages()) {
				MagellanMessageImpl msg = (MagellanMessageImpl) m;
				if (msg.getAttributes() != null) {

					boolean isVulcanoMessage = false;
					// MESSAGETYPE 745563751
					// "\"Der Vulkan in $region($regionv) bricht aus. Die Lavamassen verwüsten $region($regionn).\"";text

					if (msg.getMessageType() != null && msg.getMessageType().getID() != null
							&& msg.getMessageType().getID().intValue() == 745563751) {
						isVulcanoMessage = true;
					}

					if (isVulcanoMessage) {
						// yep, dies ist eine isVulcanoMessage Message und wir
						// haben regionskoords dafür ?
						String regionCoordinate = msg.getAttributes().get("regionv");
						CoordinateID coordinate = CoordinateID.parse(regionCoordinate, " ");
						Region r = gd.getRegion(coordinate);
						if (r == null) {
							// region with SE disappeared from game data
							log.info(getName() + " cannot display message (missing region):" + msg.getText());
						} else {
							if (!(regionsSE.contains(r))) {
								// log.info("Debug: added hunger region: " + r.toString());
								regionsSE.add(r);
							}
						}
						regionCoordinate = msg.getAttributes().get("regionn");
						coordinate = CoordinateID.parse(regionCoordinate, " ");
						r = gd.getRegion(coordinate);
						if (r == null) {
							// region with SE disappeared from game data
							log.info(getName() + " cannot display message (missing region):" + msg.getText());
						} else {
							if (!(regionsSE.contains(r))) {
								// log.info("Debug: added hunger region: " + r.toString());
								regionsSE.add(r);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Durchsucht alle regions in GameData nach besonderen Events
	 * 
	 * @return
	 */
	private int searchSpecialEvents() {

		List<Region> regionsSpecialEvents = new ArrayList<Region>(0);
		for (Region r : gd.getRegions()) {
			if (r.getMessages() != null && r.getMessages().size() > 0) {
				searchSpecialEventsForRegion(r, regionsSpecialEvents);
			}
		}

		for (Faction f : gd.getFactions()) {
			if (f.getMessages() != null && f.getMessages().size() > 0) {
				searchVulcanoEventsForFaction(f, regionsSpecialEvents);
			}
		}

		// Tags setzen
		for (Region r : regionsSpecialEvents) {
			setRegionIcon(MAPICON_SPECIALEVENT, r);
		}

		return regionsSpecialEvents.size();
	}

	/**
	 * durchsucht die Events der Region und ergänzt die Liste der specialEvents-Regionen
	 * 
	 * @param f
	 * @param regionsSpecialEvents
	 * @return
	 */
	private void searchSpecialEventsForRegion(Region r, List<Region> regionsSpecialEvents) {

		if (r.getMessages() != null && r.getMessages().size() > 0) {
			for (Message m : r.getMessages()) {
				MagellanMessageImpl msg = (MagellanMessageImpl) m;
				if (msg.getAttributes() != null) {

					boolean isSpecialEventMessage = false;
					// MESSAGETYPE 919533243
					// "\"$unit($unit) erscheint plötzlich.\"";text
					if (msg.getMessageType().getID().intValue() == 919533243) {
						isSpecialEventMessage = true;
					}

					// MESSAGETYPE 2037322195
					// "\"$unit($unit) wird durchscheinend und verschwindet.\"";text
					if (msg.getMessageType().getID().intValue() == 2037322195) {
						isSpecialEventMessage = true;
					}

					// MESSAGETYPE 6037032
					// "\"Hier wütete die Pest, und $int($dead) Bauern starben.\"";text
					if (msg.getMessageType().getID().intValue() == 6037032) {
						isSpecialEventMessage = true;
					}

					// MESSAGETYPE 729133963
					// "\"$unit($unit) in $region($region): $int($number) $race($race,$number) $if($eq($number,1),\"verschwand\", \"verschwanden\") über Nacht.\"";text
					if (msg.getMessageType().getID().intValue() == 729133963) {
						isSpecialEventMessage = true;
					}

					// MESSAGETYPE 894791686
					// "\"Der Eisberg $region($region) schmilzt.\"";text
					if (msg.getMessageType().getID().intValue() == 894791686) {
						isSpecialEventMessage = true;
					}

					// MESSAGETYPE 1897415733
					// "\"Das Wurmloch in $region($region) schließt sich.\"";text
					if (msg.getMessageType().getID().intValue() == 1897415733) {
						isSpecialEventMessage = true;
					}

					// MESSAGETYPE 744775869
					// "\"Eine gewaltige Flutwelle verschlingt $region($region) und alle Bewohner.\"";text
					if (msg.getMessageType().getID().intValue() == 744775869) {
						isSpecialEventMessage = true;
					}

					// MESSAGETYPE 1651515350
					// "\"$unit($unit) reist durch ein Wurmloch nach $region($region).\"";text
					if (msg.getMessageType().getID().intValue() == 1651515350) {
						isSpecialEventMessage = true;
					}

					// MESSAGETYPE 1687287742
					// "\"In $region($region) erscheint ein Wurmloch.\"";text
					if (msg.getMessageType().getID().intValue() == 1687287742) {
						isSpecialEventMessage = true;
					}

					// MESSAGETYPE 1176996908
					// "\"Ein Wirbel aus blendendem Licht erscheint.\"";text
					if (msg.getMessageType().getID().intValue() == 1176996908) {
						isSpecialEventMessage = true;
					}

					// MESSAGETYPE 194649789
					// "\"$unit($unit) belagert $building($building).\"";text
					if (msg.getMessageType().getID().intValue() == 194649789) {
						isSpecialEventMessage = true;
					}

					// MESSAGETYPE 248430228
					// "\"$unit($target) wird von $unit($unit) in eine andere Welt geschleudert.\"";text
					if (msg.getMessageType().getID().intValue() == 248430228) {
						isSpecialEventMessage = true;
					}

					// MESSAGETYPE 1057880810
					// "\"$unit($target) fühlt sich $if($isnull($spy),\"\",\"durch $unit($spy) \")beobachtet.\"";text
					if (msg.getMessageType().getID().intValue() == 1057880810) {
						isSpecialEventMessage = true;
					}

					// MESSAGETYPE 198782656
					// "\"Ein Bauernmob erhebt sich und macht Jagd auf Schwarzmagier.\"";text
					if (msg.getMessageType().getID().intValue() == 198782656) {
						isSpecialEventMessage = true;
					}

					// MESSAGETYPE 488105396
					// "\"$unit($mage) ruft ein fürchterliches Unwetter über seine Feinde. Der magischen Regen lässt alles Eisen rosten.\"";text
					if (msg.getMessageType().getID().intValue() == 488105396) {
						isSpecialEventMessage = true;
					}

					// MESSAGETYPE 1378226579
					// "\"$unit($unit) reißt einen Teil von $building($building) ein.\"";text
					if (msg.getMessageType().getID().intValue() == 1378226579) {
						isSpecialEventMessage = true;
					}

					// MESSAGETYPE 212341694
					// "\"$unit($unit) ertrinkt in $region($region).\"";text
					if (msg.getMessageType().getID().intValue() == 212341694) {
						isSpecialEventMessage = true;
					}

					if (isSpecialEventMessage) {
						// yep, dies ist eine Hunger Message und wir
						// haben regionskoords dafür
						if (!(regionsSpecialEvents.contains(r))) {
							// log.info("Debug: added special events region: " + r.toString());
							regionsSpecialEvents.add(r);
							return;
						}
					}
				}
			}
		}
	}

	/**
	 * Durchsucht alle Factions in GameData nach Diebstahlmeldungen
	 * 
	 * @return
	 */
	private int searchThiefs() {

		List<Region> regionsThiefs = new ArrayList<Region>(0);
		for (Faction f : gd.getFactions()) {
			if (f.getMessages() != null && f.getMessages().size() > 0) {
				searchThiefsForFaction(f, regionsThiefs);
			}
		}
		// Tags setzen
		for (Region r : regionsThiefs) {
			setRegionIcon(MAPICON_THIEF, r);
		}

		return regionsThiefs.size();
	}

	/**
	 * durchsucht die Meldungen einer Faction und ergänzt die Liste der Diebstahl-Regionen
	 * 
	 * @param f
	 * @param regionsThiefs
	 * @return
	 */
	private void searchThiefsForFaction(Faction f, List<Region> regionsThiefs) {

		if (f.getMessages() != null && f.getMessages().size() > 0) {
			for (Message m : f.getMessages()) {
				MagellanMessageImpl msg = (MagellanMessageImpl) m;
				if (msg.getAttributes() != null) {
					String regionCoordinate = msg.getAttributes().get("region");

					if (regionCoordinate != null) {
						CoordinateID coordinate = CoordinateID.parse(regionCoordinate, ",");

						if (coordinate == null) {
							coordinate = CoordinateID.parse(regionCoordinate, " ");
						}

						if (coordinate != null) {
							boolean isThiefMessage = false;

							// MESSAGETYPE 1543395091
							// "\"$unit($unit) wurden in $region($region) $int($amount) Silberstücke geklaut.\"";text
							if (msg.getMessageType().getID().intValue() == 1543395091) {
								isThiefMessage = true;
							}

							// 1565770951
							// MESSAGETYPE 1565770951
							// "\"$unit($target) ertappte $unit($unit) beim versuchten Diebstahl.\"";text
							if (msg.getMessageType().getID().intValue() == 1565770951) {
								isThiefMessage = true;
							}

							if (isThiefMessage) {
								// yep, dies ist eine Diebstahl Message und wir
								// haben regionskoords dafür
								Region r = gd.getRegion(coordinate);
								if (!(regionsThiefs.contains(r))) {
									// log.info("Debug: added hunger region: " + r.toString());
									regionsThiefs.add(r);
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Setzt den tag bei der Region, achtet auf Double-Tags
	 * 
	 * @param iconname
	 * @param r
	 */
	private void setRegionIcon(String iconname, Region r) {
		String finalIconName = MapiconsPlugin.myIconPREFIX + iconname;
		if (r == null) {
			log.error(getName() + ": error: region is null, iconname: " + iconname);
			return;
		}
		if (r.containsTag(MarkingsImageCellRenderer.ICON_TAG)) {
			StringTokenizer st = new StringTokenizer(r.getTag(MarkingsImageCellRenderer.ICON_TAG), " ");
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				if (token.equalsIgnoreCase(finalIconName)) {
					// bereits vorhanden
					return;
				}
			}
		}
		// nicht bereits vorhanden->ergänzen
		String newTag = "";
		if (r.containsTag(MarkingsImageCellRenderer.ICON_TAG)) {
			newTag = r.getTag(MarkingsImageCellRenderer.ICON_TAG).concat(" ");
		}
		newTag = newTag.concat(finalIconName);
		r.putTag(MarkingsImageCellRenderer.ICON_TAG, newTag);
		// log.info(getName() + ": put to " + r.getName() + " " + r.getCoordinate().toString() +
		// " tagvalue " + newTag);
	}

	/**
	 * entfernt "unsere" Regionicon-tags aus GameData
	 */
	private void removeMyRegionIcons() {
		for (Region r : gd.getRegions()) {
			removeMyRegionIconsRegion(r);
		}

	}

	/**
	 * entfernt "unsere" regionicon-tag-einträge aus dem tag
	 * 
	 * @param r
	 */
	private void removeMyRegionIconsRegion(Region r) {
		if (r.containsTag(MarkingsImageCellRenderer.ICON_TAG)) {
			StringBuilder newTag = new StringBuilder();
			StringTokenizer st = new StringTokenizer(r.getTag(MarkingsImageCellRenderer.ICON_TAG), " ");
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				if (!(token.startsWith(MapiconsPlugin.myIconPREFIX))) {
					if (newTag.length() > 0) {
						newTag.append(" ");
					}
					newTag.append(token);
				}
			}
			if (newTag.length() > 0) {
				r.putTag(MarkingsImageCellRenderer.ICON_TAG, newTag.toString());
			} else {
				r.removeTag(MarkingsImageCellRenderer.ICON_TAG);
			}
		}
	}

	/**
	 * Prüft, ob eine priviligierte Faction zu _f HELFE BEWACHE hat
	 * 
	 * @param _f
	 *          zu prüfende Faction
	 * @return true or false
	 */
	public boolean hasHelpGuard(Faction _f) {
		for (Faction f : gd.getFactions()) {
			if (f.isPrivileged() && (f.getAllies() != null)) { // privileged

				for (Alliance alliance : f.getAllies().values()) {
					Faction ally = alliance.getFaction();
					if (ally.equals(_f)) {
						if (alliance.getState(16)) {
							return true;
						}
					}
				}
			}
		}

		return false;
	}

	// TH: Load all enemy factions from a parameter file;
	// File name = MIPlugin_Enemies.ini
	// File format = List of faction IDs
	private void loadEnemyFactions(Client client) throws Exception {
		try {
			FileInputStream fstream = new FileInputStream(Client.getSettingsDirectory()
					+ "/MIPlugin_Enemies.ini");
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			// Read File Line By Line
			while ((strLine = br.readLine()) != null) {
				// Read the enemy faction into the list
				// Fiete added simple redundancy check
				if (!enemyFactionList.contains(strLine)) {
					enemyFactionList.add(strLine);
				}
			}
			// Close the input stream
			in.close();
			enemy_faction_list_exists = true;
		} catch (Exception e) {// Catch exception if any
			log.info(getName() + " Error: " + e.getMessage());
		}
	}

	// Fiete: Load all enemy factions from orders
	// order format: // EnemyFaction:abcd
	// not case sencitive, abcd is added
	private void getEnemyFactionsFromOrders() {
		int cnt = 0;

		for (Unit u : gd.getUnits()) {
			for (Order order : u.getOrders2()) {
				String orderString = order.getText();
				if (orderString.toUpperCase().startsWith(ENEMY_FACTION_LIST_IDENTIFIER.toUpperCase())) {
					// Treffer...
					// faction number extrahieren
					String f_number = orderString.substring(ENEMY_FACTION_LIST_IDENTIFIER.length());
					if (f_number.length() > 0) {
						if (!enemyFactionList.contains(f_number)) {
							enemyFactionList.add(f_number);
							log.info(getName() + " added a enemy Faction from orders of " + u.toString() + ": "
									+ f_number);
							cnt++;
							enemy_faction_list_exists = true;
						} else {
							log.info(getName() + " enemy Faction from orders of " + u.toString() + ": "
									+ f_number + " -> already on list");
						}
					} else {
						log.info(getName() + " Error while expecting orders of " + u.toString());
					}
				}
			}
		}
		log.info(getName() + " read " + cnt + " enemy factions from orders");
	}

}
