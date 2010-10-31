package magellan.plugin.mapicons;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import magellan.client.Client;
import magellan.client.extern.MagellanPlugIn;
import magellan.client.swing.map.MarkingsImageCellRenderer;
import magellan.client.swing.preferences.PreferencesFactory;
import magellan.library.Battle;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.Unit;
import magellan.library.event.GameDataEvent;
import magellan.library.rules.Race;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;


public class MapiconsPlugin implements MagellanPlugIn, ActionListener {
		
	
	public static final String version="0.2";
	
	private Client client = null;
	private Properties properties = null;
	private GameData gd = null;
	
	private static Logger log = null;
	
	private static final String myIconPREFIX = "MIplugin_";
	
	private static final String MAPICON_BATTLE = "battle.gif";
	private static final String MAPICON_MONSTER = "monster.gif";
	
	private static final String MONSTER_FACTION = "ii";
	
	final private static List<String> monsterTypeList = new ArrayList<String>() {
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
            add("Zombies");
        }
    };

	
	
	
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
					client.getDispatcher().fire(new GameDataEvent(client, client.getData()));
				}
			}).start();
		}
		
		// recreateAllIcons
		if (e.getActionCommand().equalsIgnoreCase("recreateAllIcons")){
			new Thread(new Runnable() {
				public void run() {
					processGameData();
					client.getDispatcher().fire(new GameDataEvent(client, client.getData()));
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
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see magellan.client.extern.MagellanPlugIn#init(magellan.client.Client, java.util.Properties)
	 */
	public void init(Client _client, Properties _properties) {
		client = _client;
		properties = _properties;
		Resources.getInstance().initialize(Client.getMagellanDirectory(), "mapiconsplugin_");

		// initProperties();

		log = Logger.getInstance(MapiconsPlugin.class);
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
		// TODO Auto-generated method stub
		
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
		// die einzelnen Bereiche aufrufen...
		int regionsWithBattles = this.searchBattles();
		log.info(getName() +  " found " + regionsWithBattles + " battle-regions");
		
		
		int regionsWithMonsters = this.searchMonsters(); 
		log.info(getName() +  " found " + regionsWithMonsters + " regions with monsters");
		
		
	}
	
	/**
	 * Durchsucht alle Factions in GameData nach Battles
	 * @return
	 */
	private int searchBattles(){
		
		List<Region> regionsBattles = new ArrayList<Region>(0); 
		for (Faction f:gd.factions().values()){
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
				if (!regionsBattles.contains(r)){
					regionsBattles.add(r);
					// debug
					// log.info(getName() +  ": found " + r.getName() + " as battle-region");
				}
			}
		}
	}
	
	/**
	 * durchsucht alle regionen nach Monstern
	 */
	private int searchMonsters(){
		int erg = 0;
		for (Region r:gd.regions().values()){
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
				setRegionIcon(MAPICON_MONSTER,r);
				return 1;
			}
			if (u.isHideFaction()){
				Race race = u.getRace();
				if (monsterTypeList.contains(race.getName())){
					setRegionIcon(MAPICON_MONSTER,r);
					return 1;
				}
			}
		}
		return erg;
	}
	
	
	
	
	
	
	
	
	
	
	/**
	 * Setzt den tag bei der Region, achtet auf Double-Tags
	 * @param iconname
	 * @param r
	 */
	private void setRegionIcon(String iconname, Region r){
		String finalIconName = MapiconsPlugin.myIconPREFIX + iconname;
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
		for (Region r:gd.regions().values()){
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
	
	
}
