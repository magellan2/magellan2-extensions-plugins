package magellan.plugin.mapicons;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.text.MessageFormat;
import java.util.ArrayList;
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
import magellan.library.Battle;
import magellan.library.CoordinateID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Message;
import magellan.library.Region;
import magellan.library.Unit;
import magellan.library.event.GameDataEvent;
import magellan.library.impl.MagellanMessageImpl;
import magellan.library.rules.Race;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;


public class MapiconsPlugin implements MagellanPlugIn, ActionListener,ShortcutListener {
		
	
	

	public static final String version="0.3";
	
	private Client client = null;
	
	private GameData gd = null;
	
	private static Logger log = null;
	
	private static final String myIconPREFIX = "MIplugin_";
	
	private static final String MAPICON_BATTLE = "battle.gif";
	private static final String MAPICON_MONSTER = "monster.gif";
	private static final String MAPICON_HUNGER = "hunger.gif";
	private static final String MAPICON_SPECIALEVENT = "specialevents.gif";
	private static final String MAPICON_THIEF = "dieb.gif";
	
	private static final String MONSTER_FACTION = "ii";
	
	private boolean mapIcons_showing_all = true;
	
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
        }
    };

	
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
	    }
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
		
		Resources.getInstance().initialize(Client.getMagellanDirectory(), "mapiconsplugin_");

		// initProperties();

		log = Logger.getInstance(MapiconsPlugin.class);
		
		initShortcuts();
		
		log.info(getName() + " initialized (Client)...");
		
	}
	
	
	/**
	 * init the shortcuts
	 */
	private void initShortcuts(){
		shortcuts = new ArrayList<KeyStroke>();
		// 0: toggle Map Icons
	    shortcuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.ALT_MASK));
	    
	    DesktopEnvironment.registerShortcutListener(this);
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
		// die einzelnen Bereiche aufrufen..
		log.info(getName() +  " found " + this.searchBattles() + " battle-regions");
		log.info(getName() +  " found " + this.searchMonsters() + " regions with monsters");
		log.info(getName() +  " found " + this.searchHunger() + " regions with hunger");
		log.info(getName() +  " found " + this.searchSpecialEvents() + " regions with special events");
		log.info(getName() +  " found " + this.searchThiefs() + " regions with thief-events");
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
	 * Durchsucht alle Factions in GameData nach Hunger
	 * @return
	 */
	private int searchHunger(){
		
		List<Region> regionsHunger = new ArrayList<Region>(0); 
		for (Faction f:gd.factions().values()){
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
		          		if (msg.getMessageType().getID().intValue()==1158830147){
		          			isHungerMessage=true;
		          		}
		          		
		          		// Schwächung 829394366
		          		if (msg.getMessageType().getID().intValue()==829394366){
		          			isHungerMessage=true;
		          		}
		          		
		          		if (isHungerMessage){
		          			// yep, dies ist eine Hunger Message und wir
		          			// haben regionskoords dafür
		          			Region r = gd.getRegion(coordinate);
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
	
	
	
	/**
	 * Durchsucht alle regions in GameData nach besonderen Events
	 * @return
	 */
	private int searchSpecialEvents(){
		
		List<Region> regionsSpecialEvents = new ArrayList<Region>(0); 
		for (Region r:gd.regions().values()){
			if (r.getMessages()!=null && r.getMessages().size()>0){
				searchSpecialEventsForRegion(r,regionsSpecialEvents);
			}
		}
		// Tags setzen
		for (Region r:regionsSpecialEvents){
			setRegionIcon(MAPICON_SPECIALEVENT,r);
		}
		
		return regionsSpecialEvents.size();
	}
	
	/**
	 * durchsucht die Battles einer Faction und ergänzt die Liste der Regionen
	 * @param f
	 * @param regionsSpecialEvents
	 * @return
	 */
	private void searchSpecialEventsForRegion(Region r, List<Region> regionsSpecialEvents){
		
		if (r.getMessages()!=null && r.getMessages().size()>0){
			for (Message m : r.getMessages()){
				MagellanMessageImpl msg = (MagellanMessageImpl)m;
				if (msg.getAttributes() != null) {
		            
	            	 boolean isSpecialEventMessage = false;   
	            	// MESSAGETYPE 919533243
	            	//  "\"$unit($unit) erscheint plötzlich.\"";text
	          		if (msg.getMessageType().getID().intValue()==919533243){
	          			isSpecialEventMessage=true;
	          		}
	          		
	          		// MESSAGETYPE 2037322195
	          		// "\"$unit($unit) wird durchscheinend und verschwindet.\"";text
	          		if (msg.getMessageType().getID().intValue()==2037322195){
	          			isSpecialEventMessage=true;
	          		}
	          		
	          		// MESSAGETYPE 6037032
	          		// "\"Hier wütete die Pest, und $int($dead) Bauern starben.\"";text
	          		if (msg.getMessageType().getID().intValue()==6037032){
	          			isSpecialEventMessage=true;
	          		}
	          		
	          		// MESSAGETYPE 729133963
	          		// "\"$unit($unit) in $region($region): $int($number) $race($race,$number) $if($eq($number,1),\"verschwand\", \"verschwanden\") über Nacht.\"";text
	          		if (msg.getMessageType().getID().intValue()==729133963){
	          			isSpecialEventMessage=true;
	          		}
	          		
	          		// MESSAGETYPE 894791686
	          		// "\"Der Eisberg $region($region) schmilzt.\"";text
	          		if (msg.getMessageType().getID().intValue()==894791686){
	          			isSpecialEventMessage=true;
	          		}
	          		
	          		
	          		// MESSAGETYPE 1897415733
	          		// "\"Das Wurmloch in $region($region) schließt sich.\"";text
	          		if (msg.getMessageType().getID().intValue()==1897415733){
	          			isSpecialEventMessage=true;
	          		}
	          		
	          		// MESSAGETYPE 744775869
	          		// "\"Eine gewaltige Flutwelle verschlingt $region($region) und alle Bewohner.\"";text
	          		if (msg.getMessageType().getID().intValue()==744775869){
	          			isSpecialEventMessage=true;
	          		}
	          		
	          		// MESSAGETYPE 1651515350
	          		// "\"$unit($unit) reist durch ein Wurmloch nach $region($region).\"";text
	          		if (msg.getMessageType().getID().intValue()==1651515350){
	          			isSpecialEventMessage=true;
	          		}
	          		
	          		// MESSAGETYPE 1687287742
	          		// "\"In $region($region) erscheint ein Wurmloch.\"";text
	          		if (msg.getMessageType().getID().intValue()==1687287742){
	          			isSpecialEventMessage=true;
	          		}
	          		
	          		// MESSAGETYPE 1176996908
	          		// "\"Ein Wirbel aus blendendem Licht erscheint.\"";text
	          		if (msg.getMessageType().getID().intValue()==1176996908){
	          			isSpecialEventMessage=true;
	          		}
	          		
	          		// MESSAGETYPE 194649789
	          		// "\"$unit($unit) belagert $building($building).\"";text
	          		if (msg.getMessageType().getID().intValue()==194649789){
	          			isSpecialEventMessage=true;
	          		}
	          		
	          		// MESSAGETYPE 248430228
	          		// "\"$unit($target) wird von $unit($unit) in eine andere Welt geschleudert.\"";text
	          		if (msg.getMessageType().getID().intValue()==248430228){
	          			isSpecialEventMessage=true;
	          		}
	          		
	          		// MESSAGETYPE 1057880810
	          		// "\"$unit($target) fühlt sich $if($isnull($spy),\"\",\"durch $unit($spy) \")beobachtet.\"";text
	          		if (msg.getMessageType().getID().intValue()==1057880810){
	          			isSpecialEventMessage=true;
	          		}
	          		
	          		// MESSAGETYPE 198782656
	          		// "\"Ein Bauernmob erhebt sich und macht Jagd auf Schwarzmagier.\"";text
	          		if (msg.getMessageType().getID().intValue()==198782656){
	          			isSpecialEventMessage=true;
	          		}
	          		
	          		// MESSAGETYPE 488105396
	          		// "\"$unit($mage) ruft ein fürchterliches Unwetter über seine Feinde. Der magischen Regen lässt alles Eisen rosten.\"";text
	          		if (msg.getMessageType().getID().intValue()==488105396){
	          			isSpecialEventMessage=true;
	          		}
	          		
	          		// MESSAGETYPE 1378226579
	          		// "\"$unit($unit) reißt einen Teil von $building($building) ein.\"";text
	          		if (msg.getMessageType().getID().intValue()==1378226579){
	          			isSpecialEventMessage=true;
	          		}
	          		
	          		// MESSAGETYPE 212341694
	          		// "\"$unit($unit) ertrinkt in $region($region).\"";text
	          		if (msg.getMessageType().getID().intValue()==212341694){
	          			isSpecialEventMessage=true;
	          		}
	          		
	          		if (isSpecialEventMessage){
	          			// yep, dies ist eine Hunger Message und wir
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
		for (Faction f:gd.factions().values()){
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
		              
		              if (coordinate!=null){
		            	  boolean isThiefMessage = false;   
		            	
		          		
		          		// MESSAGETYPE 1543395091
		          		// "\"$unit($unit) wurden in $region($region) $int($amount) Silberstücke geklaut.\"";text
		          		if (msg.getMessageType().getID().intValue()==1543395091){
		          			isThiefMessage=true;
		          		}
		          		
		          		// 1565770951
		          		// MESSAGETYPE 1565770951
		          		// "\"$unit($target) ertappte $unit($unit) beim versuchten Diebstahl.\"";text
		          		if (msg.getMessageType().getID().intValue()==1565770951){
		          			isThiefMessage=true;
		          		}
		          		
		          		
		          		if (isThiefMessage){
		          			// yep, dies ist eine Diebstahl Message und wir
		          			// haben regionskoords dafür
		          			Region r = gd.getRegion(coordinate);
		          			if (!(regionsThiefs.contains(r))){
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
