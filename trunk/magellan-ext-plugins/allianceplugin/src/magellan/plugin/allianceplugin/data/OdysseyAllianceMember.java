// Created on 26.02.2008
//
package magellan.plugin.allianceplugin.data;

import org.w3c.dom.Element;

import magellan.library.utils.Utils;
import magellan.library.utils.logging.Logger;

/**
 * This is a container for an alliance member (faction)
 *
 * @author <a href="thoralf@m84.de">Thoralf Rickert</a>
 * @version 1.0
 */
public class OdysseyAllianceMember {
  private static final Logger log = Logger.getInstance(OdysseyAllianceMember.class);
  
  private String name = null;
  private String id = null;
  private String mail = null;
  

  /**
   *     <members>
   *       <faction id="...">...</faction>
   *       ...
   *     </members>
   */
  public OdysseyAllianceMember(Element root) {
    if (!root.getNodeName().equals("faction")) throw new IllegalArgumentException("This is not a faction XML node.");

    name = Utils.getCData(root);
    log.info(" - Found Alliance Member '"+name+"'");
    
    id = root.getAttribute("id");
    mail = root.getAttribute("mail");
  }


  /**
   * This method returns the field id
   *
   * @return the id
   */
  public String getId() {
    return id;
  }


  /**
   * This method sets the field id to id
   *
   * @param id the id to set
   */
  public void setId(String id) {
    this.id = id;
  }


  /**
   * This method returns the field mail
   *
   * @return the mail
   */
  public String getMail() {
    return mail;
  }


  /**
   * This method sets the field mail to mail
   *
   * @param mail the mail to set
   */
  public void setMail(String mail) {
    this.mail = mail;
  }


  /**
   * This method returns the field name
   *
   * @return the name
   */
  public String getName() {
    return name;
  }


  /**
   * This method sets the field name to name
   *
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }
  
  
}
