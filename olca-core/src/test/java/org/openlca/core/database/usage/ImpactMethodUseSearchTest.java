package org.openlca.core.database.usage;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Project;
import org.openlca.core.model.descriptors.Descriptor;

public class ImpactMethodUseSearchTest {

	private final IDatabase db = Tests.getDb();
	private final UsageSearch search = UsageSearch.of(ModelType.IMPACT_METHOD, db);
	private Project project;
	private ImpactMethod method;

	@Before
	public void setUp() {
		project = db.insert(Project.of("test project"));
		method = db.insert(ImpactMethod.of("test method"));
	}

	@After
	public void tearDown() {
		db.delete(project, method);
	}

	@Test
	public void testNoUsage() {
		var descriptors = search.find(method.id);
		Assert.assertNotNull(descriptors);
		Assert.assertTrue(descriptors.isEmpty());
	}

	@Test
	public void testFindInProject() {
		project.impactMethod = method;
		project = db.update(project);
		var descriptors = search.find(method.id);
		Assert.assertEquals(1, descriptors.size());
		Assert.assertEquals(Descriptor.of(project), descriptors.iterator().next());
	}

}
