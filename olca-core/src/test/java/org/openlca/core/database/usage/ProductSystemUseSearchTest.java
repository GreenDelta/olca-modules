package org.openlca.core.database.usage;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Project;
import org.openlca.core.model.ProjectVariant;

public class ProductSystemUseSearchTest {

	private final IDatabase db = Tests.getDb();
	private ProductSystem system;

	@Before
	public void setup() {
		system = new ProductSystem();
		system.name = "system";
		system = db.insert(system);
	}

	@After
	public void tearDown() {
		db.delete(system);
	}

	@Test
	public void testFindNoUsage() {
		UsageTests.expectEmpty(system);
	}

	@Test
	public void testFindInProjects() {
		var project = new Project();
		project.name = "project";
		ProjectVariant variant = new ProjectVariant();
		variant.productSystem = system;
		project.variants.add(variant);
		db.insert(project);
		UsageTests.expectOne(system, project);
		db.delete(project);
	}
}
