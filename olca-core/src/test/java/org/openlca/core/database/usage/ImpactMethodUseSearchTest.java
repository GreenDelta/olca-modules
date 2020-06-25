package org.openlca.core.database.usage;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.database.ProjectDao;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Project;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;

public class ImpactMethodUseSearchTest {

	private IDatabase db = Tests.getDb();
	private IUseSearch<ImpactMethodDescriptor> search;
	private ProjectDao projectDao;
	private Project project;
	private ImpactMethodDao impactDao;
	private ImpactMethod method;

	@Before
	public void setUp() {
		search = IUseSearch.FACTORY.createFor(ModelType.IMPACT_METHOD, db);
		projectDao = new ProjectDao(db);
		project = new Project();
		project.name = "test project";
		project = projectDao.insert(project);
		impactDao = new ImpactMethodDao(db);
		method = new ImpactMethod();
		method.name = "test method";
		method = impactDao.insert(method);
	}

	@After
	public void tearDown() {
		projectDao.delete(project);
		impactDao.delete(method);
	}

	@Test
	public void testNoUsage() {
		ImpactMethodDescriptor d = Descriptor.of(method);
		List<CategorizedDescriptor> descriptors = search.findUses(d);
		Assert.assertNotNull(descriptors);
		Assert.assertTrue(descriptors.isEmpty());
	}

	@Test
	public void testFindInProject() {
		project.impactMethodId = method.id;
		project = projectDao.update(project);
		ImpactMethodDescriptor d = Descriptor.of(method);
		List<CategorizedDescriptor> descriptors = search.findUses(d);
		Assert.assertEquals(1, descriptors.size());
		Assert.assertEquals(Descriptor.of(project),
				descriptors.get(0));
	}

}
