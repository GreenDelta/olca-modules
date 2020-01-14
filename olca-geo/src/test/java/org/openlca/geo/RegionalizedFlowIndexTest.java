package org.openlca.geo;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.LocationDescriptor;

import java.util.ArrayList;
import java.util.List;

public class RegionalizedFlowIndexTest {

	private long nextID = 1L;

	@Test
	public void testIt() {
		RegionalizedFlowIndex idx = new RegionalizedFlowIndex();
		List<FlowDescriptor> flows = new ArrayList<>();
		List<LocationDescriptor> locations = new ArrayList<>();
		List<Boolean> isInput = new ArrayList<>();

		// build the index
		boolean b = false;
		for (int i = 0; i < 10_000; i++) {
			FlowDescriptor flow = randFlow();
			flows.add(flow);
			LocationDescriptor loc = randLocation();
			locations.add(loc);
			isInput.add(b);

			Assert.assertFalse(idx.contains(flow.id, loc.id));
			Assert.assertFalse(idx.contains(flow, loc));
			int j = b
					? idx.putInput(flow, loc)
					: idx.putOutput(flow, loc);
			Assert.assertEquals(i, j);
			b = !b;
		}

		// check the index
		for (int i = 0; i < 10_000; i++) {
			FlowDescriptor flow = idx.flowAt(i);
			Assert.assertEquals(flows.get(i), flow);
			LocationDescriptor loc = idx.locationAt(i);
			Assert.assertEquals(locations.get(i), loc);

			Assert.assertEquals(i, idx.of(flow.id, loc.id));
			Assert.assertEquals(i, idx.of(flow, loc));
			Assert.assertTrue(idx.contains(flow.id, loc.id));
			Assert.assertTrue(idx.contains(flow, loc));
			Assert.assertEquals(isInput.get(i), idx.isInput(flow.id));
			Assert.assertEquals(isInput.get(i), idx.isInput(flow));
		}
	}

	private FlowDescriptor randFlow() {
		FlowDescriptor flow = new FlowDescriptor();
		flow.id = nextID++;
		flow.name = "Flow " + flow.id;
		return flow;
	}

	private LocationDescriptor randLocation() {
		LocationDescriptor loc = new LocationDescriptor();
		loc.id = nextID++;
		loc.name = "Location " + loc.id;
		return loc;
	}
}
