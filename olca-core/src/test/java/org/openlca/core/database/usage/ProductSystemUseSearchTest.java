package org.openlca.core.database.usage;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Project;
import org.openlca.core.model.ProjectVariant;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.Descriptors;
import org.openlca.core.model.descriptors.ProductSystemDescriptor;

public class ProductSystemUseSearchTest {

	private IDatabase database = Tests.getDb();
	private IUseSearch<ProductSystemDescriptor> search;
	private ProductSystem system;

	@Before
	public void setup() {
		system = new ProductSystem();
		system.setName("process");
		system = database.createDao(ProductSystem.class).insert(system);
		search = IUseSearch.FACTORY.createFor(ModelType.PRODUCT_SYSTEM,
				database);
	}

	@After
	public void tearDown() {
		database.createDao(ProductSystem.class).delete(system);
	}

	@Test
	public void testFindNoUsage() {
		List<CategorizedDescriptor> models = search.findUses(Descriptors
				.toDescriptor(system));
		Assert.assertNotNull(models);
		Assert.assertTrue(models.isEmpty());
	}

	@Test
	public void testFindInProjects() {
		Project project = createProject();
		List<CategorizedDescriptor> results = search.findUses(Descriptors
				.toDescriptor(system));
		database.createDao(Project.class).delete(project);
		BaseDescriptor expected = Descriptors.toDescriptor(project);
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(expected, results.get(0));
	}

	private Project createProject() {
		Project project = new Project();
		project.setName("project");
		ProjectVariant variant = new ProjectVariant();
		variant.setProductSystem(system);
		project.getVariants().add(variant);
		return database.createDao(Project.class).insert(project);
	}

}
