package org.openlca.core.matrix.solvers;

import static org.junit.Assert.*;

import org.apache.commons.math3.linear.NonSquareMatrixException;
import org.apache.commons.math3.linear.SingularMatrixException;
import org.junit.Ignore;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.math.SystemCalculator;
import org.openlca.core.matrix.format.DenseMatrix;
import org.openlca.core.matrix.format.JavaMatrix;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.UnitGroup;
import org.openlca.julia.Julia;

public class MatrixExceptionTest {

	@Test(expected = SingularMatrixException.class)
	public void testJavaSolveSingular() {
		var matrix = JavaMatrix.of(new double[][]{
			{1.0, -2.0},
			{-1.0, 2.0},
		});
		new JavaSolver().solve(matrix, 1, 1.0);
	}

	@Test(expected = NonSquareMatrixException.class)
	public void testJavaSolveNonSquare() {
		var matrix = JavaMatrix.of(new double[][]{
			{1.0, 0.0},
			{-1.0, 2.0},
			{-1.0, 0.0},
		});
		new JavaSolver().solve(matrix, 1, 1.0);
	}

	@Test(expected = NonSquareMatrixException.class)
	public void testNativeSolveNonSquare() {
		var matrix = DenseMatrix.of(new double[][]{
			{1.0, 0.0},
			{-1.0, 2.0},
			{-1.0, 0.0},
		});
		new JuliaSolver().solve(matrix, 1, 1.0);
	}

	@Test(expected = SingularMatrixException.class)
	public void testBlasSolveSingular() {
		assertTrue(Julia.load());
		var matrix = DenseMatrix.of(new double[][]{
			{1.0, -2.0},
			{-1.0, 2.0},
		});
		new JuliaSolver().solve(matrix, 1, 1.0);
	}

	@Test(expected = SingularMatrixException.class)
	public void testBlasInvertSingular() {
		assertTrue(Julia.load());
		var matrix = DenseMatrix.of(new double[][]{
			{1.0, -2.0},
			{-1.0, 2.0},
		});
		new JuliaSolver().invert(matrix);
	}

	// TODO: singularity checks are currently not supported for dense factorizations
	@Ignore
	@Test(expected = SingularMatrixException.class)
	public void testBlasFactorizeSingular() {
		assertTrue(Julia.load());
		var matrix = DenseMatrix.of(new double[][]{
			{1.0, -2.0},
			{-1.0, 2.0},
		});
		new JuliaSolver().factorize(matrix);
	}

	@Test
	public void testSingularSystem() {
		var db = Tests.getDb();
		var units = db.insert(UnitGroup.of("Mass units", "kg"));
		var mass = db.insert(FlowProperty.of("Mass", units));
		var p = db.insert(Flow.product("p", mass));
		var q = db.insert(Flow.product("q", mass));

		var pp = Process.of("pp", p);
		pp.input(q, 1);
		db.insert(pp);

		var qq = Process.of("qq", q);
		qq.input(p, 1);
		db.insert(qq);

		var sys = ProductSystem.of(pp);
		sys.link(qq, pp);
		sys.link(pp, qq);
		db.insert(sys);

		boolean caughtIt = false;
		assertTrue(Julia.load());
		try {
			var setup = CalculationSetup.simple(sys);
			new SystemCalculator(db).calculateSimple(setup);
		} catch (SingularMatrixException e) {
			caughtIt = true;
		}
		assertTrue(caughtIt);

		db.delete(sys, pp, qq, p, q, mass, units);
	}
}
