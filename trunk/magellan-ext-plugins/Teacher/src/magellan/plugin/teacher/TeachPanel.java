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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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
import magellan.plugin.teacher.Teacher.SUnit;

/**
 * DOCUMENT ME!
 * 
 * @author stm
 */
public class TeachPanel extends InternationalizedDataDialog implements SelectionListener,
		ActionListener, UnitOrdersListener {
	private static Logger log = Logger.getInstance(TeachPanel.class);
	public static final String IDENTIFIER = "TEACH";

	private static String learnChar;
	private static String teachChar;
	private static String otherChar;
	private static String errorChar;
	private static String valueChar;

	protected TeachTable tTable;

	private Teacher teacher;

	private String namespace = "";
	private JComboBox rBox;

	/**
	 * Creates a new TradeOrganizer object.
	 * 
	 * 
	 */
	public TeachPanel(Frame owner, EventDispatcher dispatcher, GameData data, Properties settings,
			Region region) {
		super(owner, false, dispatcher, data, settings);

		learnChar = TeachPlugin.getString("teachpanel.constants.learnChar");
		teachChar = TeachPlugin.getString("teachpanel.constants.teachChar");
		otherChar = TeachPlugin.getString("teachpanel.constants.otherChar");
		errorChar = TeachPlugin.getString("teachpanel.constants.errorChar");
		valueChar = TeachPlugin.getString("teachpanel.constants.valueChar");

		// register for events
		dispatcher.addGameDataListener(this);
		dispatcher.addSelectionListener(this);
		dispatcher.addUnitOrdersListener(this);

		init();

		namespace = "stm";
		setVisible(true);
		rBox.setSelectedIndex(0);
	}

	private void setUnits() {
		if (teacher == null)
			tTable.setUnits(Collections.<SUnit> emptyList());
		else {
			Collection<SUnit> units = teacher.getUnits(namespace, false);
			tTable.setUnits(units);
		}
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

		// TODO fill top panel
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
		setActiveRegion(getCurrentRegion());
	}

	/**
	 * 
	 * @see magellan.client.event.SelectionListener#selectionChanged(magellan.client.event.SelectionEvent)
	 */
	public void selectionChanged(SelectionEvent se) {
	}

	@Override
	protected void quit() {
		// store settings
		// settings.setProperty("TradeOrganizer.width", String.valueOf(getWidth()));
		// settings.setProperty("TradeOrganizer.height", String.valueOf(getHeight()));
		// settings.setProperty("TradeOrganizer.xPos", String.valueOf(getLocation().x));
		// settings.setProperty("TradeOrganizer.yPos", String.valueOf(getLocation().y));
		// settings.setProperty("TradeOrganizer.minSellMultiplier", String.valueOf(minSellMultiplier));

		super.quit();
	}

	/**
	 * DOCUMENT-ME
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
						SUnit u = (SUnit) sorter.getValueAt(i, 0);

						if (u != null) {
							dispatcher.fire(new SelectionEvent<Unit>(this, null, u.getUnit(),
									SelectionEvent.ST_DEFAULT));
						}
					}
				}
			});
			;

			// // sorting
			// this.getTableHeader().addMouseListener(new MouseAdapter() {
			// @Override
			// public void mouseClicked(MouseEvent e) {
			// int i = getTableHeader().getColumnModel().getColumnIndexAtX(e.getPoint().x);
			// model.sort(i);
			// tTable.revalidate();
			// tTable.repaint();
			// }
			// });
			this.addMouseListener(this);
		}

		/**
		 * @return
		 */
		public Collection<SUnit> getUnits() {
			return model.getUnits();
		}

		public void setUnits(Collection<SUnit> units) {
			model.setUnits(units);
			tTable.revalidate();
			tTable.repaint();
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
		 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
		 */
		public void mouseClicked(final MouseEvent mouseEvent) {

			if (mouseEvent.getButton() == MouseEvent.BUTTON3) {
				JPopupMenu menu = new JPopupMenu();
				int row = rowAtPoint(mouseEvent.getPoint());
				int col = columnAtPoint(mouseEvent.getPoint());
				JMenuItem setValueMenu;
				JMenuItem delValueMenu;
				if (getInternalModel().getColumnType(col) == magellan.plugin.teacher.TeachPanel.TeachTableModel.ColumnType.LEARN) {
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
			if (getSelectedRowCount() <= 0)
				return;
			int selectedColumn = getColumnModel().getColumnIndexAtX(m.getPoint().x);

			if (selectedColumn < getInternalModel().getColumnCount()
					- getInternalModel().getTalentCount() * 3)
				return;
			String talent = getInternalModel().getColumnName(selectedColumn);
			String mode = talent.substring(talent.length() - 1);
			talent = talent.substring(0, talent.length() - 2);

			if (mode.equals("L") || mode.equals("T")) {
				String userInput = JOptionPane.showInputDialog(TeachPanel.this, TeachPlugin.getString(
						"teachpanel.contextmenu.add.text." + mode, new Object[] { talent }), "100");
				if (userInput != null) {
					try {
						double value = Double.parseDouble(userInput);
						Collection<Unit> units = null;
						units = new ArrayList<Unit>(getSelectedRowCount());

						for (int row : getSelectedRows()) {
							units.add((Unit) getModel().getValueAt(row, 0));
						}
						if (mode.equals(learnChar)) {
							Teacher.addOrder(units, namespace, new Order(talent, value));
						} else if (mode.equals(teachChar)) {
							Teacher.addOrder(units, namespace, new Order(talent, value));
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
				if (mode.equals(learnChar)) {
					Teacher.delOrder(units, namespace, new Order(talent, 0.0));
				} else if (mode.equals(teachChar)) {
					Teacher.delOrder(units, namespace, new Order(talent, 0));
				}
				getDispatcher().fire(new GameDataEvent(this, getData()));
			}

		}
	}

	@SuppressWarnings("serial")
	private static class TeachTableModel extends AbstractTableModel {
		private int curSort = 1;
		private List<SUnit> tableUnits = new ArrayList<SUnit>();
		private static final int numFixedColumns = 5;

		enum ColumnType {
			UNIT(0, TeachPlugin.getString("teachpanel.column.persons.title"), Object.class), ID(0,
					TeachPlugin.getString("teachpanel.column.id.title"), Object.class), PERSONS(0,
					TeachPlugin.getString("teachpanel.column.persons.title"), Integer.class), ORDERMODE(0,
					TeachPlugin.getString("teachpanel.column.ordermode.title"), Object.class), ORDERTALENT(0,
					TeachPlugin.getString("teachpanel.column.ordertalent.title"), Object.class), LEARN(0,
					learnChar, Float.class), TEACH(0, teachChar, Integer.class), VALUE(0, valueChar,
					Integer.class);

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

		Object content[][];

		Map<String, Integer> talentIndices;
		List<String> talents;

		public TeachTableModel() {
			init();
		}

		/**
		 * Initialize content.
		 */
		protected void init() {
			talentIndices = new HashMap<String, Integer>();
			talents = new ArrayList<String>();
			int learnCount = 0;
			int teachCount = 0;
			int talentCount = 0;
			for (SUnit unit : tableUnits) {
				for (String lTalent : unit.getLearnTalents()) {
					if (!talentIndices.containsKey(lTalent)) {
						talentIndices.put(lTalent, talentCount++);
						learnCount++;
						talents.add(lTalent);
					}
				}
				for (String tTalent : unit.getTeachTalents()) {
					if (!talentIndices.containsKey(tTalent)) {
						talentIndices.put(tTalent, talentCount++);
						teachCount++;
						talents.add(tTalent);
					}
				}
			}

			content = new Object[tableUnits.size()][getColumnCount() + 1];
			int count = 0;
			for (SUnit unit : tableUnits) {
				content[count][0] = unit;
				content[count][1] = unit.getUnit().getID();
				content[count][2] = unit.getUnit().getModifiedPersons();
				Order o = Teacher.getCurrentOrder(unit.getUnit());
				if (o == null) {
					content[count][3] = errorChar;
					content[count][4] = "--";
				} else if (o.getTalent().equals(Order.ALL)) {
					content[count][3] = otherChar;
					content[count][4] = "--";
				} else if (o.getType() == Order.LEARN) {
					content[count][3] = learnChar;
					content[count][4] = o.getTalent();
				} else if (o.getType() == Order.TEACH) {
					content[count][3] = teachChar;
					content[count][4] = "";// o.getTalent();
				}

				for (String lTalent : unit.getLearnTalents()) {
					content[count][numFixedColumns + talentIndices.get(lTalent) * 3] = unit.getDiff(lTalent);
					content[count][numFixedColumns + talentIndices.get(lTalent) * 3 + 2] = Teacher.getLevel(
							unit.getUnit(), lTalent);
				}

				for (String tTalent : unit.getTeachTalents()) {
					content[count][numFixedColumns + talentIndices.get(tTalent) * 3 + 1] = unit
							.getMaximumDifference(tTalent);
					content[count][numFixedColumns + talentIndices.get(tTalent) * 3 + 2] = Teacher.getLevel(
							unit.getUnit(), tTalent);
				}

				content[count][getColumnCount()] = count;
				count++;
			}
			fireTableStructureChanged();
		}

		/**
		 * 
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
			return numFixedColumns + getTalentCount() * 3;
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

		@Override
		public String getColumnName(int col) {
			if (col < numFixedColumns)
				return ColumnType.values()[col].getTitle();
			else if ((col - numFixedColumns) % 3 == 0)
				return talents.get((col - numFixedColumns) / 3) + " " + learnChar;
			else if ((col - numFixedColumns) % 3 == 1)
				return talents.get((col - numFixedColumns) / 3) + " " + teachChar;
			else
				return talents.get((col - numFixedColumns) / 3) + " " + valueChar;
		}

		/**
		 * 
		 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
		 */
		@Override
		public Class<? extends Object> getColumnClass(int col) {
			if (col < numFixedColumns)
				return ColumnType.values()[col].getType();
			else if ((col - numFixedColumns) % 3 == 0)
				return ColumnType.LEARN.getClass();
			else if ((col - numFixedColumns) % 3 == 1)
				return ColumnType.TEACH.getClass();
			else
				return ColumnType.VALUE.getClass();
		}

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

			default:
				if ((col - numFixedColumns) % 3 == 0)
					return ColumnType.LEARN;
				else if ((col - numFixedColumns) % 3 == 1)
					return ColumnType.TEACH;
				else
					return ColumnType.VALUE;
			}
		}

		/**
		 * 
		 */
		public Collection<SUnit> getUnits() {
			return tableUnits;
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

			// if (Math.abs(curSort)-1 < getColumnCount())
			// sort();
		}

		/**
		 * 
		 */
		public SUnit getUnit(int row) {
			// return tableUnits.get(Integer.parseInt(content[row][getColumnCount()]));
			return (SUnit) content[row][0];
		}

		/**
		 * @param i
		 */
		public void sort(int i) {
			if (curSort > 0 && curSort - 1 == i) {
				curSort = -i - 1;
			} else {
				curSort = i + 1;
			}
			sort();
		}

		public void sort() {
			Arrays.sort(content, new ColumnComparator(curSort));

		}

		public static int getFixedColumns() {
			return numFixedColumns;
		}
	}

	/**
	 * Compares rows according to a pre-specified column.
	 * 
	 * @author stm
	 * 
	 */
	public static class ColumnComparator implements Comparator<Object[]> {

		private int sortCol;

		/**
		 * The arrays are compared by column (i-1) if i&gt;0 and by column (-i-1) if i&lt;0.
		 * 
		 * @param i
		 */
		public ColumnComparator(int i) {
			sortCol = i;
		}

		public int compare(Object[] o1, Object[] o2) {
			int index, sgn;
			if (sortCol < 0) {
				index = -sortCol - 1;
				sgn = -1;
			} else {
				index = sortCol - 1;
				sgn = 1;
			}

			if (o1 == null) {
				if (o2 == null)
					return 0;
				else
					return -1;
			}
			if (o2 == null)
				return 1;
			if (o1[index] == null)
				return o2[index] == null ? 0 : 1;
			if (o2[index] == null)
				return -1;

			if (index >= TeachTableModel.getFixedColumns() - 1) {
				try {
					float f1 = (Float) o1[index]; // Float.parseFloat(o1[index]);
					float f2 = (Float) o2[index]; // Float.parseFloat(o2[index]);
					return f1 > f2 ? 2 * sgn : (f1 == f2 ? 0 : -2 * sgn);
				} catch (Exception e) {
				}
			}
			if (o1[index] instanceof Comparable)
				return ((Comparable) o1[index]).compareTo(o2[index]) * sgn;
			return 0;
		}

	}

	/**
	 * Change set of units when a new region is selected.
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JComboBox) {
			JComboBox box = (JComboBox) e.getSource();
			Region region = (Region) box.getSelectedItem();
			setActiveRegion(region);
		}
	}

	private void setActiveRegion(Region region) {
		if (region == null)
			teacher = null;
		else
			teacher = new Teacher(region.units(), namespace, new NullUserInterface());
		setUnits();

	}

}
