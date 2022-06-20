package org.openlca.core.results;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.openlca.core.matrix.Demand;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.format.DenseMatrix;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.matrix.index.EnviIndex;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.results.providers.SolverContext;

public class DemandTest {

	private MatrixData data;

	@Before
	public void setup() {
		data = new MatrixData();
		data.techIndex = new TechIndex();
		for (int i = 0; i < 3; i++) {
			var techFlow = TechFlow.of(
				ProcessDescriptor.create().id(i).get(),
				FlowDescriptor.create().id(i + 42).get());
			data.techIndex.add(techFlow);
		}
		data.techMatrix = DenseMatrix.of(new double[][]{
			{1, 0, 0},
			{-1, 1, 0},
			{0, -1, 1}
		});

		data.enviIndex = EnviIndex.create();
		var enviFlow = EnviFlow.outputOf(
			FlowDescriptor.create().id(99).get());
		data.enviIndex.add(enviFlow);
		data.enviMatrix = DenseMatrix.of(new double[][]{
			{1, 1, 1},
		});
	}

	@Test
	public void testDemandPos() {
		for (int i = 0; i < 3; i++) {
			data.demand = Demand.of(data.techIndex.at(i), 2);
			var context = SolverContext.of(data);
			var result = FullResult.of(context);
			var g = result.totalFlowResults[0];
			switch (i) {
				case 0 -> assertEquals(6, g, 1e-16);
				case 1 -> assertEquals(4, g, 1e-16);
				case 2 -> assertEquals(2, g, 1e-16);
			}
		}
	}

}
