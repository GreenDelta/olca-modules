package org.openlca.core.matrix.index;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.linking.ProviderIndex;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Result;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.Descriptor;

public class ProviderIndexTest {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testProductFlow() {
		test(FlowType.PRODUCT_FLOW);
	}

	@Test
	public void testWasteFlow() {
		test(FlowType.WASTE_FLOW);
	}

	private void test(FlowType flowType) {

		var units = UnitGroup.of("Mass units", "kg");
		var mass = FlowProperty.of("Mass", units);
		var flow = Flow.of("Flow", flowType, mass);
		var process = Process.of("Process", flow);
		var system = ProductSystem.of(process);
		var result = Result.of("Result", flow);
		db.insert(units, mass, flow, process, system, result);

		var lazy = ProviderIndex.lazy(db);
		var eager = ProviderIndex.eager(db);

		for (var idx : List.of(lazy, eager)) {
			var providers = idx.getProvidersOf(flow.id);
			assertEquals(3, providers.size());
			var flag = 0;
			for (var p : providers) {
				assertEquals(Descriptor.of(flow), p.flow());
				if (p.isProductSystem()) {
					assertEquals(Descriptor.of(system), p.provider());
					flag |= 1;
				}  else if (p.isResult()) {
					assertEquals(Descriptor.of(result), p.provider());
					flag |= 2;
				} else  {
					assertEquals(Descriptor.of(process), p.provider());
					flag |= 4;
				}
			}
			assertEquals(7, flag);
			assertTrue(idx.getProvidersOf(-100).isEmpty());
		}
		// delete the model
		db.delete(system, process, flow, mass, units);
	}
}
