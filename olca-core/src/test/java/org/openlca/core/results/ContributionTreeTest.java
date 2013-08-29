package org.openlca.core.results;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.math.IMatrix;
import org.openlca.core.math.InventorySolver;
import org.openlca.core.math.MatrixFactory;
import org.openlca.core.matrices.FlowIndex;
import org.openlca.core.matrices.InventoryMatrix;
import org.openlca.core.matrices.LongPair;
import org.openlca.core.matrices.ProductIndex;
import org.openlca.core.model.descriptors.FlowDescriptor;

public class ContributionTreeTest {

	@Test
	public void testSimpleTree() {

		InventoryMatrix matrix = new InventoryMatrix();
		ProductIndex productIndex = new ProductIndex(LongPair.of(1, 1), 1.0);
		productIndex.put(LongPair.of(2, 2));
		productIndex.put(LongPair.of(3, 3));
		productIndex.putLink(LongPair.of(1, 2), LongPair.of(2, 2));
		productIndex.putLink(LongPair.of(1, 3), LongPair.of(3, 3));
		matrix.setProductIndex(productIndex);

		FlowIndex flowIndex = new FlowIndex();
		flowIndex.putOutputFlow(4);
		matrix.setFlowIndex(flowIndex);

		IMatrix techMatrix = MatrixFactory.create(new double[][] { { 1, 0, 0 },
				{ -1, 1, 0 }, { -1, 0, 1 } });
		matrix.setTechnologyMatrix(techMatrix);
		IMatrix enviMatrix = MatrixFactory.create(new double[][] { { 0, 0.5,
				0.5 } });
		matrix.setInterventionMatrix(enviMatrix);

		AnalysisResult result = new InventorySolver().analyse(matrix);
		FlowDescriptor flow = new FlowDescriptor();
		flow.setId(4);

		Assert.assertEquals(1.0, result.getFlowResults().getTotalResult(flow),
				1e-16);
		ContributionTree tree = result.getContributions().getTree(flow);
		Assert.assertEquals(2, tree.getRoot().getChildren().size());
		Assert.assertEquals(1.0, tree.getRoot().getAmount(), 1e-16);
		Assert.assertEquals(0.5, tree.getRoot().getChildren().get(0)
				.getAmount(), 1e-16);
		Assert.assertEquals(0.5, tree.getRoot().getChildren().get(1)
				.getAmount(), 1e-16);
	}

}
