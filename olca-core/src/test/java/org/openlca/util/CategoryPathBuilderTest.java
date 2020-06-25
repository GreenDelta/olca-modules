package org.openlca.util;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;

public class CategoryPathBuilderTest {

	@Test
	public void testPath() {
		var root = Category.of("Elementary flows", ModelType.FLOW);
		var top = Category.childOf(root, "air");
		var sub = Category.childOf(top, "high population density");
		new CategoryDao(Tests.getDb()).insert(root);

		var paths = new CategoryPathBuilder(Tests.getDb());
		assertEquals("Elementary flows",
				paths.build(root.id));
		assertEquals("Elementary flows/air",
				paths.build(top.id));
		assertEquals("Elementary flows/air/high population density",
				paths.build(sub.id));
	}

}
