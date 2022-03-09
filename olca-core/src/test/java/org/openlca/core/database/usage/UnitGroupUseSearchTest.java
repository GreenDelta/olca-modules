package org.openlca.core.database.usage;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.UnitGroup;

public class UnitGroupUseSearchTest {

	private final IDatabase db = Tests.getDb();
	private UnitGroup group;

	@Before
	public void setup() {
		group = db.insert(UnitGroup.of("group", "kg"));
	}

	@After
	public void tearDown() {
		db.delete(group);
	}

	@Test
	public void testFindNoUsage() {
		UsageTests.expectEmpty(group);
	}

	@Test
	public void testFindInFlowProperties() {
		var property = db.insert(FlowProperty.of("prop", group));
		UsageTests.expectOne(group, property);
		db.delete(property);
	}
}
