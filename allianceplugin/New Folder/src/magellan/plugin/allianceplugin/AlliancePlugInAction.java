package magellan.plugin.allianceplugin;

import java.awt.event.ActionEvent;

/**
 * 
 * @author Thoralf Rickert
 * @version 1.0
 */
public enum AlliancePlugInAction {

  UPLOAD_REPORT("mainmenu.upload_report"),
  DOWNLOAD_REPORT("mainmenu.download_report"),
  MERGE_ORDERS("mainmenu.merge_orders"),
  UNKNOWN("");
  
  private String id;
  
  private AlliancePlugInAction(String id) {
    this.id = id;
  }
  
  public String getID() {
    return id;
  }
  
  public static AlliancePlugInAction getAction(ActionEvent e) {
    if (e == null) return UNKNOWN;
    for (AlliancePlugInAction action : values()) {
      if (action.id.equalsIgnoreCase(e.getActionCommand())) return action;
    }
    return UNKNOWN;
  }
}
