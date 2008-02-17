// Copyright 2003-2007 by magellan project team
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

/**
 * This plug in facilitates making teaching orders. To be included in the process, a unit must have
 * a meta order of the following type:
 * 
 * <pre>
 * // $$L Talent1 value1 Talent2 value2
 * </pre>
 * 
 * denotes a student learning two skills of different values
 * 
 * <pre>
 * // $$L ALLES maxVal minVal
 * </pre>
 * 
 * means that the student will learn any talent it already knows. The highest skill will have
 * maxVal; skills with lower ranks will have values down to minVal
 * 
 * <pre>
 * // $$T Talent1 maxDiff1 Talent2 maxDiff2
 * </pre>
 * 
 * denotes a teacher teaching two skills. Students having a skill differing more than maxDiff from
 * the teachers talent are penalized. maxDiff==0 has the special meaning that there is no such
 * penalty. maxDiff==1 means that the teacher will not teach this talent
 * 
 * <pre>
 * // $$T ALLES maxDiff
 * </pre>
 * 
 * denotes a teacher teaching all the skills he knows.
 * 
 * <pre>
 * // $$T ALLES 0 Hiebwaffen 2
 * </pre>
 * 
 * would also be feasible
 * 
 * <pre>
 * // $namespace1$T ...
 * </pre>
 * <pre>
 * // $namespace1$L ...
 * </pre>
 * 
 * defines an order belonging to a namespace; it can be used to teaching only to units with certain
 * namespace
 * 
 * It is feasible (in fact, desirable) for a unit to be teacher and student at the same time.
 * 
 *  @author stm
 */
public class TeachPlugin implements MagellanPlugIn, UnitContainerContextMenuProvider {
	private static Logger log = null;

	private Client client = null;
	private Properties properties = null;
	private GameData gd = null;

	/**
	 * @see magellan.client.extern.MagellanPlugIn#init(magellan.client.Client, java.util.Properties)
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

		// do teaching for this unit container
		JMenuItem editMenu = new JMenuItem("teach");
		editMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new Thread(new Runnable() {

					public void run() {
						Teacher.teach(container, new ProgressBarUI(client));

						dispatcher.fire(new GameDataEvent(client, client.getData()));
					}
				}).start();
			}
		});
		menu.add(editMenu);

		// clear all $$$ comments 
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
