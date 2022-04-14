package org.openlca.core.database.descriptors;

import java.util.stream.Stream;

import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FlowDescriptorTest {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testGetRefFlowPropertyId() {
		var property =  db.insert(new FlowProperty());
		var flow = new Flow();
		flow.referenceFlowProperty = property;
		flow = db.insert(flow);
		var d = (FlowDescriptor) db.getDescriptor(Flow.class, flow.id);
		assertEquals(property.id, d.refFlowPropertyId);
		db.delete(flow, property);
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
			.toList();
		var dao = new FlowDao(db);
		for (var f : descriptors) {
			var xs = dao.getDescriptors(f.flowType);
			for (var g : descriptors) {
				if (f == g) {
					assertTrue(xs.contains(g));
				} else {
					assertFalse(xs.contains(g));
				}
			}
		}

		var all = dao.getDescriptors(FlowType.values());
		for (var d : descriptors) {
			assertTrue(all.contains(d));
		}

		db.delete(e, w, p, mass, units);
	}

}
