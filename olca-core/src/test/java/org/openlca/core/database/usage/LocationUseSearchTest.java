package org.openlca.core.database.usage;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Flow;
import org.openlca.core.model.Location;
import org.openlca.core.model.Process;

public class LocationUseSearchTest {

	private final IDatabase db = Tests.getDb();
	private Location location;

	@Before
	public void setup() {
		location = db.insert(Location.of("location"));
	}

	@After
	public void tearDown() {
		db.delete(location);
	}

	@Test
	public void testFindNoUsage() {
		UsageTests.expectEmpty(location);
	}

	@Test
	public void testFindInFlows() {
		var flow = new Flow();
		flow.name = "flow";
		flow.location = location;
		db.insert(flow);
		UsageTests.expectOne(location, flow);
		db.delete(flow);
	}

	@Test
	public void testFindInProcesses() {
		var process = new Process();
		process.name = "process";
		process.location = location;
		db.insert(process);
		UsageTests.expectOne(location, process);
		db.delete(process);
	}
}
