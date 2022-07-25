package org.openlca.core.database.descriptors;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.LocationDao;
import org.openlca.core.model.Location;

public class LocationDescriptorTest {

	@Test
	public void test() {

		var loc = new Location();
		loc.name = "LOC";
		loc.refId = "LOC";
		loc.code = "LOC";

		var dao = new LocationDao(Tests.getDb());
		loc = dao.insert(loc);
		var d = dao.descriptorMap().get(loc.id);
		dao.delete(loc);

		assertEquals(loc.name, d.name);
		assertEquals(loc.refId, d.refId);
		assertEquals(loc.code, d.code);
	}
}
