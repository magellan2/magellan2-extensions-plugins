// class magellan.plugin.statistics.db.DerbyConnector
// created on 20.05.2008
//
// Copyright 2003-2008 by Thoralf Rickert
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import magellan.library.utils.logging.Logger;

import org.apache.torque.Torque;

public class DerbyConnector {
  private static Logger log = Logger.getInstance(DerbyConnector.class);
  
  protected static String DERBY_HOME = ".";
  protected static String MAGELLAN_HOME = ".";
  protected static final String DATABASE_SCHEMA_FILE = "etc/statistics/statistics-schema.sql";
  protected static final String TORQUE_PROPERTIES_FILE = "etc/statistics/torque.properties";
  protected static final String DATABASE_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
  protected static final String DATABASE_NAME = "statistics";
  protected static final String DATABASE_USER = "statistics";
  protected static final String DATABASE_PWD  = "statistics";
  protected static final String protocol = "jdbc:derby:";
  
  protected static DerbyConnector _instance = null;
  protected boolean initialized = false;
  protected Properties settings = null;
  
  /**
   * Creates the singleton of this class.
   */
  protected DerbyConnector() {
    _instance = this;
  }
  
  /**
   * Returns the singleton of this Connector.
   */
  public static DerbyConnector getInstance() {
    if (_instance == null) _instance = new DerbyConnector();
    return _instance;
  }
  
  /**
   * This method initializes the database system. If the database doesn't
   * exist it will be created.
   */
  public boolean init() {
    return init(null,null,null);
  }
  
  
  /**
   * This method initializes the database system. If the database doesn't
   * exist it will be created.
   */
  public boolean init(Properties settings, File magellanHome, File settingsDirectory) {
    if (initialized) return true;
    
    if (settingsDirectory != null) DERBY_HOME = settingsDirectory.getAbsolutePath();
    if (magellanHome != null) MAGELLAN_HOME = magellanHome.getAbsolutePath();
    this.settings = settings;
    
    try {
      log.info("Load Derby drivers");
      ClassPathHack.addFile(new File(MAGELLAN_HOME+"/plugins/statisticsplugin/lib/derby.jar"));
      ClassPathHack.addFile(new File(MAGELLAN_HOME+"/plugins/statisticsplugin/lib/derbytools.jar"));
      ClassPathHack.addFile(new File(MAGELLAN_HOME+"/lib/derbytools.jar"));
      Class.forName(DATABASE_DRIVER).newInstance();
    } catch (Exception exception) {
      log.error(exception);
    }

    
    if (!checkDatabase()) {
      if (!createDatabase()) {
        log.error("Could not create database");
        return false;
      }
    } else {
      log.info("Database exists");
    }

    try {
      // initialize torque
      log.info("Initializing persistance layer");
      Torque.init(TORQUE_PROPERTIES_FILE);
      Connection connection = Torque.getConnection();
      if (connection != null) {
        log.info("Database connection established");
        
        updateDatabase(connection);
        
        connection.close();
      } else {
        log.error("no database connection availabe");
        return false;
      }
    } catch (Exception exception) {
      exception.printStackTrace(System.err);
    }
    
    initialized = true;
    
    return true;
  }
  
  /**
   * This method shuts down the database connection. It's equivalent to init()
   */
  public void shutdown() {
    try {
      log.info("Shutting down database connections");
      if (Torque.isInit()) Torque.shutdown();
  //    DriverManager.getConnection("jdbc:derby:;shutdown=true");
    } catch (Exception exception) {
      exception.printStackTrace(System.err);
    }
  }
  
  /**
   * This method checks, if the database files exists. It checks, if there
   * is a directory called "statistics" in the Magellan home directory.
   */
  protected boolean checkDatabase() {
    File directory = new File(DERBY_HOME+"/"+DATABASE_NAME);
    return directory.exists() && directory.canWrite();
  }
  
  /**
   * This method creates a new database inside the Magellan home and
   * imports the database structure from a known sql file.
   */
  protected boolean createDatabase() {
    try {
      Properties properties = new Properties();
      properties.put("user", DATABASE_USER);
      properties.put("password", DATABASE_PWD);
      properties.put("derby.system.home", DERBY_HOME);
      
      // create database
      log.info("Create database");
      Connection connection = DriverManager.getConnection(protocol + DATABASE_NAME + ";create=true", properties);
      
      log.info("Creating database structure");
      FileReader fr = new FileReader(MAGELLAN_HOME+"/"+DATABASE_SCHEMA_FILE);
      BufferedReader br = new BufferedReader(fr);
      LineNumberReader reader = new LineNumberReader(br);
      StringBuffer buffer = new StringBuffer();
      String line = null;
      while ((line = reader.readLine()) != null) {
        if (line.startsWith("--")) continue; // ignore comments
        if (line.startsWith("drop ")) continue; // ignore drop table
        if (line.trim().endsWith(";")) {
          line = line.trim();
          line = line.substring(0,line.length()-1);
          buffer.append(line);
          
          String sql = buffer.toString();
          System.out.println("'"+sql+"'");
          
          buffer = new StringBuffer();
          
          try {
            // execute single SQL commands (with multiple lines).
            Statement s = connection.createStatement();
            s.execute(sql);
          } catch (Exception exception) {
            // ignore single sql errors.
            exception.printStackTrace(System.err);
          }
        } else {
          buffer.append(line).append("\r\n");
        }
      }
      
      reader.close();
      br.close();
      fr.close();
      
      return true;
    } catch (Exception exception) {
      exception.printStackTrace(System.err);
      return false;
    }
  }
  
  protected void updateDatabase(Connection connection) throws SQLException {
//    String sql = "ALTER TABLE report ADD lastsave BIGINT";
//    connection.prepareStatement(sql).execute();
  }
  
  public static void main(String[] args) {
    DerbyConnector connector = DerbyConnector.getInstance();
    connector.init();
    connector.shutdown();
  }
}
