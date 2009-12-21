package magellan.plugin.mapcleaner;

import java.util.ArrayList;
import java.util.Iterator;

import magellan.client.Client;
import magellan.library.GameData;
import magellan.library.ID;
import magellan.library.Island;
import magellan.library.Region;
import magellan.library.event.GameDataEvent;
import magellan.library.utils.Regions;

public class RemoveUnusedIslands implements MapCleanAction{

	/**
	 * The Name to display in MenuList
	 * @return
	 */
	public String getName(){
		return "Remove unsued islands";
	}
	
	/**
	 * Action to be called if MenuEntry is selected
	 * @param client
	 */
	public void clean(Client client){
		GameData data = client.getData();
		// List of islandKeys to delete
		ArrayList<ID> islandKeys = new ArrayList<ID>();
		int counter=0;
		if (data!=null ){
			// Feuerwand-Regionen -> keine Inseln
			for (Iterator<Region> iter = data.regions().values().iterator();iter.hasNext();){
				Region actRegion = (Region)iter.next();
				if (actRegion!=null && actRegion.getRegionType()!=null && actRegion.getRegionType().equals(Regions.getFeuerwandRegionType(data.rules, data)) && actRegion.getIsland()!=null){
					actRegion.setIsland(null);
					counter++;
				}
			}
			if (counter>0){
				client.getDispatcher().fire(new GameDataEvent(this, data, true));
				new MsgBox(client,"Removed islands from " + counter + " Firewall-regions.","OK",false);
			}
			
			
			for (Iterator<Island> iter = data.islands().values().iterator();iter.hasNext();){
				Island actIsland = (Island)iter.next();
				if (actIsland.regions()==null || actIsland.regions().size()==0){
					islandKeys.add(actIsland.getID());
				}
			}
			if (islandKeys.size()>0){
				for (Iterator<ID> iter = islandKeys.iterator();iter.hasNext();){
					ID actID = (ID)iter.next();
					data.islands().remove(actID);
				}
				client.getDispatcher().fire(new GameDataEvent(this, data, true));
			}
			new MsgBox(client,"Removed " + islandKeys.size() + " Islands.","OK",false);

		} else {
			new MsgBox(client,"No data loaded.","OK",false);
		}
	}
	
}
