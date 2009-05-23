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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import magellan.client.Client;
import magellan.client.event.EventDispatcher;
import magellan.client.extern.MagellanPlugIn;
import magellan.client.swing.ProgressBarUI;
import magellan.client.swing.context.UnitContainerContextMenuProvider;
import magellan.client.swing.context.UnitContextMenuProvider;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.client.swing.preferences.PreferencesFactory;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.event.GameDataEvent;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;

/**
 * This plug in facilitates making teaching orders. To be included in the process, a unit must have
 * a meta order of the following type:
 * 
 * <pre>
 * // $$L prio Talent1 target1 max1 Talent2 target2 max2
 * </pre>
 * 
 * denotes a student learning two skills of different values.
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
	public static final String NAMESPACE_PROPERTY = "plugins.teacher.namespace";

	public static final String UNCONFIRM_PROPERTY = "plugins.teacher.unconfirm";

	public static final String CONFIRMFULLTEACHERS_PROPERTY = "plugins.teacher.confirmfullteachers";

	public static final String CONFIRMEMPTYTEACHERS_PROPERTY = "plugins.teacher.confirmemptyteachers";

	public static final String CONFIRMTAUGHTSTUDENTS_PROPERTY = "plugins.teacher.confirmtaughtstudents";

	public static final String CONFIRMUNTAUGHTSTUDENTS_PROPERTY = "plugins.teacher.confirmuntaughtstudents";

	public static final String PERCENTFULL_PROPERTY = "plugins.teacher.percentfull";

	private static Logger log = null;

	private Client client = null;

	private Properties properties = null;
	private GameData gd = null;

	private String namespace = "";

	public boolean unconfirm = true;

	public boolean confirmFullTeachers = true;

	public boolean confirmEmptyTeachers = false;

	public boolean confirmTaughtStudents = true;

	public boolean confirmUntaughtStudents = false;

	public int percentFull;

	protected Boolean running = false;

	/**
	 * An enum for all action types in this plugin.
	 * 
	 * @author Thoralf Rickert, stm
	 */
	public enum PlugInAction {
		EXECUTE("mainmenu.execute"), EXECUTE_ALL("mainmenu.executeall"), TAG_ALL("mainmenu.tagall"), UNTAG_ALL(
				"mainmenu.untagall"), CLEAR("mainmenu.clear"), CLEAR_ALL("mainmenu.clearall"), CONVERT_ALL(
				"mainmenu.convertall"), PANEL("mainmenu.panel"), UNKNOWN("");

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
		Resources.getInstance().initialize(Client.getSettingsDirectory(), "teachplugin_");

		initProperties();

		log = Logger.getInstance(TeachPlugin.class);
		log.info(getName() + " initialized...");
	}

	private void initProperties() {
		setNamespace(properties.getProperty(NAMESPACE_PROPERTY, getNamespace()));
		unconfirm = properties.getProperty(UNCONFIRM_PROPERTY,
				unconfirm ? "true" : "false").equals("true");
		confirmEmptyTeachers = properties.getProperty(CONFIRMEMPTYTEACHERS_PROPERTY,
				confirmEmptyTeachers ? "true" : "false").equals("true");
		confirmFullTeachers = properties.getProperty(CONFIRMFULLTEACHERS_PROPERTY,
				confirmFullTeachers ? "true" : "false").equals("true");
		confirmTaughtStudents = properties.getProperty(CONFIRMTAUGHTSTUDENTS_PROPERTY,
				confirmTaughtStudents ? "true" : "false").equals("true");
		confirmUntaughtStudents = properties.getProperty(CONFIRMUNTAUGHTSTUDENTS_PROPERTY,
				confirmUntaughtStudents ? "true" : "false").equals("true");
		percentFull = Integer.parseInt(properties.getProperty(PERCENTFULL_PROPERTY, String
				.valueOf(percentFull)));
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
		JMenuItem namespaceMenu = new JMenuItem(getString("plugin.teacher.mainmenu.namespace.title",
				new Object[] { getNamespace() }));
		namespaceMenu.setEnabled(false);
		menu.add(namespaceMenu);

		JMenuItem executeAllMenu = new JMenuItem(getString("plugin.teacher.mainmenu.executeall.title"));
		executeAllMenu.setActionCommand(PlugInAction.EXECUTE_ALL.getID());
		executeAllMenu.addActionListener(this);
		menu.add(executeAllMenu);

		JMenuItem tagAllMenu = new JMenuItem(getString("plugin.teacher.mainmenu.tagall.title"));
		tagAllMenu.setActionCommand(PlugInAction.TAG_ALL.getID());
		tagAllMenu.addActionListener(this);
		menu.add(tagAllMenu);

		JMenuItem untagAllMenu = new JMenuItem(getString("plugin.teacher.mainmenu.untagall.title"));
		untagAllMenu.setActionCommand(PlugInAction.UNTAG_ALL.getID());
		untagAllMenu.addActionListener(this);
		menu.add(untagAllMenu);

		JMenuItem clearAllCommentsMenu = new JMenuItem(
				getString("plugin.teacher.mainmenu.clearall.title"));
		clearAllCommentsMenu.setActionCommand(PlugInAction.CLEAR_ALL.getID());
		clearAllCommentsMenu.addActionListener(this);
		menu.add(clearAllCommentsMenu);

		JMenuItem convertAllMenu = new JMenuItem(getString("plugin.teacher.mainmenu.convertall.title"));
		convertAllMenu.setActionCommand(PlugInAction.CONVERT_ALL.getID());
		convertAllMenu.addActionListener(this);
		menu.add(convertAllMenu);

		JMenuItem panelMenu = new JMenuItem(getString("plugin.teacher.mainmenu.panel.title"));
		panelMenu.setActionCommand(PlugInAction.PANEL.getID());
		panelMenu.addActionListener(this);
		menu.add(panelMenu);

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
	 *      magellan.library.GameData, magellan.library.UnitContainer, Collection)
	 */
	public JMenuItem createContextMenu(final EventDispatcher dispatcher, final GameData data,
			final UnitContainer container, final Collection selectedObjects) {
		JMenu menu = new JMenu(getString("plugin.teacher.contextmenu.title"));

		// do teaching for this unit container
		JMenuItem editMenu = new JMenuItem(getString("plugin.teacher.contextmenu.execute.title"));
		editMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new Thread(new Runnable() {
					public void run() {
						TeachClosingListener listener = new TeachClosingListener();
						ProgressBarUI ui = new ProgressBarUI(client, true, 50, listener);
						doTeachUnits(container.units(), listener, ui);
						client.getDispatcher().fire(new GameDataEvent(client, client.getData()));
					}
				}).run();
			}
		});
		menu.add(editMenu);

		// set tags
		editMenu = new JMenuItem(getString("plugin.teacher.contextmenu.tag.title"));
		editMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new Thread(new Runnable() {
					public void run() {
						TeachClosingListener listener = new TeachClosingListener();
						ProgressBarUI ui = new ProgressBarUI(client, true, 50, listener);
						doParse(container.units(), listener, ui);
						client.getDispatcher().fire(new GameDataEvent(client, client.getData()));
					}
				}).run();
			}
		});
		menu.add(editMenu);

		// clear tags
		editMenu = new JMenuItem(getString("plugin.teacher.contextmenu.untag.title"));
		editMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new Thread(new Runnable() {
					public void run() {
						TeachClosingListener listener = new TeachClosingListener();
						ProgressBarUI ui = new ProgressBarUI(client, true, 50, listener);
						doUnTag(container.units(), listener, ui);
						client.getDispatcher().fire(new GameDataEvent(client, client.getData()));
					}
				}).run();
			}
		});
		menu.add(editMenu);

		// clear all $$$ comments
		editMenu = new JMenuItem(getString("plugin.teacher.contextmenu.clear.title"));
		editMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new Thread(new Runnable() {
					public void run() {
						TeachClosingListener listener = new TeachClosingListener();
						ProgressBarUI ui = new ProgressBarUI(client, true, 50, listener);
						doClear(container.units(), listener, ui);
						client.getDispatcher().fire(new GameDataEvent(client, client.getData()));
					}
				}).run();
			}
		});
		menu.add(editMenu);

		// clear all $$$ comments
		editMenu = new JMenuItem(getString("plugin.teacher.contextmenu.deleteall.title"));
		editMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (JOptionPane.showConfirmDialog(client, Resources
						.get("plugin.teacher.delall.confirm.message"), Resources
						.get("plugin.teacher.delall.confirm.title"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					Teacher.delAllOrders(container.units(), getNamespace());
					client.getDispatcher().fire(new GameDataEvent(client, client.getData()));
				}
			}
		});
		menu.add(editMenu);

		return menu;
	}

	/**
	 * 
	 * 
	 * @see magellan.client.swing.context.UnitContextMenuProvider#createContextMenu(magellan.client.event.EventDispatcher,
	 *      magellan.library.GameData, magellan.library.Unit, java.util.Collection)
	 */
	@SuppressWarnings("unchecked")
	public JMenuItem createContextMenu(EventDispatcher dispatcher, GameData data, final Unit unit,
			final Collection selectedObjects) {
		JMenu menu = new JMenu(getString("plugin.teacher.contextmenu.title"));

		JMenuItem addLearnMenu = new JMenuItem(getString("plugin.teacher.contextmenu.addlearn.title",
				new Object[] { unit.getName(), unit.getID().toString() }));
		addLearnMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String userInput = JOptionPane.showInputDialog(client,
						getString("plugin.teacher.addlearn.askTalent.message"), "Hiebwaffen 20 20");
				if (userInput != null) {
					try {
						StringTokenizer st = new StringTokenizer(userInput, " ", false);
						String talent = st.nextToken();
						int target = Integer.parseInt(st.nextToken());
						int max = Integer.parseInt(st.nextToken());
						Order newOrder;
						// if (talent.equals(Order.ALL)) {
						// double value2 = Double.parseDouble(st.nextToken());
						// newOrder = new Order(value, value2);
						// } else {
						newOrder = new Order(talent, 1d, target, max);
						// }
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
						Order newOrder = new Order(talent, diff, true);
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
						// if (talent.equals(Order.ALL))
						// newOrder = new Order(1d, 1d);
						// else
						newOrder = new Order(talent, 1d, 1, 1);
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
		if (selectedObjects == null) {
			hasOrder = hasLearnOrder(unit);
		} else {
			for (Object o : selectedObjects) {
				if (o instanceof Unit) {
					if (hasLearnOrder((Unit) o)) {
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
							newOrder = new Order(talent, 0, true);
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
		if (selectedObjects == null) {
			hasOrder = hasLearnOrder(unit);
		} else {
			for (Object o : selectedObjects) {
				if (o instanceof Unit) {
					if (hasTeachOrder((Unit) o)) {
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
		SUnit su = Teacher.parseUnit(unit, getNamespace(), false);
		return su != null && !su.getLearnTalents().isEmpty();
	}

	private boolean hasTeachOrder(Unit unit) {
		SUnit su = Teacher.parseUnit(unit, getNamespace(), false);
		return su != null && !su.getTeachTalents().isEmpty();
	}

	protected void delOrder(Unit unit, Collection<Unit> selectedObjects, Order newOrder) {
		Teacher.delOrder(selectedObjects != null ? selectedObjects : Collections.singletonList(unit),
				getNamespace(), newOrder);
		client.getDispatcher().fire(new GameDataEvent(client, client.getData()));
	}

	protected void addOrder(Unit unit, Collection<Unit> selectedObjects, Order newOrder) {
		Teacher.addOrder(selectedObjects != null ? selectedObjects : Collections.singletonList(unit),
				getNamespace(), newOrder);
		client.getDispatcher().fire(new GameDataEvent(client, client.getData()));
	}

	public void actionPerformed(ActionEvent e) {
		log.info(e.getActionCommand());
		switch (PlugInAction.getAction(e)) {
		case EXECUTE_ALL: {
			new Thread(new Runnable() {
				public void run() {
					TeachClosingListener listener = new TeachClosingListener();
					ProgressBarUI ui = new ProgressBarUI(client, true, 50, listener);
					ui.setTitle("");
					ui.show();
					for (Region r : gd.regions().values()) {
						ui.setTitle(r.getName());
						doTeachUnits(r.units(), listener, ui);
					}
					client.getDispatcher().fire(new GameDataEvent(client, client.getData()));
					ui.ready();
				}
			}).start();

			break;
		}
		case TAG_ALL: {
			new Thread(new Runnable() {
				public void run() {
					TeachClosingListener listener = new TeachClosingListener();
					ProgressBarUI ui = new ProgressBarUI(client, true, 50, listener);
					ui.setTitle("");
					ui.show();
					for (Region r : gd.regions().values()) {
						ui.setTitle(r.getName());
						doParse(r.units(), listener, ui);
					}
					client.getDispatcher().fire(new GameDataEvent(client, client.getData()));
					ui.ready();
				}
			}).start();

			break;
		}
		case UNTAG_ALL: {
			new Thread(new Runnable() {
				public void run() {
					TeachClosingListener listener = new TeachClosingListener();
					ProgressBarUI ui = new ProgressBarUI(client, true, 50, listener);
					ui.setTitle("");
					ui.show();
					for (Region r : gd.regions().values()) {
						ui.setTitle(r.getName());
						doUnTag(r.units(), listener, ui);
					}
					client.getDispatcher().fire(new GameDataEvent(client, client.getData()));
					ui.ready();
				}
			}).start();

			break;
		}
		case CLEAR_ALL: {
			new Thread(new Runnable() {
				public void run() {
					TeachClosingListener listener = new TeachClosingListener();
					ProgressBarUI ui = new ProgressBarUI(client, true, 50, listener);
					ui.setTitle("");
					ui.show();
					for (Region r : gd.regions().values()) {
						ui.setTitle(r.getName());
						doClear(r.units(), listener, ui);
					}
					client.getDispatcher().fire(new GameDataEvent(client, client.getData()));
					ui.ready();
				}
			}).start();

			break;
		}
		case CONVERT_ALL: {
			new Thread(new Runnable() {
				public void run() {
					TeachClosingListener listener = new TeachClosingListener();
					ProgressBarUI ui = new ProgressBarUI(client, true, 50, listener);
					ui.setTitle("");
					ui.show();
					for (Region r : gd.regions().values()) {
						ui.setTitle(r.getName());
						doConvert(r.units(), listener, ui);
					}
					client.getDispatcher().fire(new GameDataEvent(client, client.getData()));
					ui.ready();
				}
			}).start();

			break;
		}
		case PANEL: {
			showPanel();
			break;
		}
		}

	}

	private void showPanel() {
		new TeachPanel(client, client.getDispatcher(), gd, properties, getNamespace(), null);

	}

	/**
	 * Convert all "old format" orders to "new format" orders
	 * 
	 * @param values
	 * @param ui
	 * @param listener
	 */
	private void doConvert(final Collection<Unit> values, TeachClosingListener listener,
			ProgressBarUI ui) {
		Teacher.convert(values, getNamespace());
	}

	private void doClear(final Collection<Unit> values, TeachClosingListener listener,
			ProgressBarUI ui) {
		Teacher.clear(values, getNamespace());
	}

	private void doTeachUnits(final Collection<Unit> values, TeachClosingListener listener,
			ProgressBarUI ui) {
		synchronized (client) {
			final Teacher t = new Teacher(values, namespace, ui);
			listener.setTeacher(t);
			t.setUnconfirm(unconfirm);
			t.setConfirmFullTeachers(confirmFullTeachers);
			t.setConfirmEmptyTeachers(confirmEmptyTeachers);
			t.setPercentFull(percentFull);
			t.setConfirmTaughtStudents(confirmTaughtStudents);
			t.setConfirmUntaughtStudents(confirmUntaughtStudents);

			t.mainrun();

		}
	}

	private class TeachClosingListener implements ProgressBarUI.ClosingListener {
		Teacher teacher = null;

		public void setTeacher(Teacher t) {
			teacher = t;
		}

		public boolean proceed(WindowEvent e) {
			if (JOptionPane.showConfirmDialog(client, Resources.get("progressbarui.abort.message"),
					Resources.get("progressbarui.abort.title"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				if (teacher != null)
					teacher.stop();
				return false;
			} else
				return false;
		}
	}

	private void doParse(final Collection<Unit> values, TeachClosingListener listener,
			ProgressBarUI progressBarUI) {
		Teacher.parse(values, getNamespace(), progressBarUI);
	}

	private void doUnTag(final Collection<Unit> values, TeachClosingListener listener,
			ProgressBarUI ui) {
		Teacher.untag(values, getNamespace(), ui);
	}

	/**
	 * @param namespace
	 *          the namespace to set
	 */
	private void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	/**
	 * @return the namespace
	 */
	private String getNamespace() {
		return namespace;
	}

	/**
	 * @see magellan.client.extern.MagellanPlugIn#quit(boolean)
	 */
	public void quit(boolean storeSettings) {
		// do nothing
	}

	/**
	 * @see magellan.client.extern.MagellanPlugIn#getDocks()
	 */
	public Map<String, Component> getDocks() {
		return null;
	}

	/**
	 * 
	 */
	public PreferencesFactory getPreferencesProvider() {
		return new PreferencesFactory() {

			public PreferencesAdapter createPreferencesAdapter() {
				return new TeachPreferences();
			}

		};
	}

	class TeachPreferences implements PreferencesAdapter {

		JTextArea txtNamespace;
		private JCheckBox chkUnconfirm;
		private JCheckBox chkConfirmFullTeachers;
		private JCheckBox chkConfirmEmptyTeachers;
		private JCheckBox chkConfirmTaughtStudents;
		private JCheckBox chkConfirmUntaughtStudents;
		private JSlider sldPercentTeacher;
		private JPanel westPanel;

		public TeachPreferences() {
			initGUI();
		}

		private void initGUI() {
			JPanel panel = new JPanel(new GridBagLayout());

			txtNamespace = new JTextArea(getNamespace());
			txtNamespace.setMinimumSize(new Dimension(100, 20));
			txtNamespace.setPreferredSize(new java.awt.Dimension(100, 20));
			JLabel lblNamespace = new JLabel(getString("plugin.teacher.preferences.label.namespace"));
			lblNamespace.setLabelFor(txtNamespace);

			chkUnconfirm = new JCheckBox(
					getString("plugin.teacher.preferences.label.unconfirm"));
			chkConfirmFullTeachers = new JCheckBox(
					getString("plugin.teacher.preferences.label.confirmfullteachers"));
			chkConfirmEmptyTeachers = new JCheckBox(
					getString("plugin.teacher.preferences.label.confirmemptyteachers"));
			final JLabel lblPercentTeacher = new JLabel(
					getString("plugin.teacher.preferences.label.percentTeacher"));
			sldPercentTeacher = new JSlider(0, 99);
			sldPercentTeacher.setMajorTickSpacing(33);
			sldPercentTeacher.setMinorTickSpacing(3);
			sldPercentTeacher.setPaintTicks(true);
			sldPercentTeacher.setPaintLabels(true);
			sldPercentTeacher.setPaintTrack(true);
			lblPercentTeacher.setLabelFor(sldPercentTeacher);

			sldPercentTeacher.addChangeListener(new ChangeListener() {

				public void stateChanged(ChangeEvent e) {
					int val = ((JSlider) e.getSource()).getValue();

					lblPercentTeacher.setText(getString("plugin.teacher.preferences.label.percentTeacher",
							new Object[] { String.format("%02d", val) }));
				}
			});
			chkConfirmTaughtStudents = new JCheckBox(
					getString("plugin.teacher.preferences.label.confirmtaughtstudents"));
			chkConfirmUntaughtStudents = new JCheckBox(
					getString("plugin.teacher.preferences.label.confirmuntaughtstudents"));

			// GridBagConstraints con = new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST,
			// GridBagConstraints.HORIZONTAL, new Insets(3, 3, 3, 3), 0, 0);
			GridBagConstraints con = new GridBagConstraints();
			con.insets = new Insets(2, 2, 2, 2);
			con.fill = GridBagConstraints.HORIZONTAL;
			con.anchor = GridBagConstraints.WEST;

			con.insets.left = 0;
			con.gridx = 0;
			con.gridy = 0;
			panel.add(lblNamespace, con);

			con.insets.left = 0;
			con.gridy++;
			panel.add(txtNamespace, con);

			con.gridx = 0;
			con.gridy++;
			panel.add(chkUnconfirm, con);

			con.gridy++;
			panel.add(chkConfirmFullTeachers, con);

			con.gridy++;
			panel.add(chkConfirmEmptyTeachers, con);

			con.gridy++;
			panel.add(lblPercentTeacher, con);
			con.gridy++;
			panel.add(sldPercentTeacher, con);
			con.gridx = 0;

			con.gridy++;
			panel.add(chkConfirmTaughtStudents, con);

			con.gridy++;
			panel.add(chkConfirmUntaughtStudents, con);

			con.gridx = 1;
			con.gridy = 0;
			con.fill = GridBagConstraints.NONE;
			con.anchor = GridBagConstraints.EAST;
			con.weightx = 0.1;
			panel.add(new JLabel(""), con);

			JPanel restrictPanel = new JPanel(new BorderLayout());
			westPanel = new JPanel(new BorderLayout());
			westPanel.add(restrictPanel, BorderLayout.CENTER);
			restrictPanel.add(panel, BorderLayout.NORTH);
			restrictPanel.setBorder(new javax.swing.border.TitledBorder(BorderFactory
					.createEtchedBorder(), getString("plugin.teacher.preferences.title.options")));
		}

		public void applyPreferences() {
			setNamespace(txtNamespace.getText());
			properties.setProperty(NAMESPACE_PROPERTY, getNamespace());
			unconfirm = chkUnconfirm.isSelected();
			properties.setProperty(UNCONFIRM_PROPERTY, unconfirm ? "true" : "false");
			confirmFullTeachers = chkConfirmFullTeachers.isSelected();
			properties.setProperty(CONFIRMFULLTEACHERS_PROPERTY, confirmFullTeachers ? "true" : "false");
			confirmEmptyTeachers = chkConfirmEmptyTeachers.isSelected();
			properties
					.setProperty(CONFIRMEMPTYTEACHERS_PROPERTY, confirmEmptyTeachers ? "true" : "false");
			confirmTaughtStudents = chkConfirmTaughtStudents.isSelected();
			properties.setProperty(CONFIRMTAUGHTSTUDENTS_PROPERTY, confirmTaughtStudents ? "true"
					: "false");
			confirmUntaughtStudents = chkConfirmUntaughtStudents.isSelected();
			properties.setProperty(CONFIRMUNTAUGHTSTUDENTS_PROPERTY, confirmUntaughtStudents ? "true"
					: "false");
			percentFull = sldPercentTeacher.getValue();
			properties.setProperty(PERCENTFULL_PROPERTY, String.valueOf(percentFull));
		}

		public Component getComponent() {
			return westPanel;
		}

		public String getTitle() {
			return getString("plugin.teacher.preferences.title");
		}

		public void initPreferences() {
			chkUnconfirm.setSelected(unconfirm);
			chkConfirmEmptyTeachers.setSelected(confirmEmptyTeachers);
			chkConfirmFullTeachers.setSelected(confirmFullTeachers);
			chkConfirmTaughtStudents.setSelected(confirmTaughtStudents);
			chkConfirmUntaughtStudents.setSelected(confirmUntaughtStudents);
			sldPercentTeacher.setValue(percentFull);
		}

	}

	// private static final String BUNDLE_NAME = "magellan.plugin.teacher.teacher_resources";

	// private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	protected static String getString(String key) {
		return Resources.get(key);
		// ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_NAME, Locales.getGUILocale());
		// try {
		// return bundle.getString(key);
		// } catch (MissingResourceException e) {
		// log.warn("resource key " + key + " not found in bundle " + BUNDLE_NAME + ", "
		// + Locales.getGUILocale());
		// return '!' + key + '!';
		// }
	}

	protected static String getString(String key, Object[] args) {
		String value = getString(key);
		if (value != null) {
			value = new MessageFormat(value).format(args);
		}
		return value;
	}

}
