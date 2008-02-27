// Created on 26.02.2008
//
package magellan.plugin.allianceplugin;

import static pagelayout.EasyCell.center;
import static pagelayout.EasyCell.column;
import static pagelayout.EasyCell.eol;
import static pagelayout.EasyCell.grid;
import static pagelayout.EasyCell.none;
import static pagelayout.EasyCell.right;
import static pagelayout.EasyCell.row;
import static pagelayout.EasyCell.vgap;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.URI;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import pagelayout.Column;

import magellan.client.Client;
import magellan.library.utils.PropertiesHelper;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;
import magellan.plugin.allianceplugin.data.OdysseyAlliance;
import magellan.plugin.allianceplugin.data.OdysseyMap;
import magellan.plugin.allianceplugin.data.OdysseyMapPart;
import magellan.plugin.allianceplugin.net.OdysseyClient;
import magellan.plugin.allianceplugin.net.OdysseyServerInformation;

/**
 * This dialog implements the GUI for the odyssey connection.
 *
 * @author <a href="thoralf@m84.de">Thoralf Rickert</a>
 * @version 1.0
 */
public abstract class AbstractOdysseyConnectDialog extends JDialog implements KeyListener, ItemListener {
  private static final Logger log = Logger.getInstance(AbstractOdysseyConnectDialog.class);

  protected Client client = null;
  protected ActionListener listener = null;

  protected JTextField serverURLField = null;
  protected JTextField userEMailField = null;
  protected JPasswordField userPasswordField = null;
  protected JCheckBox autoConnectBox = null;
  protected JButton connectButton = null;
  protected JTextField servernameField = null;
  protected JComboBox allianceChooser = null;
  protected JComboBox mapChooser = null;
  protected JComboBox mapPartChooser = null;
  
  protected JProgressBar progressBar = null;
  
  public AbstractOdysseyConnectDialog(Client client) {
    super(client,true);
    this.client = client;
  }
  
  public void setActionListener(ActionListener listener) {
    this.listener = listener;
  }


  /**
   * Initialize the GUI
   */
  protected void initGUI() {
    initDialog();
    
    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    int x = Integer.parseInt(client.getProperties().getProperty(Constants.PROPERTY_DOWNLOAD_DIALOG_X_POS, Integer.toString((screen.width - getWidth()) / 2)));
    int y = Integer.parseInt(client.getProperties().getProperty(Constants.PROPERTY_DOWNLOAD_DIALOG_Y_POS, Integer.toString((screen.height - getHeight()) / 2)));
    setLocation(x,y);
    
    setLayout(new BorderLayout());
    
    JPanel panel = new JPanel(new BorderLayout());
    panel.add(initServerConnectionPanel(),BorderLayout.NORTH);
    panel.add(initServerAlliancePanel(),BorderLayout.CENTER);
    panel.add(initProgressPanel(),BorderLayout.SOUTH);
    
    add(panel,BorderLayout.CENTER);
    
    if (PropertiesHelper.getBoolean(client.getProperties(), Constants.PROPERTY_SERVER_AUTOCONNECT, false)) {
      connect();
    }
  }
  
  /**
   * Sets the "outer" layouts of this dialog, like size and title.
   */
  protected abstract void initDialog();
  
