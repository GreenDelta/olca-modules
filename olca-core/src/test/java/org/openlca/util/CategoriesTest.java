package org.openlca.util;

import java.util.List;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;

public class CategoriesTest {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testPaths() {
		var root = Category.of("Elementary flows", ModelType.FLOW);
		var top = Category.childOf(root, "air");
		var sub = Category.childOf(top, "high population density");
		new CategoryDao(db).insert(root);

		var categories = Categories.pathsOf(db);

		// test string paths
		assertNull(categories.pathOf(null));
		assertNull(categories.pathOf(-999L));
		assertEquals("Elementary flows",
				categories.pathOf(root.id));
		assertEquals("Elementary flows/air",
				categories.pathOf(top.id));
		assertEquals("Elementary flows/air/high population density",
				categories.pathOf(sub.id));

		// test lists
		assertTrue(categories.listOf(null).isEmpty());
		assertTrue(categories.listOf(-999L).isEmpty());
		assertEquals(List.of("Elementary flows"),
			categories.listOf(root.id));
		assertEquals(List.of("Elementary flows", "air"),
			categories.listOf(top.id));
		assertEquals(List.of(
			"Elementary flows", "air", "high population density"),
			categories.listOf(sub.id));

		// clean up
		db.delete(sub, top, root);
	}

}
