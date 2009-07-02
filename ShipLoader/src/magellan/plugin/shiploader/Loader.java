/**
 * 
 */
package magellan.plugin.shiploader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JOptionPane;

import magellan.client.Client;
import magellan.client.event.UnitOrdersEvent;
import magellan.library.GameData;
import magellan.library.Ship;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.gamebinding.EresseaOrderParser;
import magellan.library.relation.ItemTransferRelation;
import magellan.library.relation.ReserveRelation;
import magellan.library.relation.UnitRelation;
import magellan.library.rules.ItemType;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;

/**
 * Class for helping loading of units onto ships.
 * 
 * @author stm
 * 
 */
public class Loader {

	public class SelectionEvent {

		private Ship ship;
		private Unit unit;
		private boolean added;

		public SelectionEvent(Ship ship, boolean added) {
			this.ship = ship;
			this.added = added;
		}

		public SelectionEvent(Unit unit, boolean added) {
			this.unit = unit;
			this.added = added;
		}

		public Collection<Unit> getUnits() {
			return units;
		}

		public Collection<Ship> getShips() {
			return ships;
		}

		public Unit getUnit() {
			return unit;
		}

		public Ship getShip() {
			return ship;
		}

		public boolean isAdded() {
			return added;
		}
	}

	public interface SelectionListener {
		public void selectionChanged(SelectionEvent e);
	}

	private Client client;

	protected ItemType silver;

	protected Set<Ship> ships;
	protected Set<Unit> units;

	private String marker;
	private boolean keepSilver = false;
	private boolean keepSilverInFaction = true;
	private int safety;
	private int safetyPerPerson;
	private int errors;
	private boolean changeShip;

	private Collection<SelectionListener> listeners = new LinkedList<SelectionListener>();

	public Loader(ShipLoaderPlugin shipLoaderPlugin, Client client) {
		this.client = client;
		init(client.getData());
	}

	/**
	 * Reset all parameters (except client).
	 * 
	 * @param gameData
	 */
	void init(GameData gameData) {
		silver = gameData.rules.getItemType(EresseaConstants.I_SILVER);
		ships = new HashSet<Ship>();
		units = new HashSet<Unit>();
		setMarkerName("");
		setSafety(1000);
		setSafetyPerPerson(10);
		setChangeShip(false);
	}

	/**
	 * Add a container to the list of ships (if it is a ship).
	 * 
	 * @param container
	 */
	public void add(UnitContainer container) {
		if (container instanceof Ship && !ships.contains(container)) {
			ships.add((Ship) container);
			notifyAddition((Ship) container);
		}
		// else if (container instanceof UnitContainer) {
		// for (Unit u : container.units())
		// add(u);
		// }
	}

	private void notifyAddition(Ship ship) {
		SelectionEvent event = new SelectionEvent(ship, true);
		for (SelectionListener listener : listeners) {
			listener.selectionChanged(event);
		}
	}

	private void notifyRemoval(Ship ship) {
		SelectionEvent event = new SelectionEvent(ship, false);
		for (SelectionListener listener : listeners) {
			listener.selectionChanged(event);
		}
	}

	/**
	 * Remove a ship.
	 * 
	 * @param container
	 */
	public void remove(UnitContainer container) {
		if (container instanceof Ship && ships.contains(container)) {
			ships.remove((Ship) container);
			notifyRemoval((Ship) container);
		}
	}

	/**
	 * Add a unit.
	 * 
	 * @param unit
	 */
	public void add(Unit unit) {
		if (!units.contains(unit)) {
			units.add(unit);
			notifyAddition(unit);
		}
	}

	/**
	 * Remove a unit.
	 * 
	 * @param unit
	 */
	public void remove(Unit unit) {
		if (units.contains(unit)) {
			units.remove(unit);
			notifyRemoval(unit);
		}
	}

	private void notifyAddition(Unit unit) {
		SelectionEvent event = new SelectionEvent(unit, true);
		for (SelectionListener listener : listeners) {
			listener.selectionChanged(event);
		}
	}

	private void notifyRemoval(Unit unit) {
		SelectionEvent event = new SelectionEvent(unit, false);
		for (SelectionListener listener : listeners) {
			listener.selectionChanged(event);
		}
	}

	/**
	 * Adds all ships and units in selectedObjects.
	 * 
	 * @param selectedObjects
	 */
	public void add(Collection<?> selectedObjects) {
		if (selectedObjects == null)
			return;
		for (Object o : selectedObjects) {
			if (o instanceof UnitContainer)
				add((UnitContainer) o);
			if (o instanceof Unit)
				add((Unit) o);
		}
	}

