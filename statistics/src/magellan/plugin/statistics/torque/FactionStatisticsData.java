package magellan.plugin.statistics.torque;


import java.io.PrintWriter;

import org.apache.torque.om.Persistent;

/**
 * Contains informations about turn specific faction data
 *
 * The skeleton for this class was autogenerated by Torque on:
 *
 * [Wed May 21 18:19:59 CEST 2008]
 *
 * You should add additional methods to this class to meet the
 * application requirements.  This class will only be generated as
 * long as it does not already exist in the output directory.
 */
public  class FactionStatisticsData extends BaseFactionStatisticsData implements Persistent {
  /** Serial version */
  private static final long serialVersionUID = 1211386799035L;

  /**
   * Saves the data of this object into a XML file.
   */
  public void save(PrintWriter pw) throws Exception {
    pw.println("<data factionId=\""+getFactionId()+"\" turn=\""+getTurn()+"\">");
    pw.println("<name>"+getName()+"</name>");
    pw.println("<description>"+getDescription()+"</description>");
    pw.println("<persons>"+getPersons()+"</persons>");
    pw.println("<units>"+getUnits()+"</units>");
    pw.println("<race>"+getRace()+"</race>");
    pw.println("<heroes>"+getHeroes()+"</heroes>");
    pw.println("<maxHeroes>"+getMaxHeroes()+"</maxHeroes>");
    pw.println("<maxMigrants>"+getMaxMigrants()+"</maxMigrants>");
    pw.println("<averageScore>"+getAverageScore()+"</averageScore>");
    pw.println("<score>"+getScore()+"</score>");
    pw.println("</data>");
  }

}
