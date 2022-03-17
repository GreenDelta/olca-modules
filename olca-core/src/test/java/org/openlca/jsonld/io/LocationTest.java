package org.openlca.jsonld.io;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.LocationDao;
import org.openlca.core.model.Location;
import org.openlca.jsonld.AbstractZipTest;
import org.openlca.jsonld.input.JsonImport;
import org.openlca.jsonld.output.JsonExport;

public class LocationTest extends AbstractZipTest {

	@Test
	public void testLocation() {
		LocationDao dao = new LocationDao(Tests.getDb());
		Location location = createModel(dao);
		doExport(location, dao);
		doImport(dao, location);
		dao.delete(location);
	}

	private Location createModel(LocationDao dao) {
		Location location = new Location();
		location.name = "location";
		location.refId = UUID.randomUUID().toString();
		dao.insert(location);
		return location;
	}

	private void doExport(Location location, LocationDao dao) {
		with(zip -> {
			JsonExport export = new JsonExport(Tests.getDb(), zip);
			export.write(location);
		});
		dao.delete(location);
		Assert.assertFalse(dao.contains(location.refId));
	}

	private void doImport(LocationDao dao, Location location) {
		with(zip -> {
			JsonImport jImport = new JsonImport(zip, Tests.getDb());
			jImport.run();
		});
		Assert.assertTrue(dao.contains(location.refId));
		Location clone = dao.getForRefId(location.refId);
		Assert.assertEquals(location.name, clone.name);
	}
}
