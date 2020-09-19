package org.openlca.core.results.solutions;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.ProcessProduct;
import org.openlca.core.matrix.TechIndex;
import org.openlca.core.matrix.format.JavaMatrix;
import org.openlca.core.matrix.solvers.JavaSolver;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;

@RunWith(Parameterized.class)
public class ResultProviderTest {

	private final ResultProvider provider;

	public ResultProviderTest(ResultProvider provider) {
		this.provider = provider;
	}

	@Parameterized.Parameters
	public static Collection<ResultProvider> providers() {
		var data = createModel();
		var solver = new JavaSolver();
		return List.of(
				EagerResultProvider.create(data, solver),
				LazyResultProvider.create(data, solver));
	}

	private static MatrixData createModel() {

		Function<Integer, ProcessProduct> product = i -> {
			var process = new ProcessDescriptor();
			process.name = "p" + i;
			process.id = i;
			var flow = new FlowDescriptor();
			flow.name = "p" + i;
			flow.id = i;
			return ProcessProduct.of(process, flow);
		};

		Function<Integer, FlowDescriptor> flow = i -> {
			var f = new FlowDescriptor();
			f.id = i + 42;
			f.name = "e" + i;
			return f;
		};

		var data = new MatrixData();

		// tech. flows
		data.techIndex = new TechIndex(product.apply(1));
		data.techIndex.setDemand(1.0);
		data.techIndex.put(product.apply(2));
		data.techMatrix = JavaMatrix.of(new double[][]{
				{0.5, -0.5},
				{-0.5, 1.0},
		});

		// env. flows
		data.flowIndex = FlowIndex.create();
		data.flowIndex.putOutput(flow.apply(1));
		data.flowIndex.putInput(flow.apply(2));
		data.flowMatrix = JavaMatrix.of(new double[][]{
				{1.0, 2.0},
				{-3.0, -3.0},
		});

		return data;
	}

	@Test
	public void testIndices() {
		assertEquals(2, provider.techIndex().size());
		assertEquals(2, provider.flowIndex().size());
		assertTrue(provider.hasFlows());
	}

	@Test
	public void testScalingVector() {
		assertArrayEquals(
				d(4, 2),
				provider.scalingVector(),
				1e-10);
	}

	@Test
	public void testScalingFactorOf() {
		assertEquals(4, provider.scalingFactorOf(0), 1e-10);
		assertEquals(2, provider.scalingFactorOf(1), 1e-10);
	}

	@Test
	public void testTotalRequirements() {
		assertArrayEquals(
				d(2, 2),
				provider.totalRequirements(),
				1e-10);
	}

	@Test
	public void testTotalRequirementsOf() {
		assertEquals(2.0, provider.totalRequirementsOf(0), 1e-10);
		assertEquals(2.0, provider.totalRequirementsOf(1), 1e-10);
	}

	@Test
	public void testTotalFlows() {
		assertArrayEquals(
				d(8, -18),
				provider.totalFlows(),
				1e-10);
	}

	private double[] d(double... values) {
		return values;
	}
}