	/**
	 * Removes all ships and units in selectedObjects.
	 * 
	 * @param selectedObjects
	 */
	public void remove(Collection<?> selectedObjects) {
		if (selectedObjects == null)
			return;
		for (Object o : selectedObjects) {
			if (o instanceof UnitContainer)
				remove((UnitContainer) o);
			if (o instanceof Unit)
				remove((Unit) o);
		}
	}

	/**
	 * Removes all ships and units.
	 */
	public void clear() {
		for (Unit u : units)
			notifyRemoval(u);
		for (Ship s : ships)
			notifyRemoval(s);
		ships.clear();
		units.clear();
	}

	/**
	 * Removes all orders with marker from all units.
	 */
	public void clearOrders() {
		for (Unit u : units) {
			removeOrders(u, getComment(), true);
		}
	}

	protected void checkInput() {
		for (Iterator<Unit> it = units.iterator(); it.hasNext();) {
			Unit u = it.next();
			// check if we have the exact data of this unit
			if (!u.isWeightWellKnown())
				ShipLoaderPlugin.log.warn("estimating weight of " + u);
			// check if unit is on ship
			if (u.getUnitContainer() instanceof Ship) {
				if (isChangeShip()) {
					u.addOrder(Resources.getOrderTranslation(EresseaConstants.O_LEAVE, u.getFaction()
							.getLocale()), true, 1);
					client.getDispatcher().fire(new UnitOrdersEvent(this, u));
				} else {
					ShipLoaderPlugin.log.warn("removing unit, which is on ship: " + u);
					it.remove();
					notifyRemoval(u);
				}
			}
		}
		// check if we have the exact data of all ships
		for (Ship s : ships) {
			if (s.getCapacity() < 0)
				ShipLoaderPlugin.log.warn("estimating capacity of " + s);
		}

	}

	/**
	 * Returns the amount of silver of a unit.
	 * 
	 * @param unit
	 * @return
	 */
	public int getSilver(Unit unit) {
		return unit.getModifiedItem(silver) == null ? 0 : unit.getModifiedItem(silver).getAmount();
	}

	/**
	 * Get the free amount of silver, that is, the unit's modified silver minus the reserved silver
	 * minus the silver the unit gets from other units.
	 * 
	 * @param unit
	 * @return
	 */
	public int getFreeSilver(Unit unit) {
		int amount = getSilver(unit);
		int reserved = 0, gotten = 0;
		for (UnitRelation r : unit.getRelations(UnitRelation.class)) {
			if (r instanceof ReserveRelation) {
				ReserveRelation res = (ReserveRelation) r;
				if (res.itemType.equals(silver))
					reserved += res.amount;
			}
			if (r instanceof ItemTransferRelation) {
				ItemTransferRelation trans = (ItemTransferRelation) r;
				if (trans.itemType.equals(silver) && trans.target.equals(unit)) {
					gotten += trans.amount;
				}
			}
		}

		return amount - reserved - gotten;
	}

	/**
	 * Distribute silver such that every unit u has at least getSafety(null, u).
	 */
	public void distribute() {
		if (silver == null) {
			silver = client.getData().rules.getItemType("Silber");
			if (silver == null) {
				JOptionPane.showMessageDialog(client, "error, no silver type");
				return;
			}
		}
		// check, but ignore if units are on ships
		boolean change = isChangeShip();
		setChangeShip(true);
		checkInput();
		setChangeShip(change);

		// find units with more (resp. less) silver than needed
		Collection<Unit> givers = new LinkedList<Unit>();
		Collection<Unit> getters = new LinkedList<Unit>();
		for (Unit u : units) {
			int giverAmount = getSilver(u) - getSafety(null, u);
			if (giverAmount > 0)
				givers.add(u);
			if (giverAmount < 0)
				getters.add(u);
		}

		// iterate through givers
		for (Unit giver : givers) {
			boolean given = false;
			if (getFreeSilver(giver) > getSafety(null, giver)) {
				// as long as there is silver left, give silver to getters
				for (Iterator<Unit> it = getters.iterator(); it.hasNext();) {
					Unit getter = it.next();
					int amount = Math.min(getSafety(null, getter) - getSilver(getter), getFreeSilver(giver)
							- getSafety(null, giver));
					if (amount > 0) {
						given = true;
						give(giver, getter, amount, silver);
					}
					if (getSafety(null, getter) - getSilver(getter) <= 0)
						it.remove();
					if (getFreeSilver(giver) - getSafety(null, giver) <= 0)
						break;
				}
			}
			if (given)
				client.getDispatcher().fire(new UnitOrdersEvent(this, giver));
		}
		Logger log = Logger.getInstance(EresseaOrderParser.class);
		if (getters.size() > 0)
			log.info("not enough silver");
	}

