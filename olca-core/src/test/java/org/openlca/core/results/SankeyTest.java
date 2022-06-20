package org.openlca.core.results;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.matrix.index.EnviIndex;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.matrix.Demand;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.matrix.format.JavaMatrix;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;

public class SankeyTest {

	@Test
	public void testCycles() {
		var data = new MatrixData();
		data.demand = Demand.of(product(1), 1.0);
		data.techIndex = new TechIndex(product(1));
		data.techIndex.add(product(2));
		data.techIndex.add(product(3));
		data.techMatrix = JavaMatrix.of(new double[][]{
				{1.0, 0.0, 0.0},
				{-1.0, 1.0, -0.1},
				{0.0, -2.0, 1.0},
		});

		data.enviIndex = EnviIndex.create();
		var flow = new FlowDescriptor();
		flow.id = 42;
		data.enviIndex.add(EnviFlow.outputOf(flow));
		data.enviMatrix = JavaMatrix.of(new double[][]{
				{1.0, 2.0, 3.0},
		});
		var result = FullResult.of(Tests.getDb(), data);

		var sankey = Sankey.of(data.enviIndex.at(0), result)
				.build();
		Assert.assertEquals(3, sankey.nodeCount);
		var visited = new AtomicInteger(0);
		sankey.traverse(node -> {
			visited.incrementAndGet();

			switch (node.index) {
				case 0 -> {
					Assert.assertEquals(1.0, node.direct, 1e-10);
					Assert.assertEquals(11.0, node.total, 1e-10);
					Assert.assertEquals(1.0, node.share, 1e-10);
				}
				case 1 -> {
					Assert.assertEquals(2.5, node.direct, 1e-10);
					Assert.assertEquals(10, node.total, 1e-10);
					Assert.assertEquals(10.0 / 11.0, node.share, 1e-10);
				}
				case 2 -> {
					Assert.assertEquals(7.5, node.direct, 1e-10);
					Assert.assertEquals(8.0, node.total, 1e-10);
					Assert.assertEquals(8.0 / 11.0, node.share, 1e-10);
				}
			}
		});
		Assert.assertEquals(3, visited.get());
	}

	private TechFlow product(int i) {
		var process = new ProcessDescriptor();
		process.id = i;
		process.name = "process " + i;
		var flow = new FlowDescriptor();
		flow.id = i;
		flow.name = "product " + i;
		return TechFlow.of(process, flow);
	}

}
