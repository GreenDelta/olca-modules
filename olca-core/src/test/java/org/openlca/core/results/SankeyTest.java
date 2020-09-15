package org.openlca.core.results;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.math.LcaCalculator;
import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.ProcessProduct;
import org.openlca.core.matrix.TechIndex;
import org.openlca.core.matrix.format.JavaMatrix;
import org.openlca.core.matrix.solvers.JavaSolver;
import org.openlca.core.model.Flow;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;

public class SankeyTest {

	@Test
	public void testCycles() {
		var data = new MatrixData();
		data.techIndex = new TechIndex(product(1));
		data.techIndex.setDemand(1.0);
		data.techIndex.put(product(2));
		data.techIndex.put(product(3));
		data.techMatrix = JavaMatrix.of(new double[][]{
				{1.0, 0.0, 0.0},
				{-1.0, 1.0, -0.1},
				{0.0, -2.0, 1.0},
		});

		data.flowIndex = FlowIndex.create();
		var flow = new FlowDescriptor();
		flow.id = 42;
		data.flowIndex.putOutput(flow);
		data.enviMatrix = JavaMatrix.of(new double[][] {
				{1.0 , 2.0, 3.0},
		});

		var calculator = new LcaCalculator(new JavaSolver(), data);
		var result = calculator.calculateFull();

		var sankey = Sankey.of(data.flowIndex.at(0), result)
				.build();

		Assert.assertEquals(3, sankey.nodeCount);

	}

	private ProcessProduct product(int i) {
		var process = new ProcessDescriptor();
		process.id = i;
		process.name = "process " + i;
		var flow = new FlowDescriptor();
		flow.id = i;
		flow.name = "product " + i;
		return ProcessProduct.of(process, flow);
	}

}
