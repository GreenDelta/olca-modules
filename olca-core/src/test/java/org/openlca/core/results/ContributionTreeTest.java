package org.openlca.core.results;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.math.LcaCalculator;
import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.Provider;
import org.openlca.core.matrix.TechIndex;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;

public class ContributionTreeTest {

	@Test
	public void testSimpleTree() {

		MatrixData data = new MatrixData();
		TechIndex techIndex = new TechIndex(provider(1, 1));
		techIndex.put(provider(2, 2));
		techIndex.put(provider(3, 3));
		techIndex.putLink(LongPair.of(1, 2), provider(2, 2));
		techIndex.putLink(LongPair.of(1, 3), provider(3, 3));
		data.techIndex = techIndex;

		FlowIndex enviIndex = new FlowIndex();
		FlowDescriptor outFlow = new FlowDescriptor();
		outFlow.setId(4);
		enviIndex.putOutput(outFlow);
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

		UpstreamTree tree = new UpstreamTree(result,
				result.upstreamFlowResults.getRow(0));
		Assert.assertEquals(2, tree.childs(tree.root).size());
		Assert.assertEquals(1.0, tree.root.result, 1e-16);
		Assert.assertEquals(0.5, tree.childs(tree.root).get(0).result, 1e-16);
		Assert.assertEquals(0.5, tree.childs(tree.root).get(1).result, 1e-16);
	}

	private Provider provider(long id, long flowId) {
		ProcessDescriptor process = new ProcessDescriptor();
		process.setName("Process " + id);
		process.setId(id);
		FlowDescriptor flow = new FlowDescriptor();
		flow.setName("Flow " + flowId);
		flow.setId(flowId);
		return Provider.of(process, flow);
	}
}
