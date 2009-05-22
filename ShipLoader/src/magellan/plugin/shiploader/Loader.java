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

import magellan.client.Client;
import magellan.client.event.UnitOrdersEvent;
import magellan.library.GameData;
import magellan.library.Ship;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.event.GameDataEvent;
import magellan.library.event.GameDataListener;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.relation.ItemTransferRelation;
import magellan.library.relation.ReserveRelation;
import magellan.library.relation.UnitRelation;
import magellan.library.rules.ItemType;
import magellan.library.utils.Resources;

public class Loader implements GameDataListener {

	/**
	 * 
	 */
	private final ShipLoaderPlugin shipLoaderPlugin;

	private Client client;

	protected ItemType silver;

	Set<Ship> ships;
	Set<Unit> units;
	private String marker;
	private boolean keepSilver = false;
	private boolean keepSilverInFaction = true;
	private int safety;
	private int safetyPerPerson;
	private int errors;
	private boolean changeShip;

	public Loader(ShipLoaderPlugin shipLoaderPlugin, Client client) {
		this.shipLoaderPlugin = shipLoaderPlugin;
		this.client = client;
		init(client.getData());
		client.getDispatcher().addGameDataListener(this);
	}

	private void init(GameData gameData) {
		silver = client.getData().rules.getItemType(EresseaConstants.I_SILVER);
		ships = new HashSet<Ship>();
		units = new HashSet<Unit>();
		setMarkerName("");
		setSafety(1000);
		setSafetyPerPerson(10);
		setChangeShip(false);
	}

	public void add(UnitContainer container) {
		if (container instanceof Ship) {
			ships.add((Ship) container);
		} else if (container instanceof UnitContainer) {
			for (Unit u : container.units())
				add(u);
		}
	}

	public void remove(UnitContainer container) {
		if (container instanceof Ship) {
			ships.remove((Ship) container);
		} else if (container instanceof UnitContainer) {
			for (Unit u : container.units())
				remove(u);
		}
	}

	public void add(Unit unit) {
		units.add(unit);
	}

	public void remove(Unit unit) {
		units.remove(unit);
	}

	public void add(Collection selectedObjects) {
		if (selectedObjects == null)
			return;
		for (Object o : selectedObjects) {
			if (o instanceof UnitContainer)
				add((UnitContainer) o);
			if (o instanceof Unit)
				add((Unit) o);
		}
	}

	public void remove(Collection selectedObjects) {
		if (selectedObjects == null)
			return;
		for (Object o : selectedObjects) {
			if (o instanceof UnitContainer)
				remove((UnitContainer) o);
			if (o instanceof Unit)
				remove((Unit) o);
		}
	}

	public void clear() {
		ships.clear();
		units.clear();
	}

	public void clearOrders() {
		for (Unit u : units) {
			removeOrders(u, getMarker(), true);
		}
		fireEvents();
	}

	protected void checkInput() {
		for (Iterator<Unit> it = units.iterator(); it.hasNext();) {
			Unit u = it.next();
			if (!u.isWeightWellKnown())
				ShipLoaderPlugin.log.warn("estimating weight of " + u);
			if (u.getUnitContainer() instanceof Ship) {
				if (isChangeShip())
					u.addOrder(Resources.getOrderTranslation(EresseaConstants.O_LEAVE, u.getFaction()
							.getLocale()), true, 1);
				else {
					ShipLoaderPlugin.log.warn("removing unit, which is on ship: " + u);
					it.remove();
				}
			}
		}
		for (Ship s : ships) {
			if (s.getCapacity() < 0)
				ShipLoaderPlugin.log.warn("estimating capacity of " + s);
		}

	}

	public int getSilver(Unit unit){
	  return unit.getModifiedItem(silver)==null?0:unit.getModifiedItem(silver).getAmount();
	}
	
	public int getFreeSilver(Unit unit){
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
	  boolean change = isChangeShip();
	  setChangeShip(true);
		checkInput();
		setChangeShip(change);
		Collection<Unit> givers = new LinkedList<Unit>();
    Collection<Unit> getters = new LinkedList<Unit>();
		for (Unit u: units){
      int giverAmount = getSilver(u) - getSafety(null, u); 
      if (giverAmount>0)
        givers.add(u);
      int getterAmount = u.getModifiedItem(silver)==null?0:u.getModifiedItem(silver).getAmount();
      if (getterAmount<getSafety(null, u))
        getters.add(u);
		}
		for (Unit giver : givers) {
			int giverAmount = getFreeSilver(giver);
			if (giverAmount > getSafetyPerPerson()) {
				for (Iterator<Unit> it = getters.iterator(); it.hasNext();) {
				  Unit getter = it.next();
          int amount = Math.min(getSafety(null, getter)-getSilver(getter), getFreeSilver(giver));
          if (amount > 0) {
          	give(giver, getter, amount, silver);
          }
          if (getSafety(null, getter)-getSilver(getter)<=0)
            it.remove();
          if(getFreeSilver(giver)<=0)
            break;
				}
			}
		}
		fireEvents();
	}

