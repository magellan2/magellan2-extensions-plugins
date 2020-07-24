/*
 *  Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe,
 *                          Stefan Goetz, Sebastian Pappert,
 *                          Klaas Prause, Enno Rehling,
 *                          Sebastian Tusk, Ulrich Kuester,
 *                          Ilja Pavkovic
 *
 * This file is part of the Eressea Java Code Base, see the
 * file LICENSING for the licensing information applying to
 * this file.
 *
 */

package magellan.plugin.teacher;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import magellan.client.event.EventDispatcher;
import magellan.client.event.SelectionEvent;
import magellan.client.event.SelectionListener;
import magellan.client.event.UnitOrdersEvent;
import magellan.client.event.UnitOrdersListener;
import magellan.client.swing.InternationalizedDataDialog;
import magellan.client.swing.table.TableSorter;
import magellan.client.swing.tree.ContextManager;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.Unit;
import magellan.library.event.GameDataEvent;
import magellan.library.utils.NullUserInterface;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;

/**
 * DOCUMENT ME!
 * 
 * @author stm
 */
public class TeachPanel extends InternationalizedDataDialog implements SelectionListener,
ActionListener, UnitOrdersListener {
	private static Logger log = Logger.getInstance(TeachPanel.class);
	public static final String IDENTIFIER = "TEACH";

	private static String learnTargetChar;
	private static String learnMaxChar;
	private static String teachDiffChar;
	private static String otherChar;
	private static String errorChar;
	@SuppressWarnings("unused")
	private static String valueChar;

	private static String currentLearnChar;
	private static String currentTeachChar;

	protected TeachTable tTable;

	private Teacher teacher;

	private final Collection<String> namespaces;
	private JComboBox<Region> rBox;

	/**
	 * Creates a new TradeOrganizer object.
	 * 
	 * @param namespace
	 * 
	 * 
	 */
	public TeachPanel(Frame owner, EventDispatcher dispatcher, GameData data, Properties settings,
			Collection<String> namespaces, Region region) {
		super(owner, false, dispatcher, data, settings);

		if (namespaces==null)
			throw new NullPointerException();

		this.namespaces = new ArrayList<String>(namespaces);

		learnTargetChar = TeachPlugin.getString("teachpanel.constants.learnTargetChar");
		learnMaxChar = TeachPlugin.getString("teachpanel.constants.learnMaxChar");
		teachDiffChar = TeachPlugin.getString("teachpanel.constants.teachDiffChar");
		currentLearnChar = TeachPlugin.getString("teachpanel.constants.currentLearnChar");
		currentTeachChar = TeachPlugin.getString("teachpanel.constants.currentTeachChar");
		otherChar = TeachPlugin.getString("teachpanel.constants.otherChar");
		errorChar = TeachPlugin.getString("teachpanel.constants.errorChar");
		valueChar = TeachPlugin.getString("teachpanel.constants.valueChar");

		// register for events
		dispatcher.addGameDataListener(this);
		dispatcher.addSelectionListener(this);
		dispatcher.addUnitOrdersListener(this);

		init();

		setVisible(true);
		rBox.setSelectedIndex(0);
	}

	protected void init() {
		final int width = 800; // Integer.parseInt(settings.getProperty("TradeOrganizer.width", "800"));
		final int height = 600; // Integer.parseInt(settings.getProperty("TradeOrganizer.height", "600"));
		int xPos = -1; // Integer.parseInt(settings.getProperty("TradeOrganizer.xPos", "-1"));
		int yPos = -1; // Integer.parseInt(settings.getProperty("TradeOrganizer.yPos", "-1"));

		if (xPos == -1) {
			xPos = ((int) Toolkit.getDefaultToolkit().getScreenSize().getWidth() - width) / 2;
		}

		if (yPos == -1) {
			yPos = ((int) Toolkit.getDefaultToolkit().getScreenSize().getHeight() - height) / 2;
		}

		setSize(width, height);
		setLocation(xPos, yPos);
		setTitle(Resources.get("teachpanel.title", new Object[] { getNamespaces().toString() }));

		// build GUI

		// build top panel
		final JPanel topPanel = new JPanel();
		topPanel.setBorder(new TitledBorder(""));
		topPanel.setLayout(new GridBagLayout());

		final GridBagConstraints c = new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0);

		rBox = new JComboBox<Region>(data.getRegions().toArray(new Region[] {}));
		rBox.setEditable(false);
		rBox.addActionListener(this);
		topPanel.add(new JLabel(TeachPlugin.getString("teachpanel.regionbox.lable")), c);
		c.gridx++;
		topPanel.add(rBox, c);

		final JScrollPane tablePanel = createTablePanel();

		final Container cp = getContentPane();
		cp.setLayout(new BorderLayout());

		cp.add(topPanel, BorderLayout.NORTH);
		cp.add(tablePanel, BorderLayout.CENTER);
	}

	private JScrollPane createTablePanel() {
		tTable = new TeachTable();
		tTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		// tTable.setAutoResizeMode(TeachTable.AUTO_RESIZE_ALL_COLUMNS);
		tTable.setDragEnabled(true);
		final JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JScrollPane(tTable), BorderLayout.CENTER);
		return new JScrollPane(panel);

	}

	private void updateGUI() {
		rBox.removeAllItems();
		for (final Region region : data.getRegions())
			rBox.addItem(region);
	}

	private Collection<String> getNamespaces() {
		return namespaces;
	}

	private void setUnits() {
		if (teacher == null)
			tTable.setUnits(Collections.<SUnit> emptyList());
		else {
			final Collection<SUnit> units = teacher.getUnits(false);
			tTable.setUnits(units);
		}
	}

	private Region getCurrentRegion() {
		return (Region) rBox.getSelectedItem();
	}

	/**
	 * 
	 * 
	 * @see magellan.client.swing.InternationalizedDataDialog#gameDataChanged(magellan.library.event.GameDataEvent)
	 */
	@Override
	public void gameDataChanged(GameDataEvent ge) {
		super.gameDataChanged(ge);
		updateGUI();
		setActiveRegion(getCurrentRegion());
	}

	/**
	 * 
	 * 
	 * @see magellan.client.event.UnitOrdersListener#unitOrdersChanged(magellan.client.event.UnitOrdersEvent)
	 */
	public void unitOrdersChanged(UnitOrdersEvent e) {
		// setActiveRegion(getCurrentRegion());
		if (getAtiveRegion() == e.getUnit().getRegion()) {
			tTable.updateUnit(e.getUnit());
		}
	}

	/**
	 * 
	 * @see magellan.client.event.SelectionListener#selectionChanged(magellan.client.event.SelectionEvent)
	 */
	public void selectionChanged(SelectionEvent se) {
	}

	/**
	 * The table for displaying <code>SUnit</code>s. Supports sorting, various context menu actions...
	 * 
	 * @author stm
	 * @version $Revision: 384 $
	 */
	@SuppressWarnings("serial")
	public class TeachTable extends JTable implements MouseListener {
		private final TeachTableModel model;
		private final TableSorter sorter;
//		private final TableCellRenderer sunitRenderer = new SUnitRenderer();

		class SUnitRenderer implements TableCellRenderer {

			public Component getTableCellRendererComponent(JTable table, Object value,
					boolean isSelected, boolean hasFocus, int row, int column) {
				return getDefaultRenderer(String.class).getTableCellRendererComponent(table,
						((SUnit) value).getName(), isSelected, hasFocus, row, column);
			}

		}

		@Override
		public TableCellRenderer getCellRenderer(int row, int column) {
//			if (false && column == 0)
//				return sunitRenderer;
//			else
				return super.getCellRenderer(row, column);
		}

		@Override
		public javax.swing.table.TableModel getModel() {
			return sorter;
		}

		private TeachTableModel getInternalModel() {
			return model;
		}

		/**
		 * Creates a new TeachTable object.
		 */
		public TeachTable() {
			super();
			model = new TeachTableModel();
			sorter = new TableSorter(model);
			this.setModel(sorter);
			sorter.setTableHeader(getTableHeader());
			this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			this.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					final int i = TeachTable.this.getSelectedRow();
					if (i >= 0) {
						final Unit u = (Unit) sorter.getValueAt(i, 0);

						if (u != null) {
							// newer version:
							dispatcher.fire(SelectionEvent.create(this, u));
							// 2.0.5 version:
							//							dispatcher.fire(new SelectionEvent(this, null, u));
						}
					}
				}
			});

			this.addMouseListener(this);
		}

		/**
		 * @return
		 */
		@Override
		protected JTableHeader createDefaultTableHeader() {
			return new JTableHeader(getColumnModel()) {
				@Override
				public String getToolTipText(MouseEvent e) {
					final java.awt.Point p = e.getPoint();
					final int index = columnModel.getColumnIndexAtX(p.x);
					final int realIndex = columnModel.getColumn(index).getModelIndex();
					TableModel m = getModel();
					if (m instanceof TableSorter) {
						m = ((TableSorter) m).getTableModel();
					}
					if (m instanceof TeachTableModel)
						return ((TeachTableModel) m).getColumnType(realIndex).getTooltip();
					else
						return null;
				}
			};
		}

		/**
		 * @return
		 */
		public Collection<SUnit> getUnits() {
			return model.getUnits();
		}

		/**
		 * Set the set of units managed by this component to <code>units</code>.
		 * 
		 * @param units
		 */
		public void setUnits(Collection<SUnit> units) {
			model.setUnits(units);
			tTable.revalidate();
			tTable.repaint();
		}

		/**
		 * Add (or replace) a unit.
		 * 
		 * @param unit
		 */
		private void updateUnit(Unit unit) {
			if (teacher != null) {
				final SUnit su = Teacher.parseUnit(unit, getNamespaces(), false);
				if (su != null)
					model.addUnit(su);
				else if (model.findRow(unit) >= 0)
					model.addUnit(new SUnit(teacher, unit));
			}
		}

		/**
		 * Sort table according to current search key.
		 */
		public void sort() {
			// model.sort(model.curSort);
			sorter.fireTableDataChanged();
			// tTable.revalidate();
			// tTable.repaint();
		}

		/**
		 * Create and show context menu.
		 * 
		 * FIXME add options for max, prio etc...
		 * 
		 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
		 */
		public void mouseClicked(final MouseEvent mouseEvent) {

			if (mouseEvent.getButton() == MouseEvent.BUTTON3) {
				final JPopupMenu menu = new JPopupMenu();
				// int row = rowAtPoint(mouseEvent.getPoint());
				final int col = columnAtPoint(mouseEvent.getPoint());
				JMenuItem setValueMenu;
				JMenuItem delValueMenu;

				if (getInternalModel().getColumnType(col) == magellan.plugin.teacher.TeachPanel.TeachTableModel.ColumnType.LEARNMAX
						|| getInternalModel().getColumnType(col) == magellan.plugin.teacher.TeachPanel.TeachTableModel.ColumnType.LEARNTARGET) {
					// clicked in learn column
					setValueMenu = new JMenuItem(TeachPlugin
							.getString("teachpanel.contextmenu.addlearn.title"));
					delValueMenu = new JMenuItem(TeachPlugin
							.getString("teachpanel.contextmenu.dellearn.title"));
					menu.add(setValueMenu);
					menu.add(delValueMenu);
					setValueMenu.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent menuEvent) {
							askForAdd(menuEvent, mouseEvent);
						}

					});
					delValueMenu.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent menuEvent) {
							askForDel(menuEvent, mouseEvent);
						}
					});
					if (menu != null) {
						ContextManager.showMenu(menu, this, mouseEvent.getX(), mouseEvent.getY());
					}
				} else if (getInternalModel().getColumnType(col) == magellan.plugin.teacher.TeachPanel.TeachTableModel.ColumnType.TEACH) {
					// clicked in teach column
					setValueMenu = new JMenuItem(TeachPlugin
							.getString("teachpanel.contextmenu.addteach.title"));
					delValueMenu = new JMenuItem(TeachPlugin
							.getString("teachpanel.contextmenu.delteach.title"));
					menu.add(setValueMenu);
					menu.add(delValueMenu);
					setValueMenu.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent menuEvent) {
							askForAdd(menuEvent, mouseEvent);
						}

					});
					delValueMenu.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent menuEvent) {
							askForDel(menuEvent, mouseEvent);
						}
					});
					if (menu != null) {
						ContextManager.showMenu(menu, this, mouseEvent.getX(), mouseEvent.getY());
					}
				} else if (getInternalModel().getColumnType(col).equals(
						magellan.plugin.teacher.TeachPanel.TeachTableModel.ColumnType.PRIORITY)) {
					setValueMenu = new JMenuItem(TeachPlugin
							.getString("teachpanel.contextmenu.changeprio.title"));
					menu.add(setValueMenu);
					setValueMenu.addActionListener(new ActionListener() {

						public void actionPerformed(ActionEvent menuEvent) {
							askForPrio(menuEvent, mouseEvent);
						}
					});
					if (menu != null) {
						ContextManager.showMenu(menu, this, mouseEvent.getX(), mouseEvent.getY());
					}
				}
			}
		}

		public void mouseEntered(MouseEvent e) {
		}

		public void mouseExited(MouseEvent e) {
		}

		public void mousePressed(MouseEvent e) {
		}

		public void mouseReleased(MouseEvent e) {
		}

		/**
		 * Ask for addition of learn or teach order.
		 * 
		 * @param a
		 * @param m
		 */
		protected void askForAdd(ActionEvent a, MouseEvent m) {
			// determine kind of cell
			if (getSelectedRowCount() <= 0)
				return;
			final int selectedColumn = getColumnModel().getColumnIndexAtX(m.getPoint().x);

			if (selectedColumn < getInternalModel().getColumnCount()
					- getInternalModel().getTalentCount() * 4)
				return;
			String talent = getInternalModel().getColumnName(selectedColumn);
			final String mode = talent.substring(talent.length() - 1);
			talent = talent.substring(0, talent.length() - 2);

			if (mode.equals(learnTargetChar) || mode.equals(learnMaxChar)) {
				// add a learn oder
				final String userInput = JOptionPane.showInputDialog(TeachPanel.this, TeachPlugin.getString(
						"teachpanel.contextmenu.add.text." + mode, new Object[] { talent }), "20 20");
				if (userInput != null) {
					try {
						final StringTokenizer st = new StringTokenizer(userInput, " ", false);
						final int target = Integer.parseInt(st.nextToken());
						final int max = Integer.parseInt(st.nextToken());

						Collection<Unit> units = null;
						units = new ArrayList<Unit>(getSelectedRowCount());

						for (final int row : getSelectedRows()) {
							units.add((Unit) getModel().getValueAt(row, 0));
						}
						if (mode.equals(learnTargetChar) || mode.equals(learnMaxChar)) {
							Teacher.addOrder(units, getNamespaces().iterator().next(), new Order(talent, 1d, target, max));
						}
						getDispatcher().fire(new GameDataEvent(this, getData()));

					} catch (final Exception ex) {
						log.warn(ex);
						JOptionPane.showMessageDialog(this, TeachPlugin
								.getString("plugin.teacher.addlearn.error"));
					}
				}
			} else if (mode.equals(teachDiffChar)) {
				// add a teach order
				final String userInput = JOptionPane.showInputDialog(TeachPanel.this, TeachPlugin.getString(
						"teachpanel.contextmenu.add.text." + mode, new Object[] { talent }), "20");
				if (userInput != null) {
					try {
						final StringTokenizer st = new StringTokenizer(userInput, " ", false);
						final int diff = Integer.parseInt(st.nextToken());

						Collection<Unit> units = null;
						units = new ArrayList<Unit>(getSelectedRowCount());

						for (final int row : getSelectedRows()) {
							units.add((Unit) getModel().getValueAt(row, 0));
						}
						if (mode.equals(teachDiffChar)) {
							Teacher.addOrder(units, getNamespaces().iterator().next(), new Order(talent, diff, true));
						}
						getDispatcher().fire(new GameDataEvent(this, getData()));

					} catch (final Exception ex) {
						log.warn(ex);
						JOptionPane.showMessageDialog(this, TeachPlugin
								.getString("plugin.teacher.addlearn.error"));
					}

				}
			}
		}

		/**
		 * Ask for removal of learn or teach order.
		 * 
		 * @param a
		 * @param m
		 */
		protected void askForDel(ActionEvent a, MouseEvent m) {
			if (getSelectedRowCount() <= 0)
				return;

			final int selectedColumn = getColumnModel().getColumnIndexAtX(m.getPoint().x);
			String talent = getModel().getColumnName(selectedColumn);
			final String mode = talent.substring(talent.length() - 1);
			talent = talent.substring(0, talent.length() - 2);

			if ((mode.equals("L") || mode.equals("T"))
					&& JOptionPane.showConfirmDialog(this, TeachPlugin.getString(
							"teachpanel.delentry.confirm.message." + mode, new Object[] { talent }), TeachPlugin
							.getString("teachpanel.delentry.confirm.title." + mode, new Object[] { talent }),
							JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				Collection<Unit> units = null;
				units = new ArrayList<Unit>(getSelectedRowCount());
				for (final int row : getSelectedRows()) {
					units.add((Unit) getModel().getValueAt(row, 0));
				}
				if (mode.equals(learnTargetChar) || mode.equals(learnMaxChar)) {
					Teacher.delOrder(units, getNamespaces(), new Order(talent, 1d, 1, 0));
				} else if (mode.equals(teachDiffChar)) {
					Teacher.delOrder(units, getNamespaces(), new Order(talent, 0, true));
				}
				getDispatcher().fire(new GameDataEvent(this, getData()));
			}

		}

		protected void askForPrio(ActionEvent a, MouseEvent m) {
			// determine kind of cell
			if (getSelectedRowCount() <= 0)
				return;

			final String userInput = JOptionPane.showInputDialog(TeachPanel.this, TeachPlugin
					.getString("plugin.teacher.prio.message"), model.getValueAt(rowAtPoint(m.getPoint()),
							columnAtPoint(m.getPoint())));
			if (userInput != null) {
				try {
					final double newPrio = Double.parseDouble(userInput);

					final Collection<Unit> units = new ArrayList<Unit>(getSelectedRowCount());
					for (final int row : getSelectedRows()) {
						units.add((Unit) getModel().getValueAt(row, 0));
					}

					Teacher.setPrio(units, getNamespaces(), newPrio);

					getDispatcher().fire(new GameDataEvent(this, getData()));

				} catch (final Exception ex) {
					log.warn(ex);
					JOptionPane.showMessageDialog(this, TeachPlugin.getString("plugin.teacher.prio.error"));
				}
			}
		}
	}

	/**
	 * Represents a TableModel for <code>SUnit</code>s. Shows columns for name, id, persons, current
	 * order and meta orders.
	 * 
	 * @author stm
	 * 
	 */
	@SuppressWarnings("serial")
	private static class TeachTableModel extends AbstractTableModel {
		private final Set<SUnit> tableUnits = new HashSet<SUnit>();
		private static final int numFixedColumns = 6;

		/**
		 * Bundles information about different columns like type, title, column index in content
		 * 
		 * @author stm
		 * 
		 */
		private enum ColumnType {
			UNIT(0, "name", Object.class), ID(1, "id", Object.class), PERSONS(2, "persons", Integer.class), ORDERMODE(
					3, "ordermode", Object.class), ORDERTALENT(4, "ordertalent", Object.class), PRIORITY(5,
							"priority", Double.class), LEARNTARGET(0, "target", Integer.class), LEARNMAX(1, "max",
									Integer.class), TEACH(2, "diff", Integer.class), VALUE(3, "value", Integer.class);

			private int initialColumn;
			private Class<? extends Object> type;
			private String name;

			public Class<? extends Object> getType() {
				return type;
			}

			public String getTitle() {
				return TeachPlugin.getString("teachpanel.column." + getInternalName() + ".title");
			}

			private String getInternalName() {
				return name;
			}

			ColumnType(int col, String title, Class<? extends Object> type) {
				this.initialColumn = col;
				this.name = title;
				this.type = type;
			}

			int getColumn() {
				return initialColumn;
			}

			String getTooltip() {
				final String result = TeachPlugin
				.getString("teachpanel.column." + getInternalName() + ".tooltip");
				return result; // (result=="" ||
				// result.equals("teachpanel.column."+getInternalName()+".tooltip"))?null:result;
			}
		};

		/**
		 * The internal representation of the model.
		 */
		Object content[][];

		/**
		 * All talents of units of this model.
		 */
		List<String> talents;
		/**
		 * Each talent has an internal index which is stored here.
		 */
		Map<String, Integer> talentIndices;

		public TeachTableModel() {
			init();
		}

		/**
		 * Initialize content.
		 */
		protected void init() {

			// find all skills
			talentIndices = new HashMap<String, Integer>();
			talents = new ArrayList<String>();
			int talentCount = 0;
			for (final SUnit unit : tableUnits) {
				for (final String lTalent : unit.getLearnTalentsAsString()) {
					if (!talentIndices.containsKey(lTalent)) {
						talentIndices.put(lTalent, talentCount++);
						talents.add(lTalent);
					}
				}
				// for (String tTalent : unit.getTeachTalentsAsString()) {
				// if (!talentIndices.containsKey(tTalent)) {
				// talentIndices.put(tTalent, talentCount++);
				// teachCount++;
				// talents.add(tTalent);
				// }
				// }
			}

			// fill content
			content = new Object[tableUnits.size()][getColumnCount() + 1];
			int count = 0;
			for (final SUnit unit : tableUnits) {
				setRow(content[count], unit);

				content[count][getColumnCount()] = count;
				count++;
			}
			fireTableStructureChanged();
		}

		/**
		 * Fill a row with the right information about a unit.
		 * 
		 * @param row
		 * @param unit
		 */
		private void setRow(Object[] row, SUnit unit) {
			row[ColumnType.UNIT.getColumn()] = unit.getUnit();
			row[ColumnType.ID.getColumn()] = unit.getUnit().getID();
			row[ColumnType.PERSONS.getColumn()] = unit.getUnit().getModifiedPersons();
			final Order o = Teacher.getCurrentOrder(unit.getUnit());
			if (o == null) {
				row[ColumnType.ORDERMODE.getColumn()] = errorChar;
				row[ColumnType.ORDERTALENT.getColumn()] = "--";
			} else if (o.getTalent().equals(Order.ALL)) {
				row[ColumnType.ORDERMODE.getColumn()] = otherChar;
				row[ColumnType.ORDERTALENT.getColumn()] = "--";
			} else if (o.isLearnOrder()) {
				row[ColumnType.ORDERMODE.getColumn()] = currentLearnChar;
				row[ColumnType.ORDERTALENT.getColumn()] = o.getTalent();
			} else if (o.isTeachOrder()) {
				row[ColumnType.ORDERMODE.getColumn()] = currentTeachChar;
				row[ColumnType.ORDERTALENT.getColumn()] = "";
			}

			row[ColumnType.PRIORITY.getColumn()] = unit.getPrio();

			for (int col = numFixedColumns; col < row.length; ++col) {
				row[col] = null;
			}
			for (final String lTalent : unit.getLearnTalentsAsString()) {
				row[numFixedColumns + talentIndices.get(lTalent) * 4] = unit.getTarget(lTalent);
				row[numFixedColumns + talentIndices.get(lTalent) * 4 + 1] = unit.getMax(lTalent);
				row[numFixedColumns + talentIndices.get(lTalent) * 4 + 3] = Teacher.getLevel(
						unit.getUnit(), lTalent);
			}

			for (final String tTalent : unit.getTeachTalentsAsString()) {
				if (talentIndices.containsKey(tTalent)) {
					row[numFixedColumns + talentIndices.get(tTalent) * 4 + 2] = unit
					.getMaximumDifference(tTalent);
					row[numFixedColumns + talentIndices.get(tTalent) * 4 + 3] = Teacher.getLevel(unit
							.getUnit(), tTalent);
				}
			}
		}

		/**
		 * 
		 * @see javax.swing.table.TableModel#getRowCount()
		 */
		public int getRowCount() {
			return tableUnits.size();
		}

		/**
		 * 
		 * 
		 * @see javax.swing.table.TableModel#getColumnCount()
		 */
		public int getColumnCount() {
			return numFixedColumns + getTalentCount() * 4;
		}

		/**
		 * Return number of different talents.
		 * 
		 * @return
		 */
		public int getTalentCount() {
			return talents.size();
		}

		/**
		 * 
		 * 
		 * @see javax.swing.table.TableModel#getValueAt(int, int)
		 */
		public Object getValueAt(int row, int col) {
			return content[row][col];
		}

		/**
		 * 
		 * 
		 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
		 */
		@Override
		public String getColumnName(int col) {
			if (col < numFixedColumns)
				return ColumnType.values()[col].getTitle();
			else if ((col - numFixedColumns) % 4 == 0)
				return talents.get((col - numFixedColumns) / 4) + " " + ColumnType.LEARNTARGET.getTitle();
			else if ((col - numFixedColumns) % 4 == 1)
				return talents.get((col - numFixedColumns) / 4) + " " + ColumnType.LEARNMAX.getTitle();
			else if ((col - numFixedColumns) % 4 == 2)
				return talents.get((col - numFixedColumns) / 4) + " " + ColumnType.TEACH.getTitle();
			else
				return talents.get((col - numFixedColumns) / 4) + " " + ColumnType.VALUE.getTitle();
		}

		/**
		 * 
		 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
		 */
		@Override
		public Class<? extends Object> getColumnClass(int col) {
			if (col < numFixedColumns)
				return ColumnType.values()[col].getType();
			else if ((col - numFixedColumns) % 4 == 0)
				return ColumnType.LEARNTARGET.getClass();
			else if ((col - numFixedColumns) % 4 == 1)
				return ColumnType.LEARNMAX.getClass();
			else if ((col - numFixedColumns) % 4 == 2)
				return ColumnType.TEACH.getClass();
			else
				return ColumnType.VALUE.getClass();
		}

		/**
		 * Returns the type of column which is one of UNIT, ID, PERSONS, ORDERMODE, ORDERTALENT,
		 * PRIORITY, LEARN, TEACH, VALUE.
		 * 
		 * @param col
		 * @return
		 */
		public ColumnType getColumnType(int col) {
			switch (col) {
			case 0:
				return ColumnType.UNIT;

			case 1:
				return ColumnType.ID;

			case 2:
				return ColumnType.PERSONS;

			case 3:
				return ColumnType.ORDERMODE;

			case 4:
				return ColumnType.ORDERTALENT;

			case 5:
				return ColumnType.PRIORITY;

			default:
				if ((col - numFixedColumns) % 4 == 0)
					return ColumnType.LEARNTARGET;
				else if ((col - numFixedColumns) % 4 == 1)
					return ColumnType.LEARNMAX;
				else if ((col - numFixedColumns) % 4 == 2)
					return ColumnType.TEACH;
				else
					return ColumnType.VALUE;
			}
		}

		/**
		 * A view on all the <code>SUnit</code>s managed by the model.
		 * 
		 * @return
		 */
		public Collection<SUnit> getUnits() {
			return Collections.unmodifiableCollection(tableUnits);
		}

		/**
		 * Initialize the table.
		 * 
		 * @param units
		 *          The set of units that are managed by the model
		 */
		public void setUnits(Collection<SUnit> units) {
			tableUnits.clear();
			tableUnits.addAll(units);

			init();
		}

		/**
		 * Add (or replace) a unit.
		 * 
		 * @param unit
		 */
		public void addUnit(SUnit unit) {
			// see if we have to add columns
			boolean structureChanged = false;
			for (final String lTalent : unit.getLearnTalentsAsString()) {
				if (!talentIndices.containsKey(lTalent)) {
					structureChanged = true;
					break;
				}
			}
			// if (!structureChanged)
			// for (String tTalent : unit.getTeachTalentsAsString()) {
			// if (!talentIndices.containsKey(tTalent)) {
			// structureChanged = true;
			// break;
			// }
			// }

			if (structureChanged) {
				// redo everything...
				for (final Iterator<SUnit> it = tableUnits.iterator(); it.hasNext();) {
					if (it.next().getUnit().equals(unit.getUnit()))
						it.remove();
				}
				tableUnits.add(unit);
				init();
			} else {
				// just update relevant row
				final int row = findRow(unit.getUnit());
				final Object[] sameUnit = row >= 0 ? content[row] : null;
				if (sameUnit != null) {
					// replace unit
					setRow(sameUnit, unit);
					fireTableRowsUpdated(row, row);
				} else {
					// add unit
					final Object[][] newContent = new Object[content.length + 1][getColumnCount() + 1];
					for (int i = 0; i < content.length; ++i)
						newContent[i] = content[i];
					setRow(newContent[newContent.length - 1], unit);
					newContent[newContent.length - 1][getColumnCount() - 1] = newContent.length - 1;
					tableUnits.add(unit);
					content = newContent;
					fireTableRowsInserted(content.length - 1, content.length - 1);
				}
			}
		}

		/**
		 * tries to find the index of the row for unit.
		 * 
		 * @param unit
		 * @return
		 */
		public int findRow(Unit unit) {
			int sameUnit = -1;
			for (int i = 0; i < content.length; ++i) {
				final Unit u = (Unit) content[i][ColumnType.UNIT.getColumn()];
				if (u.equals(unit)) {
					sameUnit = i;
					break;
				}
			}
			return sameUnit;
		}

		/**
		 * Returns the unit corresponding to a row.
		 */
		@SuppressWarnings("unused")
		public Unit getUnit(int row) {
			return (Unit) content[row][0];
		}

		/**
		 * The number of fixed columns (as opposed to skill columns).
		 * 
		 * @return
		 */
		@SuppressWarnings("unused")
		public static int getFixedColumns() {
			return numFixedColumns;
		}

	}

	/**
	 * Change set of units when a new region is selected.
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == rBox) {
			final Region region = (Region) rBox.getSelectedItem();
			setActiveRegion(region);
		}
	}

	/**
	 * Select a new region and refresh table.
	 * 
	 * @param region
	 */
	private void setActiveRegion(Region region) {
		if (region == null)
			teacher = null;
		else
			teacher = new Teacher(region.units(), getNamespaces(), new NullUserInterface());
		setUnits();

	}

	/**
	 * Returns the currently selected Region.
	 * 
	 * @return
	 */
	public Region getAtiveRegion() {
		return (Region) rBox.getSelectedItem();
	}

}
