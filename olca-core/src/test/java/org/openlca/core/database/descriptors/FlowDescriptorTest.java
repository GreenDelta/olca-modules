package org.openlca.core.database.descriptors;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.Descriptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FlowDescriptorTest {

	private final IDatabase db = Tests.getDb();
	private final FlowDao flowDao = new FlowDao(db);

	private FlowProperty property;
	private Flow flow;

	@Before
	public void setUp() {
		property = new FlowProperty();
		property = new FlowPropertyDao(db).insert(property);
		flow = new Flow();
		flow.referenceFlowProperty = property;
		flow = flowDao.insert(flow);
	}

	@After
	public void tearDown() {
		flowDao.delete(flow);
		new FlowPropertyDao(db).delete(property);
	}

	@Test
	public void testGetRefFlowPropertyId() {
		var d = flowDao.getDescriptor(flow.id);
		assertEquals(property.id, d.refFlowPropertyId);
	}

	@Test
	public void testGetDescriptorsForType() {

		var units = UnitGroup.of("Mass units", "kg");
		var mass = FlowProperty.of("Mass", units);
		var p = Flow.product("p", mass);
		var w = Flow.waste("w", mass);
		var e = Flow.elementary("e", mass);

		db.insert(units, mass, p, w, e);

		var descriptors = Stream.of(p, w, e)
			.map(Descriptor::of)
			.collect(Collectors.toList());
		for (var f : descriptors) {
			var xs = flowDao.getDescriptors(f.flowType);
			for (var g : descriptors) {
				if (f == g) {
					assertTrue(xs.contains(g));
				} else {
					assertFalse(xs.contains(g));
				}
			}
		}

		var all = flowDao.getDescriptors(FlowType.values());
		for (var d : descriptors) {
			assertTrue(all.contains(d));
		}

		db.delete(e, w, p, mass, units);
	}

}
