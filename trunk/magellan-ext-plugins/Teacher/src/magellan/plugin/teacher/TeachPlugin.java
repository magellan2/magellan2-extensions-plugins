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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import magellan.client.Client;
import magellan.client.event.EventDispatcher;
import magellan.client.extern.MagellanPlugIn;
import magellan.client.swing.ProgressBarUI;
import magellan.client.swing.context.UnitContainerContextMenuProvider;
import magellan.client.swing.context.UnitContextMenuProvider;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.client.swing.preferences.PreferencesFactory;
import magellan.library.GameData;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.event.GameDataEvent;
import magellan.library.utils.Locales;
import magellan.library.utils.logging.Logger;
import magellan.plugin.teacher.Teacher.SUnit;

/**
 * This plug in facilitates making teaching orders. To be included in the process, a unit must have
 * a meta order of the following type:
 * 
 * <pre>
 * // $$L Talent1 value1 Talent2 value2
 * </pre>
 * 
 * denotes a student learning two skills of different values.
 * 
 * <pre>
 * // $$L ALLES maxVal minVal
 * </pre>
 * 
 * means that the student will learn any talent it already knows. The highest skill will have
 * maxVal; skills with lower ranks will have values down to minVal.
 * 
 * <pre>
 * // $$T Talent1 maxDiff1 Talent2 maxDiff2
 * </pre>
 * 
 * denotes a teacher teaching two skills. Students having a skill differing more than maxDiff from
 * the teachers talent are penalized. maxDiff==0 has the special meaning that there is no such
 * penalty. maxDiff==1 means that the teacher will not teach this talent.
 * 
 * <pre>
 * // $$T ALLES maxDiff
 * </pre>
 * 
 * denotes a teacher teaching all the skills he knows,
 * 
 * <pre>
 * // $$T ALLES 0 Hiebwaffen 2
 * </pre>
 * 
 * would also be feasible.
 * 
 * <pre>
 * // $namespace1$T ...
 * // $namespace1$L ...
 * </pre>
 * 
 * defines an order belonging to a namespace; it can be used to teaching only to units having a
 * certain namespace.
 * 
 * It is feasible for a unit to be teacher and student at the same time. In fact, any unit with
 * teaching orders <i>must</i> also have a learning order.
 * 
 * @author stm
 */
