package org.openlca.core.database.usage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProjectDao;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Project;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.CategoryDescriptor;
import org.openlca.core.model.descriptors.Descriptor;

public class CategoryUseSearchTest {

	private IDatabase db = Tests.getDb();
	private IUseSearch<CategoryDescriptor> search;

	@Before
	public void setup() {
		search = IUseSearch.FACTORY.createFor(ModelType.CATEGORY, db);
	}

	@Test
	public void testFindNoUsage() {
		Category category = createCategory();
		List<CategorizedDescriptor> models = search.findUses(Descriptor
				.of(category));
		assertNotNull(models);
		assertTrue(models.isEmpty());
		new CategoryDao(db).delete(category);
	}

	@Test
	public void testFindInModel() {
		Category category = createCategory();
		Project project = new Project();
		project.name = "project";
		project.category = category;
		new ProjectDao(db).insert(project);
		List<CategorizedDescriptor> results = search.findUses(Descriptor
				.of(category));
		new ProjectDao(db).delete(project);
		new CategoryDao(db).delete(category);
		Descriptor expected = Descriptor.of(project);
		assertEquals(1, results.size());
		assertEquals(expected, results.get(0));
	}

	private Category createCategory() {
		var category = new Category();
		category.name = "category";
		category.modelType = ModelType.PROCESS;
		return new CategoryDao(db).insert(category);
	}

	@Test
	public void testFindInCategory() {
		var dao= new CategoryDao(db);
		var category = createCategory();
		var parent = createCategory();
		parent.category = category;
		dao.update(parent);
		var results = search.findUses(Descriptor.of(category));
		dao.delete(category);
		dao.delete(parent);
		assertEquals(1, results.size());
		assertEquals(Descriptor.of(parent), results.get(0));
	}
}
