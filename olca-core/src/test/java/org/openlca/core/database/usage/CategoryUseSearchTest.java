package org.openlca.core.database.usage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Project;
import org.openlca.core.model.descriptors.Descriptor;

public class CategoryUseSearchTest {

	private final IDatabase db = Tests.getDb();
	private final UsageSearch search = UsageSearch.of(ModelType.CATEGORY, db);

	@Test
	public void testFindNoUsage() {
		var category = db.insert(Category.of("c", ModelType.ACTOR));
		var models = search.find(category.id);
		assertNotNull(models);
		assertTrue(models.isEmpty());
		db.delete(category);
	}

	@Test
	public void testFindInModel() {
		var category = db.insert(Category.of("c", ModelType.PROJECT));
		var project = new Project();
		project.name = "project";
		project.category = category;
		db.insert(project);
		var results = search.find(category.id);
		db.delete(project, category);
		var expected = Descriptor.of(project);
		assertEquals(1, results.size());
		assertEquals(expected, results.iterator().next());
	}

	@Test
	public void testFindInCategory() {
		var category = db.insert(Category.of("c1", ModelType.PROJECT));
		var parent = db.insert(Category.of("c0", ModelType.PROJECT));
		parent.category = category;
		db.update(parent);
		var results = search.find(category.id);
		db.delete(parent, category);
		assertEquals(1, results.size());
		assertEquals(Descriptor.of(parent), results.iterator().next());
	}

}
