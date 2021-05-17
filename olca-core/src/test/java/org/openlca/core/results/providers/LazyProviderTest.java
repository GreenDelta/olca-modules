package org.openlca.core.results.providers;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.matrix.format.HashPointMatrix;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;

public class LazyProviderTest {

	@Test
	public void testScalingVector() {
		var data = new MatrixData();
		data.techMatrix = HashPointMatrix.of(new double[][]{
				{1.0, 0.0, 0.0},
				{-1.0, 1.0, 0.0},
				{0.0, -1.0, 1.0},
		});
		data.techIndex = new TechIndex(product(1));
		data.techIndex.add(product(2));
		data.techIndex.add(product(3));
		data.techIndex.setDemand(1.0);

		var provider = LazyResultProvider.create(data);
		var scaling = provider.scalingVector();
		Assert.assertArrayEquals(
				new double[]{1.0, 1.0, 1.0}, scaling, 1e-10);
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
