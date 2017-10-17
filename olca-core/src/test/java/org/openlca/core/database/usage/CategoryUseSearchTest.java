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
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.CategoryDescriptor;
import org.openlca.core.model.descriptors.Descriptors;

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
		List<CategorizedDescriptor> models = search.findUses(Descriptors
				.toDescriptor(category));
		Assert.assertNotNull(models);
		Assert.assertTrue(models.isEmpty());
		new CategoryDao(database).delete(category);
	}

	@Test
	public void testFindInModel() {
		Category category = createCategory();
		Project project = new Project();
		project.setName("project");
		project.setCategory(category);
		new ProjectDao(database).insert(project);
		List<CategorizedDescriptor> results = search.findUses(Descriptors
				.toDescriptor(category));
		new ProjectDao(database).delete(project);
		new CategoryDao(database).delete(category);
		BaseDescriptor expected = Descriptors.toDescriptor(project);
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(expected, results.get(0));
	}

	private Category createCategory() {
		Category category = new Category();
		category.setName("category");
		category.setModelType(ModelType.PROCESS);
		return new CategoryDao(database).insert(category);
	}

	@Test
	public void testFindInCategory() {
		Category category = createCategory();
		Category parent = createCategory();
		parent.setCategory(category);
		new CategoryDao(database).update(parent);
		List<CategorizedDescriptor> results = search.findUses(Descriptors
				.toDescriptor(category));
		new CategoryDao(database).delete(category);
		new CategoryDao(database).delete(parent);
		BaseDescriptor expected = Descriptors.toDescriptor(parent);
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(expected, results.get(0));
	}
}
