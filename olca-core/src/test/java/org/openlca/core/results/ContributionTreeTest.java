package org.openlca.core.results;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.math.LcaCalculator;
import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.TechIndex;
import org.openlca.core.model.descriptors.FlowDescriptor;

public class ContributionTreeTest {

	@Test
	public void testSimpleTree() {

		MatrixData data = new MatrixData();
		TechIndex techIndex = new TechIndex(LongPair.of(1, 1));
		techIndex.put(LongPair.of(2, 2));
		techIndex.put(LongPair.of(3, 3));
		techIndex.putLink(LongPair.of(1, 2), LongPair.of(2, 2));
		techIndex.putLink(LongPair.of(1, 3), LongPair.of(3, 3));
		data.techIndex = techIndex;

		FlowIndex enviIndex = new FlowIndex();
		enviIndex.putOutputFlow(4);
		data.enviIndex = enviIndex;

		data.techMatrix = Tests.getDefaultSolver().matrix(3, 3);
		data.techMatrix.setValues(new double[][] {
				{ 1, 0, 0 },
				{ -1, 1, 0 },
				{ -1, 0, 1 } });
		data.enviMatrix = Tests.getDefaultSolver().matrix(1, 3);
		data.enviMatrix.setValues(new double[][] {
				{ 0, 0.5, 0.5 } });

		FullResult result = new LcaCalculator(Tests.getDefaultSolver(), data)
				.calculateFull();
		FlowDescriptor flow = new FlowDescriptor();
		flow.setId(4);

		Assert.assertEquals(1.0, result.getTotalFlowResult(flow.getId()),
				1e-16);

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
