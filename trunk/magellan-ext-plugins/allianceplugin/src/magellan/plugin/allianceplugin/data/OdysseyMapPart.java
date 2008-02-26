// Created on 26.02.2008
//
package magellan.plugin.allianceplugin.data;

import java.text.NumberFormat;
import java.util.Date;

import magellan.library.utils.Utils;
import magellan.library.utils.logging.Logger;

import org.w3c.dom.Element;

/**
 * This is a container for a map part.
 *
 * @author <a href="thoralf@m84.de">Thoralf Rickert</a>
 * @version 1.0
 */
public class OdysseyMapPart {
  private static final Logger log = Logger.getInstance(OdysseyMapPart.class);

  private String name = null;
  private int version = 0;
  private Date lastchange = null;
  private int round = 0;
  private long size = 0l;

  /**
   *  part name="..." version="..." lastchange="..." round="...">...</part>
   */
  public OdysseyMapPart(Element root) {
    if (!root.getNodeName().equals("part")) throw new IllegalArgumentException("This is not a part XML node.");
    
    name = Utils.getCData(root);
    version = Integer.parseInt(root.getAttribute("version"));
    lastchange = Utils.toDate(root.getAttribute("lastchange"));
    round = Integer.parseInt(root.getAttribute("round"));
    size = Long.parseLong(root.getAttribute("size"));

    log.info("  - Found Part '"+name+"'");
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
   * This method returns the field round
   *
   * @return the round
   */
  public int getRound() {
    return round;
  }

  /**
   * This method sets the field round to round
   *
   * @param round the round to set
   */
  public void setRound(int round) {
    this.round = round;
  }

  /**
   * This method returns the field version
   *
   * @return the version
   */
  public int getVersion() {
    return version;
  }

  /**
   * This method sets the field version to version
   *
   * @param version the version to set
   */
  public void setVersion(int version) {
    this.version = version;
  }
  

  /**
   * This method returns the field size
   *
   * @return the size
   */
  public long getSize() {
    return size;
  }

  /**
   * This method sets the field size to size
   *
   * @param size the size to set
   */
  public void setSize(long size) {
    this.size = size;
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return getName()+" ("+toKBytes(getSize())+" KB)";
  }
  
  private String toKBytes(long value) {
    return NumberFormat.getInstance().format(value / 1024);
  }
}
