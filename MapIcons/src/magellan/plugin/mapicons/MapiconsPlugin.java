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
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
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
import magellan.library.UnitID;
import magellan.library.event.GameDataEvent;
import magellan.library.impl.MagellanMessageImpl;
import magellan.library.rules.CastleType;
import magellan.library.rules.ItemType;
import magellan.library.rules.Race;
import magellan.library.rules.SkillType;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;


public class MapiconsPlugin implements MagellanPlugIn, ActionListener,ShortcutListener {
		
	
	
	// TH: Increased version to 0.8 when adding the "show regions with enemies" feature
	// FF: 0.94: show Talents
	// FF: 0.96: show error regions
	// FF: 1.4: Items
	// FF: 1.41: Items with Colors in menu
	public static final String version="1.41";
	
	private Client client = null;
	private Properties properties = null;
	
	private GameData gd = null;
	
	private static Logger log = null;
	
	private static final String myIconPREFIX = "MIplugin_";
	
	private static final String MAPICON_BATTLE = "battle.gif";
	private static final String MAPICON_MONSTER = "monster.gif";
	private static final String MAPICON_BADMONSTER = "badmonster.gif";
	private static final String MAPICON_SPYMONSTERAR = "spymonsterAR.gif";
	private static final String MAPICON_HUNGER = "hunger.gif";
	private static final String MAPICON_SPECIALEVENT = "specialevents.gif";
	private static final String MAPICON_THIEF = "dieb.gif";
	private static final String MAPICON_ERROR = "errors.gif";
	private static final String MAPICON_NOTRADE_CASTLE = "notrade_castle.gif";
	private static final String MAPICON_NOTRADE_TRADER = "notrade_trader.gif";
	private static final String MAPICON_NOTRADE_ITEMS = "notrade_items.gif";
	
	private static final String MAPICON_GUARD_FRIEND = "guard_friend.gif";
	private static final String MAPICON_GUARD_ENEMY = "guard_enemy.gif";
	
	private static final String MAPICON_TALENT = "level_X.gif"; // X wird durch Zahlenwert ersetzt
	private static final String MAPICON_SILVER = "silveramount_X.gif"; // X wird durch Zahlenwert 1..3
	private static final String MAPICON_ITEMS = "items_X.gif"; // X wird durch Zahlenwert 1..10
	
	private static final String MAPICON_EMPTY_TOWER = "empty_tower.gif";
	
	private static final String MAPICON_ENEMY_PRESENCE = "enemy_present.gif";
	private static final String ENEMY_FACTION_LIST_IDENTIFIER = "// EnemyFaction=";
	private static final String MAPICON_MESSAGE = "message.gif";
	
	private static final String MONSTER_FACTION = "ii";
	
	private Boolean mapIcons_showing_all = true;
	private Boolean mapIcons_showing_Messages = true;
	private Boolean mapIcons_showing_Battles = true;
	private Boolean mapIcons_showing_Thiefs = true;
	private Boolean mapIcons_showing_Monsters = true;
	private Boolean mapIcons_showing_Starving = true; // Hunger
	private Boolean mapIcons_showing_Specials = true; // Special Events
	private Boolean mapIcons_showing_Guarding = false;
	private Boolean mapIcons_showing_Empty_Towers = false;
	private Boolean mapIcons_showing_enemyPresence = false;
	private Boolean mapIcons_showing_noTrade = false;
	private Boolean mapIcons_showing_Talents = false;
	private Boolean mapIcons_showing_Errors = false;
	private Boolean mapIcons_showing_Silver = false;
	private Boolean mapIcons_showing_Items = false;
	
	private static final String propertyKey_showing_all = "MIplugin.showing_all";
	private static final String propertyKey_showing_Messages = "MIplugin.showing_Messages";
	private static final String propertyKey_showing_Battles = "MIplugin.showing_Battles";
	private static final String propertyKey_showing_Thiefs = "MIplugin.showing_Thiefs";
	private static final String propertyKey_showing_Monsters = "MIplugin.showing_Monsters";
	private static final String propertyKey_showing_Starving = "MIplugin.showing_Starving";
	private static final String propertyKey_showing_Specials = "MIplugin.showing_Specials";
	private static final String propertyKey_showing_Guarding = "MIplugin.showing_Guarding";
	private static final String propertyKey_showing_Empty_Towers = "MIplugin.showing_Empty_Towers";
	private static final String propertyKey_showing_enemyPresence = "MIplugin.showing_enemyPresence";
	private static final String propertyKey_showing_noTrade = "MIplugin.showing_noTrade";
	private static final String propertyKey_showing_Talents = "MIplugin.showing_Talents";
	private static final String propertyKey_showing_Errors = "MIplugin.showing_Errors";
	private static final String propertyKey_showing_Silver = "MIplugin.showing_Silver";
	private static final String propertyKey_showing_Items = "MIplugin.showing_Items";
	
	private boolean enemy_faction_list_exists = false;
	
	private JCheckBoxMenuItem showMessagesMenu;
	private JCheckBoxMenuItem showBattlesMenu;
	private JCheckBoxMenuItem showThiefsMenu;
	private JCheckBoxMenuItem showMonstersMenu;
	private JCheckBoxMenuItem showStarvingMenu;
	private JCheckBoxMenuItem showSpecialsMenu;
	
	
	private JCheckBoxMenuItem showGuardMenu;
	private JCheckBoxMenuItem showEmptyTowersMenu;
	private JCheckBoxMenuItem showTradeWarningsMenu;
	private JCheckBoxMenuItem showErrorsMenu;
	private JCheckBoxMenuItem showSilverMenu;
	private JMenu silverLevelMenu;
	// showEnemyPresenceMenu
	private JCheckBoxMenuItem showEnemyPresenceMenu;
	private JCheckBoxMenuItem showTalentsMenu;
	private JMenu talentMenu;
	private JMenu itemsMenu;
	
	private String actTalentName="Wahrnehmung";
	private String actItemName = "nothing";
	
	// Silver Level
	private Long Level1=500000L;
	private Long Level2=1000000L;
	private Long Level3=5000000L;
	private Long Level4=10000000L;
	private Long Level5=15000000L;
	
	// Silver Level Property Name
	private static final String silverLevel1PropertyName = "MIplugin.silverlevel.1";
	private static final String silverLevel2PropertyName = "MIplugin.silverlevel.2";
	private static final String silverLevel3PropertyName = "MIplugin.silverlevel.3";
	private static final String silverLevel4PropertyName = "MIplugin.silverlevel.4";
	private static final String silverLevel5PropertyName = "MIplugin.silverlevel.5";
	
	private String silverActionHelper = "";
	
	private long ItemsMaxAnzahl=0;
	
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
            add("Juju-Zombies"); //pbem-spiele, 21.11.20010
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
	
    /* (non-Javadoc)
	 * @see magellan.client.desktop.ShortcutListener#getListenerDescription()
	 */
	public String getListenerDescription() {
		return getString("plugin.mapicons.shortcuts.description");
	}


