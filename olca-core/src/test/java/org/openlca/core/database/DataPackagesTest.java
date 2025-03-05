package org.openlca.core.database;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openlca.core.Tests;

public class DataPackagesTest {

	@Test
	public void testDatabaseDataPackages() {
		var db = Tests.getDb();

		// add libraries
		for (int i = 1; i < 4; i++) {
			db.addLibrary("lib " + i);
		}
		var dataPackages = db.getDataPackages();
		for (int i = 1; i < 4; i++) {
			assertTrue(dataPackages.contains("lib " + i));
		}

		// remove a library
		db.removeDataPackage("lib 3");
		assertFalse(db.getDataPackages().contains("lib 3"));

		// delete all libraries
		for (int i = 1; i < 4; i++) {
			db.removeDataPackage("lib " + i);
		}
		assertTrue(db.getDataPackages().isEmpty());

	}
}
