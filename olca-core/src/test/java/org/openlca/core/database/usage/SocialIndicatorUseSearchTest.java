package org.openlca.core.database.usage;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Process;
import org.openlca.core.model.SocialAspect;
import org.openlca.core.model.SocialIndicator;

public class SocialIndicatorUseSearchTest {

	private final IDatabase db = Tests.getDb();
	private SocialIndicator indicator;

	@Before
	public void setup() {
		indicator = new SocialIndicator();
		indicator.name = "indicator";
		indicator = db.insert(indicator);
	}

	@After
	public void tearDown() {
		db.delete(indicator);
	}

	@Test
	public void testFindNoUsage() {
		UsageTests.expectEmpty(indicator);
	}

	@Test
	public void testFindInProcesses() {
		var process = new Process();
		process.name = "process";
		var aspect = new SocialAspect();
		aspect.indicator = indicator;
		process.socialAspects.add(aspect);
		db.insert(process);
		UsageTests.expectOne(indicator, process);
		db.delete(process);
	}
}
