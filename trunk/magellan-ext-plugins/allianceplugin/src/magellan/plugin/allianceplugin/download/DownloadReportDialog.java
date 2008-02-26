package magellan.plugin.allianceplugin.download;

import static pagelayout.EasyCell.*;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;


import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

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
public class DownloadReportDialog extends AbstractOdysseyConnectDialog implements ActionListener, ItemListener {
  private static final Logger log = Logger.getInstance(DownloadReportDialog.class);
  boolean openAtStart = false;
  
  private JComboBox allianceChooser = null;
  private JComboBox mapChooser = null;
  private JComboBox mapPartChooser = null;
  private JButton downloadButton = null;
  
  /**
   * Creates a new dialog that asks for the server
   * options, connects to the server and downloads
   * the newest report from the server.
   * 
   * @param client The magellan client instance.
   */
  public DownloadReportDialog(Client client) {
    super(client);
  }
  
  /**
   * @see magellan.plugin.allianceplugin.AbstractOdysseyConnectDialog#initDialog()
   */
  protected void initDialog() {
    setTitle(Resources.get(Constants.RESOURCE_MAINMENU_DOWNLOADDIALOG_TITLE));
    setSize(500, 400);
    setResizable(false);
  }

  protected JPanel initCenterPanel() {
    // Center: Info and Chooser
    JPanel centerPanel = new JPanel(new BorderLayout());
    centerPanel.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(),Resources.get(Constants.RESOURCE_DOWNLOADDIALOG_ALLIANCESETTINGS_TITLE)));
    
    servernameField = new JTextField(Resources.get(Constants.RESOURCE_SERVER_NAME));
    servernameField.setEnabled(false);
    allianceChooser = new JComboBox();
    allianceChooser.addItemListener(this);
    allianceChooser.setEnabled(false);
    mapChooser = new JComboBox();
    mapChooser.addItemListener(this);
    mapChooser.setEnabled(false);
    mapPartChooser = new JComboBox();
    mapPartChooser.setEnabled(false);

    downloadButton = new JButton(Resources.get(Constants.RESOURCE_DOWNLOAD));
    downloadButton.addActionListener(this);
    downloadButton.setActionCommand("button.download");
    enableDownloadButton();

    Column layout = column(
                      grid(
                        label(Constants.RESOURCE_SERVER_NAME),servernameField,eol(),
                        label(Constants.RESOURCE_ALLIANCE),allianceChooser,eol(),
                        label(Constants.RESOURCE_ALLIANCE_MAP),mapChooser,eol(),
                        label(Constants.RESOURCE_ALLIANCE_MAPPART),mapPartChooser),
                      row(center,none,vgap(10)),
                      row(right,none,downloadButton),
                      row(center,none,vgap(50))
                      );
    layout.createLayout(centerPanel);
    
    return centerPanel;
  }
  
  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    super.actionPerformed(e);
    if (e.getActionCommand() == null) return;
    if (e.getActionCommand().equals("button.download")) {

      // get the informations from the dialog 
      OdysseyAlliance alliance = (OdysseyAlliance)allianceChooser.getSelectedItem();
      OdysseyMap map = (OdysseyMap)mapChooser.getSelectedItem();
      OdysseyMapPart mappart = (OdysseyMapPart)mapPartChooser.getSelectedItem();
      
      DownloadObserver observer = new DownloadObserver(alliance,map,mappart);
      observer.start();
    }
  }

  /**
   * @see magellan.plugin.allianceplugin.AbstractOdysseyConnectDialog#connectionEstablished(magellan.plugin.allianceplugin.net.OdysseyServerInformation)
   */
  protected void connectionEstablished(OdysseyServerInformation infos) {
    for (int i=allianceChooser.getItemCount(); i>0; i--) allianceChooser.removeItemAt(i-1);
    for (OdysseyAlliance alliance : infos.getAlliances()) {
      allianceChooser.addItem(alliance);
    }
    allianceChooser.setEnabled(true);
    allianceChooser.setSelectedIndex(0);
    progressBar.setValue(40);
    
    OdysseyAlliance alliance = (OdysseyAlliance)allianceChooser.getSelectedItem();
    for (int i=mapChooser.getItemCount(); i>0; i--) mapChooser.removeItemAt(i-1);
    for (OdysseyMap map : alliance.getMaps()) {
      mapChooser.addItem(map);
    }
    mapChooser.setEnabled(true);
    mapChooser.setSelectedIndex(0);
    progressBar.setValue(50);
    
    OdysseyMap map = (OdysseyMap)mapChooser.getSelectedItem();
    for (int i=mapPartChooser.getItemCount(); i>0; i--) mapPartChooser.removeItemAt(i-1);
    for (OdysseyMapPart mappart : map.getParts()) {
      mapPartChooser.addItem(mappart);
    }
    mapPartChooser.setEnabled(true);
    mapPartChooser.setSelectedIndex(0);
    progressBar.setValue(60);
    
    enableDownloadButton();
  }
  
  private void enableDownloadButton() {
    Object alliance = allianceChooser.getSelectedItem();
    Object map = mapChooser.getSelectedItem();
    Object mappart = mapPartChooser.getSelectedItem();
    
    downloadButton.setEnabled(alliance!=null && map!=null && mappart!=null);
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
   * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
   */
  public void itemStateChanged(ItemEvent e) {
    enableDownloadButton();
  }
  
  
  class DownloadObserver extends Thread implements Observer {
    private OdysseyAlliance alliance = null;
    private OdysseyMap map = null;
    private OdysseyMapPart mappart = null;
    private long transferedBytes = 0l;
    private File crFile = null;
    
    public DownloadObserver(OdysseyAlliance alliance, OdysseyMap map, OdysseyMapPart mappart) {
      this.alliance = alliance;
      this.map = map;
      this.mappart = mappart;

      progressBar.setEnabled(true);
      progressBar.setValue(0);
      
      // okay, do not start any other operations...
      connectButton.setEnabled(false);
      downloadButton.setEnabled(false);
    }
    
    
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
        progressBar.setValue(0);
        progressBar.setEnabled(false);
        
        Client.INSTANCE.loadCRThread(crFile);
        setVisible(false);
      } else {
        connectButton.setEnabled(true);
        downloadButton.setEnabled(true);
      }
    }


    public void transfer(long value) {
      this.transferedBytes = value;
    }


    public void setFile(File file) {
      this.crFile = file;
      transferedBytes = mappart.getSize();
    }
    
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
