package org.openlca.geo.io;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.LocationDao;
import org.openlca.core.database.derby.DerbyDatabase;
import org.openlca.geo.Tests;

public class MultiKmlImportTest {

	private IDatabase database;
	private LocationDao dao;

	@Before
	public void setup() {
		database = DerbyDatabase.createInMemory();
		dao = new LocationDao(database);
		dao.deleteAll();
	}

	@Test
	public void testImport() throws Exception {
		Assert.assertEquals(0, dao.getAll().size());
		String kml = Tests.getKml("US_counties-clipped.shp.kml");
		MultiKmlImport parser = new MultiKmlImport(database, kml);
		parser.parseAndInsert();
		Assert.assertEquals(5, dao.getAll().size());
	}

}
