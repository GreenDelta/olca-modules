package org.openlca.core.database.descriptors;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.LocationDao;
import org.openlca.core.model.Location;
import org.openlca.core.model.descriptors.LocationDescriptor;

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

		Assert.assertEquals(loc.name, d.name);
		Assert.assertEquals(loc.refId, d.refId);
		Assert.assertEquals(loc.code, d.code);
	}
}
