package org.openlca.core.database;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openlca.core.Tests;

public class LibraryTest {

	@Test
	public void testDatabaseLibraries() {
		var db = Tests.getDb();

		// add libraries
		for (int i = 1; i < 4; i++) {
			db.addLibrary("lib " + i);
		}
		var libs = db.getLibraries();
		for (int i = 1; i < 4; i++) {
			assertTrue(libs.contains("lib " + i));
		}

		// remove a library
		assertTrue(db.getLibraries().contains("lib 3"));
		db.removeLibrary("lib 3");
		assertFalse(db.getLibraries().contains("lib 3"));

		// delete all libraries
		for (int i = 1; i < 4; i++) {
			db.removeLibrary("lib " + i);
		}
		assertTrue(db.getLibraries().isEmpty());

	}
}
