package org.openlca.core.results.solutions;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openlca.core.Tests;
import org.openlca.core.library.Library;
import org.openlca.core.library.LibraryDir;
import org.openlca.core.matrix.DIndex;
import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.ProcessProduct;
import org.openlca.core.matrix.TechIndex;
import org.openlca.core.matrix.format.JavaMatrix;
import org.openlca.core.matrix.solvers.JavaSolver;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.util.Dirs;

@RunWith(Parameterized.class)
public class ResultProviderTest {

	private static File libsDir;
	private final ResultProvider provider;

	public ResultProviderTest(ResultProvider provider) {
		this.provider = provider;
	}

	@Parameterized.Parameters
	public static Collection<ResultProvider> setup() throws Exception {

		var db = Tests.getDb();
		var libID = "lib_01.00.000";

		var units = db.insert(UnitGroup.of("Mass units", Unit.of("kg")));
		var mass = db.insert(FlowProperty.of("Mass", units));
		var data = new MatrixData();

		// tech. flows
		Function<Integer, ProcessProduct> product = i -> {
			var flow = db.insert(Flow.product("p" + i, mass));
			var process = Process.of("p" + i, flow);
			process.library = libID;
			process = db.insert(process);
			return ProcessProduct.of(process, flow);
		};
		data.techIndex = new TechIndex(product.apply(1));
		data.techIndex.setDemand(1.0);
		data.techIndex.put(product.apply(2));
		data.techMatrix = JavaMatrix.of(new double[][]{
				{0.5, -0.5},
				{-0.5, 1.0},
		});

		// env. flows
		Function<Integer, FlowDescriptor> flow = i -> {
			var f = db.insert(Flow.elementary("e" + i, mass));
			return Descriptor.of(f);
		};
		data.flowIndex = FlowIndex.create();
		data.flowIndex.putOutput(flow.apply(1));
		data.flowIndex.putInput(flow.apply(2));
		data.flowMatrix = JavaMatrix.of(new double[][]{
				{1.0, 2.0},
				{-3.0, -3.0},
		});

		// impact factors
		Function<Integer, ImpactCategoryDescriptor> impact = i -> {
			var imp = new ImpactCategoryDescriptor();
			imp.id = i + 84;
			imp.name = "i" + i;
			return imp;
		};
		data.impactIndex = new DIndex<>();
		data.impactIndex.put(impact.apply(1));
		data.impactIndex.put(impact.apply(2));
		data.impactIndex.put(impact.apply(3));
		data.impactMatrix = JavaMatrix.of(new double[][]{
				{1.0, 0.0},
				{0.0, -1.0},
				{2.0, -0.5},
		});

		// write the matrix data as library and create a
		// foreground system
		libsDir = Files.createTempDirectory("olca_tests").toFile();
		var libDir = new File(libsDir, libID);
		Library.create(data, libDir);
		var foreground = new MatrixData();
		foreground.techIndex = new TechIndex(data.techIndex.getRefFlow());
		foreground.techIndex.setDemand(1.0);
		foreground.techMatrix = JavaMatrix.of(new double[][]{{1}});

		// create the result providers
		var solver = new JavaSolver();
		return List.of(
				EagerResultProvider.create(data, solver),
				LazyResultProvider.create(data, solver),
				LibraryResultProvider.of(
						db, new LibraryDir(libsDir), solver, foreground));
	}

