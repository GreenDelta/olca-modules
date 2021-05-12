package org.openlca.core.matrix.index;

import java.util.List;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.LocationDescriptor;


public class IndexFlowTest {

	@Test
	public void testZeroIdNonEqual() {
		var flows = List.of(
			EnviFlow.inputOf(new FlowDescriptor()),
			EnviFlow.inputOf(new FlowDescriptor()),
			EnviFlow.outputOf(new FlowDescriptor()),
			EnviFlow.inputOf(new FlowDescriptor(), new LocationDescriptor()),
			EnviFlow.inputOf(new FlowDescriptor(), new LocationDescriptor()),
			EnviFlow.outputOf(new FlowDescriptor(), new LocationDescriptor()));
		for (var a : flows) {
			for (var b : flows) {
				if (a == b)
					continue;
				assertNotEquals(a, b);
			}
		}
	}
}
