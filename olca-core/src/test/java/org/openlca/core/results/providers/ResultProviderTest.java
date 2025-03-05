package org.openlca.core.results.providers;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.nio.file.Files;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openlca.core.DataDir;
import org.openlca.core.Tests;
import org.openlca.core.library.LibraryDir;
import org.openlca.core.library.LibraryExport;
import org.openlca.core.library.reader.LibReaderRegistry;
import org.openlca.core.matrix.Demand;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.format.JavaMatrix;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.matrix.index.EnviIndex;
import org.openlca.core.matrix.index.ImpactIndex;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.matrix.solvers.NativeSolver;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.Process;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.core.results.providers.libblocks.LibraryInversionSolver;
import org.openlca.nativelib.NativeLib;
import org.openlca.util.Dirs;

@RunWith(Parameterized.class)
public record ResultProviderTest(ResultProvider provider) {

	private static LibraryDir libDir;

	@Parameterized.Parameters
	public static Collection<ResultProvider> setup() throws Exception {

		// make sure that the native libraries are loaded; otherwise the
		// result matrices are not created in the library export
		if (!NativeLib.isLoaded()) {
			NativeLib.loadFrom(DataDir.get().root());
			assumeTrue(NativeLib.isLoaded());
		}

		var db = Tests.getDb();
		db.clear();
		var libID = "test_lib 1.0";

		var units = db.insert(UnitGroup.of("Mass units", Unit.of("kg")));
		var mass = db.insert(FlowProperty.of("Mass", units));
		var data = new MatrixData();

		// tech. flows
		Function<Integer, TechFlow> product = i -> {
			var flow = db.insert(Flow.product("p" + i, mass));
			var process = Process.of("p" + i, flow);
			process.dataPackage = libID;
			process = db.insert(process);
			return TechFlow.of(process, flow);
		};
		data.techIndex = new TechIndex(product.apply(1));
		data.demand = Demand.of(data.techIndex.at(0), 0.5);
		data.techIndex.add(product.apply(2));
		data.techMatrix = JavaMatrix.of(new double[][]{
				{1.0, -0.5},
				{-1.0, 1.0},
		});

		// env. flows
		Function<Integer, FlowDescriptor> flow = i -> {
			var f = db.insert(Flow.elementary("e" + i, mass));
			return Descriptor.of(f);
		};
		data.enviIndex = EnviIndex.create();
		data.enviIndex.add(EnviFlow.outputOf(flow.apply(1)));
		data.enviIndex.add(EnviFlow.inputOf(flow.apply(2)));
		data.enviMatrix = JavaMatrix.of(new double[][]{
				{1.0, 2.0},
				{-3.0, -3.0},
		});

		// impact factors
		Function<Integer, ImpactDescriptor> impact = i -> {
			var imp = ImpactCategory.of("i" + i);
			imp.dataPackage = libID;
			imp = db.insert(imp);
			return Descriptor.of(imp);
		};
		data.impactIndex = new ImpactIndex();
		data.impactIndex.add(impact.apply(1));
		data.impactIndex.add(impact.apply(2));
		data.impactIndex.add(impact.apply(3));
		data.impactMatrix = JavaMatrix.of(new double[][]{
				{1.0, 0.0},
				{0.0, -1.0},
				{2.0, -0.5},
		});

		// write the matrix data as library and create a
		// foreground system
		var libRoot = Files.createTempDirectory("_olca_lib").toFile();
		libDir = LibraryDir.of(libRoot);
		new LibraryExport(db, new File(libRoot, libID))
				.withData(data)
				.run();
		var lib = libDir.getLibrary(libID).orElseThrow();

		var foreground = new MatrixData();
		foreground.techIndex = new TechIndex(data.techIndex.at(0));
		foreground.demand = Demand.of(data.techIndex.at(0), 0.5);
		foreground.techMatrix = JavaMatrix.of(new double[][]{{1}});
		foreground.impactIndex = data.impactIndex;

		var context = SolverContext.of(data);
		var libContext = SolverContext.of(db, foreground)
				.withSolver(new NativeSolver())
				.withLibraries(LibReaderRegistry.of(db, lib));

		return List.of(
				InversionResult.of(context).calculate().provider(),
				FactorizationSolver.solve(context),
				LazyLibrarySolver.solve(libContext),
				LibraryInversionSolver.solve(libContext),
				InMemLibrarySolver.solve(libContext)
		);
	}

	@AfterClass
	public static void tearDown() {
		Tests.getDb().clear();
		Dirs.delete(libDir.folder());
	}

	@Test
	public void testIndices() {
		assertTrue(provider.hasFlows());
		assertTrue(provider.hasImpacts());
		assertEquals(2, provider.techIndex().size());
		assertEquals(2, provider.enviIndex().size());
		assertEquals(3, provider.impactIndex().size());
	}

	@Test
	public void testScalingVector() {
		assertArrayEquals(
				d(1, 1), provider.scalingVector(), 1e-10);
	}

	@Test
	public void testScalingFactorOf() {
		assertEquals(1, provider.scalingFactorOf(0), 1e-10);
		assertEquals(1, provider.scalingFactorOf(1), 1e-10);
	}

	@Test
	public void testTotalRequirements() {
		assertArrayEquals(
				d(1, 1), provider.totalRequirements(), 1e-10);
	}

	@Test
	public void testTotalRequirementsOf() {
		assertEquals(1.0, provider.totalRequirementsOf(0), 1e-10);
		assertEquals(1.0, provider.totalRequirementsOf(1), 1e-10);
	}