public class TeachPlugin implements MagellanPlugIn, UnitContainerContextMenuProvider,
		UnitContextMenuProvider, ActionListener {
	private static Logger log = null;

	private Client client = null;
	@SuppressWarnings("unused")
	private Properties properties = null;
	private GameData gd = null;

	private String namespace = null;

	/**
	 * An enum for all action types in this plugin.
	 * 
	 * @author Thoralf Rickert
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

		// JMenuItem executeMenu = new JMenuItem(getString("mainmenu.execute.title"));
		// executeMenu.setActionCommand(PlugInAction.EXECUTE.getID());
		// executeMenu.addActionListener(this);
		// menu.add(executeMenu);
		//
		JMenuItem executeAllMenu = new JMenuItem(getString("plugin.teacher.mainmenu.executeall.title"));
		executeAllMenu.setActionCommand(PlugInAction.EXECUTE_ALL.getID());
		executeAllMenu.addActionListener(this);
		menu.add(executeAllMenu);

		// JMenuItem clearMenu = new JMenuItem(getString("mainmenu.clear.title"));
		// clearMenu.setActionCommand(PlugInAction.CLEAR.getID());
		// clearMenu.addActionListener(this);
		// menu.add(clearMenu);

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

		// set tags
		editMenu = new JMenuItem(getString("plugin.teacher.contextmenu.tag.title"));
		editMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doParse(container.units());
			}
		});
		menu.add(editMenu);

    // clear tags
    editMenu = new JMenuItem(getString("plugin.teacher.contextmenu.untag.title"));
    editMenu.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        doUnTag(container.units());
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

	@SuppressWarnings("unchecked")
	public JMenuItem createContextMenu(EventDispatcher dispatcher, GameData data, final Unit unit,
			final Collection selectedObjects) {
		JMenu menu = new JMenu(getString("plugin.teacher.contextmenu.title"));

		JMenuItem addLearnMenu = new JMenuItem(getString("plugin.teacher.contextmenu.addlearn.title",
				new Object[] { unit.getName(), unit.getID().toString() }));
		addLearnMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String userInput = JOptionPane.showInputDialog(client,
						getString("plugin.teacher.addlearn.askTalent.message"), "ALLES 100 51");
				if (userInput != null) {
					try {
						StringTokenizer st = new StringTokenizer(userInput, " ", false);
						String talent = st.nextToken();
						double value = Double.parseDouble(st.nextToken());
						Order newOrder;
						if (talent.equals(Order.ALL)) {
							double value2 = Double.parseDouble(st.nextToken());
							newOrder = new Order(value, value2);
						} else {
							newOrder = new Order(talent, value);
						}
						Collection<Unit> units = null;
						if (selectedObjects != null) {
							units = new ArrayList<Unit>(selectedObjects.size());
							for (Object o : selectedObjects) {
								if (o instanceof Unit)
									units.add((Unit) o);
							}
						}

						addOrder(unit, units, newOrder);
					} catch (Exception ex) {
						log.warn(ex);
						JOptionPane.showMessageDialog(client, getString("plugin.teacher.addlearn.error"));
					}
				}
			}
		});
		menu.add(addLearnMenu);

		JMenuItem addTeachMenu = new JMenuItem(getString("plugin.teacher.contextmenu.addteach.title",
				new Object[] { unit.getName(), unit.getID().toString() }));
		addTeachMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String userInput = JOptionPane.showInputDialog(client,
						getString("plugin.teacher.addteach.askTalent.message"), "ALLES 0");
				if (userInput != null) {
					try {
						StringTokenizer st = new StringTokenizer(userInput, " ", false);
						String talent = st.nextToken();
						int diff = Integer.parseInt(st.nextToken());
						Order newOrder = new Order(talent, diff);
						Collection<Unit> units = null;
						if (selectedObjects != null) {
							units = new ArrayList<Unit>(selectedObjects.size());
							for (Object o : selectedObjects) {
								if (o instanceof Unit)
									units.add((Unit) o);
							}
						}

						addOrder(unit, units, newOrder);
					} catch (Exception ex) {
						log.warn(ex);
						JOptionPane.showMessageDialog(client, getString("plugin.teacher.addlearn.error"));
					}
				}
			}
		});
		menu.add(addTeachMenu);

		JMenuItem delLearnMenu = new JMenuItem(getString("plugin.teacher.contextmenu.dellearn.title",
				new Object[] { unit.getName(), unit.getID().toString() }));
		delLearnMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String talent = JOptionPane.showInputDialog(client,
						getString("plugin.teacher.dellearn.askTalent.message"), "ALLES");
				if (talent != null) {
					try {
						Order newOrder;
						if (talent.equals(Order.ALL))
							newOrder = new Order(1d, 1d);
						else
							newOrder = new Order(talent, 1d);
						Collection<Unit> units = null;
						if (selectedObjects != null) {
							units = new ArrayList<Unit>(selectedObjects.size());
							for (Object o : selectedObjects) {
								if (o instanceof Unit)
									units.add((Unit) o);
							}
						}

						delOrder(unit, units, newOrder);
					} catch (Exception ex) {
						log.warn(ex);
						JOptionPane.showMessageDialog(client, getString("plugin.teacher.addlearn.error"));
					}
				}
			}
		});
		menu.add(delLearnMenu);
		boolean hasOrder = false;
		if (selectedObjects==null){
			hasOrder = hasLearnOrder(unit);
		} else {
			for (Object o : selectedObjects){
				if (o instanceof Unit){
					if (hasLearnOrder((Unit) o)){
						hasOrder = true;
						break;
					}
				}
			}
		}
		delLearnMenu.setEnabled(hasOrder);

		JMenuItem delTeachMenu = new JMenuItem(getString("plugin.teacher.contextmenu.delteach.title",
				new Object[] { unit.getName(), unit.getID().toString() }));
		delTeachMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String talent = JOptionPane.showInputDialog(client,
						getString("plugin.teacher.delteach.askTalent.message"), "ALLES");
				if (talent != null) {
					try {
						Order newOrder;
						if (talent.equals(Order.ALL))
							newOrder = new Order(0);
						else
							newOrder = new Order(talent, 0);
						Collection<Unit> units = null;
						if (selectedObjects != null) {
							units = new ArrayList<Unit>(selectedObjects.size());
							for (Object o : selectedObjects) {
								if (o instanceof Unit)
									units.add((Unit) o);
							}
						}

						delOrder(unit, units, newOrder);
					} catch (Exception ex) {
						log.warn(ex);
						JOptionPane.showMessageDialog(client, getString("plugin.teacher.addlearn.error"));
					}
				}
			}

		});
		menu.add(delLearnMenu);
		hasOrder = false;
		if (selectedObjects==null){
			hasOrder = hasLearnOrder(unit);
		} else {
			for (Object o : selectedObjects){
				if (o instanceof Unit){
					if (hasTeachOrder((Unit) o)){
						hasOrder = true;
						break;
					}
				}
			}
		}
		delTeachMenu.setEnabled(hasOrder);
		menu.add(delTeachMenu);

		return menu;

	}

	private boolean hasLearnOrder(Unit unit) {
		SUnit su = Teacher.parseUnit(unit, namespace, false);
		return su!=null && !su.getLearnTalents().isEmpty();
	}

	private boolean hasTeachOrder(Unit unit) {
		SUnit su = Teacher.parseUnit(unit, namespace, false);
		return su!=null && !su.getTeachTalents().isEmpty();
	}

	protected void delOrder(Unit unit, Collection<Unit> selectedObjects, Order newOrder) {
		Teacher.delOrder(selectedObjects != null ? selectedObjects : Collections.singletonList(unit),
				namespace, newOrder);
		client.getDispatcher().fire(new GameDataEvent(client, client.getData()));
	}

	protected void addOrder(Unit unit, Collection<Unit> selectedObjects, Order newOrder) {
		Teacher.addOrder(selectedObjects != null ? selectedObjects : Collections.singletonList(unit),
				namespace, newOrder);
		client.getDispatcher().fire(new GameDataEvent(client, client.getData()));
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

  private void doParse(final Collection<Unit> values) {
    new Thread(new Runnable() {

      public void run() {
        Teacher.parse(values, namespace, new ProgressBarUI(client));
        client.getDispatcher().fire(new GameDataEvent(client, client.getData()));
      }
    }).start();

  }

  private void doUnTag(final Collection<Unit> values) {
    new Thread(new Runnable() {

      public void run() {
        Teacher.untag(values, namespace, new ProgressBarUI(client));
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

			GridBagConstraints con = new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST,
					GridBagConstraints.HORIZONTAL, new Insets(3, 3, 3, 3), 0, 0);

			JLabel lblNamespace = new JLabel(getString("plugin.teacher.preferences.label.namespace"));
			panel.add(lblNamespace, con);

			txtNamespace = new JTextArea(namespace);
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

	protected static String getString(String key) {
		ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_NAME, Locales.getGUILocale());
		try {
			return bundle.getString(key);
		} catch (MissingResourceException e) {
			log.warn("resource key " + key + " not found in bundle " + BUNDLE_NAME + ", "
					+ Locales.getGUILocale());
			return '!' + key + '!';
		}
	}

	protected static String getString(String key, Object[] args) {
		String value = getString(key);
		if (value != null) {
			value = new MessageFormat(value).format(args);
		}
		return value;
	}

}
