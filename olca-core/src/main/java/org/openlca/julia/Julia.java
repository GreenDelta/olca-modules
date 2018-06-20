package org.openlca.julia;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides the Java interface for the native Julia libraries and contains some
 * utility methods for loading these libraries.
 */
public final class Julia {

	private static AtomicBoolean _loaded = new AtomicBoolean(false);
	private static Set<JuliaModule> loadedModules = Collections
			.synchronizedSet(new HashSet<JuliaModule>());

	/** Returns true if the Julia libraries with openLCA bindings are loaded. */
	public static boolean isLoaded() {
		return _loaded.get();
	}

	/** Returns true if the given julia module is loaded and can be used. */
	public static boolean isLoaded(JuliaModule module) {
		return loadedModules.contains(module);
	}

	/**
	 * Loads the libraries from a folder specified by the "OLCA_JULIA"
	 * environment variable.
	 */
	public static boolean load() {
		if (isLoaded())
			return true;
		Logger log = LoggerFactory.getLogger(Julia.class);
		String path = System.getenv("OLCA_JULIA");
		if (path == null || path.isEmpty()) {
			log.warn("Could not load Julia libs and bindings;"
					+ "OLCA_JULIA is not defined");
			return false;
		}
		File dir = new File(path);
		return loadFromDir(dir);
	}

	/**
	 * Loads the Julia libraries and openLCA bindings from the given folder.
	 * Returns true if the libraries could be loaded (at least there should be a
	 * `libjolca` library in the folder that could be loaded).
	 */
	public static boolean loadFromDir(File dir) {
		Logger log = LoggerFactory.getLogger(Julia.class);
		log.info("Try to load Julia libs and bindings from {}", dir);
		if (isLoaded()) {
			log.info("Julia libs already loaded; do nothing");
			return true;
		}
		if (dir == null || !dir.exists() || !dir.isDirectory()) {
			log.warn("{} does not contain the Julia libraries", dir);
			return false;
		}
		try {
			for (JuliaModule module : JuliaModule.values()) {
				File file = new File(dir, module.libName());
				if (!file.exists()) {
					log.info("Library {} is missing; " +
							"Julia bindings for {} not loaded", file, module);
					continue;
				}
				log.info("load module {} with library {}", module, file);
				System.load(file.getAbsolutePath());
				loadedModules.add(module);
			}
			_loaded.set(true);
			log.info("Julia bindings loaded");
			return true;
		} catch (Error e) {
			log.error("Failed to load Julia libs from " + dir, e);
			return false;
		}
	}

	// BLAS

	/**
	 * Matrix-matrix multiplication: C := A * B
	 *
	 * @param rowsA [in] number of rows of matrix A
	 * @param colsB [in] number of columns of matrix B
	 * @param k     [in] number of columns of matrix A and number of rows of
	 *              matrix B
	 * @param a     [in] matrix A (size = rowsA*k)
	 * @param b     [in] matrix B (size = k * colsB)
	 * @param c     [out] matrix C (size = rowsA * colsB)
	 */
	public static native void mmult(int rowsA, int colsB, int k,
			double[] a, double[] b, double[] c);

	/**
	 * Matrix-vector multiplication: y:= A * x
	 *
	 * @param rowsA [in] rows of matrix A
	 * @param colsA [in] columns of matrix A
	 * @param a     [in] the matrix A
	 * @param x     [in] the vector x
	 * @param y     [out] the resulting vector y
	 */
	public static native void mvmult(int rowsA, int colsA,
			double[] a, double[] x, double[] y);

	// LAPACK

	/**
	 * Solves a system of linear equations A * X = B for general matrices. It
	 * calls the LAPACK DGESV routine.
	 *
	 * @param n    [in] the dimension of the matrix A (n = rows = columns of A)
	 * @param nrhs [in] the number of columns of the matrix B
	 * @param a    [io] on entry the matrix A, on exit the LU factorization of A
	 *             (size = n * n)
	 * @param b    [io] on entry the matrix B, on exit the solution of the
	 *             equation (size = n * bColums)
	 * @return the LAPACK return code
	 */
	public static native int solve(int n, int nrhs, double[] a, double[] b);

	/**
	 * Inverts the given matrix.
	 *
	 * @param n [in] the dimension of the matrix (n = rows = columns)
	 * @param a [io] on entry: the matrix to be inverted, on exit: the inverse
	 *          (size = n * n)
	 * @return the LAPACK return code
	 */
	public static native int invert(int n, double[] a);

	// UMFPACK
	public static native void umfSolve(
			int n,
			int[] columnPointers,
			int[] rowIndices,
			double[] values,
			double[] demand,
			double[] result);

	public static native long umfFactorize(
			int n,
			int[] columnPointers,
			int[] rowIndices,
			double[] values);

	public static native void umfDispose(long pointer);

	public static native long umfSolveFactorized(
			long pointer, double[] demand, double[] result);
}
