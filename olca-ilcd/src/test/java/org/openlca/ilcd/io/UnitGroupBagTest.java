package org.openlca.ilcd.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openlca.ilcd.commons.Category;
import org.openlca.ilcd.units.Unit;
import org.openlca.ilcd.units.UnitGroup;
import org.openlca.ilcd.util.UnitGroupBag;
import org.openlca.ilcd.util.UnitGroups;

public class UnitGroupBagTest {

	private UnitGroupBag bag;
	private UnitGroup ug;

	@Before
	public void setUp() throws Exception {
		try (InputStream stream = this.getClass().getResourceAsStream(
				"unit.xml")) {
			XmlBinder binder = new XmlBinder();
			ug = binder.fromStream(UnitGroup.class, stream);
			this.bag = new UnitGroupBag(ug, "en");
		}
	}

	@Test
	public void testGetReferenceUnitId() {
		assertEquals(Integer.valueOf(0), bag.getReferenceUnitId());
	}

	@Test
	public void testGetUnits() {
		List<Unit> units = UnitGroups.getUnits(ug);
		assertTrue(units.size() == 4);
		assertEquals("kg*a", units.get(0).name);
	}

	@Test
	public void testGetId() {
		assertEquals("59f191d6-5dd3-4553-af88-1a32accfe308", bag.getId());
	}

	@Test
	public void testGetName() {
		assertEquals("Units of mass*time", bag.getName());
	}

	@Test
	public void testGetComment() {
		assertEquals(
				"Reference Unit Group Data Set of the International Reference "
						+ "Life Cycle Data System (ILCD).",
				bag.getComment().replace("\n", "").replace("\t", " ")
						.replace("    ", " ").trim());
	}

	@Test
	public void testGetClasses() {
		Category clazz = bag.getSortedClasses().get(0);
		assertEquals(0, clazz.level);
		assertEquals("Technical unit groups", clazz.value.trim());
	}

}
