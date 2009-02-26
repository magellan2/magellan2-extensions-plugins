// class magellan.plugin.statistics.db.CriteriaEnumeration
// created on 30.01.2009
//
// Copyright 2003-2009 by Thoralf Rickert
//
// Author : $Author: $
// $Id: $
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program (see doc/LICENCE.txt); if not, write to the
// Free Software Foundation, Inc., 
// 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
// 
package magellan.plugin.statistics.db;

import java.sql.Connection;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import magellan.library.utils.logging.Logger;

import org.apache.torque.util.Criteria;

/**
 * Mit Hilfe dieser Klasse kann man durch eine Ergebnismenge aus Torque iterieren.
 * 
 * Dafür benötigt man zwei Sachen.
 * <ul>
 * <li>Criteria: Dies ist die Abfrage, die an die Datenbank gestellt werden soll</li>
 * <li>Populator: Der dient dazu, aus der Abfrage eine Liste von Elementen zu generieren</li>
 * </ul>
 *
 * @author <a href="mailto:alexander.teuscher@cadooz.de">Alexander Teuscher</a>
 * @author <a href="mailto:marcus.linke@cadooz.de">Marcus Linke</a>
 * @author <a href="mailto:thoralf.rickert@cadooz.de">Thoralf Rickert</a>
 * @version 1.0, erstellt am 09.03.2007
 */
public class CriteriaEnumeration<E> implements Enumeration {
  private static Logger log = Logger.getInstance(DerbyConnector.class);
  private final Vector<E> fifo = new Vector<E>();
  
  private Criteria criteria = null;
  private CriteriaPopulator<E> populator = null;
  private Connection connection = null;
  
  private int offset = 0;
  private int limit = 1000;
  private boolean eot = false;
  private boolean useTransaction = false;

  /**
   * Übergeben Sie hier die Abfrage, die in einer Enumeration
   * an die Datenbank abgegeben werden soll.
   */
  public void setCriteria(Criteria criteria) {
    this.criteria = criteria;
  }
  
  /**
   * Setzt den Populator, mit dem die Abfrage der Datenbank in Elemente
   * gewandelt wird.
   */
  public void setPopulator(CriteriaPopulator<E> populator) {
    this.populator = populator;
  }
  
  /**
   * Wenn true, dann wird die gesamte Enumeration in einer Transaktion
   * abgearbeitet.
   */
  public void setTransaction(boolean useTransaction) {
    this.useTransaction = useTransaction;
  }
  
  /**
   * Setzt die Größe des Puffers der bei Abfragen an die Datenbank
   * benutzt werden soll. Default ist 1000
   */
  public void setLimit(int limit) {
    this.limit = limit;
  }
  
  /**
   * Prüft, ob es weitere Elemente gibt.
   * 
   * @see java.util.Enumeration#hasMoreElements()
   */
  public boolean hasMoreElements() {
    if (!fifo.isEmpty()) return true;
    if (eot) return false;
    
    criteria.setLimit(limit);
    criteria.setOffset(offset);
    offset+=limit;
    
    if (useTransaction && connection == null) {
      try {
        connection = Transaction.begin();
      } catch (Exception exception) {
        log.fatal("Die Verwendung von Transaktion ist nicht möglich.",exception);
        connection = null;
      }
    }
    
    List<E> elements = populator.getRows(connection,criteria);
    if (elements == null || elements.size()==0) {
      eot = true;
      if (connection != null) {
        try {
          Transaction.commit(connection);
        } catch (Exception exception) {
          log.fatal(exception);
        }
      }
    }
    if (eot) return false;
    
    fifo.addAll(elements);
    
    return true;
  }
  
  /**
   * Liefert das nächste Element oder null.
   * 
   * @see java.util.Enumeration#nextElement()
   */
  public E nextElement() {
    if (!hasMoreElements()) return null;
    return fifo.remove(0);
  }
  
  
}
