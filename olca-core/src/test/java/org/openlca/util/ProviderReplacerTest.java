package org.openlca.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.Descriptor;

public class ProviderReplacerTest {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testProviderReplacer() {
		var units = UnitGroup.of("Mass units", "kg");
		var mass = FlowProperty.of("Mass", units);
		var p = Flow.product("p", mass);
		var q = Flow.product("q", mass);

		var P1 = Process.of("P1", p);
		var P2 = Process.of("P2", p);
		db.insert(units, mass, p, q, P1, P2);

		var Q = Process.of("Q", q);
		Q.version = 1L;
		Q.input(p, 21).defaultProviderId = P1.id;
		db.insert(Q);

		var pro1 = Descriptor.of(P1);
		var pro2 = Descriptor.of(P2);
		var flow = Descriptor.of(p);

		var replacer = ProviderReplacer.of(db);
		assertTrue(replacer.getUsedProviders().contains(pro1));
		assertTrue(replacer.getProviderFlowsOf(pro1).contains(flow));
		assertTrue(replacer.getProvidersOf(flow).contains(pro2));

		replacer.replace(pro1, pro2, flow);

		Q = db.get(Process.class, Q.id);
		assertEquals(2L, Q.version);
		var input = Q.exchanges.stream()
				.filter(e -> e.isInput)
				.findAny()
				.orElseThrow();
		assertEquals(pro2.id, input.defaultProviderId);

		db.delete(Q, P1, P2, q, p, mass, units);
	}
}
