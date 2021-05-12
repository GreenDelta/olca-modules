package org.openlca.core.results;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.matrix.index.FlowIndex;
import org.openlca.core.matrix.index.IndexFlow;
import org.openlca.core.matrix.index.LongPair;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.index.ProcessProduct;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;

public class ContributionTreeTest {

	@Test
	public void testSimpleTree() {

		var data = new MatrixData();
		var techIndex = new TechIndex(provider(1, 1));
		techIndex.add(provider(2, 2));
		techIndex.add(provider(3, 3));
		techIndex.putLink(LongPair.of(1, 2), provider(2, 2));
		techIndex.putLink(LongPair.of(1, 3), provider(3, 3));
		data.techIndex = techIndex;

		var enviIndex = FlowIndex.create();
		var outFlow = new FlowDescriptor();
		outFlow.id = 4;
		enviIndex.add(IndexFlow.outputOf(outFlow));
		data.flowIndex = enviIndex;

		var solver = Tests.getDefaultSolver();
		var techMatrix = solver.matrix(3, 3);
		techMatrix.setValues(new double[][] {
				{ 1, 0, 0 },
				{ -1, 1, 0 },
				{ -1, 0, 1 } });
		data.techMatrix = techMatrix;

		var flowMatrix = solver.matrix(1, 3);
		flowMatrix.setValues(new double[][] {
				{ 0, 0.5, 0.5 } });
		data.flowMatrix = flowMatrix;

		// calculate and check the result
		var r = FullResult.of(Tests.getDb(), data);
		Assert.assertEquals(1.0, r.getTotalFlowResult(enviIndex.at(0)), 1e-16);

		var tree = r.getTree(enviIndex.at(0));
		Assert.assertEquals(2, tree.childs(tree.root).size());
		Assert.assertEquals(1.0, tree.root.result, 1e-16);
		Assert.assertEquals(0.5, tree.childs(tree.root).get(0).result, 1e-16);
		Assert.assertEquals(0.5, tree.childs(tree.root).get(1).result, 1e-16);

	}

	private ProcessProduct provider(long id, long flowId) {
		ProcessDescriptor process = new ProcessDescriptor();
		process.name = "Process " + id;
		process.id = id;
		FlowDescriptor flow = new FlowDescriptor();
		flow.name = "Flow " + flowId;
		flow.id = flowId;
		return ProcessProduct.of(process, flow);
	}
}
