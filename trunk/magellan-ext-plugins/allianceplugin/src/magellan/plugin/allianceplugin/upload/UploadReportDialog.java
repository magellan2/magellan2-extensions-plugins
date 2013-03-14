package magellan.plugin.allianceplugin.upload;

import static pagelayout.EasyCell.column;
import static pagelayout.EasyCell.none;
import static pagelayout.EasyCell.right;
import static pagelayout.EasyCell.row;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JPanel;

import magellan.client.Client;
import magellan.client.utils.ErrorWindow;
import magellan.library.io.cr.CRWriter;
import magellan.library.io.file.FileBackup;
import magellan.library.utils.NullUserInterface;
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
import pagelayout.Column;

/**
 * This dialog shows a progress bar that uploads a report
 * to the server.
 * 
 * @author Thoralf Rickert
 * @version 1.0
 */
public class UploadReportDialog extends AbstractOdysseyConnectDialog implements ActionListener, KeyListener {
  private static final Logger log = Logger.getInstance(UploadReportDialog.class);

  private JButton cancelButton = null;
  private JButton uploadButton = null;
  

  /**
   * Creates a new dialog that asks for the server
   * options, connects to the server and uploads
   * the newest report to the server.
   * 
   * @param client The magellan client instance.
   */
  public UploadReportDialog(Client client) {
    super(client);
    setActionListener(this);
    initGUI();
  }

  /**
   * @see magellan.plugin.allianceplugin.AbstractOdysseyConnectDialog#connectionEstablished(magellan.plugin.allianceplugin.net.OdysseyServerInformation)
   */
  @Override
  protected void connectionEstablished(OdysseyServerInformation infos) {
  }

  /**
   * @see magellan.plugin.allianceplugin.AbstractOdysseyConnectDialog#initDialog()
   */
  @Override
  protected void initDialog() {
    setTitle(Resources.get(Constants.RESOURCE_MAINMENU_UPLOADDIALOG_TITLE));
    setSize(500, 450);
    setResizable(false);
  }

  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(ActionEvent e) {
    super.handleActionPerformed(e);
    if (e.getActionCommand() == null) return;
    if (e.getActionCommand().equals("button.cancel")) {
      setVisible(false);
    } else if (e.getActionCommand().equals("button.upload")) {
      try {
        File file = client.getData().getFileType().getFile();
        UploadUserInterface uui = new UploadUserInterface(this);
        CRWriter crw = new CRWriter(
            client.getData(), 
            uui, 
            client.getData().getFileType(), 
            client.getData().getEncoding(), 
            Integer.parseInt(client.getProperties().getProperty("Client.CRBackups.count", FileBackup.DEFAULT_BACKUP_LEVEL + "")));
        crw.writeSynchronously();
        crw.close();
        
        progressBar.setValue(0);
        
        // get the informations from the dialog 
        OdysseyAlliance alliance = (OdysseyAlliance)allianceChooser.getSelectedItem();
        OdysseyMap map = (OdysseyMap)mapChooser.getSelectedItem();
        OdysseyMapPart mappart = (OdysseyMapPart)mapPartChooser.getSelectedItem();
        
        UploadObserver observer = new UploadObserver(alliance,map,mappart,file);
        observer.start();

      } catch (Exception exception) {
        log.error("",exception);
        ErrorWindow window = new ErrorWindow(Client.INSTANCE,Resources.get(Constants.RESOURCE_SERVER_CONNECT_FAILED_MESSAGE),exception.getMessage(),exception);
        window.setShutdownOnCancel(false);
        window.setVisible(true);

      }
    }
    
  }

  /**
   * @see magellan.plugin.allianceplugin.AbstractOdysseyConnectDialog#allianceSettingsChanged(java.awt.event.ItemEvent)
   */
  @Override
  protected void allianceSettingsChanged(ItemEvent e) {
    enableButtons();
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
    
    uploadButton = new JButton(Resources.get(Constants.RESOURCE_UPLOAD));
    uploadButton.addActionListener(this);
    uploadButton.setActionCommand("button.upload");
    enableButtons();

    Column layout = column(
                      row(right,none,new Component[]{cancelButton,uploadButton})
                    );
    
    layout.createLayout(buttonPanel);

    return buttonPanel;
  }


  /**
   * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
   */
  public void keyReleased(KeyEvent e) {
    super.keyReleased(e);
  }
  
  /**
   * 
   */
  private void enableButtons() {
    Object alliance = allianceChooser.getSelectedItem();
    Object map = mapChooser.getSelectedItem();
    Object mappart = mapPartChooser.getSelectedItem();

    log.info("Alliance "+alliance);
    log.info("Map      "+map);
    log.info("Mappart  "+mappart);
    log.info("Chooser  "+(alliance!=null && map!=null && mappart!=null));
    
    uploadButton.setEnabled(alliance!=null && map!=null && mappart!=null);
  }

  class UploadObserver extends Thread implements Observer {
    private OdysseyAlliance alliance = null;
    private OdysseyMap map = null;
    private OdysseyMapPart mappart = null;
    private long transferedBytes = 0l;
    private File crFile = null;
    
    public UploadObserver(OdysseyAlliance alliance, OdysseyMap map, OdysseyMapPart mappart, File file) {
      this.alliance = alliance;
      this.map = map;
      this.mappart = mappart;
      this.crFile = file;

      progressBar.setEnabled(true);
      progressBar.setValue(0);
      
      // okay, do not start any other operations...
      connectButton.setEnabled(false);
      uploadButton.setEnabled(false);
      cancelButton.setEnabled(false);
      autoConnectBox.setEnabled(false);
    }
    
    
    public void run() {
      UploadThread thread = new UploadThread(this);
      thread.start();
      
      while (transferedBytes < crFile.length()) {
        int percent = (int)(transferedBytes * 100 / crFile.length());
        progressBar.setValue(percent);
        
        try {sleep(500);} catch (Exception e) {}
        log.info("Check upload progress..."+transferedBytes);
      }
      
      log.info("Upload is ready...");
      
      // okay, we have downloaded the file.
      // now load it into Magellan
      progressBar.setValue(100);
      progressBar.setEnabled(false);
      
      if (crFile != null) {
        log.info("Load CR file into Magellan");
        progressBar.setValue(0);
        progressBar.setEnabled(false);
        
        setVisible(false);
      } else {
        cancelButton.setEnabled(true);
        connectButton.setEnabled(true);
        uploadButton.setEnabled(true);
//        fileChooserButton.setEnabled(true);
//        filePathField.setEnabled(true);
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
      if (file != null) transferedBytes = file.length();
    }
    

    /**
     * This class uploads the file.
     *
     * @author <a href="thoralf@m84.de">Thoralf Rickert</a>
     * @version 1.0
     */
    class UploadThread extends Thread {
      private Observer observer;
      public UploadThread(Observer observer) {
        this.observer = observer;
      }
      public void run() {
        OdysseyClient.getInstance().sendMap(observer,alliance,map,mappart,crFile);
      }
    }
  }
  
  class UploadUserInterface extends NullUserInterface {
    private UploadReportDialog dialog = null;
    
    public UploadUserInterface(UploadReportDialog dialog) {
      super();
      this.dialog = dialog;
    }

    /**
     * @see magellan.library.utils.NullUserInterface#setProgress(java.lang.String, int)
     */
    @Override
    public void setProgress(String strMessage, int progress) {
      super.setProgress(strMessage, progress);
      dialog.progressBar.setValue(progress);
    }
    
    
  }
}
