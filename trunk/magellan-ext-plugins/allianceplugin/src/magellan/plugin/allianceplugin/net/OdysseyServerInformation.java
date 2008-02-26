// Created on 26.02.2008
//
package magellan.plugin.allianceplugin.net;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import magellan.library.utils.Utils;
import magellan.library.utils.logging.Logger;
import magellan.plugin.allianceplugin.data.OdysseyAlliance;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * This class contains informations about the odyssey server that
 * are available after a connect. 
 *
 * @author <a href="thoralf@m84.de">Thoralf Rickert</a>
 * @version 1.0
 */
public class OdysseyServerInformation {
  private static final Logger log = Logger.getInstance(OdysseyServerInformation.class);
  
  private boolean error = false;
  
  private String servername = null;
  private List<OdysseyAlliance> alliances = new ArrayList<OdysseyAlliance>();
  
  /**
   * Creates a new odyssey server information object based on
   * the informations inside the xmlData String.
   * 
   * The String has the following format:
   * <odyssey version="1.0">
   *   <servername>...</servername>
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
   * </odyssey>
   * 
   */
  OdysseyServerInformation(String xmlData) {
    try { 
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(false);
      factory.setValidating(false);
      DocumentBuilder builder  = factory.newDocumentBuilder();
      Document document = builder.parse(new InputSource(new StringReader(xmlData)));
      Element rootNode = document.getDocumentElement();
      if (!rootNode.getNodeName().equals("odyssey")) {
        error = true;
        return;
      }
      
      servername = Utils.getCData(Utils.getChildNode(rootNode, "servername"));
      
      // multiple alliances? it's possible but we use just the first one.
      List<Element> allianceNodes = Utils.getChildNodes(rootNode, "alliance");
      for (Element allianceNode : allianceNodes) {
        alliances.add(new OdysseyAlliance(allianceNode));
      }
      
    } catch (Exception exception) {
      log.error("Error during XML validation",exception);
      error = true;
    }
  }
  
  /**
   * This method returns the field alliances
   *
   * @return the alliances
   */
  public List<OdysseyAlliance> getAlliances() {
    return alliances;
  }

  /**
   * This method sets the field alliances to alliances
   *
   * @param alliances the alliances to set
   */
  public void setAlliances(List<OdysseyAlliance> alliances) {
    this.alliances = alliances;
  }

  /**
   * This method returns the field servername
   *
   * @return the servername
   */
  public String getServername() {
    return servername;
  }

  /**
   * This method sets the field servername to servername
   *
   * @param servername the servername to set
   */
  public void setServername(String servername) {
    this.servername = servername;
  }

  /**
   * Returns true, if there is a problem with the given server informations.
   */
  public boolean hasErrors() {
    return error;
  }
}
