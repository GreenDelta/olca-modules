package org.openlca.util;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Version;

public class CategoryContentTestTest {

	private final IDatabase db = Tests.getDb();
	private Category category;

	@Before
	public void setup() {
		category = CategoryDao.sync(
			db, ModelType.ACTOR, "some", "deep", "category");
	}

	@After
	public void cleanup() {
		var c = category;
		while (c != null) {
			db.delete(c);
			c = c.category;
		}
	}

	@Test
	public void testLibContent() {
		var actor = Actor.of("abc");
		actor.category = category;
		var lib = "lib 1";
		db.addLibrary(lib);
		actor.dataPackage = lib;
		db.insert(actor);

		var test = new CategoryContentTest(db);
		var c = category;
		while (c != null) {
			assertTrue(test.hasDataPackageContent(c));
			assertTrue(test.hasDataPackageContent(c, lib));
			assertFalse(test.hasDataPackageContent(c, "lib 2"));
			assertTrue(test.hasLibraryContent(c));
			assertTrue(test.hasLibraryContent(c, lib));
			assertFalse(test.hasLibraryContent(c, "lib 2"));
			c = c.category;
		}

		db.delete(actor);
		db.removeDataPackage(lib);
	}

	@Test
	public void testNonLibContent() {
		var actor = Actor.of("abc");
		actor.category = category;
		actor.dataPackage = "pack 1";
		db.insert(actor);
		db.addDataPackage("pack 1", Version.of(1, 1, 1));
		
		var test = new CategoryContentTest(db);
		var c = category;
		while (c != null) {
			assertTrue(test.hasDataPackageContent(c));
			assertTrue(test.hasDataPackageContent(c, "pack 1"));
			assertTrue(test.hasNonLibraryContent(c));
			assertTrue(test.hasNonLibraryContent(c));
			assertFalse(test.hasLibraryContent(c));
			assertFalse(test.hasLibraryContent(c, "pack 1"));
			c = c.category;
		}

		db.delete(actor);
	}

	@Test
	public void testNonDataPackageContent() {
		var actor = Actor.of("abc");
		actor.category = category;
		db.insert(actor);

		var test = new CategoryContentTest(db);
		var c = category;
		while (c != null) {
			assertTrue(test.hasNonDataPackageContent(c));
			assertFalse(test.hasDataPackageContent(c));
			assertFalse(test.hasDataPackageContent(c, "lib 1"));
			assertTrue(test.hasNonLibraryContent(c));
			assertFalse(test.hasLibraryContent(c));
			assertFalse(test.hasLibraryContent(c, "lib 1"));
			c = c.category;
		}

		db.delete(actor);
	}


}