	public void distribute(Collection selectedObjects) {
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
					} else
						break;
				}
				client.getDispatcher().fire(new UnitOrdersEvent(this, u), true);
			}
		}
	}

	public void execute() {
		checkInput();
		errors = 0;

		// sort units by size
		List<Unit> sortedUnits = new ArrayList<Unit>(units);
		Collections.sort(sortedUnits, new Comparator<Unit>() {

			@Override
			public int compare(Unit o1, Unit o2) {
				int diff = o1.getModifiedWeight() - o2.getModifiedWeight();
				return diff != 0 ? diff : o1.getID().toString().compareTo(o2.getID().toString());
			}
		});

		// sort ships by size
		Set<Ship> sortedShips = new TreeSet<Ship>(new Comparator<Ship>() {
			@Override
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
					bestMatch = bestMatch;
					break;
				}
			}
			if (bestMatch != null) {
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

	private int getSpace(Ship s, Unit u) {
		return s.getMaxCapacity() - s.getModifiedLoad() - getSafety(s, u);
	}

	private void fireEvents() {
		for (Unit u : units) {
			client.getDispatcher().fire(new UnitOrdersEvent(this.shipLoaderPlugin, u));
		}
	}

	private void error(Unit u) {
		u.addOrder(getComment() + " === error ! ===", false, 0);
		errors += 1;
	}

	public void load(Unit unit, Ship ship) {
		String enter = Resources.getOrderTranslation(EresseaConstants.O_ENTER, unit.getFaction()
				.getLocale());
		String leave = Resources.getOrderTranslation(EresseaConstants.O_LEAVE, unit.getFaction()
				.getLocale());
		String shiff = Resources.getOrderTranslation(EresseaConstants.O_SHIP, unit.getFaction()
				.getLocale());
		unit
				.addOrder(enter + " " + shiff + " " + ship.getID().toString() + " " + getComment(), true, 1);
		removeOrders(unit, leave, false);
	}

	private void give(Unit giver, Unit getter, int amount, ItemType item) {
		String give = Resources.getOrderTranslation(EresseaConstants.O_GIVE, giver.getFaction()
				.getLocale());
		giver.addOrder(give + " " + getter.getID().toString() + " " + amount + " " + item.toString()
				+ getComment(), false, 0);
	}

	private void removeOrders(Unit unit, String orderStub, boolean contains) {
		Collection<String> newOrders = new ArrayList<String>();
		for (String order : unit.getOrders()) {
			if ((contains && !order.contains(orderStub))
					|| (!contains && !order.trim().startsWith(orderStub)))
				newOrders.add(order);
		}
		if (newOrders.size() != unit.getOrders().size())
			unit.setOrders(newOrders, true);
	}

	public int getSafety(Ship ship, Unit unit) {
		int amount = 0;
		if (ship!=null){
		  amount += getSafety();
			for (Unit u : ship.modifiedUnits()) {
				amount += u.getModifiedPersons() * getSafetyPerPerson();
			}
		}
		if (unit != null)
			amount += unit.getModifiedPersons() * getSafetyPerPerson();
		return amount;
	}

	public int getSafety() {
		return safety;
	}

	public void setSafety(int safety) {
		this.safety = safety;
	}

	public int getSafetyPerPerson() {
		return safetyPerPerson;
	}

	public void setSafetyPerPerson(int safety) {
		this.safetyPerPerson = safety;
	}

	public String getComment() {
		return "; " + getMarker();
	}

	public String getMarker() {
		return marker;
	}

	public void setMarkerName(String name) {
		marker = "$sl$" + name;
	}

	public void setKeepSilver(boolean keep) {
		keepSilver = keep;
	}

	public boolean isKeepSilver() {
		return keepSilver;
	}

	public void setKeepSilverInFaction(boolean keep) {
		keepSilverInFaction = keep;
	}

	public boolean isKeepSilverInFaction() {
		return keepSilverInFaction;
	}

	public boolean isChangeShip() {
		return changeShip;
	}

	public void setChangeShip(boolean change) {
		changeShip = change;
	}

	@Override
	public void gameDataChanged(GameDataEvent e) {
		init(e.getGameData());
	}

}