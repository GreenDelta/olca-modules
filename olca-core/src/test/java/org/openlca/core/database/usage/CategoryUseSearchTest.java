package org.openlca.core.database.usage;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProjectDao;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Project;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.CategoryDescriptor;

public class CategoryUseSearchTest {

	private IDatabase database = Tests.getDb();
	private IUseSearch<CategoryDescriptor> search;

	@Before
	public void setup() {
		search = IUseSearch.FACTORY.createFor(ModelType.CATEGORY, database);
	}

	@Test
	public void testFindNoUsage() {
		Category category = createCategory();
		List<CategorizedDescriptor> models = search.findUses(Descriptor
				.of(category));
		Assert.assertNotNull(models);
		Assert.assertTrue(models.isEmpty());
		new CategoryDao(database).delete(category);
	}

	@Test
	public void testFindInModel() {
		Category category = createCategory();
		Project project = new Project();
		project.name = "project";
		project.category = category;
		new ProjectDao(database).insert(project);
		List<CategorizedDescriptor> results = search.findUses(Descriptor
				.of(category));
		new ProjectDao(database).delete(project);
		new CategoryDao(database).delete(category);
		Descriptor expected = Descriptor.of(project);
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(expected, results.get(0));
	}

	private Category createCategory() {
		Category category = new Category();
		category.name = "category";
		category.modelType = ModelType.PROCESS;
		return new CategoryDao(database).insert(category);
	}

	@Test
	public void testFindInCategory() {
		Category category = createCategory();
		Category parent = createCategory();
		parent.category = category;
		new CategoryDao(database).update(parent);
		List<CategorizedDescriptor> results = search.findUses(Descriptor
				.of(category));
		new CategoryDao(database).delete(category);
		new CategoryDao(database).delete(parent);
		Descriptor expected = Descriptor.of(parent);
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(expected, results.get(0));
	}
}
