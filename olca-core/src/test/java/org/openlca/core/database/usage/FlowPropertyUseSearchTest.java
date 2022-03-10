package org.openlca.core.database.usage;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.UnitGroup;

public class FlowPropertyUseSearchTest {

	private final IDatabase db = Tests.getDb();

	private FlowProperty unused;
	private FlowProperty used;
	private Flow flow;
	private UnitGroup unitGroup;

	@Before
	public void setUp() {
		unused = db.insert(new FlowProperty());
		used = db.insert(new FlowProperty());
		unitGroup = new UnitGroup();
		unitGroup.defaultFlowProperty = used;
		unitGroup = db.insert(unitGroup);
		flow = new Flow();
		var fac = new FlowPropertyFactor();
		fac.flowProperty = used;
		flow.flowPropertyFactors.add(fac);
		flow = db.insert(flow);
	}

	@After
	public void tearDown() {
		db.delete(flow, unitGroup, unused, used);
	}

	@Test
	public void testUnused() {
		var search = UsageSearch.of(ModelType.FLOW_PROPERTY, db);
		var deps = search.find(unused.id);
		Assert.assertTrue(deps.isEmpty());
	}

	@Test
	public void testUsed() {
		var search = UsageSearch.of(ModelType.FLOW_PROPERTY, db);
		var deps = search.find(used.id);
		Assert.assertEquals(2, deps.size());
		for (var dep : deps) {
			if (dep.type != ModelType.UNIT_GROUP) {
				Assert.assertEquals(ModelType.FLOW, dep.type);
				Assert.assertEquals(flow.id, dep.id);
			} else {
				Assert.assertEquals(unitGroup.id, dep.id);
			}
		}
	}
}
