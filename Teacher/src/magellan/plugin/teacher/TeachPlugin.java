// class magellan.plugin.FFplugin.FFplugin
// created on 04.07.2007
//
// Copyright 2003-2007 by magellan project team
//
// Author : $Fiete: $
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
package magellan.plugin.teacher;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import magellan.client.Client;
import magellan.client.event.EventDispatcher;
import magellan.client.extern.MagellanPlugIn;
import magellan.client.swing.ProgressBarUI;
import magellan.client.swing.context.UnitContainerContextMenuProvider;
import magellan.client.swing.preferences.PreferencesFactory;
import magellan.library.GameData;
import magellan.library.UnitContainer;
import magellan.library.event.GameDataEvent;
import magellan.library.utils.logging.Logger;


public class TeachPlugin implements MagellanPlugIn, UnitContainerContextMenuProvider {
  private static Logger log = null;

  private Client client = null;
  private Properties properties = null;
  private GameData gd = null;

  /**
   * @see magellan.client.extern.MagellanPlugIn#init(magellan.client.Client,
   *      java.util.Properties)
   */
  public void init(Client _client, Properties _properties) {
    // init the plugin
    this.client = _client;
    this.properties = _properties;

    log = Logger.getInstance(TeachPlugin.class);
    log.info(getName() + " initialized...");

  }

  /**
   * @see magellan.client.extern.MagellanPlugIn#init(magellan.library.GameData)
   */
  public void init(GameData data) {
    // init the report
    this.gd = data;
  }

  /**
   * @see magellan.client.extern.MagellanPlugIn#getMenuItems()
   */
  public List<JMenuItem> getMenuItems() {
      return Collections.emptyList();
  }

  /**
   * @see magellan.client.extern.MagellanPlugIn#getName()
   */
  public String getName() {
    return "FFplugin";
  }

  /**
   * @see magellan.client.swing.context.UnitContainerContextMenuProvider#createContextMenu(magellan.client.event.EventDispatcher,
   *      magellan.library.GameData, magellan.library.UnitContainer)
   */
  public JMenuItem createContextMenu(final EventDispatcher dispatcher, final GameData data,
      final UnitContainer container) {
    JMenu menu = new JMenu("TeachTest");

    JMenuItem editMenu = new JMenuItem("teach");
    editMenu.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        new Thread(new Runnable() {

          public void run() {
            Teacher.foo(container, new ProgressBarUI(client));

            dispatcher.fire(new GameDataEvent(client, client.getData()));
          }
        }).start();
      }
    });
    menu.add(editMenu);

    editMenu = new JMenuItem("teach2");
    editMenu.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        new Thread(new Runnable() {

          public void run() {
            TeacherNew.foo(container, new ProgressBarUI(client));

            dispatcher.fire(new GameDataEvent(client, client.getData()));
          }
        }).start();
      }
    });
    menu.add(editMenu);

    editMenu = new JMenuItem("clear");
    editMenu.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Teacher.clear(container);
        dispatcher.fire(new GameDataEvent(client, client.getData()));
      }
    });
    menu.add(editMenu);

    return menu;
  }

  /**
   * @see magellan.client.extern.MagellanPlugIn#quit(boolean)
   */
  public void quit(boolean storeSettings) {
    // do nothing
  }

  public PreferencesFactory getPreferencesProvider() {
    return null;
  }
}
