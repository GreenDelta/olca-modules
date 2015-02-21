package org.openlca.io;

import java.io.File;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.LocationDao;
import org.openlca.core.database.NativeSql;
import org.openlca.core.database.derby.DerbyDatabase;
import org.openlca.core.database.upgrades.Upgrades;

import com.google.common.io.Resources;

public class MultiKmlImportTest {

	private IDatabase getDatabase() {
		String tmpDirPath = System.getProperty("java.io.tmpdir");
		String dbName = "olca_test_db_1.4";
		File tmpDir = new File(tmpDirPath);
		File folder = new File(tmpDir, dbName);
		IDatabase derbyDatabase = new DerbyDatabase(folder);
		try {
			// (currently) it should be always possible to run the database
			// updates on databases that were already updated as the
			// updated should check if an update is necessary or not. Thus
			// we reset the version here and test if the updates work.
			String versionReset = "update openlca_version set version = 1";
			NativeSql.on(derbyDatabase).runUpdate(versionReset);
			Upgrades.runUpgrades(derbyDatabase);
		} catch (Exception e) {
			throw new RuntimeException("DB-upgrades failed", e);
		}
		return derbyDatabase;
	}

	@Test
	public void testImport() throws Exception {
		IDatabase database = getDatabase();
		LocationDao dao = new LocationDao(database);
		Assert.assertEquals(0, dao.getAll().size());
		String kml = IOUtils.toString(Resources
				.getResource("org/openlca/geo/US_counties-clipped.shp.kml"),
				"utf-8");
		MultiKmlImport parser = new MultiKmlImport(database, kml);
		parser.parseAndInsert();
		Assert.assertEquals(703, dao.getAll().size());
	}

}
