package org.openlca.eigen;

import org.junit.Assert;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.openlca.core.math.IMatrix;
import org.openlca.core.math.IMatrixFactory;
import org.openlca.core.math.IMatrixSolver;
import org.openlca.core.math.JavaSolver;
import org.openlca.core.math.LcaCalculator;
import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.matrix.InventoryMatrix;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.matrix.TechIndex;
import org.openlca.core.results.SimpleResult;
import org.openlca.eigen.solvers.BalancedSolver;
import org.openlca.eigen.solvers.DenseSolver;
import org.openlca.util.MatrixUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Theories.class)
public class SolverTest {

	static {
		TestSession.loadLib();
	}

	private Logger log = LoggerFactory.getLogger(SolverTest.class);

	@DataPoint
	public static IMatrixSolver denseSolver = new DenseSolver();

	@DataPoint
	public static IMatrixSolver balancedSolver = new BalancedSolver();

	@DataPoint
	public static IMatrixSolver javaSolver = new JavaSolver();

	@Theory
	public void testSimpleSolve(IMatrixSolver solver) {
		log.info("Test simple solve with {}", solver.getClass());
		IMatrixFactory<?> factory = solver.getMatrixFactory();
		IMatrix a = factory.create(2, 2);
		a.setEntry(0, 0, 1);
		a.setEntry(1, 0, -5);
		a.setEntry(1, 1, 4);
		double[] x = solver.solve(a, 0, 1);
		Assert.assertArrayEquals(new double[] { 1, 1.25 }, x, 1e-14);
	}

	@Theory
	public void testSolve1x1System(IMatrixSolver solver) {
		log.info("Test solve 1x1 matrix with {}", solver.getClass());
		IMatrixFactory<?> factory = solver.getMatrixFactory();

		InventoryMatrix matrix = new InventoryMatrix();

		TechIndex productIndex = new TechIndex(LongPair.of(1, 1));
		productIndex.put(LongPair.of(1, 1));
		productIndex.setDemand(1d);
		matrix.productIndex = productIndex;

		FlowIndex flowIndex = new FlowIndex();
		flowIndex.putInputFlow(1);
		flowIndex.putInputFlow(2);
		flowIndex.putOutputFlow(3);
		flowIndex.putOutputFlow(4);
		matrix.flowIndex = flowIndex;

		IMatrix techMatrix = factory.create(1, 1);
		techMatrix.setEntry(0, 0, 1);
		matrix.technologyMatrix = techMatrix;

		IMatrix enviMatrix = factory.create(4, 1);
		for (int r = 0; r < 4; r++)
			enviMatrix.setEntry(r, 0, 1 * r);
		matrix.interventionMatrix = enviMatrix;

		LcaCalculator calculator = new LcaCalculator(solver, matrix);
		SimpleResult result = calculator.calculateSimple();
		Assert.assertArrayEquals(new double[] { 0, 1, 2, 3 },
				result.totalFlowResults, 1e-14);
	}

	@Theory
	public void testSimpleMult(IMatrixSolver solver) {
		IMatrixFactory<?> factory = solver.getMatrixFactory();
		log.info("Test simple multiplication with {}", solver.getClass());
		double[][] aData = { { 1, 2, 3 }, { 4, 5, 6 } };
		IMatrix a = MatrixUtils.create(aData, factory);
		double[][] bData = { { 7, 10 }, { 8, 11 }, { 9, 12 } };
		IMatrix b = MatrixUtils.create(bData, factory);
		IMatrix c = solver.multiply(a, b);
		Assert.assertArrayEquals(new double[] { 50, 122 }, c.getColumn(0),
				1e-14);
		Assert.assertArrayEquals(new double[] { 68, 167 }, c.getColumn(1),
				1e-14);
	}

}
