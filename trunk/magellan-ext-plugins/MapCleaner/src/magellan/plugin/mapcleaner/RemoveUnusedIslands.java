package magellan.plugin.mapcleaner;

import java.util.ArrayList;
import java.util.Iterator;

import magellan.client.Client;
import magellan.library.GameData;
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
		ArrayList<Island> islands = new ArrayList<Island>();
		int counter=0;
		if (data!=null ){
			// Feuerwand-Regionen -> keine Inseln
			for (Iterator<Region> iter = data.getRegions().iterator();iter.hasNext();){
				Region actRegion = (Region)iter.next();
				if (actRegion!=null && actRegion.getRegionType()!=null && actRegion.getRegionType().equals(Regions.getFeuerwandRegionType(data)) && actRegion.getIsland()!=null){
					actRegion.setIsland(null);
					counter++;
				}
			}
			if (counter>0){
				client.getDispatcher().fire(new GameDataEvent(this, data, true));
				new MsgBox(client,"Removed islands from " + counter + " Firewall-regions.","OK",false);
			}
			
			counter=0;
			for (Iterator<Island> iter = data.getIslands().iterator();iter.hasNext();){
				Island actIsland = (Island)iter.next();
				if (actIsland.regions()==null || actIsland.regions().size()==0){
					islands.add(actIsland);
				}
				counter++;
			}
			new MsgBox(client,"Checked " + counter + " Islands. " + islands.size() + " islands are empty","OK",false);
			if (islands.size()>0){
				for (Island I:islands){
					// data.getIslands().remove(I);
					data.removeIsland(I.getID());
				}
				client.getDispatcher().fire(new GameDataEvent(this, data, true));
			}
			new MsgBox(client,"Removed " + islands.size() + " Islands.","OK",false);

		} else {
			new MsgBox(client,"No data loaded.","OK",false);
		}
	}
	
}
