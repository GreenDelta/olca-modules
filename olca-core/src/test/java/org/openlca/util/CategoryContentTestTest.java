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
		actor.library = "lib 1";
		db.insert(actor);

		var test = new CategoryContentTest(db);
		var c = category;
		while (c != null) {
			assertTrue(test.hasLibraryContent(c));
			assertTrue(test.hasLibraryContent(c, "lib 1"));
			assertFalse(test.hasLibraryContent(c, "lib 2"));
			c = c.category;
		}

		db.delete(actor);
	}

	@Test
	public void testNonLibContent() {
		var actor = Actor.of("abc");
		actor.category = category;
		db.insert(actor);

		var test = new CategoryContentTest(db);
		var c = category;
		while (c != null) {
			assertTrue(test.hasNonLibraryContent(c));
			assertFalse(test.hasLibraryContent(c));
			assertFalse(test.hasLibraryContent(c, "lib 1"));
			c = c.category;
		}

		db.delete(actor);
	}


}
