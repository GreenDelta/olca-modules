package org.openlca.geo.io;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.LocationDao;
import org.openlca.core.database.derby.DerbyDatabase;

import com.google.common.io.Resources;

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
		String kml = IOUtils.toString(Resources
				.getResource("org/openlca/geo/US_counties-clipped.shp.kml"),
				"utf-8");
		MultiKmlImport parser = new MultiKmlImport(database, kml);
		parser.parseAndInsert();
		Assert.assertEquals(697, dao.getAll().size());
	}

}
