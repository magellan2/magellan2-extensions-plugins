package magellan.plugin.mapcleaner;

import magellan.client.Client;

/**
 * Interface for all Actions within MapCleaner
 * @author x
 *
 */

public interface MapCleanAction {
	
	/**
	 * The Name to display in MenuList
	 * @return
	 */
	public String getName();
	
	/**
	 * Action to be called if MenuEntry is selected
	 * @param client
	 */
	public void clean(Client client);
	
}
