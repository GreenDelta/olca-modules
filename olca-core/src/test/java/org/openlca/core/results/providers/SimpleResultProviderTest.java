package org.openlca.core.results.providers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.ThreadLocalRandom;

import org.junit.Before;
import org.junit.Test;
import org.openlca.core.matrix.index.EnviIndex;
import org.openlca.core.matrix.index.ImpactIndex;
import org.openlca.core.matrix.Demand;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.results.SimpleResult;

public class SimpleResultProviderTest {

	private SimpleResult result;

	@Before
	public void setup() {
		// create the indices
		var techIdx = new TechIndex(product("p1"));
		techIdx.add(product("p2"));
		var enviIdx = EnviIndex.create();
		enviIdx.add(EnviFlow.inputOf(flow("r")));
		enviIdx.add(EnviFlow.outputOf(flow("e")));
		var impactIndex = new ImpactIndex();
		impactIndex.add(impact("i1"));
		impactIndex.add(impact("i2"));

		var demand = Demand.of(techIdx.at(0), 1.0);
		result = SimpleResultProvider.of(demand, techIdx)
			.withFlowIndex(enviIdx)
			.withImpactIndex(impactIndex)
			.withScalingVector(new double[] {0.5, 0.25})
			.toResult();
	}

	@Test
	public void testTechIndex() {
		var techIndex = result.techIndex();
		assertEquals(2, techIndex.size());
		assertEquals("p1", techIndex.at(0).provider().name);
		assertEquals("p2", techIndex.at(1).provider().name);
	}

	@Test
	public void testFlowIndex() {
		var flowIndex = result.enviIndex();
		assertEquals(2, flowIndex.size());
		assertEquals("r", flowIndex.at(0).flow().name);
		assertTrue(flowIndex.at(0).isInput());
		assertEquals("e", flowIndex.at(1).flow().name);
		assertFalse(flowIndex.at(1).isInput());
	}

	@Test
	public void testImpactIndex() {
		var impactIndex = result.impactIndex();
		assertEquals(2, impactIndex.size());
		assertEquals("i1", impactIndex.at(0).name);
		assertEquals("i2", impactIndex.at(1).name);
	}

	@Test
	public void testScalingVector() {
	}

	private TechFlow product(String name) {
		var flow = new FlowDescriptor();
		flow.id = id();
		flow.name = name;
		flow.flowType = FlowType.PRODUCT_FLOW;
		var process = new ProcessDescriptor();
		process.id = id();
		process.name = name;
		return TechFlow.of(process, flow);
	}

	private FlowDescriptor flow(String name) {
		var flow = new FlowDescriptor();
		flow.id = id();
		flow.name = name;
		flow.flowType = FlowType.ELEMENTARY_FLOW;
		return flow;
	}

	private ImpactDescriptor impact(String name) {
		var impact = new ImpactDescriptor();
		impact.id = id();
		impact.name = name;
		return impact;
	}

	private long id() {
		return ThreadLocalRandom.current()
			.nextInt(1, Integer.MAX_VALUE);
	}

}
