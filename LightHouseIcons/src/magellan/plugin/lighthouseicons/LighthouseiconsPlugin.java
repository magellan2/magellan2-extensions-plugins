package magellan.plugin.lighthouseicons;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
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
import magellan.library.Building;
import magellan.library.CoordinateID;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.Unit;
import magellan.library.Region.Visibility;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.rules.BuildingType;
import magellan.library.rules.RegionType;
import magellan.library.utils.Regions;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;


public class LighthouseiconsPlugin implements MagellanPlugIn, ActionListener {

	public static final String version="0.1";
	
	private Client client = null;
	
	private GameData gd = null;
	
	private static Logger log = null;
	
	private static final String myIconPREFIX = "LHIplugin_";
	
	private static final String MAPICON_LIGHTHOUSE = "lh.gif";
	private static final String MAPICON_LIGHTHOUSERANGE = "lh_range.gif";
	private static final String MAPICON_LIGHTHOUSERANGE_OTHER = "lh_range_other.gif";

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
					new MsgBox(client,getString("plugin.lighthouseicons.menu.showInfoText") + " " + LighthouseiconsPlugin.version,getString("plugin.lighthouseicons.menu.showInfo"),false);
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
		
		
		
		JMenuItem showInfo = new JMenuItem(getString("plugin.lighthouseicons.menu.showInfo"));
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
		return getString("plugin.lighthouseicons.name");
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
		
		Resources.getInstance().initialize(Client.getMagellanDirectory(), "lighthouseiconsplugin_");

		// initProperties();

		log = Logger.getInstance(LighthouseiconsPlugin.class);

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
		
		// einmal cleanen
		removeMyRegionIcons();
		
		Collection<Region> alreadyVisibleRegions = new ArrayList<Region>();
		// alle regionen mit Visibility tag LIGHTHOUSE...
		log.info(getName() +  " added " + this.setLightHouseVisibility(alreadyVisibleRegions) + " lighthouse icons");
		
		// Reichweite aller Leuchttürme
		log.info(getName() +  " marked " + this.setLightHouseRange(alreadyVisibleRegions) + " regions as in lighthouse-range");
	}
	
	/**
	 * setzt in allen Region mit visibility = Lighthouse unser regionicon
	 * @return
	 */
	private int setLightHouseVisibility(Collection<Region> alreadyVisibleRegions){
		int counter = 0;
		for (Region r:this.gd.regions().values()){
			if (r.getVisibility().equals(Visibility.LIGHTHOUSE)){
				int i = setRegionIcon(MAPICON_LIGHTHOUSE, r);
				if (i==1){
					counter+=i;
					alreadyVisibleRegions.add(r);
				}
			}
		}
		return counter;
	}
	
	/**
	 * setzt in den Regionen, die ein Leuchtturm theoretisch sehen könnte, unser icon
	 * @return
	 */
	private int setLightHouseRange(Collection<Region> alreadyVisibleRegions){
		int counter = 0;
		BuildingType type = gd.rules.getBuildingType(EresseaConstants.B_LIGHTHOUSE);
		RegionType oceanType = gd.rules.getRegionType(EresseaConstants.RT_OCEAN);
		if (type==null){
			log.error(getName() + ": this game has no lighthouse-buildings!");
			return 0;
		}
		if (oceanType==null) {
			log.error(getName() + ": this game has no ocean-regiontype!");
			return 0;
		}
		for (Building b:gd.buildings().values()){
			if (type.equals(b.getType()) && (b.getSize() >= 10)){
				
				// find out, if it could be a friendly lighthouse...
				boolean isFriendlyLightHouse = false;
				for (Unit u:b.units()){
					if (u.isWeightWellKnown()){
						isFriendlyLightHouse=true;
						break;
					}
				}
				
				String iconName = MAPICON_LIGHTHOUSERANGE;
				if (!isFriendlyLightHouse){
					iconName = MAPICON_LIGHTHOUSERANGE_OTHER;
				}
				
				int maxRadius = (int) Math.log10(b.getSize()) + 1;
				if (maxRadius>0){
					Map<CoordinateID,Region> regions = Regions.getAllNeighbours(gd.regions(),
							   b.getRegion().getCoordinate(),
							   maxRadius, null);
					for(Region r: regions.values()){
						if(oceanType.equals(r.getType())) {
							// nicht setzen, wenn friendly und schon visible
							if (!(isFriendlyLightHouse && alreadyVisibleRegions.contains(r))){
								counter+=setRegionIcon(iconName, r);
							}
						}
					}
				}
			}
		}

		return counter;
	}
	
	
		
	
	/**
	 * Setzt den tag bei der Region, achtet auf Double-Tags
	 * @param iconname
	 * @param r
	 */
	private int setRegionIcon(String iconname, Region r){
		String finalIconName = LighthouseiconsPlugin.myIconPREFIX + iconname;
		if(r.containsTag(MarkingsImageCellRenderer.ICON_TAG)) {
			StringTokenizer st = new StringTokenizer(r.getTag(MarkingsImageCellRenderer.ICON_TAG), " ");
			while(st.hasMoreTokens()) {
                String token = st.nextToken();
                if (token.equalsIgnoreCase(finalIconName)){
                	// bereits vorhanden
                	return 0;
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
		return 1;
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
                if (!(token.startsWith(myIconPREFIX))){
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