	/* (non-Javadoc)
	 * @see magellan.client.desktop.ShortcutListener#getShortcutDescription(javax.swing.KeyStroke)
	 */
	public String getShortcutDescription(KeyStroke stroke) {
		int index = shortcuts.indexOf(stroke);
	    return getString("plugin.mapicons.shortcuts.description." + String.valueOf(index));
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
	 * @see magellan.client.desktop.ShortcutListener#shortCut(javax.swing.KeyStroke)
	 */
	public void shortCut(KeyStroke shortcut) {
		int index = shortcuts.indexOf(shortcut);

	    switch (index) {
	    case -1:
	      break; // unknown shortcut

	    case 0:
	      // Toggle MapIcons
	      if (mapIcons_showing_all){
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
    
	private void toogleEnemyPresence(){
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
    			mapIcons_showing_enemyPresence=false;
    			new MsgBox(client,m,"Impossible",false);
    		}
    	}
		processGameData();
		client.getDispatcher().fire(new GameDataEvent(client, client.getData()));
	}
	
	private void toogleShowTalents(){
		if (mapIcons_showing_Talents) {
			mapIcons_showing_Talents = false;
			removeMyRegionIcons();
    	} else {
    		mapIcons_showing_Talents = true;
    	}
		processGameData();
		client.getDispatcher().fire(new GameDataEvent(client, client.getData()));
	}
	
	private void toogleShowItems(){
		if (actItemName.equalsIgnoreCase("nothing")) {
			mapIcons_showing_Items = false;
			log.info("MapIcons-toogleShowItems nothing");
    	} else {
    		log.info("MapIcons-toogleShowItems is true -> " + actItemName);
    		mapIcons_showing_Items = true;
    	}
		removeMyRegionIcons();
		processGameData();
		// client.getDispatcher().fire(new GameDataEvent(client, client.getData()));
		// client.getDispatcher().fire(SelectionEvent.create(this));
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		log.info(getName() + ": action " + e.getActionCommand());
		if (e.getActionCommand().equalsIgnoreCase("removeAllIcons")){
			new Thread(new Runnable() {
				public void run() {
					removeMyRegionIcons();
					mapIcons_showing_all = false;
					client.getDispatcher().fire(new GameDataEvent(client, client.getData()));
				}
			}).start();
		}
		
		// recreateAllIcons
		if (e.getActionCommand().equalsIgnoreCase("recreateAllIcons")){
			new Thread(new Runnable() {
				public void run() {
					processGameData();
					mapIcons_showing_all = true;
					client.getDispatcher().fire(new GameDataEvent(client, client.getData()));
				}
			}).start();
		}
		
		// showGuard
		if (e.getActionCommand().equalsIgnoreCase("showGuard")){
			new Thread(new Runnable() {
				public void run() {
					mapIcons_showing_Guarding = !mapIcons_showing_Guarding;
					properties.setProperty(propertyKey_showing_Guarding, mapIcons_showing_Guarding.toString());
					showGuardMenu.setSelected(mapIcons_showing_Guarding);
					log.info(getName() + ": switching showing guard info to " + mapIcons_showing_Guarding);
					if (mapIcons_showing_all){
						if (!mapIcons_showing_Guarding){
							removeMyRegionIcons();
						}
						processGameData();
						client.getDispatcher().fire(new GameDataEvent(client, client.getData()));
					}
				}
			}).start();
		}
		
		
		// empty_towers
		if (e.getActionCommand().equalsIgnoreCase("emptyTower")){
			new Thread(new Runnable() {
				public void run() {
					mapIcons_showing_Empty_Towers = !mapIcons_showing_Empty_Towers;
					properties.setProperty(propertyKey_showing_Empty_Towers, mapIcons_showing_Empty_Towers.toString());
					showEmptyTowersMenu.setSelected(mapIcons_showing_Empty_Towers);
					log.info(getName() + ": switching showing empty towers info to " + mapIcons_showing_Empty_Towers);
					if (mapIcons_showing_all){
						if (!mapIcons_showing_Empty_Towers){
							removeMyRegionIcons();
						}
						processGameData();
						client.getDispatcher().fire(new GameDataEvent(client, client.getData()));
					}
				}
			}).start();
		}
		
		
		// showMessages
		if (e.getActionCommand().equalsIgnoreCase("showMessages")){
			new Thread(new Runnable() {
				public void run() {
					mapIcons_showing_Messages = !mapIcons_showing_Messages;
					properties.setProperty(propertyKey_showing_Messages, mapIcons_showing_Messages.toString());
					showMessagesMenu.setSelected(mapIcons_showing_Messages);
					log.info(getName() + ": switching showing messages to " + mapIcons_showing_Messages);
					client.getDispatcher().fire(new GameDataEvent(client, client.getData()));
				}
			}).start();
		}
		
		// showBattles
		if (e.getActionCommand().equalsIgnoreCase("showBattles")){
			new Thread(new Runnable() {
				public void run() {
					mapIcons_showing_Battles = !mapIcons_showing_Battles;
					properties.setProperty(propertyKey_showing_Battles, mapIcons_showing_Battles.toString());
					showBattlesMenu.setSelected(mapIcons_showing_Battles);
					log.info(getName() + ": switching showing battles to " + mapIcons_showing_Battles);
					client.getDispatcher().fire(new GameDataEvent(client, client.getData()));
				}
			}).start();
		}
		
		// showThiefs
		if (e.getActionCommand().equalsIgnoreCase("showThiefs")){
			new Thread(new Runnable() {
				public void run() {
					mapIcons_showing_Thiefs = !mapIcons_showing_Thiefs;
					properties.setProperty(propertyKey_showing_Thiefs, mapIcons_showing_Thiefs.toString());
					showThiefsMenu.setSelected(mapIcons_showing_Thiefs);
					log.info(getName() + ": switching showing thiefs to " + mapIcons_showing_Thiefs);
					client.getDispatcher().fire(new GameDataEvent(client, client.getData()));
				}
			}).start();
		}
		
		// showMonsters
		if (e.getActionCommand().equalsIgnoreCase("showMonsters")){
			new Thread(new Runnable() {
				public void run() {
					mapIcons_showing_Monsters = !mapIcons_showing_Monsters;
					properties.setProperty(propertyKey_showing_Monsters, mapIcons_showing_Monsters.toString());
					showMonstersMenu.setSelected(mapIcons_showing_Monsters);
					log.info(getName() + ": switching showing monsters to " + mapIcons_showing_Monsters);
					client.getDispatcher().fire(new GameDataEvent(client, client.getData()));
				}
			}).start();
		}
		
		// showStarving
		if (e.getActionCommand().equalsIgnoreCase("showStarving")){
			new Thread(new Runnable() {
				public void run() {
					mapIcons_showing_Starving = !mapIcons_showing_Starving;
					properties.setProperty(propertyKey_showing_Starving, mapIcons_showing_Starving.toString());
					showStarvingMenu.setSelected(mapIcons_showing_Starving);
					log.info(getName() + ": switching showing starving to " + mapIcons_showing_Starving);
					client.getDispatcher().fire(new GameDataEvent(client, client.getData()));
				}
			}).start();
		}
		
		// showSpecials
		if (e.getActionCommand().equalsIgnoreCase("showSpecials")){
			new Thread(new Runnable() {
				public void run() {
					mapIcons_showing_Specials = !mapIcons_showing_Specials;
					properties.setProperty(propertyKey_showing_Specials, mapIcons_showing_Specials.toString());
					showSpecialsMenu.setSelected(mapIcons_showing_Specials);
					log.info(getName() + ": switching showing specials to " + mapIcons_showing_Specials);
					client.getDispatcher().fire(new GameDataEvent(client, client.getData()));
				}
			}).start();
		}
		
		
		// TradeWarnings
		if (e.getActionCommand().equalsIgnoreCase("tradeWarnings")){
			new Thread(new Runnable() {
				public void run() {
					mapIcons_showing_noTrade = !mapIcons_showing_noTrade;
					properties.setProperty(propertyKey_showing_noTrade, mapIcons_showing_noTrade.toString());
					showTradeWarningsMenu.setSelected(mapIcons_showing_noTrade);
					log.info(getName() + ": switching showing tradewarnings info to " + mapIcons_showing_noTrade);
					if (mapIcons_showing_all){
						if (!mapIcons_showing_noTrade){
							removeMyRegionIcons();
						}
						processGameData();
						client.getDispatcher().fire(new GameDataEvent(client, client.getData()));
					}
				}
			}).start();
		}
		
		// Errors
		if (e.getActionCommand().equalsIgnoreCase("showErrors")){
			new Thread(new Runnable() {
				public void run() {
					mapIcons_showing_Errors = !mapIcons_showing_Errors;
					properties.setProperty(propertyKey_showing_Errors, mapIcons_showing_Errors.toString());
					showErrorsMenu.setSelected(mapIcons_showing_Errors);
					log.info(getName() + ": switching showing Errors info to " + mapIcons_showing_Errors);
					if (mapIcons_showing_all){
						if (!mapIcons_showing_Errors){
							removeMyRegionIcons();
						}
						processGameData();
						client.getDispatcher().fire(new GameDataEvent(client, client.getData()));
					}
				}
			}).start();
		}
		
		// toogleEnemyPresence
		if (e.getActionCommand().equalsIgnoreCase("EnemeyPresence")){
			new Thread(new Runnable() {
				public void run() {
					toogleEnemyPresence();
					properties.setProperty(propertyKey_showing_enemyPresence, mapIcons_showing_enemyPresence.toString());
				}
			}).start();
		}
		
		if (e.getActionCommand().equalsIgnoreCase("Talents")){
			new Thread(new Runnable() {
				public void run() {
					toogleShowTalents();
					properties.setProperty(propertyKey_showing_Talents, mapIcons_showing_Talents.toString());
				}
			}).start();
		}
		
		// Errors
		if (e.getActionCommand().equalsIgnoreCase("showSilver")){
			new Thread(new Runnable() {
				public void run() {
					mapIcons_showing_Silver = !mapIcons_showing_Silver;
					properties.setProperty(propertyKey_showing_Silver, mapIcons_showing_Silver.toString());
					showSilverMenu.setSelected(mapIcons_showing_Silver);
					log.info(getName() + ": switching showing Silver info to " + mapIcons_showing_Silver);
					if (mapIcons_showing_all){
						if (!mapIcons_showing_Silver){
							removeMyRegionIcons();
						}
						processGameData();
						client.getDispatcher().fire(new GameDataEvent(client, client.getData()));
					}
				}
			}).start();
		}
		
		// showInfo
		if (e.getActionCommand().equalsIgnoreCase("showInfo")){
			new Thread(new Runnable() {
				public void run() {
					new MsgBox(client,getString("plugin.mapicons.menu.showInfoText") + " " + MapiconsPlugin.version,getString("plugin.mapicons.menu.showInfo"),false);
				}
			}).start();
		}
		
		// Talentauswahl
		if (e.getActionCommand().startsWith("setNewTalent_")){
			actTalentName = e.getActionCommand().substring(13);
			new Thread(new Runnable() {
				public void run() {
					setNewTalent();
				}
			}).start();
		}
		
		// Item-auswahl
		if (e.getActionCommand().startsWith("setNewItem_")){
			actItemName = e.getActionCommand().substring(11);
			new Thread(new Runnable() {
				public void run() {
					setNewItem();
				}
			}).start();
		}
		
		
		// silverLevel
		if (e.getActionCommand().startsWith("newSilverLevel")){
			silverActionHelper = e.getActionCommand();
			new Thread(new Runnable() {
				public void run() {
					String questText = getString("plugin.mapicons.menu.silverQuestText") + silverActionHelper.substring(14);
					String TitleText = getString("plugin.mapicons.menu.silverTitleText");
					String response = JOptionPane.showInputDialog(null,
							 questText,TitleText,
							  JOptionPane.QUESTION_MESSAGE);
					Long newLevel = Long.parseLong(response);
					if (newLevel>0){
						// Properties setzen
						Properties P = client.getProperties();
						Integer I = Integer.parseInt(silverActionHelper.substring(14));
						switch (I.intValue()) {
						case 1:
							P.setProperty(silverLevel1PropertyName, newLevel.toString());
							break;
						case 2:
							P.setProperty(silverLevel2PropertyName, newLevel.toString());
							break;
						case 3:
							P.setProperty(silverLevel3PropertyName, newLevel.toString());
							break;
						case 4:
							P.setProperty(silverLevel4PropertyName, newLevel.toString());
							break;
						case 5:
							P.setProperty(silverLevel5PropertyName, newLevel.toString());
							break;
						}
						
						removeMyRegionIcons();
						processGameData();
						client.getDispatcher().fire(new GameDataEvent(client, client.getData()));
					}
				}
			}).start();
		}
	}
	
	
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
		
		JMenuItem removeAllIconsMenu = new JMenuItem(getString("plugin.mapicons.menu.removeAllIcons"));
		removeAllIconsMenu.setActionCommand("removeAllIcons");
		removeAllIconsMenu.addActionListener(this);
		menu.add(removeAllIconsMenu);
		
		JMenuItem recreateAllIconsMenu = new JMenuItem(getString("plugin.mapicons.menu.recreateAllIcons"));
		recreateAllIconsMenu.setActionCommand("recreateAllIcons");
		recreateAllIconsMenu.addActionListener(this);
		menu.add(recreateAllIconsMenu);
		
		menu.addSeparator();
		
		showTalentsMenu = new JCheckBoxMenuItem(getString("plugin.mapicons.menu.showTalents"));
		showTalentsMenu.setActionCommand("Talents");
		showTalentsMenu.setSelected(mapIcons_showing_Talents);
		showTalentsMenu.addActionListener(this);
		menu.add(showTalentsMenu);
		
		// Auswahl des Talentes, ab Version 0.95..
		talentMenu = new JMenu(getString("plugin.mapicons.menu.talentSubmenuName"));
		// geht noch nicht,w eil keine Daten geladen sind..siehe init data
		// addTalents(talentMenu);
		menu.add(talentMenu);
		
		showMessagesMenu = new JCheckBoxMenuItem(getString("plugin.mapicons.menu.showMessages"));
		showMessagesMenu.setActionCommand("showMessages");
		showMessagesMenu.setSelected(mapIcons_showing_Messages);
		showMessagesMenu.addActionListener(this);
		menu.add(showMessagesMenu);
		
		showBattlesMenu = new JCheckBoxMenuItem(getString("plugin.mapicons.menu.showBattles"));
		showBattlesMenu.setActionCommand("showBattles");
		showBattlesMenu.setSelected(mapIcons_showing_Battles);
		showBattlesMenu.addActionListener(this);
		menu.add(showBattlesMenu);
		
		showThiefsMenu = new JCheckBoxMenuItem(getString("plugin.mapicons.menu.showThiefs"));
		showThiefsMenu.setActionCommand("showThiefs");
		showThiefsMenu.setSelected(mapIcons_showing_Thiefs);
		showThiefsMenu.addActionListener(this);
		menu.add(showThiefsMenu);
		
		showMonstersMenu = new JCheckBoxMenuItem(getString("plugin.mapicons.menu.showMonsters"));
		showMonstersMenu.setActionCommand("showMonsters");
		showMonstersMenu.setSelected(mapIcons_showing_Monsters);
		showMonstersMenu.addActionListener(this);
		menu.add(showMonstersMenu);		
		
		showStarvingMenu = new JCheckBoxMenuItem(getString("plugin.mapicons.menu.showStarving"));
		showStarvingMenu.setActionCommand("showStarving");
		showStarvingMenu.setSelected(mapIcons_showing_Starving);
		showStarvingMenu.addActionListener(this);
		menu.add(showStarvingMenu);
		
		showSpecialsMenu = new JCheckBoxMenuItem(getString("plugin.mapicons.menu.showSpecials"));
		showSpecialsMenu.setActionCommand("showSpecials");
		showSpecialsMenu.setSelected(mapIcons_showing_Specials);
		showSpecialsMenu.addActionListener(this);
		menu.add(showSpecialsMenu);
		
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
		
		showEnemyPresenceMenu = new JCheckBoxMenuItem(getString("plugin.mapicons.menu.showEnemyPresence"));
		showEnemyPresenceMenu.setActionCommand("EnemeyPresence");
		showEnemyPresenceMenu.setSelected(mapIcons_showing_enemyPresence);
		showEnemyPresenceMenu.addActionListener(this);
		menu.add(showEnemyPresenceMenu);
		
		showTradeWarningsMenu = new JCheckBoxMenuItem(getString("plugin.mapicons.menu.showTradeWarnings"));
		showTradeWarningsMenu.setActionCommand("tradeWarnings");
		showTradeWarningsMenu.setSelected(mapIcons_showing_noTrade);
		showTradeWarningsMenu.addActionListener(this);
		menu.add(showTradeWarningsMenu);
		
		showErrorsMenu = new JCheckBoxMenuItem(getString("plugin.mapicons.menu.showErrors"));
		showErrorsMenu.setActionCommand("showErrors");
		showErrorsMenu.setSelected(mapIcons_showing_Errors);
		showErrorsMenu.addActionListener(this);
		menu.add(showErrorsMenu);
		
		showSilverMenu = new JCheckBoxMenuItem(getString("plugin.mapicons.menu.showSilver"));
		showSilverMenu.setActionCommand("showSilver");
		showSilverMenu.setSelected(mapIcons_showing_Silver);
		showSilverMenu.addActionListener(this);
		menu.add(showSilverMenu);
		
		// Setzen der SilberLevel, ab Version 0.99..
		silverLevelMenu = new JMenu(getString("plugin.mapicons.menu.silverSubmenuName"));
		// Inhalte werden gesetzt, wenn gamedata aktualisiert wird
		menu.add(silverLevelMenu);
		
		// Items, ab version 1.4 FF 2019-02-12 ff
		itemsMenu = new JMenu(getString("plugin.mapicons.menu.itemsSubmenuName"));
		// Inhalte werden gesetzt, wenn gamedata aktualisiert wird
		menu.add(itemsMenu);

		menu.addSeparator();
		
		JMenuItem showInfo = new JMenuItem(getString("plugin.mapicons.menu.showInfo"));
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
		return getString("plugin.mapicons.name");
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
		this.client = _client;
		this.properties = _properties;
		
		Resources.getInstance().initialize(Client.getResourceDirectory(), "mapiconsplugin_");

		log = Logger.getInstance(MapiconsPlugin.class);
		
		initShortcuts();
		
		log.info(getName() + " initialized (Client)...");
		
		// init list of enemies, if .ini file is present
		try {
			loadEnemyFactions(_client);
	    } catch (Exception e) {
	    	log.info(getName() + ": No enemy faction file. Check if MIPlugin_Enemies.ini is present in Magellan directory.");
	    }
		
		// Aus properties laden...
		this.mapIcons_showing_all = Boolean.parseBoolean(_properties.getProperty(propertyKey_showing_all, this.mapIcons_showing_all.toString()));
		this.mapIcons_showing_Messages = Boolean.parseBoolean(_properties.getProperty(propertyKey_showing_Messages, this.mapIcons_showing_Messages.toString()));
		this.mapIcons_showing_Battles = Boolean.parseBoolean(_properties.getProperty(propertyKey_showing_Battles, this.mapIcons_showing_Battles.toString()));
		this.mapIcons_showing_Thiefs = Boolean.parseBoolean(_properties.getProperty(propertyKey_showing_Thiefs, this.mapIcons_showing_Thiefs.toString()));
		this.mapIcons_showing_Monsters = Boolean.parseBoolean(_properties.getProperty(propertyKey_showing_Monsters, this.mapIcons_showing_Monsters.toString()));
		this.mapIcons_showing_Starving = Boolean.parseBoolean(_properties.getProperty(propertyKey_showing_Starving, this.mapIcons_showing_Starving.toString()));
		this.mapIcons_showing_Specials = Boolean.parseBoolean(_properties.getProperty(propertyKey_showing_Specials, this.mapIcons_showing_Specials.toString()));
		this.mapIcons_showing_Guarding = Boolean.parseBoolean(_properties.getProperty(propertyKey_showing_Guarding, this.mapIcons_showing_Guarding.toString()));
		this.mapIcons_showing_Empty_Towers = Boolean.parseBoolean(_properties.getProperty(propertyKey_showing_Empty_Towers, this.mapIcons_showing_Empty_Towers.toString()));
		this.mapIcons_showing_enemyPresence = Boolean.parseBoolean(_properties.getProperty(propertyKey_showing_enemyPresence, this.mapIcons_showing_enemyPresence.toString()));
		this.mapIcons_showing_noTrade = Boolean.parseBoolean(_properties.getProperty(propertyKey_showing_noTrade, this.mapIcons_showing_noTrade.toString()));
		this.mapIcons_showing_Talents = Boolean.parseBoolean(_properties.getProperty(propertyKey_showing_Talents, this.mapIcons_showing_Talents.toString()));
		this.mapIcons_showing_Errors = Boolean.parseBoolean(_properties.getProperty(propertyKey_showing_Errors, this.mapIcons_showing_Errors.toString()));
		this.mapIcons_showing_Silver = Boolean.parseBoolean(_properties.getProperty(propertyKey_showing_Silver, this.mapIcons_showing_Silver.toString()));
		this.mapIcons_showing_Items = Boolean.parseBoolean(_properties.getProperty(propertyKey_showing_Items, this.mapIcons_showing_Items.toString()));
		
	}
	
	
	/**
	 * init the shortcuts
	 */
	private void initShortcuts(){
		shortcuts = new ArrayList<KeyStroke>();
		// 0: toggle Map Icons
	    shortcuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_DOWN_MASK));
	    // 1: toggle display of enemy presence
	    shortcuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_DOWN_MASK));
	    
	    DesktopEnvironment.registerShortcutListener(this);
	}
	
	
	/* (non-Javadoc)
	 * @see magellan.client.extern.MagellanPlugIn#init(magellan.library.GameData)
	 */
	public void init(GameData data) {
		// init the report
		gd = data;
		log.info(getName() + " initialized with new GameData...");
		removeMyRegionIcons();
		processGameData();
		addTalents(talentMenu);
		addSilverLevel(silverLevelMenu);
		addItems(itemsMenu);
		
	}
	/* (non-Javadoc)
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
	 * Durchsucht die geladenen GameData nach Hinweisen 
	 * und setzt entsprechende regionicon-tags
	 */
	private void processGameData(){
		if (gd == null) {return;}
		
		if (!mapIcons_showing_all){
			log.info(getName() + " MapIcons turned off - no icons (no regionicon-tags)");
			return;
		}
		
				
		
		// die einzelnen Bereiche aufrufen..
		
		// init list of enemies from orders from trusted units
		// trusted...from which we know the orders 
		getEnemyFactionsFromOrders();
		if (mapIcons_showing_enemyPresence) {
			log.info(getName() +  " set " + this.searchEnemyPresence() + " regions with enemies present");
		}
		if (mapIcons_showing_Guarding){
			log.info(getName() +  " set " + this.setGuarding() + " regions with guard information");
		}
		if (mapIcons_showing_Battles){
			log.info(getName() +  " found " + this.searchBattles() + " battle-regions");
		}
		if (mapIcons_showing_Monsters){
			log.info(getName() +  " found " + this.searchMonsters() + " regions with monsters");
		}
		if (mapIcons_showing_Starving){
			log.info(getName() +  " found " + this.searchHunger() + " regions with hunger");
		}
		if (mapIcons_showing_Specials){
			log.info(getName() +  " found " + this.searchSpecialEvents() + " regions with special events");
		}
		if (mapIcons_showing_Thiefs){
			log.info(getName() +  " found " + this.searchThiefs() + " regions with thief-events");
		}
		if (mapIcons_showing_Messages){
			log.info(getName() +  " found " + this.searchBotschaften() + " regions with Messages(Botschaften)");
		}
		
		if (mapIcons_showing_Empty_Towers){
			log.info(getName() +  " found " + this.setEmptyTowers() + " regions with empty towers");
		}
		
		if (mapIcons_showing_noTrade){
			// 
			log.info(getName() +  " found " + this.search_NoTRade() + " regions with trade warnings");
			
		}
		
		if (mapIcons_showing_Talents){
			log.info(getName() +  " set " + this.searchTalents() + " regions with level information");
		}
		
		if (mapIcons_showing_Errors){
			log.info(getName() +  " found " + this.searchErrors() + " regions with error-messages");
		}
		
		if (mapIcons_showing_Silver){
			log.info(getName() +  " found " + this.searchSilver() + " regions with Silver-Information");
		}
		
		if (mapIcons_showing_Items) {
			log.info(getName() +  " found " + this.searchItems() + " regions with Items-Information");
		}
		
	}
	
	/**
	 * Durchsucht alle Regionen nach Feinden
	 * @return Anzahl der Regionen mit Feinden
	 */
	private int searchEnemyPresence(){
		int erg = 0;
		for (Region r:gd.getRegions()){
			erg += searchEnemiesRegion(r);
		}
		return erg;
	}

	/**
	 * Durchsucht die übergebene Region nach Einheiten der Partei aus der Feind-Liste
	 * und erzeugt den entsprechenden Eintrag im regionicon-tag
	 * @param r = zu durchsuchende Region
	 */
	private int searchEnemiesRegion(Region r){
		int erg = 0;
		for (Unit u:r.units()){
			if (u.getFaction()!=null){
				if (enemyFactionList.contains(u.getFaction().getID().toString())) {
					setRegionIcon(MAPICON_ENEMY_PRESENCE,r);
					return 1;
				}
				// Factions that don't show their name and don't have the necessary "HELP" status to us are dubious at best...
				// but: How to figure out if it belongs to an allied faction? Until this is clear, don't count disguised factions as enemies
				/**
				if (u.isHideFaction()){
					setRegionIcon(MAPICON_ENEMY_PRESENCE,r);
					return 1;
				}
				*/
			}
		}
		return erg;
	}
	
	/**
	 * Durchsucht alle Factions in GameData nach Battles
	 * @return
	 */
	private int searchBattles(){
		
		List<Region> regionsBattles = new ArrayList<Region>(0); 
		for (Faction f:gd.getFactions()){
			if (f.getBattles()!=null && f.getBattles().size()>0){
				searchBattlesForFaction(f,regionsBattles);
			}
		}
		// Tags setzen
		for (Region r:regionsBattles){
			setRegionIcon(MAPICON_BATTLE,r);
		}
		
		return regionsBattles.size();
	}
	
	/**
	 * durchsucht die Battles einer Faction und ergänzt die Liste der Regionen
	 * @param f
	 * @param regionsBattles
	 * @return
	 */
	private void searchBattlesForFaction(Faction f, List<Region> regionsBattles){
		
		if (f.getBattles()!=null && f.getBattles().size()>0){
			for (Battle b:f.getBattles()){
				Region r = gd.getRegion(b.getID());
				if (r==null){
      				// region with battle disappeared from game data
      				log.info(getName() + " cannot display battle (missing region):" + b.toString());
				} else {
					if (!regionsBattles.contains(r)){
						regionsBattles.add(r);
						// debug
						// log.info(getName() +  ": found " + r.getName() + " as battle-region");
					}
				}
			}
		}
	}
	
	/**
	 * durchsucht alle regionen nach Monstern
	 */
	private int searchMonsters(){
		int erg = 0;
		for (Region r:gd.getRegions()){
			erg += searchMonstersRegion(r);
		}
		return erg;
	}
	
	/**
	 * durchsucht die Regionen nach Einheiten der Partei "Monster(ii)"
	 * und erzeugt den entsprechenden Eintrag im regionicon-tag
	 * @param r
	 */
	private int searchMonstersRegion(Region r){
		int erg = 0;
		for (Unit u:r.units()){
			if (u.getFaction()!=null && u.getFaction().getID().toString().equals(MONSTER_FACTION)){
				if (realBadMonsterTypeList.contains(u.getRace().getName())){
					setRegionIcon(MAPICON_BADMONSTER,r);
				} else {
					setRegionIcon(MAPICON_MONSTER,r);
				}
				erg=1;
			}
			if (u.isHideFaction()){
				Race race = u.getRace();
				if (monsterTypeList.contains(race.getName())){
					if (realBadMonsterTypeList.contains(race.getName())){
						setRegionIcon(MAPICON_BADMONSTER,r);
					} else {
						setRegionIcon(MAPICON_MONSTER,r);
					}
					erg=1;
				}
			}
			
			// 20130513: im AR Erkennung von getarnten Monstern und Parteigetarnten
			// nennen sich Hirntöter...sind aber eine andere Rasse
			CoordinateID actC = r.getCoordinate();
			if (actC.getZ()==1){
				// Region im AR
				if (u.isHideFaction()){
					Race race = u.getRace();
					if (!race.getName().equals("Hirntöter")){
						// Bingo
						setRegionIcon(MAPICON_SPYMONSTERAR,r);
						erg=1;
					}
				}
				if (u.getFaction()!=null && u.getFaction().getID().toString().equals(MONSTER_FACTION)){
					Race race = u.getRace();
					if (!race.getName().equals("Hirntöter")){
						// Bingo
						setRegionIcon(MAPICON_SPYMONSTERAR,r);
						erg=1;
					}
				}
			}
			
		}
		return erg;
	}
	
	/**
	 * Setzt freundliche und feindliche Bewachung
	 * @return
	 */
	private int setGuarding(){
		int erg=0;
		for (Region r:gd.getRegions()){
			erg += setGuardingRegion(r);
		}
		return erg;
	}
	
	
	private int setGuardingRegion(Region r){
		int erg=0;
		boolean friendly = false;
		boolean enemy = false;
		if (r.getGuards()!=null && r.getGuards().size()>0){
			for (Unit u:r.getGuards()){
				boolean actFriendly=false;
				if (u.isWeightWellKnown()){
					actFriendly=true;
				}
				if (u.getFaction()!=null && !u.isSpy() && u.getFaction().isPrivileged()){
					actFriendly=true;
				}
				
				if (u.getFaction()!=null && !u.isSpy() && hasHelpGuard(u.getFaction())){
					actFriendly=true;
				}
				if (actFriendly){
					friendly=true;
				} else {
					enemy = true;
				}
			}
		}
		
		if (friendly){
			this.setRegionIcon(MAPICON_GUARD_FRIEND, r);
			erg=1;
		}
		if (enemy){
			this.setRegionIcon(MAPICON_GUARD_ENEMY, r);
			erg=1;
		}
		
		
		return erg;
	}
	
	
	/**
	 * Setzt freundliche und feindliche Bewachung
	 * @return
	 */
	private int setEmptyTowers(){
		int erg=0;
		for (Region r:gd.getRegions()){
			erg += setEmptyTowersRegion(r);
		}
		return erg;
	}
	
	
	private int setEmptyTowersRegion(Region r){
		int erg=0;
		if (r.buildings()==null || r.buildings().size()==0){
			return erg;
		}
		
		if (r.getVisibility().lessThan(Visibility.TRAVEL)) {
			return erg;
		}
		
		for (Building b:r.buildings()){
			if (b.getBuildingType()!=null){
				if (b.getBuildingType() instanceof CastleType){
					if (b.getModifiedOwnerUnit()==null){
						this.setRegionIcon(MAPICON_EMPTY_TOWER, r);
						return 1;
					}
				}
			}
		}
		
		
		return erg;
	}
	
	
	/**
	 * Durchsucht alle Regionen auf Handelsbeschränkunge....
	 * @return
	 */
	private int search_NoTRade(){
		int erg=0;
		for (Region r:gd.getRegions()){
			erg += search_NoTrade_Region(r);
		}
		return erg;
	}
	
	
	/**
	 * Durchsucht eine Region auf Handelsbeschränkungen
	 * @param r
	 * @return
	 */
	private int search_NoTrade_Region(Region r){
		int erg=0;
		// Regionen ohne Bauern raussuchen
		if (r.getModifiedPeasants()<100){
			return 0;
		}
		
		// Nur Regionen, in welcher wir eine Einheit "haben"
		boolean seeRegion=false;
		for (Unit u:r.units()){
			if (u.getCombatStatus()!=-1){
				seeRegion=true;
				break;
			}
		}
		// Verlassen, wenn wir die Region gerade nicht "sehen"
		if (!seeRegion){
			return 0;
		}
		
		// ab hier Probleme
		// Problem 1:keine Burg >=2
		boolean hasCastle=false;
		for (Building b:r.buildings()){
			if (b.getBuildingType() instanceof CastleType){
				if (b.getSize()>=2){
					hasCastle=true;
					break;
				}
			}
		}
		if (!hasCastle){
			this.setRegionIcon(MAPICON_NOTRADE_CASTLE, r);
			return 1;
		}
		
		
		// Problem 2: kein Händler
		boolean hasTrader=false;
		SkillType handelsSkillType = gd.getRules().getSkillType("Handeln");
		if (handelsSkillType!=null){
			for (Unit u:r.units()){
				if (u.getCombatStatus()!=-1){
					// Talentcheck
					int actTalent = 0;
					Skill handelsSkill = u.getModifiedSkill(handelsSkillType);
					if (handelsSkill!=null){
						actTalent = handelsSkill.getLevel();
					}
					if (actTalent>0){
						hasTrader=true;
						break;
					}
				}
			}
		} else {
			// Keine Handelstalent in den rules...nicht weiter machen
			return 0;
		}
		
		if (!hasTrader){
			this.setRegionIcon(MAPICON_NOTRADE_TRADER, r);
			return 1;
		}
		
		
		// Problem 3: nix zu verkaufen
		// Gesamte Region auf Waren checken, die verkauft werden könnten, also die
		// hier nicht *gekauft* werden können
		
		ArrayList<ItemType> sellItems = new ArrayList<ItemType>(); 
		Map<StringID, LuxuryPrice> priceMap = r.getPrices();
		for (LuxuryPrice LP:priceMap.values()){
			if (LP.getPrice()>0){
				sellItems.add(LP.getItemType());
			}
		}
		
		// Sachen suchen
		boolean sellItemExist = false;
		for (Unit u:r.units()){
			if (u.getCombatStatus()!=-1){
				for (Item i:u.getItems()){
					if (i.getItemType()!=null && sellItems.contains(i.getItemType())){
						// Treffer
						sellItemExist = true;
						break;
					}
				}
				if (sellItemExist){
					break;
				}
			}
		}
		
		if (!sellItemExist){
			this.setRegionIcon(MAPICON_NOTRADE_ITEMS, r);
			return 1;
		}
		
		return erg;
	}
	
	
	/**
	 * Durchsucht alle Factions in GameData nach Hunger
	 * @return
	 */
	private int searchHunger(){
		
		List<Region> regionsHunger = new ArrayList<Region>(0); 
		for (Faction f:gd.getFactions()){
			if (f.getMessages()!=null && f.getMessages().size()>0){
				searchHungerForFaction(f,regionsHunger);
			}
		}
		// Tags setzen
		
		for (Region r:regionsHunger){
			setRegionIcon(MAPICON_HUNGER,r);
		}
		
		return regionsHunger.size();
	}
	
	/**
	 * durchsucht die Battles einer Faction und ergänzt die Liste der Regionen
	 * @param f
	 * @param regionsHunger
	 * @return
	 */
	private void searchHungerForFaction(Faction f, List<Region> regionsHunger){
		
		if (f.getMessages()!=null && f.getMessages().size()>0){
			for (Message m : f.getMessages()){
				MagellanMessageImpl msg = (MagellanMessageImpl)m;
				if (msg.getAttributes() != null) {
		            String regionCoordinate = msg.getAttributes().get("region");

		            if (regionCoordinate != null) {
		            	CoordinateID coordinate = CoordinateID.parse(regionCoordinate, ",");

		              if (coordinate == null) {
		                coordinate = CoordinateID.parse(regionCoordinate, " ");
		              }
		              
		              if (coordinate!=null){
		            	  boolean isHungerMessage = false;   
		            	// Unterernährung 1158830147
		          		if (getId(msg)==1158830147){
		          			isHungerMessage=true;
		          		}
		          		
		          		// Schwächung 829394366
		          		if (getId(msg)==829394366){
		          			isHungerMessage=true;
		          		}
		          		
		          		if (isHungerMessage){
		          			// yep, dies ist eine Hunger Message und wir
		          			// haben regionskoords dafür
		          			Region r = gd.getRegion(coordinate);
		          			
		          			if (r==null){
		          				// region with hunger disappeared from game data
		          				log.info(getName() + " cannot display message (missing region):" + msg.getText());
		          			} else {
			          			if (!(regionsHunger.contains(r))){
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
	 * @return
	 */
	private int searchBotschaften(){
		
		// in den Messages der Factions suchen
		List<Region> regionsBotschaften = new ArrayList<Region>(0); 
		for (Faction f:gd.getFactions()){
			if (f.getMessages()!=null && f.getMessages().size()>0){
				searchBotschaftForFaction(f,regionsBotschaften);
			}
		}
		
		// in den Messages der Regionen suchen
		 
		for (Region r:gd.getRegions()){
			boolean hasBotschaftsMessage = false;
			if (r.getMessages()!=null && r.getMessages().size()>0){
				if (r.getMessages()!=null && r.getMessages().size()>0){
					for (Message m : r.getMessages()){
						MagellanMessageImpl msg = (MagellanMessageImpl)m;
						if (msg.getAttributes() != null) {
			            	 boolean isBotschaftsMessage = false;   
			            	// Message an Region
			          		if (getId(msg)==2110306401){
			          			isBotschaftsMessage=true;
			          		}
			          		// Uups, Quack, Quack: 621181552
			          		if (getId(msg)==621181552){
			          			isBotschaftsMessage=true;
			          		}
			          		if (isBotschaftsMessage){
			          			hasBotschaftsMessage=true;
			          			break;
			          		}
						}
					}
				}
			}
			if (hasBotschaftsMessage){
				if (!regionsBotschaften.contains(r)){
					regionsBotschaften.add(r);
				}
			}
			
		}
		
		// Tags setzen
		
		for (Region r:regionsBotschaften){
			setRegionIcon(MAPICON_MESSAGE,r);
		}
		
		return regionsBotschaften.size();
	}
	
	/**
	 * durchsucht die Msg einer Faction und ergänzt die Liste der Regionen
	 * @param f
	 * @param regionsHunger
	 * @return
	 */
	private void searchBotschaftForFaction(Faction f, List<Region> regionsBotschaften){
		
		if (f.getMessages()!=null && f.getMessages().size()>0){
			for (Message m : f.getMessages()){
				MagellanMessageImpl msg = (MagellanMessageImpl)m;
				if (msg.getAttributes() != null) {
		            String regionCoordinate = msg.getAttributes().get("region");

		            if (regionCoordinate != null) {
		            	CoordinateID coordinate = CoordinateID.parse(regionCoordinate, ",");

		              if (coordinate == null) {
		                coordinate = CoordinateID.parse(regionCoordinate, " ");
		              }
		              
		              if (coordinate!=null){
		            	  boolean isBotschaftMessage = false;   
		            	// Botschaft an Einheit 424720393
		          		if (getId(msg)==424720393){
		          			isBotschaftMessage=true;
		          		}
		          		
		          		// Botschaft an Partei 1216545701
		          		if (getId(msg)==1216545701){
		          			isBotschaftMessage=true;
		          		}
		          		
		          		if (isBotschaftMessage){
		          			// yep, dies ist eine Hunger Message und wir
		          			// haben regionskoords dafür
		          			Region r = gd.getRegion(coordinate);
		          			
		          			if (r==null){
		          				// region with hunger disappeared from game data
		          				log.info(getName() + " cannot display message (missing region):" + msg.getText());
		          			} else {
			          			if (!(regionsBotschaften.contains(r))){
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
	 * @param f
	 * @param regionsHunger
	 * @return
	 */
	private void searchVulcanoEventsForFaction(Faction f, List<Region> regionsSE){
		
		if (f.getMessages()!=null && f.getMessages().size()>0){
			for (Message m : f.getMessages()){
				MagellanMessageImpl msg = (MagellanMessageImpl)m;
				if (msg.getAttributes() != null) {
		           
	            	boolean isVulcanoMessage = false;  
	            	// MESSAGETYPE 745563751
	            	// "\"Der Vulkan in $region($regionv) bricht aus. Die Lavamassen verwüsten $region($regionn).\"";text
	            	
	          		if (getId(msg)==745563751){
	          			isVulcanoMessage=true;
	          		}

	          		if (isVulcanoMessage){
	          			// yep, dies ist eine isVulcanoMessage Message und wir
	          			// haben regionskoords dafür ?
	          			String regionCoordinate = msg.getAttributes().get("regionv");
	          			CoordinateID coordinate = CoordinateID.parse(regionCoordinate, " ");
	          			Region r = gd.getRegion(coordinate);
	          			if (r==null){
	          				// region with SE disappeared from game data
	          				log.info(getName() + " cannot display message (missing region):" + msg.getText());
	          			} else {
		          			if (!(regionsSE.contains(r))){
		          				// log.info("Debug: added hunger region: " + r.toString());
		          				regionsSE.add(r);
		          			}
	          			}
	          			regionCoordinate = msg.getAttributes().get("regionn");
	          			coordinate = CoordinateID.parse(regionCoordinate, " ");
	          			r = gd.getRegion(coordinate);
	          			if (r==null){
	          				log.info(getName() + " cannot display message (missing region):" + msg.getText());
	          			} else {
		          			if (!(regionsSE.contains(r))){
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
	 * @return
	 */
	private int searchSpecialEvents(){
		
		List<Region> regionsSpecialEvents = new ArrayList<Region>(0); 
		for (Region r:gd.getRegions()){
			if (r.getMessages()!=null && r.getMessages().size()>0){
				searchSpecialEventsForRegion(r,regionsSpecialEvents);
			}
		}
		
		for (Faction f:gd.getFactions()){
			if (f.getMessages()!=null && f.getMessages().size()>0){
				searchVulcanoEventsForFaction(f,regionsSpecialEvents);
				searchSpecialEventsForFaction(f,regionsSpecialEvents);
			}
		}
		
		// Tags setzen
		for (Region r:regionsSpecialEvents){
			setRegionIcon(MAPICON_SPECIALEVENT,r);
		}
		
		return regionsSpecialEvents.size();
	}
	
	/**
	 * durchsucht die Events der Region und ergänzt die Liste der specialEvents-Regionen
	 * @param f
	 * @param regionsSpecialEvents
	 * @return
	 */
	private void searchSpecialEventsForRegion(Region r, List<Region> regionsSpecialEvents){
		
		if (r.getMessages()!=null && r.getMessages().size()>0){
			for (Message m : r.getMessages()){
				MagellanMessageImpl msg = (MagellanMessageImpl)m;
				int mtype = getId(msg);
				if (msg.getAttributes() != null) {
		            
	            	 boolean isSpecialEventMessage = false;   
	            	// MESSAGETYPE 919533243
	            	//  "\"$unit($unit) erscheint plötzlich.\"";text
	          		if (mtype==919533243){
	          			isSpecialEventMessage=true;
	          		}
	          		
	          		// MESSAGETYPE 2037322195
	          		// "\"$unit($unit) wird durchscheinend und verschwindet.\"";text
	          		if (mtype==2037322195){
	          			isSpecialEventMessage=true;
	          		}
	          		
	          		// MESSAGETYPE 6037032
	          		// "\"Hier wütete die Pest, und $int($dead) Bauern starben.\"";text
	          		if (mtype==6037032){
	          			isSpecialEventMessage=true;
	          		}
	          		
	          		// MESSAGETYPE 729133963
	          		// "\"$unit($unit) in $region($region): $int($number) $race($race,$number) $if($eq($number,1),\"verschwand\", \"verschwanden\") über Nacht.\"";text
	          		if (mtype==729133963){
	          			isSpecialEventMessage=true;
	          		}
	          		
	          		// MESSAGETYPE 894791686
	          		// "\"Der Eisberg $region($region) schmilzt.\"";text
	          		if (mtype==894791686){
	          			isSpecialEventMessage=true;
	          		}
	          		
	          		
	          		// MESSAGETYPE 1897415733
	          		// "\"Das Wurmloch in $region($region) schließt sich.\"";text
	          		if (mtype==1897415733){
	          			isSpecialEventMessage=true;
	          		}
	          		
	          		// MESSAGETYPE 744775869
	          		// "\"Eine gewaltige Flutwelle verschlingt $region($region) und alle Bewohner.\"";text
	          		if (mtype==744775869){
	          			isSpecialEventMessage=true;
	          		}
	          		
	          		// MESSAGETYPE 1651515350
	          		// "\"$unit($unit) reist durch ein Wurmloch nach $region($region).\"";text
	          		if (mtype==1651515350){
	          			isSpecialEventMessage=true;
	          		}
	          		
	          		// MESSAGETYPE 1687287742
	          		// "\"In $region($region) erscheint ein Wurmloch.\"";text
	          		if (mtype==1687287742){
	          			isSpecialEventMessage=true;
	          		}
	          		
	          		// MESSAGETYPE 1176996908
	          		// "\"Ein Wirbel aus blendendem Licht erscheint.\"";text
	          		if (mtype==1176996908){
	          			isSpecialEventMessage=true;
	          		}
	          		
	          		// MESSAGETYPE 194649789
	          		// "\"$unit($unit) belagert $building($building).\"";text
	          		if (mtype==194649789){
	          			isSpecialEventMessage=true;
	          		}
	          		
	          		// MESSAGETYPE 248430228
	          		// "\"$unit($target) wird von $unit($unit) in eine andere Welt geschleudert.\"";text
	          		if (mtype==248430228){
	          			isSpecialEventMessage=true;
	          		}
	          		
	          		// MESSAGETYPE 1057880810
	          		// "\"$unit($target) fühlt sich $if($isnull($spy),\"\",\"durch $unit($spy) \")beobachtet.\"";text
	          		if (mtype==1057880810){
	          			isSpecialEventMessage=true;
	          		}
	          		
	          		// MESSAGETYPE 809710123
	          		// "\"$unit($unit) fühlt sich beobachtet.\"";text
	          		// "events";section
	          		if (mtype==809710123){
	          			isSpecialEventMessage=true;
	          		}
	          		
	          		
	          		// MESSAGETYPE 198782656
	          		// "\"Ein Bauernmob erhebt sich und macht Jagd auf Schwarzmagier.\"";text
	          		if (mtype==198782656){
	          			isSpecialEventMessage=true;
	          		}
	          		
	          		// MESSAGETYPE 488105396
	          		// "\"$unit($mage) ruft ein fürchterliches Unwetter über seine Feinde. Der magischen Regen lässt alles Eisen rosten.\"";text
	          		if (mtype==488105396){
	          			isSpecialEventMessage=true;
	          		}
	          		
	          		// MESSAGETYPE 1378226579
	          		// "\"$unit($unit) reißt einen Teil von $building($building) ein.\"";text
	          		if (mtype==1378226579){
	          			isSpecialEventMessage=true;
	          		}
	          		
	          		// MESSAGETYPE 212341694
	          		// "\"$unit($unit) ertrinkt in $region($region).\"";text
	          		if (mtype==212341694){
	          			isSpecialEventMessage=true;
	          		}
	          		
	          		// MESSAGETYPE 1948430386
	          		// "\"$unit($unit) in $region($region): '$order($command)' - Selbst in der Bibliothek von Xontormia konnte dieser Spruch nicht gefunden werden.\"";text
	          		if (mtype==1948430386){
	          			isSpecialEventMessage=true;
	          		}
	          		
	          		
	          		// 	MESSAGETYPE 1071183144
	          		// "\"Aus dem Vulkankrater von $region($region) steigt plötzlich Rauch.\"";text
	          		// "events";section
	          		if (mtype==1071183144){
	          			isSpecialEventMessage=true;
	          		}
	          		
	          		// MESSAGETYPE 1847594364
	          		// "\"$unit($unit) in $region($region): '$order($command)' - Der Magier zerstört den Fluch($id) auf ${target}.\"";text
	          		if (mtype==1847594364){
	          			isSpecialEventMessage=true;
	          		}
	          		
	          		
	          		// MESSAGETYPE 2122087327
	          		// "\"$int($amount) Bauern flohen aus Furcht vor $unit($unit).\"";text
	          		// "events";section
	          		if (mtype==2122087327){
	          			isSpecialEventMessage=true;
	          		}
	          		
	          		// MESSAGETYPE 1359435364
	          		// "\"$unit($unit) in $region($region): '$order($command)' - $unit($target) wird von uns aufgenommen.\"";text
	          		// "magic";section
	          		if (mtype==1359435364){
	          			isSpecialEventMessage=true;
	          		}
	          		
	          		// MESSAGETYPE 2047863741
	          		// "\"$unit($unit) wird aus der astralen Ebene nach $region($region) geschleudert.\"";text
	          		// "magic";section
	          		if (mtype==2047863741){
	          			isSpecialEventMessage=true;
	          		}
	          		
	          		
	          		
	          		if (isSpecialEventMessage){
	          			// yep, dies ist eine Special Event Message und wir
	          			// haben regionskoords dafür
	          			if (!(regionsSpecialEvents.contains(r))){
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
	 * @return
	 */
	private int searchThiefs(){
		
		List<Region> regionsThiefs = new ArrayList<Region>(0); 
		for (Faction f:gd.getFactions()){
			if (f.getMessages()!=null && f.getMessages().size()>0){
				searchThiefsForFaction(f,regionsThiefs);
			}
		}
		
		// Tags setzen
		for (Region r:regionsThiefs){
			setRegionIcon(MAPICON_THIEF,r);
		}
		
		return regionsThiefs.size();
	}
	
	/**
	 * durchsucht die Meldungen einer Faction und ergänzt die Liste der Diebstahl-Regionen
	 * @param f
	 * @param regionsThiefs
	 * @return
	 */
	private void searchThiefsForFaction(Faction f, List<Region> regionsThiefs){
		
		if (f.getMessages()!=null && f.getMessages().size()>0){
			for (Message m : f.getMessages()){
				MagellanMessageImpl msg = (MagellanMessageImpl)m;
				if (msg.getAttributes() != null) {
		            String regionCoordinate = msg.getAttributes().get("region");

		            if (regionCoordinate != null) {
		            	CoordinateID coordinate = CoordinateID.parse(regionCoordinate, ",");

		              if (coordinate == null) {
		                coordinate = CoordinateID.parse(regionCoordinate, " ");
		              }
		              boolean isThiefMessage = false;
		              Region r1 = null;
		              Region r2 = null;
		              if (coordinate!=null){

		          		// MESSAGETYPE 1543395091
		          		// "\"$unit($unit) wurden in $region($region) $int($amount) Silberstücke geklaut.\"";text
		          		if (getId(msg)==1543395091){
		          			isThiefMessage=true;
		          		}
		          		// MESSAGETYPE 771334452
		          		// "\"$unit($unit) verdient$if($eq($mode,4),\" am Handel\",\"\") in $region($region) $int($amount)$if($eq($wanted,$amount),\"\",\" statt $int($wanted)\") Silber$if($eq($mode,1),\" durch Unterhaltung\",$if($eq($mode,2),\" durch Steuern\",$if($eq($mode,3),\" durch Handel\",$if($eq($mode,5),\" durch Diebstahl\",$if($eq($mode,6),\" durch Zauberei\",\"\"))))).\"";text
		          		// wobei mode=5 = durch Diebstahl
		          		if (getId(msg)==771334452){
		          			String value = msg.getAttributes().get("mode");
		          			int MsgMode = Integer.parseInt(value);
		          			if (MsgMode==5){
		          				isThiefMessage=true;
		          			}
		          		}
		          		
		          		
		              } else {
		            	  // keine Region info in der msg
		            	  
		            	  // 1565770951
		          		  // MESSAGETYPE 1565770951
		          		  // "\"$unit($target) ertappte $unit($unit) beim versuchten Diebstahl.\"";text
		          		  // FF: funktioniert hier so nicht, da kein regiontag gesetzt ist.
		          		  if (getId(msg)==1565770951){
		          		     isThiefMessage=true;
		          		     String value = msg.getAttributes().get("unit");
		          			 int unit_ID = Integer.parseInt(value);
		          			 value = msg.getAttributes().get("target");
		          			 int target_ID = Integer.parseInt(value);
		          			 UnitID actID = UnitID.createUnitID(target_ID, 10);
		          			 Unit actUnit = gd.getUnit(actID);
		          			 if (actUnit!=null){
		          				 r1 = actUnit.getRegion();
		          			 }
		          			 actID = UnitID.createUnitID(unit_ID, 10);
		          			 actUnit = gd.getUnit(actID);
		          			 if (actUnit!=null){
		          				 r2 = actUnit.getRegion();
		          			 }
		          		     
		          		  }
		            	  
		              }

	          		  if (isThiefMessage && coordinate!=null){
	          			// yep, dies ist eine Diebstahl Message und wir
	          			// haben regionskoords dafür
	          			Region r = gd.getRegion(coordinate);
	          			if (!(regionsThiefs.contains(r))){
	          				regionsThiefs.add(r);
	          			}
	          		  }
	          		  
	          		  if (isThiefMessage && r1!=null){
	          			// yep, dies ist eine Diebstahl Message und wir
	          			// haben regionskoords dafür
	          			if (!(regionsThiefs.contains(r1))){
	          				regionsThiefs.add(r1);
	          			}
	          		  }
	          		  if (isThiefMessage && r2!=null){
	          			// yep, dies ist eine Diebstahl Message und wir
	          			// haben regionskoords dafür
	          			if (!(regionsThiefs.contains(r2))){
	          				regionsThiefs.add(r2);
	          			}
	          		  }
	          		  
		           }
				}
			}
		}
	}
	
	/**
	 * durchsucht die Meldungen einer Faction und ergänzt die Liste der Special-Event-Regionen
	 * @param f
	 * @param regionsSpecialEvents
	 * @return
	 */
	private void searchSpecialEventsForFaction(Faction f, List<Region> regionsSpecialEvents){
		
		if (f.getMessages()!=null && f.getMessages().size()>0){
			for (Message m : f.getMessages()){
				MagellanMessageImpl msg = (MagellanMessageImpl)m;
				if (msg.getAttributes() != null) {
	            	boolean isSpecialEventMessage = false;   
	            	int unit_ID = 0;
	            	int target_ID = 0;
	          		Region r1 = null;
	          		Region r2 = null;
	          		int mType = getId(msg);
	          		
	            	// MESSAGETYPE 1922066494
	            	// "\"$unit($unit) versuchte erfolglos, $unit($target) in eine andere Welt zu schleudern.\"";text
	            	// "magic";section
	          		if (mType==1922066494){
	          			isSpecialEventMessage=true;
	          			 String value = msg.getAttributes().get("unit");
	          			 unit_ID = Integer.parseInt(value);
	          			 value = msg.getAttributes().get("target");
	          			 target_ID = Integer.parseInt(value);
	          			 UnitID actID = UnitID.createUnitID(target_ID, 10);
	          			 Unit actUnit = gd.getUnit(actID);
	          			 if (actUnit!=null){
	          				 r1 = actUnit.getRegion();
	          			 }
	          			 actID = UnitID.createUnitID(unit_ID, 10);
	          			 actUnit = gd.getUnit(actID);
	          			 if (actUnit!=null){
	          				 r2 = actUnit.getRegion();
	          			 }
	          		}
	          		
	          		if (isSpecialEventMessage && r1!=null){
	          			regionsSpecialEvents.add(r1);
	          		}
	          		if (isSpecialEventMessage && r2!=null){
	          			regionsSpecialEvents.add(r2);
	          		}  
	          		
	          		ArrayList<Integer> validMessageTypeIDs = new ArrayList<Integer>();
	          		
	          		// MESSAGETYPE 861989530
	          		// "\"$unit($unit) in $region($region) kann keine Kräuter finden.\"";text
	          		validMessageTypeIDs.add(861989530);
	          		
	          		// MESSAGETYPE 2094553546
	          		// "\"$unit($unit) in $region($region): '$order($command)' - Ohne einen Handelsposten gibt es keinen Markt.\"";text
	          		validMessageTypeIDs.add(2094553546);
	          		
	          		// MESSAGETYPE 486687258
	          		// "\"$unit($unit) in $region($region): '$order($command)' - Die Region wird von Nichtalliierten bewacht.\"";text
	          		validMessageTypeIDs.add(486687258);
	          		
	          		// MESSAGETYPE 1058871066
	          		// "\"$unit($unit) in $region($region): '$order($command)' - Die Einheit ist mit uns alliiert.\"";text
	          		validMessageTypeIDs.add(1058871066);
	          		
	          		// MESSAGETYPE 735957290
	          		// "\"$unit($unit) wurde in $region($region) von $unit.dative($guard) aufgehalten.\"";text
	          		validMessageTypeIDs.add(735957290);
	          		
	          		// MESSAGETYPE 428515567
	          		// "\"$unit($unit) in $region($region): '$order($command)' - Die Region wird von $unit($guard), einer nichtalliierten Einheit, bewacht.\"";text
	          		validMessageTypeIDs.add(428515567);
	          		
	          		// MESSAGETYPE 1543909816
	          		// "\"$unit($unit) in $region($region): '$order($command)' - Migranten können keine kostenpflichtigen Talente lernen.\"";text
	          		validMessageTypeIDs.add(1543909816);
	          		
	          		// MESSAGETYPE 1060448783
	          		// "\"$unit($unit) entdeckt, dass $region($region) $localize($terrain) ist.\"";text
	          		validMessageTypeIDs.add(1060448783);
	          		
	          		// MESSAGETYPE 187891574
	          		// "\"$unit($unit) in $region($region) rekrutiert $int($amount) von $int($want) Personen.\"";text
	          		validMessageTypeIDs.add(187891574);
	          		
	          		// MESSAGETYPE 1451290990
	          		// "\"Die $ship($ship) entdeckt, dass $region($region) Festland ist.\"";text
	          		validMessageTypeIDs.add(1451290990);
	          		
	          		// MESSAGETYPE 475784769
	          		// "\"Die Mannschaft der $ship($ship) kann in letzter Sekunde verhindern, dass das Schiff in $region($region) auf Land aufläuft.\"";text
	          		validMessageTypeIDs.add(475784769);
	          		
	          		// MESSAGETYPE 212341694
	          		// "\"$unit($unit) ertrinkt in $region($region).\"";text
	          		validMessageTypeIDs.add(212341694);
	          		
	          		if (validMessageTypeIDs.contains(mType)){
	          		
	          		// if (MessageTypeID==861989530 || MessageTypeID==2094553546 || MessageTypeID==486687258){
	          			isSpecialEventMessage=true;
	          			// log.info("found MessageTypeID:" + MessageTypeID );
	          			String regionCoordinate = msg.getAttributes().get("region");
	          			if (regionCoordinate != null) {
	          				CoordinateID coordinate = CoordinateID.parse(regionCoordinate, ",");
	
	                        if (coordinate == null) {
	                          coordinate = CoordinateID.parse(regionCoordinate, " ");
	                        }

	                        if (coordinate !=null) {
	                        	r1 = gd.getRegion(coordinate);
	                        } else {
	                        	log.info("no coordinate found for MessageType " + mType );
	                        }
	                        
	                        if (r1!=null) {
	                        	regionsSpecialEvents.add(r1);
	                        } else {
	                        	log.info("no region found for MessageType " + mType );
	                        }
                        } else {
                        	log.info("no region atrribute found for MessageType " + mType );
                        }
	          		}

				}
			}
		}
	}
	
	
	private int getId(MagellanMessageImpl msg) {
		try {
		  return msg.getMessageType().getID().intValue();
		} catch (Exception e) {
			return -1;
		}
	}


	/**
	 * Durchsucht alle Factions in GameData nach Diebstahlmeldungen
	 * @return
	 */
	private int searchErrors(){
		
		List<Region> regionsErrors = new ArrayList<Region>(0); 
		for (Faction f:gd.getFactions()){
			if (f.getMessages()!=null && f.getMessages().size()>0){
				searchErrorsForFaction(f,regionsErrors);
			}
		}
		// Tags setzen
		for (Region r:regionsErrors){
			setRegionIcon(MAPICON_ERROR,r);
		}
		
		return regionsErrors.size();
	}
	
	/**
	 * durchsucht die Meldungen einer Faction und ergänzt die Liste der Diebstahl-Regionen
	 * @param f
	 * @param regionsErrors
	 * @return
	 */
	private void searchErrorsForFaction(Faction f, List<Region> regionsErrors){
		
		if (f.getMessages()!=null && f.getMessages().size()>0){
			for (Message m : f.getMessages()){
				MagellanMessageImpl msg = (MagellanMessageImpl)m;
				if (msg.getAttributes() != null) {
		            String regionCoordinate = msg.getAttributes().get("region");

		            if (regionCoordinate != null) {
		            	CoordinateID coordinate = CoordinateID.parse(regionCoordinate, ",");

		              if (coordinate == null) {
		                coordinate = CoordinateID.parse(regionCoordinate, " ");
		              }
		              
		              if (coordinate!=null){
		            	boolean isErrorMessage = false;   
		            	
		            	// andere herangehensweise, wir müssen checken
		            	// ob dieeser MsgType in sektion "errors" gehört
		          		magellan.library.rules.MessageType mT = msg.getMessageType();
		          		if (mT != null){
		          			if ("errors".equalsIgnoreCase(mT.getSection())){
		          				isErrorMessage = true;
		          			}
		          		}
		          		if (isErrorMessage){
		          			Region r = gd.getRegion(coordinate);
		          			if (!(regionsErrors.contains(r))){
		          				regionsErrors.add(r);
		          			}
		          		}
		              }
		           }
				}
			}
		}
	}
	
	
	private class SilverRegion{
		public Region region;
		public Long amount;
	}
	
	
	/**
	 * Bestimmt das bekannte Silber in der Region und setzt ggf ein Icon
	 * @return
	 */
	private int searchSilver(){
		
		
		
		ItemType silverItemType = this.gd.getRules().getItemType("Silber", false);
		
		List<SilverRegion> regionsSilver = new ArrayList<SilverRegion>(0); 
		for (Region r:gd.getRegions()){
			searchSilverForRegion(r,regionsSilver,Level1,silverItemType);
		}

		// Tags setzen
		for (SilverRegion SR:regionsSilver){
			int silverlevel = 1;
			if (SR.amount>=Level2){
				silverlevel = 2;
			}
			if (SR.amount>=Level3){
				silverlevel = 3;
			}
			if (SR.amount>=Level4){
				silverlevel = 4;
			}
			if (SR.amount>=Level5){
				silverlevel = 5;
			}
			
			setRegionIcon(MAPICON_SILVER.replace("X", Integer.valueOf(silverlevel).toString()),SR.region);
		}
		
		return regionsSilver.size();
	}
	
	/**
	 * Bestimmt die Menge des Items maximal pro Region Reportweitund  in der Region und setzt ggf ein Icon
	 * @return
	 */
	private int searchItems(){
		
		if (actItemName=="nothing") {
			this.mapIcons_showing_Items=false;
			return 0;
		}
		
		ItemType myType = this.gd.getRules().getItemType(actItemName);
		if (myType==null) {
			log.info("!!! problem - unknown item " + actItemName);
			this.mapIcons_showing_Items=false;
			return 0;
		}
		
		
		// maximalen Bestand in einer Region dieses Items Reportweit ermitteln
		// und speichern
		Long maxAnzahl=(long) 0;
		Map <Region,Long> bestand = new HashMap<Region, Long>();
		for (Region r:this.gd.getRegions()) {
			Long RegionAnzahl=(long)0;
				for (Unit u:r.units()) {
					if (u.getCombatStatus()>=0) {
						Item actItem = u.getItem(myType);
						if (actItem!=null && actItem.getAmount()>0) {
							RegionAnzahl += actItem.getAmount();
						}
					}
			}
			if (RegionAnzahl>maxAnzahl) {
				maxAnzahl = RegionAnzahl;
			}
			if (RegionAnzahl>0) {
				bestand.put(r, RegionAnzahl);
				// Debug
				// log.info("Debug MapIcons searchItems. Marked: " + r.toString() + " with amount=" + RegionAnzahl + " " + actItemName);
			}
		}
		
		log.info("MapIcons-serachItems: maximum is " + maxAnzahl + " " + actItemName);
		this.ItemsMaxAnzahl = maxAnzahl;
		if (maxAnzahl==0) {
			return 0;
		}
		
		double d = maxAnzahl / 10;
		
		for (Region r:bestand.keySet()) {
			Long stufe = (long) Math.ceil(bestand.get(r) / d);
			if (stufe>10) {
				stufe=(long)10;
			}
			// log.info("debug Mapicons: stufen, set " + r.toString() + " (" + bestand.get(r) + " => " + stufe);
			setRegionIcon(MAPICON_ITEMS.replace("X", stufe.toString()),r);
		}
		
		
		
		
		// setRegionIcon(MAPICON_SILVER.replace("X", silverInteger.toString()),SR.region);
		
		
		return bestand.keySet().size();
	}
	
	/**
	 * durchsucht alle Einheiten der Region und summiert das Silber auf
	 * fügt zur Ergebnisliste hinzu, wenn Silberbestand über minimumSilver
	 * @param f
	 * @param regionsErrors
	 * @return
	 */
	private void searchSilverForRegion(Region r, List<SilverRegion> regionsSilver, Long minimumSilver,ItemType silverItemType){
		Long actAmount = 0L;
		if (r.units().size()==0){
			return;
		}
		for(Unit u:r.units()){
			Item silver = u.getModifiedItem(silverItemType);
			if (silver!=null){
				actAmount += silver.getAmount();
			}
		}
		if (actAmount>=minimumSilver){
			SilverRegion SR = new SilverRegion();
			SR.region = r;
			SR.amount = actAmount;
			regionsSilver.add(SR);
		}
	}
	
	/**
	 * Setzt den tag bei der Region, achtet auf Double-Tags
	 * @param iconname
	 * @param r
	 */
	private void setRegionIcon(String iconname, Region r){
		String finalIconName = MapiconsPlugin.myIconPREFIX + iconname;
		if (r==null){
			log.error(getName() + ": error: region is null, iconname: " + iconname);
			return;
		}
		if(r.containsTag(MarkingsImageCellRenderer.ICON_TAG)) {
			StringTokenizer st = new StringTokenizer(r.getTag(MarkingsImageCellRenderer.ICON_TAG), " ");
			while(st.hasMoreTokens()) {
                String token = st.nextToken();
                if (token.equalsIgnoreCase(finalIconName)){
                	// bereits vorhanden
                	return;
                }
			}
		}
		// nicht bereits vorhanden->ergänzen
		String newTag = "";
		if(r.containsTag(MarkingsImageCellRenderer.ICON_TAG)) {
			newTag = r.getTag(MarkingsImageCellRenderer.ICON_TAG).concat(" ");
		} 
		newTag = newTag.concat(finalIconName);
		r.putTag(MarkingsImageCellRenderer.ICON_TAG, newTag);
		// log.info(getName() +  ": put to " + r.getName() + " " + r.getCoordinate().toString() + " tagvalue " + newTag);
	}
	
	
	/**
	 * entfernt "unsere" Regionicon-tags aus GameData
	 */
	private void removeMyRegionIcons(){
		for (Region r:gd.getRegions()){
			removeMyRegionIconsRegion(r);
		}
		
	}
	
	/**
	 * entfernt "unsere" regionicon-tag-einträge aus dem tag
	 * @param r
	 */
	private void removeMyRegionIconsRegion(Region r){
		if(r.containsTag(MarkingsImageCellRenderer.ICON_TAG)) {
			StringBuilder newTag = new StringBuilder();
			StringTokenizer st = new StringTokenizer(r.getTag(MarkingsImageCellRenderer.ICON_TAG), " ");
			while(st.hasMoreTokens()) {
                String token = st.nextToken();
                if (!(token.startsWith(MapiconsPlugin.myIconPREFIX))){
                	if (newTag.length()>0){
                		newTag.append(" ");
                	}
                	newTag.append(token);
                }
			}
			if (newTag.length()>0){
				r.putTag(MarkingsImageCellRenderer.ICON_TAG, newTag.toString());
			} else {
				r.removeTag(MarkingsImageCellRenderer.ICON_TAG);
			}
		}
	}
	
	
	/**
	 * Prüft, ob eine priviligierte Faction zu _f HELFE BEWACHE hat
	 * @param _f zu prüfende Faction
	 * @return true or false
	 */
	public boolean hasHelpGuard(Faction _f) {
		if(gd.getFactions() != null) {
			for(Faction f:gd.getFactions()) {
				if(f.isPrivileged() && (f.getAllies() != null)) { 
					// privileged
					for (Alliance alliance:f.getAllies().values()){
						Faction ally = alliance.getFaction();
						if (ally.equals(_f)){
							if (alliance.getState(16)){
								return true;
							}
						}
					}
				}
			}
		}

		return false;
	}
	
	// TH: Load all enemy factions from a parameter file; 
	//		File name = MIPlugin_Enemies.ini
	//		File format = List of faction IDs
    private void loadEnemyFactions(Client client) throws Exception {
    	try{
            FileInputStream fstream = new FileInputStream(Client.getSettingsDirectory() +"/MIPlugin_Enemies.ini");
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            //Read File Line By Line
            while ((strLine = br.readLine()) != null)   {
              // Read the enemy faction into the list
              // Fiete added simple redundancy check
              if (!enemyFactionList.contains(strLine)){
            	enemyFactionList.add(strLine);
              }
            }
            //Close the input stream
            in.close();
            enemy_faction_list_exists = true;
        }catch (Exception e){//Catch exception if any
             log.info(getName() + " Error: " + e.getMessage());
        }
    }

    // Fiete: Load all enemy factions from orders 
    // order format: // EnemyFaction:abcd
    // not case sensitive, abcd is added
    private void getEnemyFactionsFromOrders(){
    	int cnt = 0;
    	if (gd.getUnits()!=null && gd.getUnits().size()>0){
    		for (Unit u:gd.getUnits()){
    			if (u.getFaction()!=null && u.getFaction().isPrivileged() && u.getOrders2()!=null && u.getOrders2().size()>0){
    				for (Order order:u.getOrders2()){
    					String orderString = order.getText();
    					if (orderString.toUpperCase().startsWith(ENEMY_FACTION_LIST_IDENTIFIER.toUpperCase())){
    						// Treffer...
    						// faction number extrahieren
    						String f_number=orderString.substring(ENEMY_FACTION_LIST_IDENTIFIER.length());
    						if (f_number.length()>0){
    							if (!enemyFactionList.contains(f_number)){
	    							enemyFactionList.add(f_number);
	    							log.info(getName() + " added a enemy Faction from orders of " + u.toString() + ": " + f_number);
	    							cnt++;
	    							enemy_faction_list_exists=true;
    							} else {
    								log.info(getName() + " enemy Faction from orders of " + u.toString() + ": " + f_number +  " -> already on list");
    							}
    						} else {
    							log.info(getName() + " Error while expecting orders of " + u.toString());
    						}
    					}
    				}
    			}
    		}
    	}
    	log.info(getName() + " read " + cnt + " enemy factions from orders");
    }
    
    
    /**
	 * Durchsucht alle Regionen nach Einheiten, für die wir Talente kennen (müssen)
	 * @return Anzahl der Regionen, in denen der grösste Wahrnehmungswert gesetzt wurde.
	 */
	private int searchTalents(){
		int erg = 0;
		// Performance: nur einmal skill suchen
		SkillType skillType = gd.getRules().getSkillType(actTalentName, false);
		for (Region r:gd.getRegions()){
			erg += searchTalentsRegion(r,skillType);
		}
		return erg;
	}

	/**
	 * Durchsucht die übergebene Region nach Einheiten, für der Kampfstatus bekannt ist
	 * (fpür diese haben wir also report-infos, und auch wahrnehumgsinfos..)
	 * sucht dann den höchsten Wahrnehungswert und setzt das Regionicon
	 * @param r = zu durchsuchende Region
	 */
	private int searchTalentsRegion(Region r, SkillType ST){
		int erg = 0;
		int maxTalentValue=0;
		boolean weHaveInfo=false;
		for (Unit u:r.units()){
			if (u.getCombatStatus()>=0){
				weHaveInfo = true;
				Skill actTalentSkill = u.getSkill(ST);
				if (actTalentSkill!=null){
					int actTalentLevel = actTalentSkill.getLevel();
					if (actTalentLevel>maxTalentValue) {
						maxTalentValue=actTalentLevel;
					}
				}
			}
		}
		if (weHaveInfo){
			// Icon setzen
			String name = MAPICON_TALENT;  
			// X ersetzen durch Talentwert
			// Limitiert auf Talent 40
			if (maxTalentValue>40){
				maxTalentValue=40;
			}
			
			name = name.replace("X", Integer.valueOf(maxTalentValue).toString());
			setRegionIcon(name, r);
			erg=1;
		}
		return erg;
	}
	
	/*
	 * puts the known talents into the submenu
	 */
	private void addTalents(JMenu menu){
		if (gd != null){
			if (gd.getRules()!=null){
				menu.removeAll();
				Collection<SkillType> skillTypesColl = gd.getRules().getSkillTypes();
				ArrayList<SkillType> skillTypes = new ArrayList<SkillType>();
				// log.info("MapIcons - found skillTypes: " + skillTypesColl.size());
				skillTypes.addAll(skillTypesColl);
				Collections.sort(skillTypes,new SkillTypeComparator(gd));
				log.info("MapIcons - sorted skilltypes: " + skillTypes.size());
				for (SkillType skillType:skillTypes){
					String name = gd.getTranslation(skillType.getName());
					JMenuItem actItem = new JCheckBoxMenuItem(name);
					actItem.setActionCommand("setNewTalent_" + skillType.getName());
					actItem.addActionListener(this);
					// log.info("MapIcons - sorted skilltypes, found: " + name);
					if (skillType.getName().equalsIgnoreCase(actTalentName)){
						actItem.setSelected(true);
					} else {
						actItem.setSelected(false);
					}
					menu.add(actItem);
				}
			} else {
				log.info("MapIcons: rules not init");
			}
		} else {
			log.info("MapIcons: data not init");
		}
	}
	
	/*
	 * puts the known items into the submenu
	 */
	private void addItems(JMenu menu){
		if (gd != null){
			if (gd.getRules()!=null){
				menu.removeAll();
				
				// nothing - eintrag
				JMenuItem actItem = new JCheckBoxMenuItem(getString("plugin.mapicons.menu.noItems"));
				actItem.setActionCommand("setNewItem_nothing");
				actItem.addActionListener(this);
				// log.info("MapIcons - sorted skilltypes, found: " + name);
				if (actItemName=="nothing"){
					actItem.setSelected(true);
				} else {
					actItem.setSelected(false);
				}
				menu.add(actItem);
				
				long StufenWert = Math.round(this.ItemsMaxAnzahl / 10);
				
				// Farbeinträge
				if (actItemName!="nothing" && this.ItemsMaxAnzahl>0){
					JMenu colorInfo = new JMenu(getString("plugin.mapicons.menu.ItemColors"));
					for (int x=1;x<=10;x++) {
						long StartWert = ((x-1)*StufenWert) + 1;
						if (StartWert<1) {
							StartWert=1;
						}
						long EndWert = x * StufenWert;
						if (EndWert>this.ItemsMaxAnzahl) {
							EndWert = this.ItemsMaxAnzahl;
						}
						if (this.ItemsMaxAnzahl<=10) {
							StartWert = 0;
							EndWert=0;
							if (x==10) {
								StartWert = 1;
								EndWert=this.ItemsMaxAnzahl;
							}
						}
						NumberFormat nF = NumberFormat.getInstance();
						String StartWertS = nF.format(StartWert);
						String EndWertS = nF.format(EndWert);
						JMenuItem actColor = new JMenuItem(StartWertS + "-" + EndWertS);
						if (StartWert==0 && EndWert==0) {
							actColor = new JMenuItem("- - -");
						} 
						// MAPICON_ITEMS = "items_X.gif";
						String fileName = MAPICON_ITEMS.replace('X' + "", x+"");
						String iconName = "etc/images/map/MIplugin_" + fileName;
					    Icon icon = client.getMagellanContext().getImageFactory().loadImage(iconName);
					    if (icon!=null){
					    	actColor.setIcon(icon);
					    }
						colorInfo.add(actColor);
					}
					menu.add(colorInfo);
				}

				
				Collection<ItemType> itemTypesColl = gd.getRules().getItemTypes();
				ArrayList<ItemType> itemTypes = new ArrayList<ItemType>();
				itemTypes.addAll(itemTypesColl);
				Collections.sort(itemTypes,new ItemTypeComparator(gd));
				log.info("MapIcons - sorted itemtypes: " + itemTypes.size());
				
				// Submenus bauen mit den Anfangsbuchstaben....sind einfach zu viele Gegenstände
				
				ArrayList<String> firstLetters = new ArrayList<String>();
				
				for (ItemType itemType:itemTypes){
					String name = gd.getTranslation(itemType.getName());
					String firstLetter = name.substring(0, 1);
					if (!firstLetters.contains(firstLetter)) {
						firstLetters.add(firstLetter);
					}
				}
				
				for (String actFirstLetter:firstLetters) {
					JMenu actMenu = new JMenu(actFirstLetter);
					for (ItemType itemType:itemTypes){
						String name = gd.getTranslation(itemType.getName());
						String firstLetter = name.substring(0, 1);
						if (firstLetter.equalsIgnoreCase(actFirstLetter)) {
							actItem = new JCheckBoxMenuItem(name);
							actItem.setActionCommand("setNewItem_" + itemType.getName());
							actItem.addActionListener(this);
							// log.info("MapIcons - sorted skilltypes, found: " + name);
							if (itemType.getName().equalsIgnoreCase(actItemName)){
								actItem.setSelected(true);
							} else {
								actItem.setSelected(false);
							}
							actMenu.add(actItem);
						}
					}
					menu.add(actMenu);
				}
			} else {
				log.info("MapIcons: rules not init");
			}
		} else {
			log.info("MapIcons: data not init");
		}
	}
	
	
	/*
	 * puts the known talents into the submenu
	 */
	private void addSilverLevel(JMenu menu){
		// aus den Settings // Properties lesen
		
		menu.removeAll();
		
		Properties P = client.getProperties();
		Level1 = Long.parseLong(P.getProperty(silverLevel1PropertyName, Level1.toString()));
		Level2 = Long.parseLong(P.getProperty(silverLevel2PropertyName, Level2.toString()));
		Level3 = Long.parseLong(P.getProperty(silverLevel3PropertyName, Level3.toString()));
		Level4 = Long.parseLong(P.getProperty(silverLevel4PropertyName, Level4.toString()));
		Level5 = Long.parseLong(P.getProperty(silverLevel5PropertyName, Level5.toString()));
		String name = "";
		name = "1: " + NumberFormat.getInstance().format(Level1);
		JMenuItem actItem = new JCheckBoxMenuItem(name);
		actItem.setActionCommand("newSilverLevel1");
		String iconName = "etc/images/map/MIplugin_silveramount_1.gif";
	    Icon icon = client.getMagellanContext().getImageFactory().loadImage(iconName);
	    if (icon!=null){
	    	actItem.setIcon(icon);
	    }
		actItem.addActionListener(this);
		menu.add(actItem);
		
		name = "2: " + NumberFormat.getInstance().format(Level2);
		actItem = new JCheckBoxMenuItem(name);
		actItem.setActionCommand("newSilverLevel2");
		iconName = "etc/images/map/MIplugin_silveramount_2.gif";
	    icon = client.getMagellanContext().getImageFactory().loadImage(iconName);
	    if (icon!=null){
	    	actItem.setIcon(icon);
	    }
		actItem.addActionListener(this);
		menu.add(actItem);
		
		name = "3: " + NumberFormat.getInstance().format(Level3);
		actItem = new JCheckBoxMenuItem(name);
		actItem.setActionCommand("newSilverLevel3");
		iconName = "etc/images/map/MIplugin_silveramount_3.gif";
	    icon = client.getMagellanContext().getImageFactory().loadImage(iconName);
	    if (icon!=null){
	    	actItem.setIcon(icon);
	    }
		actItem.addActionListener(this);
		menu.add(actItem);
		
		name = "4: " + NumberFormat.getInstance().format(Level4);
		actItem = new JCheckBoxMenuItem(name);
		actItem.setActionCommand("newSilverLevel4");
		iconName = "etc/images/map/MIplugin_silveramount_4.gif";
	    icon = client.getMagellanContext().getImageFactory().loadImage(iconName);
	    if (icon!=null){
	    	actItem.setIcon(icon);
	    }
		actItem.addActionListener(this);
		menu.add(actItem);
		
		name = "5: " + NumberFormat.getInstance().format(Level5);
		actItem = new JCheckBoxMenuItem(name);
		actItem.setActionCommand("newSilverLevel5");
		iconName = "etc/images/map/MIplugin_silveramount_5.gif";
	    icon = client.getMagellanContext().getImageFactory().loadImage(iconName);
	    if (icon!=null){
	    	actItem.setIcon(icon);
	    }
		actItem.addActionListener(this);
		menu.add(actItem);

	}
	
	
	/*
	 * neuer Talentname ist gesetzt - Umsetzung
	 */
	private void setNewTalent(){
		// Kurzerhand alle submenus entfernen und neu anlegen
		log.info("Mapicons - changing talent name to: " + actTalentName);
		talentMenu.removeAll();
		addTalents(talentMenu);
		// Auf der Karte neu erzeugen
		mapIcons_showing_Talents = false;
		toogleShowTalents();
		// Menueintrag
		showTalentsMenu.setSelected(true);
	}
	
	/*
	 * neuer Itemname ist gesetzt - Umsetzung
	 */
	private void setNewItem(){
		// Kurzerhand alle submenus entfernen und neu anlegen
		log.info("Mapicons - changing item name to: " + actItemName);
		if (actItemName.equalsIgnoreCase("nothing")) {
			this.ItemsMaxAnzahl=0;
		}
		itemsMenu.removeAll();
		toogleShowItems();
		addItems(itemsMenu);
	}
	
	
	/**
     * 
     * a small comparator to compare translated skillNames
     *
     * @author ...
     * @version 1.0, 20.11.2007
     */  
    private class SkillTypeComparator implements Comparator<SkillType> {
      
      // Reference to Translations
      private GameData data=null;
      
      /**
       * constructs new Comparator
       * @param _data
       */
      public SkillTypeComparator(GameData _data){
        this.data = _data;
      }
      
      public int compare(SkillType o1,SkillType o2){
        String s1 = data.getTranslation(o1.getName());
        String s2 = data.getTranslation(o2.getName());
        return s1.compareToIgnoreCase(s2);
      }
    }
    
    /**
     * 
     * a small comparator to compare translated itemNames
     *
     * @author Fiete
     * @version 1.4, 12.02.2019
     */  
    private class ItemTypeComparator implements Comparator<ItemType> {
      
      // Reference to Translations
      private GameData data=null;
      
      /**
       * constructs new Comparator
       * @param _data
       */
      public ItemTypeComparator(GameData _data){
        this.data = _data;
      }
      
      public int compare(ItemType o1,ItemType o2){
        String s1 = data.getTranslation(o1.getName());
        String s2 = data.getTranslation(o2.getName());
        return s1.compareToIgnoreCase(s2);
      }
    }
	
	
}

