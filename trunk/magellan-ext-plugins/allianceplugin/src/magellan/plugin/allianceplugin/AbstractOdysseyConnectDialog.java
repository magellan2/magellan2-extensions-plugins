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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.URI;

import javax.swing.BorderFactory;
import javax.swing.JButton;
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
import magellan.plugin.allianceplugin.net.OdysseyClient;
import magellan.plugin.allianceplugin.net.OdysseyServerInformation;

/**
 * This dialog implements the GUI for the odyssey connection.
 *
 * @author <a href="thoralf@m84.de">Thoralf Rickert</a>
 * @version 1.0
 */
public abstract class AbstractOdysseyConnectDialog extends JDialog implements ActionListener, KeyListener {
  private static final Logger log = Logger.getInstance(AbstractOdysseyConnectDialog.class);

  protected Client client = null;

  protected JTextField serverURLField = null;
  protected JTextField userEMailField = null;
  protected JPasswordField userPasswordField = null;
  protected JButton connectButton = null;
  protected JTextField servernameField = null;

  protected JProgressBar progressBar = null;
  
  public AbstractOdysseyConnectDialog(Client client) {
    super(client);
    this.client = client;
    initGUI();
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
    panel.add(initNorthPanel(),BorderLayout.NORTH);
    panel.add(initCenterPanel(),BorderLayout.CENTER);
    panel.add(initSouthPanel(),BorderLayout.SOUTH);
    
    add(panel,BorderLayout.CENTER);
  }
  
  protected abstract void initDialog();
  
  protected JPanel initNorthPanel() {
    // North: Server Settings
    JPanel northPanel = new JPanel();
    northPanel.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(),Resources.get(Constants.RESOURCE_DOWNLOADDIALOG_SERVERSETTINGS_TITLE)));
    
    serverURLField = new JTextField(PropertiesHelper.getString(client.getProperties(),Constants.PROPERTY_SERVER_URL,Constants.DEFAULT_SERVER_URL));
    serverURLField.addKeyListener(this);
    userEMailField = new JTextField(PropertiesHelper.getString(client.getProperties(),Constants.PROPERTY_USER_EMAIL,""));
    userEMailField.addKeyListener(this);
    userPasswordField = new JPasswordField(PropertiesHelper.getString(client.getProperties(),Constants.PROPERTY_USER_PASSWORD,""));
    userPasswordField.addKeyListener(this);
    connectButton = new JButton(Resources.get(Constants.RESOURCE_CONNECT));
    connectButton.addActionListener(this);
    connectButton.setActionCommand("button.connect");
    enableConnectButton();
    
    Column layout = column(
                      grid(
                        label(Constants.RESOURCE_SERVER_URL),serverURLField,eol(),
                        label(Constants.RESOURCE_SERVER_USER_EMAIL),userEMailField,eol(),
                        label(Constants.RESOURCE_SERVER_USER_PASSWORD),userPasswordField),
                      row(center,none,vgap(10)),
                      row(right,none,connectButton));
    layout.createLayout(northPanel);
    
    return northPanel;
  }

  protected abstract JPanel initCenterPanel();
  
  /**
   * Creates a progress bar.
   */
  protected JPanel initSouthPanel() {
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
  public void actionPerformed(ActionEvent e) {
    if (e.getActionCommand() == null) return;
    if (e.getActionCommand().equals("button.connect")) {
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
      progressBar.setValue(30);

      // setup alliance settings
      servernameField.setText(infos.getServername());
      
      connectionEstablished(infos);

      // ready...
      progressBar.setEnabled(false);
      progressBar.setValue(0);
    }
  }
  
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
    
    connectButton.setEnabled(serverURL.length() > 10 && isURL && userEMail.length() > 5 && userPass.length() > 4);
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
    enableConnectButton();
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
  
}
