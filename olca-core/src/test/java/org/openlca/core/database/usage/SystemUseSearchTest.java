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
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.Descriptors;
import org.openlca.core.model.descriptors.ProductSystemDescriptor;

public class SystemUseSearchTest {

	private IDatabase db = Tests.getDb();
	private IUseSearch<ProductSystemDescriptor> search;
	private ProjectDao projectDao;
	private Project project;
	private ProductSystemDao systemDao;
	private ProductSystem system;

	@Before
	public void setUp() {
		search = IUseSearch.FACTORY.createFor(ModelType.PRODUCT_SYSTEM, db);
		projectDao = new ProjectDao(db);
		project = new Project();
		project.setName("test project");
		project = projectDao.insert(project);
		systemDao = new ProductSystemDao(db);
		system = new ProductSystem();
		system.setName("test system");
		system = systemDao.insert(system);
	}

	@After
	public void tearDown() {
		projectDao.delete(project);
		systemDao.delete(system);
	}

	@Test
	public void testNoUsage() {
		ProductSystemDescriptor d = Descriptors.toDescriptor(system);
		List<CategorizedDescriptor> descriptors = search.findUses(d);
		Assert.assertNotNull(descriptors);
		Assert.assertTrue(descriptors.isEmpty());
	}

	@Test
	public void testFindInProject() {
		ProjectVariant variant = new ProjectVariant();
		variant.setProductSystem(system);
		project.getVariants().add(variant);
		project = projectDao.update(project);
		ProductSystemDescriptor d = Descriptors.toDescriptor(system);
		List<CategorizedDescriptor> descriptors = search.findUses(d);
		Assert.assertEquals(1, descriptors.size());
		Assert.assertEquals(Descriptors.toDescriptor(project),
				descriptors.get(0));
	}

}
