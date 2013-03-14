package magellan.plugin.allianceplugin.download;

import static pagelayout.EasyCell.*;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.io.File;


import javax.swing.JButton;
import javax.swing.JPanel;

import pagelayout.Column;

import magellan.client.Client;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;
import magellan.plugin.allianceplugin.AbstractOdysseyConnectDialog;
import magellan.plugin.allianceplugin.Constants;
import magellan.plugin.allianceplugin.data.OdysseyAlliance;
import magellan.plugin.allianceplugin.data.OdysseyMap;
import magellan.plugin.allianceplugin.data.OdysseyMapPart;
import magellan.plugin.allianceplugin.net.Observer;
import magellan.plugin.allianceplugin.net.OdysseyClient;
import magellan.plugin.allianceplugin.net.OdysseyServerInformation;

/**
 * This dialog shows a progress bar that downloads the last report
 * from the server.
 * 
 * @author Thoralf Rickert
 * @version 1.0
 */
public class DownloadReportDialog extends AbstractOdysseyConnectDialog implements ActionListener {
  private static final Logger log = Logger.getInstance(DownloadReportDialog.class);
  boolean openAtStart = false;
  
  private JButton downloadButton = null;
  private JButton cancelButton = null;
  
  /**
   * Creates a new dialog that asks for the server
   * options, connects to the server and downloads
   * the newest report from the server.
   * 
   * @param client The magellan client instance.
   */
  public DownloadReportDialog(Client client) {
    super(client);
    setActionListener(this);
    initGUI();
  }
  
  /**
   * @see magellan.plugin.allianceplugin.AbstractOdysseyConnectDialog#initDialog()
   */
  protected void initDialog() {
    setTitle(Resources.get(Constants.RESOURCE_MAINMENU_DOWNLOADDIALOG_TITLE));
    setSize(500, 400);
    setResizable(false);
  }

  /**
   * @see magellan.plugin.allianceplugin.AbstractOdysseyConnectDialog#initSpecialGUIPanel()
   */
  @Override
  protected JPanel initSpecialGUIPanel() {
    JPanel buttonPanel = new JPanel();
    
    cancelButton = new JButton(Resources.get(Constants.RESOURCE_CANCEL));
    cancelButton.addActionListener(this);
    cancelButton.setActionCommand("button.cancel");
    
    downloadButton = new JButton(Resources.get(Constants.RESOURCE_DOWNLOAD));
    downloadButton.addActionListener(this);
    downloadButton.setActionCommand("button.download");
    enableDownloadButton();
    
    Column layout = column(
                      row(right,none,new Component[]{cancelButton,downloadButton})
                    );
        
    layout.createLayout(buttonPanel);

    return buttonPanel;
  }
  
  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(ActionEvent e) {
    super.handleActionPerformed(e);
    if (e.getActionCommand() == null) return;
    if (e.getActionCommand().equals("button.download")) {

      // get the informations from the dialog 
      OdysseyAlliance alliance = (OdysseyAlliance)allianceChooser.getSelectedItem();
      OdysseyMap map = (OdysseyMap)mapChooser.getSelectedItem();
      OdysseyMapPart mappart = (OdysseyMapPart)mapPartChooser.getSelectedItem();
      
      DownloadObserver observer = new DownloadObserver(alliance,map,mappart);
      observer.start();
    } else if (e.getActionCommand().equals("button.cancel")) {
      setVisible(false);
    }

  }

  /**
   * @see magellan.plugin.allianceplugin.AbstractOdysseyConnectDialog#connectionEstablished(magellan.plugin.allianceplugin.net.OdysseyServerInformation)
   */
  protected void connectionEstablished(OdysseyServerInformation infos) {
    enableDownloadButton();
  }
  
  /**
   * 
   */
  private void enableDownloadButton() {
    Object alliance = allianceChooser.getSelectedItem();
    Object map = mapChooser.getSelectedItem();
    Object mappart = mapPartChooser.getSelectedItem();
    
    downloadButton.setEnabled(alliance!=null && map!=null && mappart!=null);
  }
  

  /**
   * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
   */
  public void allianceSettingsChanged(ItemEvent e) {
    enableDownloadButton();
  }

  /**
   * This method returns the field openAtStart
   * 
   * @return the openAtStart
   */
  public boolean isOpenAtStart() {
    return openAtStart;
  }

  /**
   * This method sets the field openAtStart to openAtStart
   *
   * @param openAtStart the openAtStart to set
   */
  public void setOpenAtStart(boolean openAtStart) {
    this.openAtStart = openAtStart;
  }
  
  
  /**
   * This class observes the download process.
   *
   * @author <a href="thoralf@m84.de">Thoralf Rickert</a>
   * @version 1.0
   */
  class DownloadObserver extends Thread implements Observer {
    private OdysseyAlliance alliance = null;
    private OdysseyMap map = null;
    private OdysseyMapPart mappart = null;
    private long transferedBytes = 0l;
    private File crFile = null;
    
    /**
     * 
     */
    public DownloadObserver(OdysseyAlliance alliance, OdysseyMap map, OdysseyMapPart mappart) {
      this.alliance = alliance;
      this.map = map;
      this.mappart = mappart;

      progressBar.setEnabled(true);
      progressBar.setValue(0);
      
      // okay, do not start any other operations...
      connectButton.setEnabled(false);
      downloadButton.setEnabled(false);
      cancelButton.setEnabled(false);
      autoConnectBox.setEnabled(false);
    }
    
    /**
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
      DownloadThread thread = new DownloadThread(this);
      thread.start();
      
      while (transferedBytes < mappart.getSize()) {
        int percent = (int)(transferedBytes * 100 / mappart.getSize());
        progressBar.setValue(percent);
        
        try {sleep(500);} catch (Exception e) {}
        log.info("Check download progress..."+transferedBytes);
      }
      
      log.info("Download is ready...");
      
      // okay, we have downloaded the file.
      // now load it into Magellan
      progressBar.setValue(100);
      progressBar.setEnabled(false);
      
      if (crFile != null) {
        log.info("Load CR file into Magellan");
        crFile.deleteOnExit();
        progressBar.setValue(0);
        progressBar.setEnabled(false);
        
        Client.INSTANCE.loadCRThread(crFile);
        setVisible(false);
      } else {
        cancelButton.setEnabled(true);
        connectButton.setEnabled(true);
        downloadButton.setEnabled(true);
        autoConnectBox.setEnabled(true);
      }
    }

    /**
     * @see magellan.plugin.allianceplugin.net.Observer#transfer(long)
     */
    public void transfer(long value) {
      this.transferedBytes = value;
    }

    /**
     * @see magellan.plugin.allianceplugin.net.Observer#setFile(java.io.File)
     */
    public void setFile(File file) {
      this.crFile = file;
      transferedBytes = mappart.getSize();
    }
    
    /**
     * This class downloads the file.
     *
     * @author <a href="thoralf@m84.de">Thoralf Rickert</a>
     * @version 1.0
     */
    class DownloadThread extends Thread {
      private Observer observer;
      public DownloadThread(Observer observer) {
        this.observer = observer;
      }
      public void run() {
        OdysseyClient.getInstance().getMap(observer,alliance,map,mappart);
      }
    }
  }

}
