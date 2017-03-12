package magellan.plugin.mapcleaner;

import java.util.ArrayList;
import java.util.Iterator;

import magellan.client.Client;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.RegionResource;
import magellan.library.event.GameDataEvent;

public class RemoveWrongRessources implements MapCleanAction{

	/**
	 * The Name to display in MenuList
	 * @return
	 */
	public String getName(){
		return "Remove: Baum, Stein, Bauer, Pferd";
	}
	
	/**
	 * Action to be called if MenuEntry is selected
	 * @param client
	 */
	public void clean(Client client){
		GameData data = client.getData();
		int counter=0;
		ArrayList<RegionResource> delRes = new ArrayList<RegionResource>();
		if (data!=null && data.getRegions()!=null && data.getRules()!=null){
			for (Iterator<Region> iter = data.getRegions().iterator();iter.hasNext();){
				Region actRegion = (Region)iter.next();
				delRes.clear();
				for (Iterator<RegionResource> iter2 = actRegion.resources().iterator();iter2.hasNext();){
					RegionResource actRegRes = (RegionResource)iter2.next();
					if (actRegRes.getName().equalsIgnoreCase("Baum")){
						delRes.add(actRegRes);
					}
					if (actRegRes.getName().equalsIgnoreCase("Stein")){
						delRes.add(actRegRes);
					}
					if (actRegRes.getName().equalsIgnoreCase("Bauer")){
						delRes.add(actRegRes);
					}
					if (actRegRes.getName().equalsIgnoreCase("Pferd")){
						delRes.add(actRegRes);
					}
					if (actRegRes.getName().equalsIgnoreCase("Sch??linge")){
						delRes.add(actRegRes);
					}
					if (actRegRes.getName().equalsIgnoreCase("B?ume")){
						delRes.add(actRegRes);
					}
				}
				if (delRes.size()>0){
					for (Iterator<RegionResource> iter2 = delRes.iterator();iter2.hasNext();){
						RegionResource actRegRes = (RegionResource)iter2.next();
						actRegion.removeResource(actRegRes);
					}
					counter++;
				}
			}
			if (counter>0){
				client.getDispatcher().fire(new GameDataEvent(this, data, true));
			}
		}
		new MsgBox(client,"Done with " + counter + " regions.","OK",false);
	}
	
}
