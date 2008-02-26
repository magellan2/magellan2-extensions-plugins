package magellan.plugin.allianceplugin.upload;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import magellan.client.Client;
import magellan.library.utils.Resources;
import magellan.plugin.allianceplugin.AbstractOdysseyConnectDialog;
import magellan.plugin.allianceplugin.Constants;
import magellan.plugin.allianceplugin.net.OdysseyServerInformation;

/**
 * This dialog shows a progress bar that uploads a report
 * to the server.
 * 
 * @author Thoralf Rickert
 * @version 1.0
 */
public class UploadReportDialog extends AbstractOdysseyConnectDialog {
  public UploadReportDialog(Client client) {
    super(client);
  }

  /**
   * @see magellan.plugin.allianceplugin.AbstractOdysseyConnectDialog#connectionEstablished(magellan.plugin.allianceplugin.net.OdysseyServerInformation)
   */
  @Override
  protected void connectionEstablished(OdysseyServerInformation infos) {
    
    
  }

  /**
   * @see magellan.plugin.allianceplugin.AbstractOdysseyConnectDialog#initCenterPanel()
   */
  @Override
  protected JPanel initCenterPanel() {
    // Center: Info and Chooser
    JPanel centerPanel = new JPanel(new BorderLayout());
    centerPanel.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(),Resources.get(Constants.RESOURCE_UPLOADDIALOG_FILE_TITLE)));
    
    return centerPanel;
  }

  /**
   * @see magellan.plugin.allianceplugin.AbstractOdysseyConnectDialog#initDialog()
   */
  @Override
  protected void initDialog() {
    setTitle(Resources.get(Constants.RESOURCE_MAINMENU_UPLOADDIALOG_TITLE));
    setSize(500, 400);
    setResizable(false);
  }

}