  /**
   * Initializes the connection handling settings.
   */
  protected JPanel initServerConnectionPanel() {
    // North: Server Settings
    JPanel northPanel = new JPanel();
    northPanel.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(),Resources.get(Constants.RESOURCE_SERVERSETTINGS_TITLE)));
    
    serverURLField = new JTextField(PropertiesHelper.getString(client.getProperties(),Constants.PROPERTY_SERVER_URL,Constants.DEFAULT_SERVER_URL));
    serverURLField.addKeyListener(this);
    userEMailField = new JTextField(PropertiesHelper.getString(client.getProperties(),Constants.PROPERTY_USER_EMAIL,""));
    userEMailField.addKeyListener(this);
    userPasswordField = new JPasswordField(PropertiesHelper.getString(client.getProperties(),Constants.PROPERTY_USER_PASSWORD,""));
    userPasswordField.addKeyListener(this);
    
    autoConnectBox = new JCheckBox(Resources.get(Constants.RESOURCE_SERVER_AUTOCONNECT),PropertiesHelper.getBoolean(client.getProperties(), Constants.PROPERTY_SERVER_AUTOCONNECT, false));
    
    connectButton = new JButton(Resources.get(Constants.RESOURCE_CONNECT));
    connectButton.addActionListener(listener);
    connectButton.setActionCommand("button.connect");
    enableConnectButton();
    
    Column layout = column(
                      grid(
                        label(Constants.RESOURCE_SERVER_URL),serverURLField,eol(),
                        label(Constants.RESOURCE_SERVER_USER_EMAIL),userEMailField,eol(),
                        label(Constants.RESOURCE_SERVER_USER_PASSWORD),userPasswordField),
                      row(center,none,vgap(10)),
                      row(right,none,autoConnectBox,connectButton));
    layout.createLayout(northPanel);
    
    return northPanel;
  }

  /**
   * Initializes the alliance and odyssey settings.
   */
  protected JPanel initServerAlliancePanel() {
    log.info("Initialize Center Panel");
    
    // Center: Info and Chooser
    JPanel centerPanel = new JPanel(new BorderLayout());
    centerPanel.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(),Resources.get(Constants.RESOURCE_ALLIANCESETTINGS_TITLE)));
    
    servernameField = new JTextField("");
    servernameField.setEditable(false);
    servernameField.setSelectionColor(servernameField.getBackground());
    servernameField.setSelectedTextColor(servernameField.getForeground());
    allianceChooser = new JComboBox();
    allianceChooser.addItemListener(this);
    allianceChooser.setEnabled(false);
    mapChooser = new JComboBox();
    mapChooser.addItemListener(this);
    mapChooser.setEnabled(false);
    mapPartChooser = new JComboBox();
    mapPartChooser.setEnabled(false);
    mapPartChooser.addItemListener(this);
    
    JPanel panel = initSpecialGUIPanel();

    Column layout = column(
                      grid(
                        label(Constants.RESOURCE_SERVER_NAME),servernameField,eol(),
                        label(Constants.RESOURCE_ALLIANCE),allianceChooser,eol(),
                        label(Constants.RESOURCE_ALLIANCE_MAP),mapChooser,eol(),
                        label(Constants.RESOURCE_ALLIANCE_MAPPART),mapPartChooser),
                      row(center,none,vgap(10)),
                      row(center,none,panel),
                      row(center,none,vgap(50))
                    );
    layout.createLayout(centerPanel);

    return centerPanel;
  }
  
  /**
   * Contains the GUI elements that are special for this
   * dialog
   */
  protected abstract JPanel initSpecialGUIPanel();
  
  /**
   * Creates a progress bar.
   */
  protected JPanel initProgressPanel() {
    // South: progressbar
    JPanel southPanel = new JPanel();
    add(southPanel,BorderLayout.SOUTH);
    
    progressBar = new JProgressBar();
    progressBar.setEnabled(false);
    progressBar.setPreferredSize(new Dimension(450,10));
    progressBar.setMinimum(0);
    progressBar.setMaximum(100);
    southPanel.add(progressBar,BorderLayout.CENTER);
    
    return southPanel;
  }
  

  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void handleActionPerformed(ActionEvent e) {
    if (e.getActionCommand() == null) return;
    if (e.getActionCommand().equals("button.connect")) {
      connect();
    }
  }
  
  /**
   * This method is called, when the connect button is pressed.
   */
  protected void connect() {
    log.info("Try to connect to odyssey server");
    // try to connect to server...
    String serverURL = serverURLField.getText();
    String userEMail = userEMailField.getText();
    String userPass  = new String(userPasswordField.getPassword());
    
    progressBar.setEnabled(true);
    progressBar.setMinimum(0);
    progressBar.setMaximum(100);
    progressBar.setValue(10);
    
    OdysseyClient odyssey = OdysseyClient.getInstance();
    odyssey.setServerURL(serverURL);
    odyssey.setUserMail(userEMail);
    odyssey.setUserPass(userPass);
    progressBar.setValue(20);
    OdysseyServerInformation infos = odyssey.connect();
    if (infos == null || infos.hasErrors()) {
      progressBar.setEnabled(false);
      progressBar.setValue(0);
      return;
    }
    
    client.getProperties().setProperty(Constants.PROPERTY_SERVER_URL, serverURL);
    client.getProperties().setProperty(Constants.PROPERTY_USER_EMAIL, userEMail);
    client.getProperties().setProperty(Constants.PROPERTY_USER_PASSWORD, userPass);
    client.getProperties().setProperty(Constants.PROPERTY_SERVER_AUTOCONNECT, Boolean.toString(autoConnectBox.isSelected()));
    progressBar.setValue(30);

    // setup alliance settings
    servernameField.setText(infos.getServername());
    
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
    
    connectionEstablished(infos);

    // ready...
    progressBar.setEnabled(false);
    progressBar.setValue(0);
  }
  
  /**
   * This method is called, if the connection could be established
   * via the server connection settings and if there are
   * server informations available. Everything else is saved in the
   * OdysseyClient.
   */
  protected abstract void connectionEstablished(OdysseyServerInformation infos);

  /**
   * This method checks, if there is text in the server settings fields
   * and if yes, it enables the button.
   */
  private void enableConnectButton() {
    String serverURL = serverURLField.getText();
    String userEMail = userEMailField.getText();
    String userPass  = new String(userPasswordField.getPassword());
    
    boolean isURL = true;
    try {
      isURL = new URI(serverURL).isAbsolute();
    } catch (Exception exception) {
      isURL = false;
    }
    
    boolean seemsOK2Me = serverURL.length() > 10 && isURL && userEMail.length() > 5 && userPass.length() > 4;
    
    connectButton.setEnabled(seemsOK2Me);
    autoConnectBox.setEnabled(seemsOK2Me);
  }
  
  /**
   * Creates a label
   */
  protected JLabel label(String resourceKey) {
    JLabel label = new JLabel(Resources.get(resourceKey));
    label.setHorizontalAlignment(JLabel.RIGHT);
    return label;
  }
  

  /**
   * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
   */
  public void keyPressed(KeyEvent e) {
    // do nothing...
  }

  /**
   * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
   */
  public void keyReleased(KeyEvent e) {
    if (e.getComponent().equals(serverURLField)) enableConnectButton();
    if (e.getComponent().equals(userEMailField)) enableConnectButton();
    if (e.getComponent().equals(userPasswordField)) enableConnectButton();
  }

  /**
   * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
   */
  public void keyTyped(KeyEvent e) {
    // do nothing...
  }
  

  /**
   * This method returns the field client
   *
   * @return the client
   */
  public Client getClient() {
    return client;
  }

  /**
   * This method sets the field client to client
   *
   * @param client the client to set
   */
  public void setClient(Client client) {
    this.client = client;
  }
  

  /**
   * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
   */
  public void itemStateChanged(ItemEvent e) {
    allianceSettingsChanged(e);
  }
  
  protected abstract void allianceSettingsChanged(ItemEvent e);
}
