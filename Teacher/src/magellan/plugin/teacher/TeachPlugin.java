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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import magellan.client.Client;
import magellan.client.event.EventDispatcher;
import magellan.client.extern.MagellanPlugIn;
import magellan.client.swing.ProgressBarUI;
import magellan.client.swing.context.UnitContainerContextMenuProvider;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.client.swing.preferences.PreferencesFactory;
import magellan.library.GameData;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.event.GameDataEvent;
import magellan.library.utils.Locales;
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
 * @author stm
 */
public class TeachPlugin implements MagellanPlugIn, UnitContainerContextMenuProvider,
		ActionListener {
	private static Logger log = null;

	private Client client = null;
	private Properties properties = null;
	private GameData gd = null;

	private String namespace = null;

	/**
	 * An enum for all action types in this plugin.
	 * 
	 * @author Thoralf Rickert
	 * @version 1.0, 11.09.2007
	 */
	public enum PlugInAction {
		EXECUTE("mainmenu.execute"), EXECUTE_ALL("mainmenu.executeall"), CLEAR("mainmenu.clear"), CLEAR_ALL(
				"mainmenu.clearall"), UNKNOWN("");

		private String id;

		private PlugInAction(String id) {
			this.id = id;
		}

		public String getID() {
			return id;
		}

		public static PlugInAction getAction(ActionEvent e) {
			if (e == null)
				return UNKNOWN;
			for (PlugInAction action : values()) {
				if (action.id.equalsIgnoreCase(e.getActionCommand()))
					return action;
			}
			return UNKNOWN;
		}

	}

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
		List<JMenuItem> items = new ArrayList<JMenuItem>();

		JMenu menu = new JMenu(getString("plugin.teacher.mainmenu.title"));
		items.add(menu);

//		JMenuItem executeMenu = new JMenuItem(getString("mainmenu.execute.title"));
//		executeMenu.setActionCommand(PlugInAction.EXECUTE.getID());
//		executeMenu.addActionListener(this);
//		menu.add(executeMenu);
//
		JMenuItem executeAllMenu = new JMenuItem(getString("plugin.teacher.mainmenu.executeall.title"));
		executeAllMenu.setActionCommand(PlugInAction.EXECUTE_ALL.getID());
		executeAllMenu.addActionListener(this);
		menu.add(executeAllMenu);

//		JMenuItem clearMenu = new JMenuItem(getString("mainmenu.clear.title"));
//		clearMenu.setActionCommand(PlugInAction.CLEAR.getID());
//		clearMenu.addActionListener(this);
//		menu.add(clearMenu);

		JMenuItem clearAllMenu = new JMenuItem(getString("plugin.teacher.mainmenu.clearall.title"));
		clearAllMenu.setActionCommand(PlugInAction.CLEAR_ALL.getID());
		clearAllMenu.addActionListener(this);
		menu.add(clearAllMenu);

		return items;
	}

	/**
	 * @see magellan.client.extern.MagellanPlugIn#getName()
	 */
	public String getName() {
		return getString("plugin.teacher.name");
	}

	/**
	 * @see magellan.client.swing.context.UnitContainerContextMenuProvider#createContextMenu(magellan.client.event.EventDispatcher,
	 *      magellan.library.GameData, magellan.library.UnitContainer)
	 */
	public JMenuItem createContextMenu(final EventDispatcher dispatcher, final GameData data,
			final UnitContainer container) {
		JMenu menu = new JMenu(getString("plugin.teacher.contextmenu.title"));

		// do teaching for this unit container
		JMenuItem editMenu = new JMenuItem(getString("plugin.teacher.contextmenu.execute.title"));
		editMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doTeach(container.units());
			}
		});
		menu.add(editMenu);

		// clear all $$$ comments
		editMenu = new JMenuItem(getString("plugin.teacher.contextmenu.clear.title"));
		editMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doClear(container.units());
			}
		});
		menu.add(editMenu);

		return menu;
	}

	public void actionPerformed(ActionEvent e) {
		log.info(e.getActionCommand());
		switch (PlugInAction.getAction(e)) {
		case EXECUTE_ALL: {
			doTeach(gd.units().values());
			break;
		}
		case CLEAR_ALL: {
			doClear(gd.units().values());
		}
		}

	}

	private void doClear(final Collection<Unit> values) {
		new Thread(new Runnable() {
			public void run() {
				Teacher.clear(values, namespace);
				client.getDispatcher().fire(new GameDataEvent(client, client.getData()));
			}
		}).start();
	}

	private void doTeach(final Collection<Unit> values) {
		new Thread(new Runnable() {

			public void run() {
				Teacher.teach(values, namespace, new ProgressBarUI(client));
				client.getDispatcher().fire(new GameDataEvent(client, client.getData()));
			}
		}).start();

	}

	/**
	 * @see magellan.client.extern.MagellanPlugIn#quit(boolean)
	 */
	public void quit(boolean storeSettings) {
		// do nothing
	}

	public PreferencesFactory getPreferencesProvider() {
		return new PreferencesFactory() {

			public PreferencesAdapter createPreferencesAdapter() {
				return new TeachPreferences();
			}

		};
	}

	class TeachPreferences implements PreferencesAdapter {

		JTextArea txtNamespace;

		public void applyPreferences() {
			namespace = txtNamespace.getText();
		}

		public Component getComponent() {
			JPanel panel = new JPanel(new GridBagLayout());
			panel.setBorder(new javax.swing.border.TitledBorder(BorderFactory.createEtchedBorder(),
					getString("plugin.teacher.preferences.label.namespace")));

			GridBagConstraints con = new GridBagConstraints(0, 0, 1, 1, 0, 0,
					GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(3, 3, 3, 3),
					0, 0);

			JLabel lblNamespace = new JLabel(getString("plugin.teacher.preferences.label.namespace"));
			panel.add(lblNamespace, con);

			txtNamespace = new JTextArea();
			txtNamespace.setMinimumSize(new Dimension(100, 20));
			txtNamespace.setPreferredSize(new java.awt.Dimension(100, 20));
			// txtNamespace.setText("stm");

			con.insets.left = 0;
			con.gridx = 1;
			panel.add(txtNamespace, con);

			return panel;
		}

		public String getTitle() {
			return getString("plugin.teacher.preferences.title");
		}

		public void initPreferences() {

		}

	}

	  private static final String BUNDLE_NAME = "magellan.plugin.teacher.teacher_resources";

	  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	  public static String getString(String key) {
		ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_NAME, Locales.getGUILocale());
	    try {
	      return bundle.getString(key);
	    } catch (MissingResourceException e) {
	      log.warn("resource key "+key+" not found in bundle "+BUNDLE_NAME+", "+Locales.getGUILocale());
	      return '!' + key + '!';
	    }
	  }

}
