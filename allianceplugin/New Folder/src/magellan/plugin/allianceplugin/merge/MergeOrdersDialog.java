package magellan.plugin.allianceplugin.merge;

import javax.swing.JDialog;

import magellan.client.Client;


/**
 * This dialog shows a progress bar that merges the orders
 * from the client with the server.
 * 
 * @author Thoralf Rickert
 * @version 1.0
 */
public class MergeOrdersDialog extends JDialog {
  public MergeOrdersDialog(Client client) {
    super(client);
  }

}
