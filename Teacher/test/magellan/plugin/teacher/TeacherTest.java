package magellan.plugin.teacher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;

import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.Unit;
import magellan.library.utils.logging.Logger;
import magellan.test.GameDataBuilder;
import magellan.test.MagellanTestWithResources;

import org.junit.BeforeClass;
import org.junit.Test;

public class TeacherTest extends MagellanTestWithResources {
	private static final Logger log = Logger.getInstance(Teacher.class);

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		setResourceDir("../Magellan2");
		initResources();
	}

	@Test
	public final void testGetGlobalWeight() throws Exception {

		GameDataBuilder builder = new GameDataBuilder();
		GameData gd = builder.createSimplestGameData(350, false);
		Region region = gd.getRegions().iterator().next();

		Collection<Unit> units2 = new ArrayList<Unit>();

		Unit u1;
		units2.add(u1 = builder.addUnit(gd, "U1", region));
		u1.setPersons(1);
		builder.addSkill(u1, "Hiebwaffen", 10);
		builder.addSkill(u1, "Ausdauer", 2);

		Unit u2;
		units2.add(u2 = builder.addUnit(gd, "U2", region));
		u2.setPersons(10);
		builder.addSkill(u2, "Hiebwaffen", 8);
		builder.addSkill(u2, "Ausdauer", 2);

		Unit u3;
		units2.add(u3 = builder.addUnit(gd, "U3", region));
		u3.setPersons(50);
		builder.addSkill(u3, "Hiebwaffen", 6);
		builder.addSkill(u3, "Ausdauer", 2);

		Unit u4;
		units2.add(u4 = builder.addUnit(gd, "U4", region));
		u4.setPersons(10);
		builder.addSkill(u4, "Ausdauer", 10);

		TeachPlugin tp = new TeachPlugin();

		for (Unit u : units2) {
			tp.init(gd);
			tp.addOrder(u, null, new Order(2));
			tp.addOrder(u, null, new Order("Hiebwaffen", 100, 19, 99));
			tp.addOrder(u, null, new Order("Ausdauer", 100, 6, 99));
		}
		tp.delOrder(u4, null, new Order("Hiebwaffen", 100, 6, 99));
		// tp.addOrder(u3, null, new Order("Ausdauer", 100, 6, 99));

		final Teacher t = new Teacher(units2, null, null);
		t.setMinDist(2);
		t.setQuality(1);

		boolean debug = false;
		if (debug) {
			t.setMinGlobalWeight(1);
			t.mainrun();
			Logger.setLevel(Logger.INFO);
			for (Unit u : units2) {
				log.info(u.getOrders2().get(u1.getOrders2().size() - 2));
				log.info(u.getOrders2().get(u1.getOrders2().size() - 1));
			}
			log.info("");
			Logger.setLevel(Logger.ERROR);
			assertEquals("LERNEN Hiebwaffen", u3.getOrders2().get(u3.getOrders2().size() - 1).toString());
		}

		t.setMinGlobalWeight(.5);
		t.mainrun();

		if (debug) {
			Logger.setLevel(Logger.INFO);
			for (Unit u : units2) {
				log.info(u.getOrders2().get(u1.getOrders2().size() - 2));
				log.info(u.getOrders2().get(u1.getOrders2().size() - 1));
			}
			log.info("");
			Logger.setLevel(Logger.ERROR);
		}

		assertTrue(t.getGlobalWeight(t.getSkillIndex("Hiebwaffen"), 6) < 1);
		assertTrue(t.getGlobalWeight(t.getSkillIndex("Hiebwaffen"), 8) < 1);
		assertTrue(t.getGlobalWeight(t.getSkillIndex("Hiebwaffen"), 6) < t.getGlobalWeight(
				t.getSkillIndex("Hiebwaffen"), 8));
		assertTrue(t.getGlobalWeight(t.getSkillIndex("Hiebwaffen"), 10) > 1);

	}

	@Test
	public final void testGetGlobalWeight2() throws Exception {

		GameDataBuilder builder = new GameDataBuilder();
		GameData gd = builder.createSimplestGameData(350, false);
		Region region = gd.getRegions().iterator().next();

		Collection<Unit> units2 = new ArrayList<Unit>();

		Unit u1;
		units2.add(u1 = builder.addUnit(gd, "U1", region));
		u1.setPersons(1);
		builder.addSkill(u1, "Hiebwaffen", 9);
		builder.addSkill(u1, "Ausdauer", 2);

		Unit u2;
		units2.add(u2 = builder.addUnit(gd, "U2", region));
		u2.setPersons(10);
		builder.addSkill(u2, "Hiebwaffen", 8);
		builder.addSkill(u2, "Ausdauer", 2);

		TeachPlugin tp = new TeachPlugin();

		for (Unit u : units2) {
			tp.init(gd);
			tp.addOrder(u, null, new Order(2));
			tp.addOrder(u, null, new Order("Hiebwaffen", 100, 16, 99));
			tp.addOrder(u, null, new Order("Ausdauer", 100, 4, 99));
		}

		final Teacher t = new Teacher(units2, null, null);
		t.setMinDist(2);
		t.setQuality(1);

		t.setMinGlobalWeight(1);
		t.mainrun();
		Logger.setLevel(Logger.INFO);
		for (Unit u : units2) {
			log.info(u.getOrders2().get(u1.getOrders2().size() - 2));
			log.info(u.getOrders2().get(u1.getOrders2().size() - 1));
		}
		log.info("");
		Logger.setLevel(Logger.ERROR);
		assertEquals("LERNEN Hiebwaffen", u2.getOrders2().get(u2.getOrders2().size() - 1).toString());

		t.setMinGlobalWeight(.5);
		t.mainrun();

		Logger.setLevel(Logger.INFO);
		for (Unit u : units2) {
			log.info(u.getOrders2().get(u1.getOrders2().size() - 2));
			log.info(u.getOrders2().get(u1.getOrders2().size() - 1));
		}
		log.info("");
		Logger.setLevel(Logger.ERROR);

		// this might fail every on rare occasions, because of randomization
		assertEquals("LERNEN Ausdauer", u2.getOrders2().get(u2.getOrders2().size() - 1).toString());

	}
}