	/**
	 * Distributes the silver of the selected units to the ships until they are full.
	 * 
	 * @param selectedObjects
	 */
	public void distribute(Collection<?> selectedObjects) {
		if (selectedObjects == null)
			return;
		if (ships.size() == 0) {
			ShipLoaderPlugin.log.warn("no ships to distribute to...");
			return;
		}
		for (Object o : selectedObjects) {
			if (o instanceof Unit) {
				Unit u = (Unit) o;
				for (Ship s : ships) {
					int amount = Math.min(getSpace(s, null), getFreeSilver(u));
					if (amount > 0) {
						give(u, s.getOwnerUnit(), amount, silver);
					}
				}
				client.getDispatcher().fire(new UnitOrdersEvent(this, u), true);
			}
		}
	}

	/**
	 * Packs units on ships with a simple best fit approach.
	 */
	public void execute() {
		checkInput();
		errors = 0;

		// sort units by size
		List<Unit> sortedUnits = new ArrayList<Unit>(units);
		Collections.sort(sortedUnits, new Comparator<Unit>() {

			public int compare(Unit o1, Unit o2) {
				int diff = o1.getModifiedWeight() - o2.getModifiedWeight();
				return diff != 0 ? diff : o1.getID().toString().compareTo(o2.getID().toString());
			}
		});

		// sort ships by size
		Set<Ship> sortedShips = new TreeSet<Ship>(new Comparator<Ship>() {
			public int compare(Ship o1, Ship o2) {
				int diff = getSpace(o2, null) - getSpace(o1, null);
				return diff != 0 ? diff : o1.getID().toString().compareTo(o2.getID().toString());
			}
		});
		sortedShips.addAll(ships);

		while (sortedUnits.size() > 0) {
			// for every unit
			Unit u = sortedUnits.remove(sortedUnits.size() - 1);
			// find ship that best matches
			Ship bestMatch = null;
			Iterator<Ship> shipIt;
			for (shipIt = sortedShips.iterator(); shipIt.hasNext();) {
				Ship s = shipIt.next();
				if (getSpace(s, u) >= u.getModifiedWeight()) {
					bestMatch = s;
				} else {
					break;
				}
			}
			if (bestMatch != null) {
				// put unit on this ship
				sortedShips.remove(bestMatch);
				int l1 = bestMatch.getModifiedLoad();
				load(u, bestMatch);
				int l2 = bestMatch.getModifiedLoad();
				if (l2 - l1 != u.getModifiedWeight())
					ShipLoaderPlugin.log.info(l2 + "-" + l1 + "=" + (l2 - l1) + "=" + u.getModifiedWeight()
							+ "!!!!!!!!!!!");
				sortedShips.add(bestMatch);
			} else {
				error(u);
			}
		}
		if (errors > 0)
			ShipLoaderPlugin.log.warn(errors + " errors");

		fireEvents();
	}

	/**
	 * Returns the capacity of the ship minus its modified load minus the safety margin.
	 * 
	 * @param s
	 * @param u
	 * @return
	 */
	public int getSpace(Ship s, Unit u) {
		return s.getMaxCapacity() - s.getModifiedLoad() - getSafety(s, u);
	}

	/**
	 * Fire order events for all units.
	 */
	private void fireEvents() {
		for (Unit u : units) {
			client.getDispatcher().fire(new UnitOrdersEvent(this, u));
		}
	}

	/**
	 * Mark a unit with an error marker. Does <em>not</em> fire UnitOrderEvents.
	 * 
	 * @param u
	 */
	private void error(Unit u) {
		u.addOrder(getComment() + " === error ! ===", false, 0);
		errors += 1;
	}

	public int getErrors() {
		return errors;
	}

	/**
	 * Add order to put unit onto ship.
	 * 
	 * @param unit
	 * @param ship
	 */
	public void load(Unit unit, Ship ship) {
		String enter = Resources.getOrderTranslation(EresseaConstants.O_ENTER, unit.getFaction()
				.getLocale());
		String leave = Resources.getOrderTranslation(EresseaConstants.O_LEAVE, unit.getFaction()
				.getLocale());
		String shiff = Resources.getOrderTranslation(EresseaConstants.O_SHIP, unit.getFaction()
				.getLocale());
		unit
				.addOrder(enter + " " + shiff + " " + ship.getID().toString() + " " + getComment(), true, 1);
		if (!removeOrders(unit, leave, false))
			// otherwise removeOrders will already have fired
			client.getDispatcher().fire(new UnitOrdersEvent(this, unit));
	}

