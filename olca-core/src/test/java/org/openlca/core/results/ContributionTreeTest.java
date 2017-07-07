package org.openlca.core.results;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.math.LcaCalculator;
import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.matrix.InventoryMatrix;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.matrix.TechIndex;
import org.openlca.core.matrix.format.IMatrix;
import org.openlca.core.model.descriptors.FlowDescriptor;

public class ContributionTreeTest {

	@Test
	public void testSimpleTree() {

		InventoryMatrix matrix = new InventoryMatrix();
		TechIndex productIndex = new TechIndex(LongPair.of(1, 1));
		productIndex.put(LongPair.of(2, 2));
		productIndex.put(LongPair.of(3, 3));
		productIndex.putLink(LongPair.of(1, 2), LongPair.of(2, 2));
		productIndex.putLink(LongPair.of(1, 3), LongPair.of(3, 3));
		matrix.productIndex = productIndex;

		FlowIndex flowIndex = new FlowIndex();
		flowIndex.putOutputFlow(4);
		matrix.flowIndex = flowIndex;

		IMatrix techMatrix = Tests.getDefaultSolver().matrix(3, 3);
		techMatrix.setValues(new double[][] { { 1, 0, 0 },
				{ -1, 1, 0 }, { -1, 0, 1 } });
		matrix.technologyMatrix = techMatrix;
		IMatrix enviMatrix = Tests.getDefaultSolver().matrix(1, 3);
		enviMatrix.setValues(new double[][] { { 0, 0.5, 0.5 } });
		matrix.interventionMatrix = enviMatrix;

		FullResult result = new LcaCalculator(Tests.getDefaultSolver(), matrix)
				.calculateFull();
		FlowDescriptor flow = new FlowDescriptor();
		flow.setId(4);

		Assert.assertEquals(1.0, result.getTotalFlowResult(flow.getId()), 1e-16);

		UpstreamTreeCalculator treeCalculator = new UpstreamTreeCalculator(
				result);
		UpstreamTree tree = treeCalculator.calculate(flow);
		Assert.assertEquals(2, tree.getRoot().getChildren().size());
		Assert.assertEquals(1.0, tree.getRoot().getAmount(), 1e-16);
		Assert.assertEquals(0.5, tree.getRoot().getChildren().get(0)
				.getAmount(), 1e-16);
		Assert.assertEquals(0.5, tree.getRoot().getChildren().get(1)
				.getAmount(), 1e-16);
	}
}
