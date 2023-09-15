package examples;

import org.openlca.core.DataDir;
import org.openlca.core.library.LibMatrix;
import org.openlca.core.matrix.format.CSCMatrix;
import org.openlca.core.matrix.solvers.mkl.MKL;
import org.openlca.julia.Julia;
import org.openlca.nativelib.NativeLib;

import java.io.File;

public class ThinLibSolvingExample {

	public static void main(String[] args) {
		MKL.loadFromDefault();
		NativeLib.loadFrom(DataDir.get().root());

		var m = DataDir.get()
			.getLibraryDir()
			.getLibrary("ei39")
			.orElseThrow()
			.getMatrix(LibMatrix.A)
			.orElseThrow();
		var csc = CSCMatrix.of(m);

		System.out.println("\nUMFPACK:");
		testUmfpack(csc);

		System.out.println("\nMKL:");
		testMkl(csc);
	}

	private static void testUmfpack(CSCMatrix csc) {
		var n = csc.rows();
		var b = new double[n];
		b[0] = 1;
		var x = new double[n];

		System.out.println("iteration\ttime [ms]");
		for (int i = 1; i <= 20; i++) {
			long start = System.nanoTime();
			Julia.umfSolve(
				n,
				csc.columnPointers,
				csc.rowIndices,
				csc.values,
				b,
				x);
			double time = ((double) (System.nanoTime() - start)) / 1e6;
			System.out.printf("%d\t%.0f%n", i, time);
		}
	}

	private static void testMkl(CSCMatrix csc) {
		var n = csc.rows();
		var b = new double[n];
		b[0] = 1;
		var x = new double[n];

		System.out.println("iteration\ttime [ms]");
		for (int i = 1; i <= 20; i++) {
			long start = System.nanoTime();
			MKL.solveSparse(
				n,
				csc.values,
				csc.rowIndices,
				csc.columnPointers,
				b,
				x);
			double time = ((double) (System.nanoTime() - start)) / 1e6;
			System.out.printf("%d\t%.0f%n", i, time);
		}
	}
}
