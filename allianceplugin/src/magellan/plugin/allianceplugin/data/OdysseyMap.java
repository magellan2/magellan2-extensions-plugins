// Created on 26.02.2008
//
package magellan.plugin.allianceplugin.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.w3c.dom.Element;

import magellan.library.utils.Utils;
import magellan.library.utils.logging.Logger;

/**
 * This is a container for a map.
 *
 * @author <a href="thoralf@m84.de">Thoralf Rickert</a>
 * @version 1.0
 */
public class OdysseyMap {
  private static final Logger log = Logger.getInstance(OdysseyMap.class);
  
  private String name = null;
  private Date lastchange = null;
  private List<OdysseyMapPart> parts = new ArrayList<OdysseyMapPart>();
  

  /**
   *     <map name="..." version="..." lastchange="..." round="...">
   *       <part id="">...</part>
   *       ...
   *     </map>
   */
  public OdysseyMap(Element root) {
    if (!root.getNodeName().equals("map")) throw new IllegalArgumentException("This is not a map XML node.");
    
    name = root.getAttribute("name");
    lastchange = Utils.toDate(root.getAttribute("lastchange"));
    
    log.info(" - Found Map '"+name+"'");
    
    List<Element> partElements = Utils.getChildNodes(root, "part");
    for (Element partElement : partElements) {
      parts.add(new OdysseyMapPart(partElement));
    }
  }


  /**
   * This method returns the field lastchange
   *
   * @return the lastchange
   */
  public Date getLastchange() {
    return lastchange;
  }


  /**
   * This method sets the field lastchange to lastchange
   *
   * @param lastchange the lastchange to set
   */
  public void setLastchange(Date lastchange) {
    this.lastchange = lastchange;
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


  /**
   * This method returns the field parts
   *
   * @return the parts
   */
  public List<OdysseyMapPart> getParts() {
    return parts;
  }


  /**
   * This method sets the field parts to parts
   *
   * @param parts the parts to set
   */
  public void setParts(List<OdysseyMapPart> parts) {
    this.parts = parts;
  }
  

  /**
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return getName();
  }
}
