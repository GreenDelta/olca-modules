package org.openlca.core.database.usage;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.database.ProjectDao;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Project;
import org.openlca.core.model.ProjectVariant;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.ProductSystemDescriptor;

public class ProductSystemUseSearchTest {

	private IDatabase database = Tests.getDb();
	private IUseSearch<ProductSystemDescriptor> search;
	private ProductSystem system;

	@Before
	public void setup() {
		system = new ProductSystem();
		system.name = "process";
		system = new ProductSystemDao(database).insert(system);
		search = IUseSearch.FACTORY.createFor(ModelType.PRODUCT_SYSTEM,
				database);
	}

	@After
	public void tearDown() {
		new ProductSystemDao(database).delete(system);
	}

	@Test
	public void testFindNoUsage() {
		List<CategorizedDescriptor> models = search.findUses(Descriptor
				.of(system));
		Assert.assertNotNull(models);
		Assert.assertTrue(models.isEmpty());
	}

	@Test
	public void testFindInProjects() {
		Project project = createProject();
		List<CategorizedDescriptor> results = search.findUses(Descriptor
				.of(system));
		new ProjectDao(database).delete(project);
		Descriptor expected = Descriptor.of(project);
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(expected, results.get(0));
	}

	private Project createProject() {
		Project project = new Project();
		project.name = "project";
		ProjectVariant variant = new ProjectVariant();
		variant.productSystem = system;
		project.variants.add(variant);
		return new ProjectDao(database).insert(project);
	}

}
