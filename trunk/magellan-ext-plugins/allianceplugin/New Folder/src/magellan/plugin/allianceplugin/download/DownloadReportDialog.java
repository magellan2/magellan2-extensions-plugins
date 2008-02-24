package magellan.plugin.allianceplugin.download;

import javax.swing.JDialog;

import magellan.client.Client;

/**
 * This dialog shows a progress bar that downloads the last report
 * from the server.
 * 
 * @author Thoralf Rickert
 * @version 1.0
 */
public class DownloadReportDialog extends JDialog {
  public DownloadReportDialog(Client client) {
    super(client);
  }
}
