// class magellan.plugin.statistics.db.Transaction
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

import magellan.plugin.statistics.torque.ReportPeer;

/**
 * Diese Klasse dient dazu, Datenbanktransaktionen besser
 * zu kapseln.  
 *
 * @author <a href="mailto:thoralf.rickert@cadooz.de">Thoralf Rickert</a>
 * @version 1.0, erstellt am 25.08.2005
 */
public class Transaction {
  /**
   * Begin a transaction.  This method will fallback gracefully to
   * return a normal connection, if the database being accessed does
   * not support transactions.
   * 
   * @return The Connection for the transaction.
   * @throws Exception Any exceptions caught during processing will be
   *         rethrown wrapped into a Exception.
   */
  public static Connection begin() throws Exception {
    return begin(ReportPeer.DATABASE_NAME);
  }

  /**
   * Begin a transaction.  This method will fallback gracefully to
   * return a normal connection, if the database being accessed does
   * not support transactions.
   * 
   * @return The Connection for the transaction.
   * @throws Exception Any exceptions caught during processing will be
   *         rethrown wrapped into a Exception.
   */
  public static Connection begin(String db) throws Exception {
    Connection connection = org.apache.torque.util.Transaction.begin(db);
    connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
    return connection;
  }
  

  /**
   * Begin a transaction.  This method will fallback gracefully to
   * return a normal connection, if the database being accessed does
   * not support transactions.
   * 
   * @param level kann eine der folgenden Werte sein.
   *              <ul>
   *               <li>Connection.TRANSACTION_READ_UNCOMMITTED
   *               <li>Connection.TRANSACTION_READ_COMMITTED
   *               <li>Connection.TRANSACTION_REPEATABLE_READ
   *               <li>Connection.TRANSACTION_SERIALIZABLE
   *             </ul>
   * @return The Connection for the transaction.
   * @throws Exception Any exceptions caught during processing will be
   *         rethrown wrapped into a Exception.
   */
  public static Connection begin(int level) throws Exception {
    Connection connection = begin();
    connection.setTransactionIsolation(level);
    return connection;
  }
  
  /**
   * Commit a transaction.  This method takes care of releasing the
   * connection after the commit.  In databases that do not support
   * transactions, it only returns the connection.
   *
   * @param con The Connection for the transaction.
   * @throws Exception Any exceptions caught during processing will be
   *         rethrown wrapped into a Exception.
   */
  public static void commit(Connection connection) throws Exception {
    org.apache.torque.util.Transaction.commit(connection);
  }
  
  /**
   * Roll back a transaction in databases that support transactions.
   * It also releases the connection. In databases that do not support
   * transactions, this method will log the attempt and release the
   * connection.
   *
   * @param con The Connection for the transaction.
   * @throws Exception Any exceptions caught during processing will be
   *         rethrown wrapped into a Exception.
   */
  public static void rollback(Connection connection) throws Exception {
    org.apache.torque.util.Transaction.rollback(connection);
  }
  
  /**
   * Roll back a transaction without throwing errors if they occur.
   * A null Connection argument is logged at the debug level and other
   * errors are logged at warn level.
   * 
   * @param con The Connection for the transaction.
   * @see safeRollback
   */
  public static void safeRollback(Connection connection) {
    org.apache.torque.util.Transaction.safeRollback(connection);
  }
}