	@Test
	public void testTechColumnOf() {
		assertArrayEquals(d(1.0, -1.0), provider.techColumnOf(0), 1e-10);
		assertArrayEquals(d(-0.5, 1.0), provider.techColumnOf(1), 1e-10);
	}

	@Test
	public void testTechValueOf() {
		double[][] expected = {
				{1.0, -0.5},
				{-1.0, 1.0},
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
				{1.0, -0.5},
				{-1.0, 1.0},
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
		assertArrayEquals(d(2.0, 2.0), provider.solutionOfOne(0), 1e-10);
		assertArrayEquals(d(1.0, 2.0), provider.solutionOfOne(1), 1e-10);
	}

	@Test
	public void testLoopFactorOf() {
		assertEquals(0.5, provider.loopFactorOf(0), 1e-10);
		assertEquals(0.5, provider.loopFactorOf(1), 1e-10);
	}

	@Test
	public void testTotalFactorOf() {
		assertEquals(0.5, provider.totalFactorOf(0), 1e-10);
		assertEquals(0.5, provider.totalFactorOf(1), 1e-10);
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
		assertArrayEquals(d(1, -3.0), provider.directFlowsOf(0), 1e-10);
		assertArrayEquals(d(2, -3.0), provider.directFlowsOf(1), 1e-10);
	}

	@Test
	public void testDirectFlowOf() {
		double[][] expected = {
				{1.0, 2.0},
				{-3.0, -3.0}
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
		assertArrayEquals(d(6, -12), provider.totalFlowsOfOne(0), 1e-10);
		assertArrayEquals(d(5, -9), provider.totalFlowsOfOne(1), 1e-10);
	}

	@Test
	public void testTotalFlowOfOne() {
		double[][] expected = {
				{6, 5},
				{-12, -9}
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
		assertArrayEquals(d(3, -6), provider.totalFlowsOf(0), 1e-10);
		assertArrayEquals(d(2.5, -4.5), provider.totalFlowsOf(1), 1e-10);
	}

	@Test
	public void totalFlowOf() {
		double[][] expected = {
				{3, 2.5},
				{-6, -4.5}
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
				d(3, -6), provider.totalFlows(), 1e-10);
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

	@Test
	public void testFlowImpactsOf() {
		assertArrayEquals(
				d(3, 0, 6), provider.flowImpactsOf(0), 1e-10);
		assertArrayEquals(
				d(0, 6, 3), provider.flowImpactsOf(1), 1e-10);
	}

	@Test
	public void testFlowImpactOf() {
		double[][] expected = new double[][]{
				{3.0, 0.0},
				{0.0, 6.0},
				{6.0, 3.0},
		};
		for (int impact = 0; impact < expected.length; impact++) {
			for (int flow = 0; flow < expected[impact].length; flow++) {
				assertEquals(
						expected[impact][flow],
						provider.flowImpactOf(impact, flow),
						1e-10);
			}
		}
	}

	@Test
	public void testDirectImpactsOf() {
		assertArrayEquals(
				d(1, 3, 3.5), provider.directImpactsOf(0), 1e-10);
		assertArrayEquals(
				d(2, 3, 5.5), provider.directImpactsOf(1), 1e-10);
	}

	@Test
	public void testDirectImpactOf() {
		double[][] expected = new double[][]{
				{1.0, 2.0},
				{3.0, 3.0},
				{3.5, 5.5},
		};
		for (int impact = 0; impact < expected.length; impact++) {
			for (int product = 0; product < expected[impact].length; product++) {
				assertEquals(
						expected[impact][product],
						provider.directImpactOf(impact, product),
						1e-10);
			}
		}
	}

	@Test
	public void testTotalImpactsOfOne() {
		assertArrayEquals(
				d(6, 12, 18), provider.totalImpactsOfOne(0), 1e-10);
		assertArrayEquals(
				d(5, 9, 14.5), provider.totalImpactsOfOne(1), 1e-10);
	}

	@Test
	public void testTotalImpactOfOne() {
		double[][] expected = new double[][]{
				{6.0, 5.0},
				{12.0, 9.0},
				{18.0, 14.5},
		};
		for (int impact = 0; impact < expected.length; impact++) {
			for (int product = 0; product < expected[impact].length; product++) {
				assertEquals(
						expected[impact][product],
						provider.totalImpactOfOne(impact, product),
						1e-10);
			}
		}
	}

	@Test
	public void testTotalImpactsOf() {
		assertArrayEquals(
				d(3, 6, 9), provider.totalImpactsOf(0), 1e-10);
		assertArrayEquals(
				d(2.5, 4.5, 7.25), provider.totalImpactsOf(1), 1e-10);
	}

	@Test
	public void testTotalImpactOf() {
		double[][] expected = new double[][]{
				{3.0, 2.5},
				{6.0, 4.5},
				{9.0, 7.25},
		};
		for (int impact = 0; impact < expected.length; impact++) {
			for (int product = 0; product < expected[impact].length; product++) {
				assertEquals(
						expected[impact][product],
						provider.totalImpactOf(impact, product),
						1e-10);
			}
		}
	}

	@Test
	public void testTotalImpacts() {
		assertArrayEquals(
				d(3, 6, 9), provider.totalImpacts(), 1e-10);
	}

	private double[] d(double... values) {
		return values;
	}
}
