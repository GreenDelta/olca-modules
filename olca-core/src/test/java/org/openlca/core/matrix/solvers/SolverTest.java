package org.openlca.core.matrix.solvers;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.openlca.core.DataDir;
import org.openlca.core.Tests;
import org.openlca.core.matrix.Demand;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.format.Matrix;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.matrix.index.EnviIndex;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.results.SimpleResult;
import org.openlca.nativelib.NativeLib;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
@RunWith(Theories.class)
public class SolverTest {

	@BeforeClass
	public static void setup() {
		NativeLib.loadFrom(DataDir.get().root());
	}

	private final Logger log = LoggerFactory.getLogger(SolverTest.class);

	@DataPoint
	public static MatrixSolver denseSolver = new NativeSolver();

	@DataPoint
	public static MatrixSolver javaSolver = new JavaSolver();

	@Theory
	public void testSimpleSolve(MatrixSolver solver) {
		log.info("Test simple solve with {}", solver.getClass());
		Matrix a = solver.matrix(2, 2);
		a.set(0, 0, 1);
		a.set(1, 0, -5);
		a.set(1, 1, 4);
		double[] x = solver.solve(a, 0, 1);
		Assert.assertArrayEquals(new double[] { 1, 1.25 }, x, 1e-14);
	}

	@Theory
	public void testSolve1x1System(MatrixSolver solver) {
		log.info("Test solve 1x1 matrix with {}", solver.getClass());

		var flow = new FlowDescriptor();
		flow.id = 1;
		var process = new ProcessDescriptor();
		process.id = 1;
		var refFlow = TechFlow.of(process, flow);
		
		var data = new MatrixData();
		data.demand = new Demand(refFlow, 1);
		data.techIndex = new TechIndex(refFlow);

		var enviIndex = EnviIndex.create();
		enviIndex.add(EnviFlow.inputOf(flow(1)));
		enviIndex.add(EnviFlow.inputOf(flow(2)));
		enviIndex.add(EnviFlow.outputOf(flow(3)));
		enviIndex.add(EnviFlow.outputOf(flow(4)));
		data.enviIndex = enviIndex;

		var techMatrix = solver.matrix(1, 1);
		techMatrix.set(0, 0, 1);
		data.techMatrix = techMatrix;

		var enviMatrix = solver.matrix(4, 1);
		for (int r = 0; r < 4; r++) {
			enviMatrix.set(r, 0, r);
		}
		data.enviMatrix = enviMatrix;

		var result = SimpleResult.of(Tests.getDb(), data);
		Assert.assertArrayEquals(new double[] { 0, 1, 2, 3 },
				result.totalFlowResults(), 1e-14);
	}

	@Theory
	public void testSimpleMult(MatrixSolver solver) {
		log.info("Test simple multiplication with {}", solver.getClass());
		Matrix a = solver.matrix(2, 3);
		a.setValues(new double[][] {
				{ 1, 2, 3 },
				{ 4, 5, 6 }
		});
		Matrix b = solver.matrix(3, 2);
		b.setValues(new double[][] {
				{ 7, 10 },
				{ 8, 11 },
				{ 9, 12 }
		});
		Matrix c = solver.multiply(a, b);
		Assert.assertArrayEquals(new double[] { 50, 122 },
				c.getColumn(0), 1e-14);
		Assert.assertArrayEquals(new double[] { 68, 167 },
				c.getColumn(1), 1e-14);
	}

	private FlowDescriptor flow(int id) {
		FlowDescriptor flow = new FlowDescriptor();
		flow.id = id;
		return flow;
	}

}