	/**
	 * Add orders to give amount of item from giver to getter. Does <em>not</em> fire UnitOrderEvents!
	 * 
	 * @param giver
	 * @param getter
	 * @param amount
	 * @param item
	 */
	private void give(Unit giver, Unit getter, int amount, ItemType item) {
		String give = Resources.getOrderTranslation(EresseaConstants.O_GIVE, giver.getFaction()
				.getLocale());
		giver.addOrder(give + " " + getter.getID().toString() + " " + amount + " " + item.toString()
				+ getComment(), false, 0);
	}

	/**
	 * Remove orders from unit that match orderStub. If contains==true, remove all orders that contain
	 * it. Else, remove all orders that start with it (not counting leading spaces).
	 * 
	 * @param unit
	 * @param orderStub
	 * @param contains
	 * @return <code>true</code> if orders were removed.
	 */
	private boolean removeOrders(Unit unit, String orderStub, boolean contains) {
		Collection<String> newOrders = new ArrayList<String>();
		for (String order : unit.getOrders()) {
			if ((contains && !order.contains(orderStub))
					|| (!contains && !order.trim().startsWith(orderStub)))
				newOrders.add(order);
		}
		if (newOrders.size() != unit.getOrders().size()) {
			unit.setOrders(newOrders, true);
			client.getDispatcher().fire(new UnitOrdersEvent(this, unit));
			return true;
		}
		return false;
	}

	/**
	 * Return the safety margin for the specified ship if unit is added to it. If ship==null, return
	 * only margin for unit. If unit==null, only for ship.
	 * 
	 * @param ship
	 * @param unit
	 * @return
	 */
	public int getSafety(Ship ship, Unit unit) {
		int amount = 0;
		if (ship != null) {
			amount += getSafety();
			for (Unit u : ship.modifiedUnits()) {
				amount += u.getModifiedPersons() * getSafetyPerPerson();
			}
		}
		if (unit != null)
			amount += unit.getModifiedPersons() * getSafetyPerPerson();
		return amount;
	}

	/**
	 * Returns safety margin (in silver) for a ship.
	 * 
	 * @return
	 */
	public int getSafety() {
		return safety;
	}

	/**
	 * Sets safety margin (in silver) for a ship.
	 * 
	 * @param safety
	 */
	public void setSafety(int safety) {
		this.safety = safety;
	}

	/**
	 * Returns the safety margin per person.
	 * 
	 * @return
	 */
	public int getSafetyPerPerson() {
		return safetyPerPerson;
	}

	/**
	 * Set the safety margin per person.
	 * 
	 * @param safety
	 */
	public void setSafetyPerPerson(int safety) {
		this.safetyPerPerson = safety;
	}

	/**
	 * Return a comment which marks orders coming from us.
	 * 
	 * @return
	 */
	public String getComment() {
		return "; $" + getMarker() + "$loaded";
	}

	/**
	 * Return the marker which marks orders coming from us.
	 * 
	 * @return
	 */
	public String getMarker() {
		return marker;
	}

	/**
	 * Sets the marker.
	 * 
	 * @param name
	 */
	public void setMarkerName(String name) {
		marker = name;
	}

	/**
	 * Specify if units should keep their silver. If false, units may distribute silver to other
	 * units.
	 * 
	 * @param keep
	 * @deprecated not yet in use
	 */
	public void setKeepSilver(boolean keep) {
		keepSilver = keep;
	}

	/**
	 * Returns true if units should keep their silver. If false, units may distribute silver to other
	 * units.
	 * 
	 * @return
	 */
	public boolean isKeepSilver() {
		return keepSilver;
	}

	/**
	 * Specify if units must pass their silver only to units of the same faction. If false and
	 * isKeepSilver() is true, units may pass silver to any unit.
	 * 
	 * @param keep
	 */
	public void setKeepSilverInFaction(boolean keep) {
		keepSilverInFaction = keep;
	}

	/**
	 * Returns if units must pass their silver only to units of the same faction. If false and
	 * isKeepSilver() is true, units may pass silver to any unit.
	 * 
	 * @return
	 */
	public boolean isKeepSilverInFaction() {
		return keepSilverInFaction;
	}

	/**
	 * Returns if units may change ships if they are already on a ship.
	 * 
	 * @return
	 */
	public boolean isChangeShip() {
		return changeShip;
	}

	/**
	 * Specifies if units may change ships if they are already on a ship.
	 * 
	 * @param change
	 */
	public void setChangeShip(boolean change) {
		changeShip = change;
	}

	public void addListener(SelectionListener listener) {
		listeners.add(listener);
	}

	public void removeSelectionListener(SelectionListener listener) {
		listeners.remove(listener);
	}

}