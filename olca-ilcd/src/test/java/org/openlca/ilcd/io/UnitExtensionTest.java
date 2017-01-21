package org.openlca.ilcd.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXB;

import org.junit.Test;
import org.openlca.ilcd.units.Unit;
import org.openlca.ilcd.util.UnitExtension;

public class UnitExtensionTest {

	@Test
	public void testEmpty() {
		Unit unit = io(createUnit());
		UnitExtension extension = new UnitExtension(unit);
		assertFalse(extension.isValid());
		assertNull(extension.getUnitId());
	}

	@Test
	public void testExtension() {
		Unit unit = createUnit();
		UnitExtension extension = new UnitExtension(unit);
		extension.setUnitId("00-12-4-5");
		Unit alias = io(unit);
		assertTrue(unit != alias);
		extension = new UnitExtension(alias);
		assertTrue(extension.isValid());
		assertEquals("00-12-4-5", extension.getUnitId());
	}

	private Unit createUnit() {
		Unit unit = new Unit();
		unit.id = 1;
		unit.factor = 42d;
		unit.name = "kg";
		return unit;
	}

	private Unit io(Unit unit) {
		StringWriter writer = new StringWriter();
		JAXB.marshal(unit, writer);
		writer.flush();
		String xml = writer.toString();
		StringReader reader = new StringReader(xml);
		return JAXB.unmarshal(reader, Unit.class);
	}

}
