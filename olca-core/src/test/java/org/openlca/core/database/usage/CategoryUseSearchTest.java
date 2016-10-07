package org.openlca.core.database.usage;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
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
		database.createDao(Category.class).delete(category);
	}

	@Test
	public void testFindInModel() {
		Category category = createCategory();
		Project project = new Project();
		project.setName("project");
		project.setCategory(category);
		database.createDao(Project.class).insert(project);
		List<CategorizedDescriptor> results = search.findUses(Descriptors
				.toDescriptor(category));
		database.createDao(Project.class).delete(project);
		database.createDao(Category.class).delete(category);
		BaseDescriptor expected = Descriptors.toDescriptor(project);
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(expected, results.get(0));
	}

	private Category createCategory() {
		Category category = new Category();
		category.setName("category");
		return database.createDao(Category.class).insert(category);
	}

	@Test
	public void testFindInCategory() {
		Category category = createCategory();
		Category parent = createCategory();
		parent.setCategory(category);
		database.createDao(Category.class).update(parent);
		List<CategorizedDescriptor> results = search.findUses(Descriptors
				.toDescriptor(category));
		database.createDao(Category.class).delete(category);
		database.createDao(Category.class).delete(parent);
		BaseDescriptor expected = Descriptors.toDescriptor(parent);
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(expected, results.get(0));
	}
}
