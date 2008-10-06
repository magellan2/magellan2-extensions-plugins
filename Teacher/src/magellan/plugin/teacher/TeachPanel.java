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
import java.util.List;
import java.util.Map;
import java.util.Properties;
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
import javax.swing.table.TableCellRenderer;

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
	private static String valueChar;

	private static String currentLearnChar;
	private static String currentTeachChar;

	protected TeachTable tTable;

	private Teacher teacher;

	private String namespace = "";
	private JComboBox rBox;

	/**
	 * Creates a new TradeOrganizer object.
	 * 
	 * @param namespace
	 * 
	 * 
	 */
	public TeachPanel(Frame owner, EventDispatcher dispatcher, GameData data, Properties settings,
			String namespace, Region region) {
		super(owner, false, dispatcher, data, settings);

		this.namespace = namespace;

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
		int width = 800; // Integer.parseInt(settings.getProperty("TradeOrganizer.width", "800"));
		int height = 600; // Integer.parseInt(settings.getProperty("TradeOrganizer.height", "600"));
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
		setTitle(Resources.get("tradeorganizer.title"));

		// build GUI

		// build top panel
		JPanel topPanel = new JPanel();
		topPanel.setBorder(new TitledBorder(""));
		topPanel.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0);

		rBox = new JComboBox(data.regions().values().toArray());
		rBox.setEditable(false);
		rBox.addActionListener(this);
		topPanel.add(new JLabel(TeachPlugin.getString("teachpanel.regionbox.lable")), c);
		c.gridx++;
		topPanel.add(rBox, c);

		JScrollPane tablePanel = createTablePanel();

		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());

		cp.add(topPanel, BorderLayout.NORTH);
		cp.add(tablePanel, BorderLayout.CENTER);
	}

	private JScrollPane createTablePanel() {
		tTable = new TeachTable();
		tTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		// tTable.setAutoResizeMode(TeachTable.AUTO_RESIZE_ALL_COLUMNS);
		tTable.setDragEnabled(true);
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JScrollPane(tTable), BorderLayout.CENTER);
		return new JScrollPane(panel);

	}

	private void updateGUI() {
		rBox.removeAllItems();
		for (Region region : data.regions().values())
			rBox.addItem(region);
	}

	private String getNamespace() {
		return namespace;
	}

	private void setUnits() {
		if (teacher == null)
			tTable.setUnits(Collections.<SUnit> emptyList());
		else {
			Collection<SUnit> units = teacher.getUnits(false);
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
	@SuppressWarnings("unchecked")
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
		private TeachTableModel model;
		private TableSorter sorter;
		private TableCellRenderer sunitRenderer = new SUnitRenderer();

		class SUnitRenderer implements TableCellRenderer {

			public Component getTableCellRendererComponent(JTable table, Object value,
					boolean isSelected, boolean hasFocus, int row, int column) {
				return getDefaultRenderer(String.class).getTableCellRendererComponent(table,
						((SUnit) value).getName(), isSelected, hasFocus, row, column);
			}

		}

		@Override
		public TableCellRenderer getCellRenderer(int row, int column) {
			if (false && column == 0)
				return sunitRenderer;
			else
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
			// this.getTableHeader().setReorderingAllowed(false);
			this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			this.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					int i = TeachTable.this.getSelectedRow();
					if (i >= 0) {
						Unit u = (Unit) sorter.getValueAt(i, 0);

						if (u != null) {
							dispatcher.fire(new SelectionEvent<Unit>(this, null, u, SelectionEvent.ST_DEFAULT));
						}
					}
				}
			});

			this.addMouseListener(this);
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
				SUnit su = Teacher.parseUnit(unit, getNamespace(), false);
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
				JPopupMenu menu = new JPopupMenu();
