package org.openlca.core.matrix;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.LocationDescriptor;

public class FlowIndexTest {

	private long nextID = 1L;

	@Test
	public void testIt() {
		FlowIndex idx = FlowIndex.createRegionalized();
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
			IndexFlow iflow = idx.at(i);
			Assert.assertNotNull(iflow);
			Assert.assertEquals(i, idx.of(iflow));
			Assert.assertEquals(flows.get(i), iflow.flow);
			Assert.assertNotNull(iflow.location);
			Assert.assertEquals(locations.get(i), iflow.location);

			Assert.assertEquals(i, idx.of(iflow.flow.id, iflow.location.id));
			Assert.assertEquals(i, idx.of(iflow.flow, iflow.location));
			Assert.assertTrue(idx.contains(iflow.flow.id, iflow.location.id));
			Assert.assertTrue(idx.contains(iflow.flow, iflow.location));
			Assert.assertEquals(isInput.get(i), iflow.isInput);
		}
	}

	@Test
	public void testNonRegFlows() {

		FlowIndex idx = FlowIndex.createRegionalized();

		// add 500 flows with location and 500 without
		boolean isInput = true;
		for (int i = 0; i < 1000; i++) {
			if (i % 5 == 0) {
				isInput = !isInput;
			}
			if (i % 2 == 0) {
				int _i = isInput
						? idx.putInput(randFlow())
						: idx.putOutput(randFlow());
				Assert.assertEquals(i, _i);
			} else {
				int _i = isInput
						? idx.putInput(randFlow(), randLocation())
						: idx.putOutput(randFlow(), randLocation());
				Assert.assertEquals(i, _i);
			}
		}

		// check the index
		Assert.assertEquals(1000, idx.flows().size());
		for (int i = 0; i < 1000; i++) {
			IndexFlow iflow = idx.at(i);
			Assert.assertNotNull(iflow);
			if (i % 2 == 0) {
				Assert.assertNull(iflow.location);
			} else {
				Assert.assertNotNull(iflow.location);
			}
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
