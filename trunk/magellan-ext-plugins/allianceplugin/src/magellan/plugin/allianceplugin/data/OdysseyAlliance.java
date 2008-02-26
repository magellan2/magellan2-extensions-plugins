// Created on 26.02.2008
//
package magellan.plugin.allianceplugin.data;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import magellan.library.utils.Utils;
import magellan.library.utils.logging.Logger;

/**
 * A container for an alliance.
 *
 * @author <a href="thoralf@m84.de">Thoralf Rickert</a>
 * @version 1.0
 */
public class OdysseyAlliance {
  private static final Logger log = Logger.getInstance(OdysseyAlliance.class);
  
  private String name = null;
  private List<OdysseyAllianceMember> members = new ArrayList<OdysseyAllianceMember>();
  private List<OdysseyMap> maps = new ArrayList<OdysseyMap>();
  
  /**
   *   <alliance>
   *     <name>...</name>
   *     <members>
   *       <faction id="...">...</faction>
   *       ...
   *     </members>
   *     <map name="..." version="..." lastchange="..." round="...">
   *       <part id="">...</part>
   *       ...
   *     </map>
   *   </alliance>
   */
  public OdysseyAlliance(Element root) {
    if (!root.getNodeName().equals("alliance")) throw new IllegalArgumentException("This is not an alliance XML node.");
    
    name = Utils.getCData(Utils.getChildNode(root, "name"));
    log.info("Found Alliance '"+name+"'");
    
    List<Element> memberElements = Utils.getChildNodes(Utils.getChildNode(root, "members"), "faction");
    for (Element memberElement : memberElements) {
      members.add(new OdysseyAllianceMember(memberElement));
    }

    List<Element> mapElements = Utils.getChildNodes(root, "map");
    for (Element mapElement : mapElements) {
      maps.add(new OdysseyMap(mapElement));
    }
  }

  /**
   * This method returns the field maps
   *
   * @return the maps
   */
  public List<OdysseyMap> getMaps() {
    return maps;
  }

  /**
   * This method sets the field maps to maps
   *
   * @param maps the maps to set
   */
  public void setMaps(List<OdysseyMap> maps) {
    this.maps = maps;
  }

  /**
   * This method returns the field members
   *
   * @return the members
   */
  public List<OdysseyAllianceMember> getMembers() {
    return members;
  }

  /**
   * This method sets the field members to members
   *
   * @param members the members to set
   */
  public void setMembers(List<OdysseyAllianceMember> members) {
    this.members = members;
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
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return getName();
  }
  
}
