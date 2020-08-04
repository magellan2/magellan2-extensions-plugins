package magellan.plugin.mapcleaner;

import java.util.Iterator;

import magellan.plugin.mapcleaner.MsgBox;

import magellan.client.Client;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.event.GameDataEvent;
import magellan.library.utils.Regions;

public class DelNamedOceans implements MapCleanAction{

	/**
	 * The Name to display in MenuList
	 * @return
	 */
	public String getName(){
		return "Delete named oceans";
	}
	
	/**
	 * Action to be called if MenuEntry is selected
	 * @param client
	 */
	public void clean(Client client){
		GameData data = client.getData();
		int counter=0;
		if (data!=null && data.getRegions()!=null && data.getRules()!=null){
			for (Iterator<Region> iter = data.getRegions().iterator();iter.hasNext();){
				Region actRegion = (Region)iter.next();
				if (actRegion!=null && actRegion.getRegionType()!=null && Regions.getOceanRegionTypes(data.getRules()).get(actRegion.getRegionType().getID())!=null && actRegion.getName()!=null){
					actRegion.setName(null);
					counter++;
				}
			}
			client.getDispatcher().fire(new GameDataEvent(this, data, true));
		}
		new MsgBox(client,"Done with " + counter + " regions.","OK",false);
	}
	
}
