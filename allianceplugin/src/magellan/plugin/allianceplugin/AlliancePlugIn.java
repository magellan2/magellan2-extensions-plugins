/**
 * 
 */
package magellan.plugin.allianceplugin;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import magellan.client.Client;
import magellan.client.extern.MagellanPlugIn;
import magellan.client.swing.preferences.PreferencesFactory;
import magellan.library.GameData;
import magellan.library.utils.PropertiesHelper;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;
import magellan.plugin.allianceplugin.download.DownloadReportDialog;
import magellan.plugin.allianceplugin.merge.MergeOrdersDialog;
import magellan.plugin.allianceplugin.upload.UploadReportDialog;

/**
 * This is a plugin for Magellan2 that manages alliance report handling with the
 * help of a server singleton report solution. Every faction in an alliance
 * imports its report to the server and loads the report from there.
 * Additionally everybody merges his/her orders on the server.
 * 
 * @author Thoralf Rickert
 * @version 1.0
 */
public class AlliancePlugIn implements MagellanPlugIn, ActionListener {
  private static Logger log = null;
  private Client client = null;
  private Properties settings = null;

  /**
   * @see magellan.client.extern.MagellanPlugIn#init(magellan.client.Client,
   *      java.util.Properties)
   */
  public void init(Client client, Properties properties) {
    // init the plugin
    log = Logger.getInstance(AlliancePlugIn.class);
    Resources.getInstance().initialize(Client.getResourceDirectory(), "allianceplugin_");
    this.client = client;
    settings = properties;
    log.info(getName() + " initialized...(Client)");

    // check, if the client loads automatically the last loaded report on start
    // and inform user.
    if (PropertiesHelper.getBoolean(settings, PropertiesHelper.CLIENTPREFERENCES_LOAD_LAST_REPORT, true)) {
      // okay, yes - should we inform the user about that?
      if (PropertiesHelper.getBoolean(settings, Constants.PROPERTY_INFORM_USER_ABOUT_LOAD_LAST_REPORT, true)) {
        // okay, then we should do it and ask if we should disable that feature.
        int change = JOptionPane.showConfirmDialog(this.client, Resources.get(Constants.RESOURCE_LOAD_LAST_REPORT_MESSAGE), Resources.get(Constants.RESOURCE_LOAD_LAST_REPORT_TITLE), JOptionPane.YES_NO_OPTION);
        if (change == JOptionPane.YES_OPTION) {
          // ok, disable that feature
          settings.put(PropertiesHelper.CLIENTPREFERENCES_LOAD_LAST_REPORT, Boolean.FALSE.toString());
          settings.put(Constants.PROPERTY_INFORM_USER_ABOUT_LOAD_LAST_REPORT, Boolean.TRUE.toString());
        } else {
          // ok, we have asked, so let's disable this feature.
          settings.put(Constants.PROPERTY_INFORM_USER_ABOUT_LOAD_LAST_REPORT, Boolean.FALSE.toString());
        }
      }
    }

    if (PropertiesHelper.getBoolean(settings, Constants.PROPERTY_SHOW_DOWNLOAD_DIALOG_ON_START, true)) {
      DownloadReportDialog dialog = new DownloadReportDialog(client);
      dialog.setOpenAtStart(true);
      dialog.setVisible(true);
    }
  }

  /**
   * @see magellan.client.extern.MagellanPlugIn#init(magellan.library.GameData)
   */
  public void init(GameData data) {
    // init the report
    log.info(getName() + " initialized...(GameData)");

    // hmm, do we need something to checks?
  }

  /**
   * @see magellan.client.extern.MagellanPlugIn#getMenuItems()
   */
  public List<JMenuItem> getMenuItems() {
    List<JMenuItem> items = new ArrayList<JMenuItem>();

    JMenu menu = new JMenu(Resources.get(Constants.RESOURCE_MAINMENU_TITLE));
    items.add(menu);

    JMenuItem uploadReportMenu = new JMenuItem(Resources.get(Constants.RESOURCE_MAINMENU_UPLOADDIALOG_TITLE));
    uploadReportMenu.setActionCommand(AlliancePlugInAction.UPLOAD_REPORT.getID());
    uploadReportMenu.addActionListener(this);
    menu.add(uploadReportMenu);

    JMenuItem downloadReportMenu = new JMenuItem(Resources.get(Constants.RESOURCE_MAINMENU_DOWNLOADDIALOG_TITLE));
    downloadReportMenu.setActionCommand(AlliancePlugInAction.DOWNLOAD_REPORT.getID());
    downloadReportMenu.addActionListener(this);
    menu.add(downloadReportMenu);

    JMenuItem mergeOrdersMenu = new JMenuItem(Resources.get(Constants.RESOURCE_MAINMENU_MERGEORDERS_TITLE));
    mergeOrdersMenu.setActionCommand(AlliancePlugInAction.MERGE_ORDERS.getID());
    mergeOrdersMenu.addActionListener(this);
    menu.add(mergeOrdersMenu);

    return items;
  }

  /**
   * @see magellan.client.extern.MagellanPlugIn#getName()
   */
  public String getName() {
    return "Alliance Plugin";
  }

  /**
   * @see magellan.client.extern.MagellanPlugIn#getPreferencesProvider()
   */
  public PreferencesFactory getPreferencesProvider() {
    // we don't need that yet.
    return null;
  }

  /**
   * @see magellan.client.extern.MagellanPlugIn#getDocks()
   */
  public Map<String, Component> getDocks() {
    return null;
  }

  /**
   * @see magellan.client.extern.MagellanPlugIn#quit(boolean)
   */
  public void quit(boolean storeSettings) {
    // hmm...
  }

  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(ActionEvent e) {
    log.info(e.getActionCommand());
    switch (AlliancePlugInAction.getAction(e)) {
    case UPLOAD_REPORT: {
      log.info("Upload a report and merge on the server...");
      UploadReportDialog dialog = new UploadReportDialog(client);
      dialog.setVisible(true);
      break;
    }
    case DOWNLOAD_REPORT: {
      log.info("Download the merged report...");
      DownloadReportDialog dialog = new DownloadReportDialog(client);
      dialog.setVisible(true);
      break;
    }
    case MERGE_ORDERS: {
      log.info("Merge orders with server...");
      MergeOrdersDialog dialog = new MergeOrdersDialog(client);
      dialog.setVisible(true);
      break;
    }
    }
  }

}
