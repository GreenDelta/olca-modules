package org.openlca.geo.io;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.LocationDao;
import org.openlca.core.database.derby.DerbyDatabase;

import com.google.common.io.Resources;

public class MultiKmlImportTest {

	@Test
	public void testImport() throws Exception {
		IDatabase database = DerbyDatabase.createInMemory();
		LocationDao dao = new LocationDao(database);
		Assert.assertEquals(0, dao.getAll().size());
		String kml = IOUtils.toString(Resources.getResource(
				"org/openlca/geo/US_counties-clipped.shp.kml"),
				"utf-8");
		MultiKmlImport parser = new MultiKmlImport(database, kml);
		parser.parseAndInsert();
		Assert.assertEquals(703, dao.getAll().size());
	}

}
