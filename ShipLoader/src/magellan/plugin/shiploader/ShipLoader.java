/**
 *
 */
package magellan.plugin.shiploader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JOptionPane;

import magellan.client.Client;
import magellan.client.event.UnitOrdersEvent;
import magellan.library.EntityID;
import magellan.library.GameData;
import magellan.library.Order;
import magellan.library.Region;
import magellan.library.Ship;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.gamebinding.EresseaOrderParser;
import magellan.library.gamebinding.MovementEvaluator;
import magellan.library.gamebinding.OrderChanger;
import magellan.library.relation.ItemTransferRelation;
import magellan.library.relation.ReserveRelation;
import magellan.library.relation.UnitRelation;
import magellan.library.rules.ItemType;
import magellan.library.utils.logging.Logger;

/**
 * Class for helping loading of units onto ships.
 *
 * @author stm
 */
public class ShipLoader {

  public class ShipStruct implements Comparable<ShipStruct> {

    private int capacity;
    private Ship ship;

    public ShipStruct(Ship s) {
      ship = s;
      capacity = getSpace(ship) - getSafety(ship);
    }

    public int compare(ShipStruct s1, ShipStruct s2) {
      int diff = getSpace(s1) - getSpace(s2);
      return diff != 0 ? diff : s1.ship.getID().compareTo(s2.ship.getID());
    }

    public int compareTo(ShipStruct o) {
      return compare(this, o);
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof ShipStruct) {
        return ((ShipStruct) obj).ship.getID().equals(ship.getID());
      }
      return false;
    }

    @Override
    public int hashCode() {
      return ship.hashCode();
    }

    @Override
    public String toString() {
      return ship.toString() + " (" + capacity + ")";
    }

