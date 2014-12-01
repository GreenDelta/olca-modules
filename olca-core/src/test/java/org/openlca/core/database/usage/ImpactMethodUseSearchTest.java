package org.openlca.core.database.usage;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.TestSession;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.database.ProjectDao;
import org.openlca.core.database.usage.IUseSearch;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Project;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.Descriptors;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;

public class ImpactMethodUseSearchTest {

	private IDatabase database = TestSession.getDefaultDatabase();
	private IUseSearch<ImpactMethodDescriptor> search;
	private ProjectDao projectDao;
	private Project project;
	private ImpactMethod method;

	@Before
	public void setUp() {
		this.search = IUseSearch.FACTORY.createFor(ModelType.IMPACT_METHOD,
				database);
		projectDao = new ProjectDao(database);
		Project project = new Project();
		project.setName("test project");
		this.project = projectDao.insert(project);
		ImpactMethodDao impactDao = new ImpactMethodDao(database);
		ImpactMethod method = new ImpactMethod();
		method.setName("test method");
		this.method = impactDao.insert(method);
	}

	@After
	public void tearDown() {
		projectDao.delete(project);
		ImpactMethodDao dao = new ImpactMethodDao(database);
		dao.delete(method);
	}

	@Test
	public void testNoUsage() {
		List<BaseDescriptor> descriptors = search.findUses(Descriptors
				.toDescriptor(method));
		Assert.assertNotNull(descriptors);
		Assert.assertTrue(descriptors.isEmpty());
	}

	@Test
	public void testFindInProjects() {
		project.setImpactMethodId(method.getId());
		this.project = projectDao.update(project);
		List<BaseDescriptor> descriptors = search.findUses(Descriptors
				.toDescriptor(method));
		Assert.assertEquals(1, descriptors.size());
		Assert.assertEquals(Descriptors.toDescriptor(project),
				descriptors.get(0));
	}

}
