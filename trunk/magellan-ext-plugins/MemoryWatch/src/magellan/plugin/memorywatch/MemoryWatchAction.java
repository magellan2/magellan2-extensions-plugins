package magellan.plugin.memorywatch;

import magellan.client.Client;

/**
 * Interface for all Actions within MemoryWatch
 * @author x
 *
 */

public interface MemoryWatchAction {
	
	/**
	 * The Name to display in MenuList
	 * @return
	 */
	public String getName();
	
	/**
	 * Action to be called if MenuEntry is selected
	 * @param client
	 */
	public void activate(Client client);
	
}