//				int row = rowAtPoint(mouseEvent.getPoint());
				int col = columnAtPoint(mouseEvent.getPoint());
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
			int selectedColumn = getColumnModel().getColumnIndexAtX(m.getPoint().x);

			if (selectedColumn < getInternalModel().getColumnCount()
					- getInternalModel().getTalentCount() * 4)
				return;
			String talent = getInternalModel().getColumnName(selectedColumn);
			String mode = talent.substring(talent.length() - 1);
			talent = talent.substring(0, talent.length() - 2);

			if (mode.equals(learnTargetChar) || mode.equals(learnMaxChar)) {
				// add a learn oder
				String userInput = JOptionPane.showInputDialog(TeachPanel.this, TeachPlugin.getString(
						"teachpanel.contextmenu.add.text." + mode, new Object[] { talent }), "20 20");
				if (userInput != null) {
					try {
						StringTokenizer st = new StringTokenizer(userInput, " ", false);
						int target = Integer.parseInt(st.nextToken());
						int max = Integer.parseInt(st.nextToken());

						Collection<Unit> units = null;
						units = new ArrayList<Unit>(getSelectedRowCount());

						for (int row : getSelectedRows()) {
							units.add((Unit) getModel().getValueAt(row, 0));
						}
						if (mode.equals(learnTargetChar) || mode.equals(learnMaxChar)) {
							Teacher.addOrder(units, getNamespace(), new Order(talent, 1d, target, max));
						}
						getDispatcher().fire(new GameDataEvent(this, getData()));

					} catch (Exception ex) {
						log.warn(ex);
						JOptionPane.showMessageDialog(this, TeachPlugin
								.getString("plugin.teacher.addlearn.error"));
					}
				}
			} else if (mode.equals(teachDiffChar)) {
				// add a teach order
				String userInput = JOptionPane.showInputDialog(TeachPanel.this, TeachPlugin.getString(
						"teachpanel.contextmenu.add.text." + mode, new Object[] { talent }), "20");
				if (userInput != null) {
					try {
						StringTokenizer st = new StringTokenizer(userInput, " ", false);
						int diff = Integer.parseInt(st.nextToken());

						Collection<Unit> units = null;
						units = new ArrayList<Unit>(getSelectedRowCount());

						for (int row : getSelectedRows()) {
							units.add((Unit) getModel().getValueAt(row, 0));
						}
						if (mode.equals(teachDiffChar)) {
							Teacher.addOrder(units, getNamespace(), new Order(talent, diff, true));
						}
						getDispatcher().fire(new GameDataEvent(this, getData()));

					} catch (Exception ex) {
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

			int selectedColumn = getColumnModel().getColumnIndexAtX(m.getPoint().x);
			String talent = getModel().getColumnName(selectedColumn);
			String mode = talent.substring(talent.length() - 1);
			talent = talent.substring(0, talent.length() - 2);

			if ((mode.equals("L") || mode.equals("T"))
					&& JOptionPane.showConfirmDialog(this, TeachPlugin.getString(
							"teachpanel.delentry.confirm.message." + mode, new Object[] { talent }), TeachPlugin
							.getString("teachpanel.delentry.confirm.title." + mode, new Object[] { talent }),
							JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				Collection<Unit> units = null;
				units = new ArrayList<Unit>(getSelectedRowCount());
				for (int row : getSelectedRows()) {
					units.add((Unit) getModel().getValueAt(row, 0));
				}
				if (mode.equals(learnTargetChar) || mode.equals(learnMaxChar)) {
					Teacher.delOrder(units, getNamespace(), new Order(talent, 1d, 1, 0));
				} else if (mode.equals(teachDiffChar)) {
					Teacher.delOrder(units, getNamespace(), new Order(talent, 0, true));
				}
				getDispatcher().fire(new GameDataEvent(this, getData()));
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
		private List<SUnit> tableUnits = new ArrayList<SUnit>();
		private static final int numFixedColumns = 6;

		/**
		 * Bundles information about different columns like type, title, column index in content
		 * 
		 * @author stm
		 * 
		 */
		private enum ColumnType {
			UNIT(0, TeachPlugin.getString("teachpanel.column.persons.title"), Object.class), ID(0,
					TeachPlugin.getString("teachpanel.column.id.title"), Object.class), PERSONS(1,
					TeachPlugin.getString("teachpanel.column.persons.title"), Integer.class), ORDERMODE(2,
					TeachPlugin.getString("teachpanel.column.ordermode.title"), Object.class), ORDERTALENT(3,
					TeachPlugin.getString("teachpanel.column.ordertalent.title"), Object.class), PRIORITY(4,
					TeachPlugin.getString("teachpanel.column.priority.title"), Double.class), LEARNTARGET(0,
					learnTargetChar, Integer.class), LEARNMAX(1, learnMaxChar, Integer.class), TEACH(2,
					teachDiffChar, Integer.class), VALUE(3, valueChar, Integer.class);

			private int initialColumn;
			private Class<? extends Object> type;
			private String title;

			public Class<? extends Object> getType() {
				return type;
			}

			public String getTitle() {
				return title;
			}

			ColumnType(int col, String title, Class<? extends Object> type) {
				this.initialColumn = col;
				this.title = title;
				this.type = type;
			}

			int getColumn() {
				return initialColumn;
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
			int learnCount = 0;
			int teachCount = 0;
			int talentCount = 0;
			for (SUnit unit : tableUnits) {
				for (String lTalent : unit.getLearnTalentsAsString()) {
					if (!talentIndices.containsKey(lTalent)) {
						talentIndices.put(lTalent, talentCount++);
						learnCount++;
						talents.add(lTalent);
					}
				}
				for (String tTalent : unit.getTeachTalentsAsString()) {
					if (!talentIndices.containsKey(tTalent)) {
						talentIndices.put(tTalent, talentCount++);
						teachCount++;
						talents.add(tTalent);
					}
				}
			}

			// fill content
			content = new Object[tableUnits.size()][getColumnCount() + 1];
			int count = 0;
			for (SUnit unit : tableUnits) {
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
			row[0] = unit.getUnit();
			row[1] = unit.getUnit().getID();
			row[2] = unit.getUnit().getModifiedPersons();
			Order o = Teacher.getCurrentOrder(unit.getUnit());
			if (o == null) {
				row[3] = errorChar;
				row[4] = "--";
			} else if (o.getTalent().equals(Order.ALL)) {
				row[3] = otherChar;
				row[4] = "--";
			} else if (o.isLearnOrder()) {
				row[3] = currentLearnChar;
				row[4] = o.getTalent();
			} else if (o.isTeachOrder()) {
				row[3] = currentTeachChar;
				row[4] = "";// o.getTalent();
			}

			row[5] = unit.getPrio();

			for (int col = numFixedColumns; col < row.length; ++col) {
				row[col] = null;
			}
			for (String lTalent : unit.getLearnTalentsAsString()) {
				row[numFixedColumns + talentIndices.get(lTalent) * 4] = unit.getTarget(lTalent);
				row[numFixedColumns + talentIndices.get(lTalent) * 4 + 1] = unit.getMax(lTalent);
				row[numFixedColumns + talentIndices.get(lTalent) * 4 + 3] = Teacher.getLevel(
						unit.getUnit(), lTalent);
			}

			for (String tTalent : unit.getTeachTalentsAsString()) {
				row[numFixedColumns + talentIndices.get(tTalent) * 4 + 2] = unit
						.getMaximumDifference(tTalent);
				row[numFixedColumns + talentIndices.get(tTalent) * 4 + 3] = Teacher.getLevel(
						unit.getUnit(), tTalent);
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
			for (String lTalent : unit.getLearnTalentsAsString()) {
				if (!talentIndices.containsKey(lTalent)) {
					structureChanged = true;
					break;
				}
			}
			if (!structureChanged)
				for (String tTalent : unit.getTeachTalentsAsString()) {
					if (!talentIndices.containsKey(tTalent)) {
						structureChanged = true;
						break;
					}
				}

			if (structureChanged) {
				// redo everything...
				tableUnits.add(unit);
				init();
			} else {
				// just update relevant row
				int row = findRow(unit.getUnit());
				Object[] sameUnit = row >= 0 ? content[row] : null;
				if (sameUnit != null) {
					// replace unit
					setRow(sameUnit, unit);
					fireTableRowsUpdated(row, row);
				} else {
					// add unit
					Object[][] newContent = new Object[content.length + 1][getColumnCount() + 1];
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
				SUnit su = (SUnit) content[i][ColumnType.UNIT.getColumn()];
				if (su.getUnit().equals(unit)) {
					sameUnit = i;
					break;
				}
			}
			return sameUnit;
		}

		/**
		 * Returns the unit corresponding to a row.
		 */
		public Unit getUnit(int row) {
			return (Unit) content[row][0];
		}

		/**
		 * The number of fixed columns (as opposed to skill columns).
		 * 
		 * @return
		 */
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
			Region region = (Region) rBox.getSelectedItem();
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
			teacher = new Teacher(region.units(), getNamespace(), new NullUserInterface());
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