    public void update() {
      capacity = getSpace(ship) - getSafety(ship);
    }

  }

  public class InclusionEvent {

    private Ship ship;
    private Unit unit;
    private boolean added;

    public InclusionEvent(Ship ship, boolean added) {
      this.ship = ship;
      this.added = added;
    }

    public InclusionEvent(Unit unit, boolean added) {
      this.unit = unit;
      this.added = added;
    }

    public Collection<Unit> getUnits() {
      return units;
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

  public interface InclusionListener {
    public void selectionChanged(InclusionEvent e);
  }

  private Client client;

  protected ItemType silver;

  Map<EntityID, ShipStruct> ships = new HashMap<EntityID, ShipStruct>();

  protected Set<Unit> units;

  private String marker;
  private boolean keepSilver = false;
  private boolean keepSilverInFaction = true;
  private int safety;
  private int safetyPerPerson;
  private int errors;
  private boolean changeShip;

  private Collection<InclusionListener> listeners = new LinkedList<InclusionListener>();

  private String enterToken;
  private String leaveToken;
  private String shipToken;
  private String giveToken;

  private MovementEvaluator evaluator;

  private Locale locale = null;

  public ShipLoader(ShipLoaderPlugin shipLoaderPlugin, Client client) {
    this.client = client;
    init(client.getData());
    setMarkerName("");
    setSafety(1000);
    setSafetyPerPerson(10);
    setChangeShip(false);
  }

  /**
   * Reset all parameters (except client).
   *
   * @param gameData
   */
  protected void init(GameData gameData) {
    silver = gameData.getRules().getItemType(EresseaConstants.I_USILVER);
    ships = new HashMap<EntityID, ShipStruct>();
    units = new HashSet<Unit>();

    evaluator = gameData.getGameSpecificStuff().getMovementEvaluator();
  }

  /**
   * Add a container to the list of ships (if it is a ship).
   *
   * @param container
   */
  public void add(UnitContainer container) {
    if (container instanceof Ship && !ships.containsKey(container.getID())) {
      ships.put(((Ship) container).getID(), new ShipStruct((Ship) container));
      notifyAddition((Ship) container);
    }
    // else if (container instanceof UnitContainer) {
    // for (Unit u : container.units())
    // add(u);
    // }
  }

  protected void notifyAddition(Ship ship) {
    InclusionEvent event = new InclusionEvent(ship, true);
    for (InclusionListener listener : listeners) {
      listener.selectionChanged(event);
    }
  }

  protected void notifyRemoval(Ship ship) {
    InclusionEvent event = new InclusionEvent(ship, false);
    for (InclusionListener listener : listeners) {
      listener.selectionChanged(event);
    }
  }

  /**
   * Remove a ship.
   *
   * @param container
   */
  public void remove(UnitContainer container) {
    if (container instanceof Ship && ships.containsKey(container.getID())) {
      ships.remove(container.getID());
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
    InclusionEvent event = new InclusionEvent(unit, true);
    for (InclusionListener listener : listeners) {
      listener.selectionChanged(event);
    }
  }

  private void notifyRemoval(Unit unit) {
    InclusionEvent event = new InclusionEvent(unit, false);
    for (InclusionListener listener : listeners) {
      listener.selectionChanged(event);
    }
  }

  /**
   * Adds all ships and units in selectedObjects.
   *
   * @param selectedObjects
   */
  public void add(Collection<?> selectedObjects) {
    if (selectedObjects == null) {
      return;
    }
    for (Object o : selectedObjects) {
      if (o instanceof UnitContainer) {
        add((UnitContainer) o);
      }
      if (o instanceof Unit) {
        add((Unit) o);
      }
    }
  }

  /**
   * Removes all ships and units in selectedObjects.
   *
   * @param selectedObjects
   */
  public void remove(Collection<?> selectedObjects) {
    if (selectedObjects == null) {
      return;
    }
    for (Object o : selectedObjects) {
      if (o instanceof UnitContainer) {
        remove((UnitContainer) o);
      }
      if (o instanceof Unit) {
        remove((Unit) o);
      }
    }
  }

  /**
   * Removes all ships and units.
   */
  public void clear() {
    for (Unit u : units) {
      notifyRemoval(u);
    }
    for (ShipStruct s : ships.values()) {
      notifyRemoval(s.ship);
    }
    ships.clear();
    units.clear();
  }

  protected ShipStruct getManagedShip(Unit unit) {
    if (unit.getModifiedShip() != null) {
      return ships.get(unit.getModifiedShip().getID());
    }
    return null;
  }

  public int getSpace(Ship ship) {
    return ship.getMaxCapacity() - ship.getModifiedLoad();
  }

  /**
   * Removes all orders with marker from all units.
   */
  public void clearOrders() {
    for (Unit u : units) {
      removeOrders(u, getComment(), true);
    }
  }

  protected SortedSet<ShipStruct> checkInput(boolean leaveShip, boolean removeShipUnits) {
    // check if we have the exact data of all ships
    for (ShipStruct s : ships.values()) {
      s.update();
      if (s.ship.getCapacity() < 0) {
        ShipLoaderPlugin.log.warn("estimating capacity of " + s);
      }
    }

    for (Iterator<Unit> it = units.iterator(); it.hasNext();) {
      Unit u = it.next();
      // check if we have the exact data of this unit
      if (!u.isWeightWellKnown()) {
        ShipLoaderPlugin.log.warn("estimating weight of " + u);
      }
      // check if unit is on ship
      ShipStruct sc = getManagedShip(u);
      if (sc != null) {
        if (leaveShip) {
          unload(u, sc);
        }
        if (removeShipUnits) {
          ShipLoaderPlugin.log.warn("removing unit, which is on ship: " + u);
          it.remove();
          notifyRemoval(u);
        }
      }
    }

    SortedSet<ShipStruct> sortedShips = new TreeSet<ShipStruct>();
    for (ShipStruct s : ships.values()) {
      sortedShips.add(s);
    }
    return sortedShips;
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
        if (res.itemType.equals(silver)) {
          reserved += res.amount;
        }
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
      silver = client.getData().getRules().getItemType("Silber");
      if (silver == null) {
        JOptionPane.showMessageDialog(client, "error, no silver type");
        return;
      }
    }
    // check, but ignore if units are on ships
    checkInput(true, false);

    // find units with more (resp. less) silver than needed
    Collection<Unit> givers = new LinkedList<Unit>();
    Collection<Unit> getters = new LinkedList<Unit>();
    Set<Region> doneRegions = new HashSet<Region>();
    for (Unit u2 : units) {
      givers.clear();
      getters.clear();
      if (!doneRegions.contains(u2.getRegion())) {
        doneRegions.add(u2.getRegion());
        for (Unit u : units) {
          if (!u2.getRegion().equals(u.getRegion())) {
            continue;
          }
          int giverAmount = getSilver(u) - getSafety(u);
          if (giverAmount > 0) {
            givers.add(u);
          }
          if (giverAmount < 0) {
            getters.add(u);
          }
        }

        // iterate through givers
        Iterator<Unit> getIt, giveIt;
        boolean done = getters.isEmpty() || givers.isEmpty();
        Unit giver, getter;
        int wantedSilver, freeSilver;
        if (!done) {
          for (getIt = getters.iterator(), giveIt = givers.iterator(), giver = giveIt.next(), getter =
              getIt.next(), freeSilver = getFreeSilver(giver) - getSafety(giver), wantedSilver =
                  getSafety(getter) - getSilver(getter); !done;) {
            int amount = Math.min(wantedSilver, freeSilver);
            give(giver, getter, amount, silver);
            if (amount >= wantedSilver) {
              if (getIt.hasNext()) {
                getter = getIt.next();
                wantedSilver = getSafety(getter) - getSilver(getter);
              } else {
                done = true;
              }
            } else {
              wantedSilver -= amount;
            }
            if (amount >= freeSilver) {
              if (giveIt.hasNext()) {
                giver = giveIt.next();
                freeSilver = getFreeSilver(giver) - getSafety(giver);
              } else {
                if (wantedSilver > 0) {
                  Logger log = Logger.getInstance(EresseaOrderParser.class);
                  if (getters.size() > 0) {
                    log.info("not enough silver");
                  }
                }
                done = true;
              }
            } else {
              freeSilver -= amount;
            }
          }
        }
        for (Unit u : givers) {
          client.getDispatcher().fire(new UnitOrdersEvent(this, u));
        }
      }

    }

  }

  /**
   * Distributes the silver of the selected units to the ships until they are full.
   *
   * @param selectedObjects
   */
  public void distribute(Collection<?> selectedObjects) {
    if (selectedObjects == null) {
      return;
    }

    if (ships.size() == 0) {
      ShipLoaderPlugin.log.warn("no ships to distribute to...");
      return;
    }
    SortedSet<ShipStruct> sortedShips = checkInput(false, false);
    for (Object o : selectedObjects) {
      if (o instanceof Unit) {
        Unit u = (Unit) o;
        ShipStruct scGive = getManagedShip(u);
        for (ShipStruct scGet : new ArrayList<ShipStruct>(sortedShips)) {
          if (scGet.ship.getRegion() == u.getRegion() && scGive != scGet) {
            int amount = Math.min(getSpace(scGet), getFreeSilver(u));
            if (amount > 0) {
              sortedShips.remove(scGet);
              if (scGive != null) {
                sortedShips.remove(scGive);
              }
              give(u, scGet.ship.getOwnerUnit(), amount, silver);
              sortedShips.add(scGet);
              if (scGive != null) {
                sortedShips.add(scGive);
              }
            }
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
    SortedSet<ShipStruct> sortedShips = checkInput(isChangeShip(), !isChangeShip());
    errors = 0;

    // sort units by size
    List<Unit> sortedUnits = new ArrayList<Unit>(units);
    Collections.sort(sortedUnits, new Comparator<Unit>() {

      public int compare(Unit o1, Unit o2) {
        int diff = evaluator.getModifiedWeight(o1) - evaluator.getModifiedWeight(o1);
        return diff != 0 ? diff : o1.getID().toString().compareTo(o2.getID().toString());
      }
    });

    while (sortedUnits.size() > 0) {
      // for every unit
      Unit u = sortedUnits.remove(sortedUnits.size() - 1);
      // find ship that best matches
      ShipStruct bestMatch = null;
      for (ShipStruct s : sortedShips) {
        if (s.ship.getRegion() == u.getRegion() && getSpace(s) >= getSpace(u)) {
          bestMatch = s;
          break;
        }
      }
      if (bestMatch != null) {
        // put unit on this ship
        sortedShips.remove(bestMatch);
        load(u, bestMatch);
        sortedShips.add(bestMatch);
      } else {
        error(u);
      }
      client.getDispatcher().fire(new UnitOrdersEvent(this, u), true);
    }
    if (errors > 0) {
      ShipLoaderPlugin.log.warn(errors + " errors");
    }

    fireEvents();
  }

  /**
   * Packs 1 unit on each ship.
   * Fiete 20200918
   */
  public void execute_one() {
    SortedSet<ShipStruct> sortedShips = checkInput(isChangeShip(), !isChangeShip());
    errors = 0;

    // sort units by size
    List<Unit> sortedUnits = new ArrayList<Unit>(units);
    Collections.sort(sortedUnits, new Comparator<Unit>() {

      public int compare(Unit o1, Unit o2) {
        int diff = evaluator.getModifiedWeight(o1) - evaluator.getModifiedWeight(o1);
        return diff != 0 ? diff : o1.getID().toString().compareTo(o2.getID().toString());
      }
    });

    while (sortedUnits.size() > 0) {
      // for every unit
      Unit u = sortedUnits.remove(sortedUnits.size() - 1);
      // find ship that best matches
      ShipStruct bestMatch = null;
      for (ShipStruct s : sortedShips) {
        if (s.ship.getRegion() == u.getRegion() && getSpace(s) >= getSpace(u)) {
          bestMatch = s;
          break;
        }
      }
      if (bestMatch != null) {
        // put unit on this ship
        sortedShips.remove(bestMatch);
        load(u, bestMatch);

      } else {
        error(u);
      }
      client.getDispatcher().fire(new UnitOrdersEvent(this, u), true);
    }
    if (errors > 0) {
      ShipLoaderPlugin.log.warn(errors + " errors");
    }
    fireEvents();
  }

  protected int getSpace(Unit u) {
    return evaluator.getModifiedWeight(u) + getSafetyPerPerson() * u.getModifiedPersons();
  }

  /**
   * Returns the capacity of the ship minus its modified load minus the safety margin.
   *
   * @param s1
   * @return
   */
  protected int getSpace(ShipStruct s1) {
    return s1.capacity;
  }

  /**
   * Fire order events for all units.
   */
  protected void fireEvents() {
    for (Unit u : units) {
      client.getDispatcher().fire(new UnitOrdersEvent(this, u));
    }
  }

  /**
   * Mark a unit with an error marker. Does <em>not</em> fire UnitOrderEvents.
   *
   * @param u
   */
  protected void error(Unit u) {
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
   * @param shipStruct
   */
  protected void load(Unit unit, ShipStruct shipStruct) {
    updateLocale(unit);

    // put unit on this ship
    int delta = getSpace(unit);
    unit.addOrder(enterToken + " " + shipToken + " " + shipStruct.ship.getID().toString() + " "
        + getComment(), true, 1);
    removeOrders(unit, leaveToken, true);
    shipStruct.capacity -= delta;
  }

  protected void updateLocale(Unit unit) {
    OrderChanger orderChanger;
    if (!unit.getFaction().getLocale().equals(locale)) {
      locale = unit.getFaction().getLocale();
      orderChanger = unit.getData().getGameSpecificStuff().getOrderChanger();
      enterToken = orderChanger.getOrderO(locale, EresseaConstants.OC_ENTER).getText();
      leaveToken = orderChanger.getOrderO(locale, EresseaConstants.OC_LEAVE).getText();
      shipToken = orderChanger.getOrderO(locale, EresseaConstants.OC_SHIP).getText();
      giveToken = orderChanger.getOrderO(locale, EresseaConstants.OC_GIVE).getText();
    }
  }

  /**
   * Add order to put unit onto ship.
   *
   * @param unit
   * @param ship
   */
  protected void unload(Unit unit, ShipStruct ship) {
    ship.capacity += getSpace(unit);
  }

  /**
   * Add orders to give amount of item from giver to getter. Does <em>not</em> fire UnitOrderEvents!
   *
   * @param giver
   * @param getter
   * @param amount
   * @param item
   */
  protected void give(Unit giver, Unit getter, int amount, ItemType item) {
    updateLocale(giver);
    giver.addOrder(giveToken + " " + getter.getID().toString() + " " + amount + " "
        + item.toString() + getComment(), false, 0);

    ShipStruct sc = getManagedShip(giver);
    if (sc != null) {
      sc.capacity += amount;
    }
    sc = getManagedShip(getter);
    if (sc != null) {
      sc.capacity -= amount;
    }
  }

  /**
   * Remove orders from unit that match orderStub. If contains==true, remove all orders that contain
   * it. Otherwise, remove all orders that start with it (not counting leading spaces).
   *
   * @param unit
   * @param orderStub
   * @param contains
   * @return <code>true</code> if orders were removed.
   */
  protected static boolean removeOrders(Unit unit, String orderStub, boolean contains) {
    Collection<String> newOrders = new ArrayList<String>();
    for (Order order : unit.getOrders2()) {
      String orderText = order.getText();
      if ((contains && !orderText.contains(orderStub))
          || (!contains && !orderText.trim().startsWith(orderStub))) {
        newOrders.add(orderText);
      }
    }
    if (newOrders.size() != unit.getOrders2().size()) {
      unit.setOrders(newOrders);
      // client.getDispatcher().fire(new UnitOrdersEvent(this, unit));
      return true;
    }
    return false;
  }

  /**
   * Return the safety margin for the specified unit.
   *
   * @param unit
   * @return
   */
  protected int getSafety(Unit unit) {
    int amount = 0;
    amount += unit.getModifiedPersons() * getSafetyPerPerson();
    return amount;
  }

  /**
   * Return the safety margin for the specified ship.
   *
   * @param s1
   * @return
   */
  protected int getSafety(Ship s1) {
    int amount = 0;
    amount += getSafety();
    for (Unit u : s1.modifiedUnits()) {
      amount += u.getModifiedPersons() * getSafetyPerPerson();
    }
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
    safetyPerPerson = safety;
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
  @Deprecated
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

  public void addListener(InclusionListener listener) {
    listeners.add(listener);
  }

  public void removeListener(InclusionListener listener) {
    listeners.remove(listener);
  }

  public Collection<?> getUnits() {
    return units;
  }

  public Collection<Ship> getShips() {
    Collection<Ship> shipList = new ArrayList<Ship>(ships.size());
    for (ShipStruct sc : ships.values()) {
      shipList.add(sc.ship);
    }
    return shipList;
  }

}
