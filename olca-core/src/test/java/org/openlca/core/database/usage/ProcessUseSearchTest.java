package org.openlca.core.database.usage;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;

public class ProcessUseSearchTest {

	private final IDatabase db = Tests.getDb();
	private Process process;

	@Before
	public void setup() {
		process = new Process();
		process.name = "process";
		process = db.insert(process);
	}

	@After
	public void tearDown() {
		db.delete(process);
	}

	@Test
	public void testFindNoUsage() {
		UsageTests.expectEmpty(process);
	}

	@Test
	public void testFindInSystems() {
		var system = new ProductSystem();
		system.name = "system";
		system.processes.add(process.id);
		db.insert(system);
		UsageTests.expectOne(process, system);
		db.delete(system);
	}
}
