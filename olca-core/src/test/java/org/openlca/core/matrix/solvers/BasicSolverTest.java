package org.openlca.core.matrix.solvers;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openlca.core.DataDir;
import org.openlca.core.matrix.solvers.mkl.MKL;
import org.openlca.core.matrix.solvers.mkl.MKLSolver;
import org.openlca.nativelib.NativeLib;

public class BasicSolverTest {

	private static final List<MatrixSolver> solvers = new ArrayList<>();

	@BeforeClass
	public static void setup() {
		solvers.add(new JavaSolver());
		var dataDir = DataDir.get().root();

		// the OpenBLAS based solvers
		if (!NativeLib.isLoaded() && NativeLib.isLibraryDir(dataDir)) {
			NativeLib.loadFrom(dataDir);
		}
		if (NativeLib.isLoaded()) {
			solvers.add(new NativeSolver());
		}

		// the MKL based solver
		if (!MKL.isLoaded() && MKL.isLibraryDir(dataDir)) {
			MKL.loadFrom(dataDir);
		}
		if (MKL.isLoaded()) {
			solvers.add(new MKLSolver());
		}
	}

	@Test
	public void testSolversLoaded() {
		assertFalse(solvers.isEmpty());
	}

	@Test
	public void testDot() {
		var v = new double[] {1, 2, 3};
		for (var s : solvers) {
			assertEquals(14.0, s.dot(v, v), 1e-16);
		}
	}

}