	@Parameterized.AfterParam
	public static void tearDown() {
		Tests.clearDb();
		Dirs.delete(libsDir);
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
	public void testTechColumnOf() {
		assertArrayEquals(d(0.5, -0.5), provider.techColumnOf(0), 1e-10);
		assertArrayEquals(d(-0.5, 1.0), provider.techColumnOf(1), 1e-10);
	}

	@Test
	public void testTechValueOf() {
		double[][] expected = {
				{0.5, -0.5},
				{-0.5, 1.0},
		};
		for (int row = 0; row < expected.length; row++) {
			for (int col = 0; col < expected[row].length; col++) {
				assertEquals(
						expected[row][col],
						provider.techValueOf(row, col),
						1e-10);
			}
		}
	}

	@Test
	public void testScaledTechValueOf() {
		double[][] expected = {
				{2.0, -1.0},
				{-2.0, 2.0},
		};
		for (int row = 0; row < expected.length; row++) {
			for (int col = 0; col < expected[row].length; col++) {
				assertEquals(
						expected[row][col],
						provider.scaledTechValueOf(row, col),
						1e-20);
			}
		}
	}

	@Test
	public void testSolutionOfOne() {
		assertArrayEquals(d(4.0, 2.0), provider.solutionOfOne(0), 1e-10);
		assertArrayEquals(d(2.0, 2.0), provider.solutionOfOne(1), 1e-10);
	}

	@Test
	public void testLoopFactorOf() {
		assertEquals(0.5, provider.loopFactorOf(0), 1e-10);
		assertEquals(0.5, provider.loopFactorOf(1), 1e-10);
	}

	@Test
	public void testTotalFactorOf() {
		assertEquals(1.0, provider.totalFactorOf(0), 1e-10);
		assertEquals(1.0, provider.totalFactorOf(1), 1e-10);
	}

	@Test
	public void testUnscaledFlowsOf() {
		assertArrayEquals(d(1.0, -3.0), provider.unscaledFlowsOf(0), 1e-10);
		assertArrayEquals(d(2.0, -3.0), provider.unscaledFlowsOf(1), 1e-10);
	}

	@Test
	public void testUnscaledFlowOf() {
		double[][] expected = {
				{1.0, 2.0},
				{-3, -3.0}
		};
		for (int flow = 0; flow < expected.length; flow++) {
			for (int product = 0; product < expected[flow].length; product++) {
				assertEquals(
						expected[flow][product],
						provider.unscaledFlowOf(flow, product),
						1e-10);
			}
		}
	}

	@Test
	public void testDirectFlowsOf() {
		assertArrayEquals(d(4.0, -12.0), provider.directFlowsOf(0), 1e-10);
		assertArrayEquals(d(4.0, -6.0), provider.directFlowsOf(1), 1e-10);
	}

	@Test
	public void testDirectFlowOf() {
		double[][] expected = {
				{4.0, 4.0},
				{-12.0, -6.0}
		};
		for (int flow = 0; flow < expected.length; flow++) {
			for (int product = 0; product < expected[flow].length; product++) {
				assertEquals(
						expected[flow][product],
						provider.directFlowOf(flow, product),
						1e-10);
			}
		}
	}

	@Test
	public void testTotalFlowsOfOne() {
		assertArrayEquals(d(8, -18), provider.totalFlowsOfOne(0), 1e-10);
		assertArrayEquals(d(6, -12), provider.totalFlowsOfOne(1), 1e-10);
	}

	@Test
	public void testTotalFlowOfOne() {
		double[][] expected = {
				{8, 6},
				{-18, -12}
		};
		for (int flow = 0; flow < expected.length; flow++) {
			for (int product = 0; product < expected[flow].length; product++) {
				assertEquals(
						expected[flow][product],
						provider.totalFlowOfOne(flow, product),
						1e-10);
			}
		}
	}

	@Test
	public void totalFlowsOf() {
		assertArrayEquals(d(8, -18), provider.totalFlowsOf(0), 1e-10);
		assertArrayEquals(d(6, -12), provider.totalFlowsOf(1), 1e-10);
	}

	@Test
	public void totalFlowOf() {
		double[][] expected = {
				{8, 6},
				{-18, -12}
		};
		for (int flow = 0; flow < expected.length; flow++) {
			for (int product = 0; product < expected[flow].length; product++) {
				assertEquals(
						expected[flow][product],
						provider.totalFlowOf(flow, product),
						1e-10);
			}
		}
	}

	@Test
	public void testTotalFlows() {
		assertArrayEquals(
				d(8, -18),
				provider.totalFlows(),
				1e-10);
	}

	@Test
	public void testImpactFactorsOf() {
		assertArrayEquals(
				d(1, 0, 2),
				provider.impactFactorsOf(0),
				1e-10);
		assertArrayEquals(
				d(0, -1, -0.5),
				provider.impactFactorsOf(1),
				1e-10);
	}

	@Test
	public void testImpactFactorOf() {
		double[][] expected = {
				{1.0, 0.0},
				{0.0, -1.0},
				{2.0, -0.5},
		};
		for (int impact = 0; impact < expected.length; impact++) {
			for (int flow = 0; flow < expected[impact].length; flow++) {
				assertEquals(
						expected[impact][flow],
						provider.impactFactorOf(impact, flow),
						1e-10);
			}
		}
	}

	private double[] d(double... values) {
		return values;
	}
}
