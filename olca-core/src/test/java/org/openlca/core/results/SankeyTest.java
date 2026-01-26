package org.openlca.core.results;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.matrix.Demand;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.format.HashPointMatrix;
import org.openlca.core.matrix.format.JavaMatrix;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.matrix.index.EnviIndex;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.matrix.solvers.mkl.MKL;
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
		var result = LcaResult.of(Tests.getDb(), data).provider();

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

	/// Tests that the Sankey graph can handle deep supply chains without
	/// causing a StackOverflowError. This creates a linear chain of processes
	/// where each process depends on the next one: `P0 -> P1 -> P2 -> ... -> Pn`.
	@Test
	@Ignore
	public void testDeepSupplyChain() {
		MKL.loadFromDefault();
		Assert.assertTrue(MKL.isLoaded());

		// Create a chain deep enough to potentially cause stack overflow
		// with recursive implementation. Default JVM stack is typically
		// around 512KB-1MB, allowing ~1000-10000 recursive calls.
		int chainDepth = 15_000;

		// create tech flows
		var products = new TechFlow[chainDepth];
		for (int i = 0; i < chainDepth; i++) {
			products[i] = product(i);
		}
		var data = new MatrixData();
		data.techIndex = new TechIndex(products[0]);
		for (int i = 1; i < chainDepth; i++) {
			data.techIndex.add(products[i]);
		}
		data.demand = Demand.of(products[0], 1.0);

		// Build a sparse tech matrix representing a linear chain:
		// Each process i produces product i (diagonal = 1.0)
		// and consumes product i+1 (entry [i+1, i] = -1.0)
		var techMatrix = new HashPointMatrix(chainDepth, chainDepth);
		for (int i = 0; i < chainDepth; i++) {
			techMatrix.set(i, i, 1.0);
			if (i < chainDepth - 1) {
				techMatrix.set(i + 1, i, -1.0);
			}
		}
		data.techMatrix = techMatrix;

		// Add a simple environmental flow emitted by all processes
		data.enviIndex = EnviIndex.create();
		var emissionFlow = new FlowDescriptor();
		emissionFlow.id = 999_999;
		data.enviIndex.add(EnviFlow.outputOf(emissionFlow));
		var enviMatrix = new HashPointMatrix(1, chainDepth);
		for (int i = 0; i < chainDepth; i++) {
			enviMatrix.set(0, i, 1.0);
		}
		data.enviMatrix = enviMatrix;

		var result = LcaResult.of(Tests.getDb(), data).provider();

		// This should not throw StackOverflowError
		var sankey = Sankey.of(data.enviIndex.at(0), result).build();
		// Verify the graph was built correctly
		Assert.assertEquals(chainDepth, sankey.nodeCount);
		// The total result should be the sum of all direct emissions
		Assert.assertEquals(chainDepth, sankey.root.total, 1e-10);
	}

}
