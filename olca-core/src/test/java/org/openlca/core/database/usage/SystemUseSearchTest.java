package org.openlca.core.database.usage;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Project;
import org.openlca.core.model.ProjectVariant;

public class SystemUseSearchTest {

	private final IDatabase db = Tests.getDb();
	private Project project;
	private ProductSystem system;

	@Before
	public void setUp() {
		project = db.insert(Project.of("test project"));
		system = new ProductSystem();
		system.name = "test system";
		system = db.insert(system);
	}

	@After
	public void tearDown() {
		db.delete(project, system);
	}

	@Test
	public void testNoUsage() {
		UsageTests.expectEmpty(system);
	}

	@Test
	public void testFindInProject() {
		var variant = new ProjectVariant();
		variant.productSystem = system;
		project.variants.add(variant);
		project = db.update(project);
		UsageTests.expectOne(system, project);
	}

}
