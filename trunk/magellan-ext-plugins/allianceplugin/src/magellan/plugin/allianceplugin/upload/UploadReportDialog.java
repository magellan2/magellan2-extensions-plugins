package magellan.plugin.allianceplugin.upload;

import javax.swing.JDialog;

import magellan.client.Client;

/**
 * This dialog shows a progress bar that uploads a report
 * to the server.
 * 
 * @author Thoralf Rickert
 * @version 1.0
 */
public class UploadReportDialog extends JDialog {
  public UploadReportDialog(Client client) {
    super(client);
  }

}
