package org.openlca.julia;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

import org.openlca.util.OS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides the Java interface for the native Julia libraries and contains some
 * utility methods for loading these libraries.
 */
public final class Julia {

	private enum LinkOption {
		NONE, BLAS, ALL
	}

	private static final AtomicBoolean _loaded = new AtomicBoolean(false);
	private static final AtomicBoolean _withUmfpack = new AtomicBoolean(false);

	/** Returns true if the Julia libraries with openLCA bindings are loaded. */
	public static boolean isLoaded() {
		return _loaded.get();
	}

	public static boolean isWithUmfpack() {
		return _withUmfpack.get();
	}

	/**
	 * Loads the libraries from a folder specified by the "OLCA_JULIA" environment
	 * variable.
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
	 * Loads the Julia libraries and openLCA bindings from the given folder. Returns
	 * true if the libraries could be loaded (at least there should be a `libjolca`
	 * library in the folder that could be loaded).
	 */
	public static boolean loadFromDir(File dir) {
		Logger log = LoggerFactory.getLogger(Julia.class);
		log.info("Try to load Julia libs and bindings from {}", dir);
		if (_loaded.get()) {
			log.info("Julia libs already loaded; do nothing");
			return true;
		}
		if (dir == null || !dir.exists() || !dir.isDirectory()) {
			log.warn("{} does not contain the Julia libraries", dir);
			return false;
		}
		synchronized (_loaded) {
			if (_loaded.get())
				return true;
			try {
				LinkOption opt = linkOption(dir);
				if (opt == null || opt == LinkOption.NONE) {
					log.info("No native libraries found");
					return false;
				}
				for (String lib : libs(opt)) {
					File f = new File(dir, lib);
					System.load(f.getAbsolutePath());
					log.info("loaded native library {}", f);
				}
				_loaded.set(true);
				if (opt == LinkOption.ALL) {
					_withUmfpack.set(true);
					log.info("Math libraries loaded with UMFPACK support.");
				} else {
					log.info("Math libraries loaded without UMFPACK support.");
				}
				return true;
			} catch (Error e) {
				log.error("Failed to load Julia libs from " + dir, e);
				return false;
			}
		}
	}

	private static String[] libs(LinkOption opt) {
		if (opt == null || opt == LinkOption.NONE)
			return null;

		OS os = OS.get();

		if (os == OS.WINDOWS) {
			if (opt == LinkOption.ALL) {
				return new String[] {
						"libsuitesparseconfig.dll",
						"libamd.dll",
						"libcamd.dll",
						"libccolamd.dll",
						"libcolamd.dll",
						"libwinpthread-1.dll",
						"libgcc_s_seh-1.dll",
						"libquadmath-0.dll",
						"libgfortran-4.dll",
						"libopenblas64_.dll",
						"libcholmod.dll",
						"libumfpack.dll",
						"olcar_withumf.dll",
				};
			} else {
				return new String[] {
						"libwinpthread-1.dll",
						"libgcc_s_seh-1.dll",
						"libquadmath-0.dll",
						"libgfortran-4.dll",
						"libopenblas64_.dll",
						"olcar.dll"
				};
			}
		}

		if (os == OS.LINUX) {
			if (opt == LinkOption.ALL) {
				return new String[] {
						"libgcc_s.so.1",
						"libsuitesparseconfig.so.5",
						"libccolamd.so.2",
						"libamd.so.2",
						"libcamd.so.2",
						"libcolamd.so.2",
						"libquadmath.so.0",
						"libgfortran.so.4",
						"libopenblas64_.so.0",
						"libcholmod.so.3",
						"libumfpack.so.5",
						"libolcar_withumf.so",
				};
			} else {
				return new String[] {
						"libgcc_s.so.1",
						"libquadmath.so.0",
						"libgfortran.so.4",
						"libopenblas64_.so.0",
						"libolcar.so",
				};
			}
		}

		if (os == OS.MAC) {
			if (opt == LinkOption.ALL) {
				return new String[] {
						"libgcc_s.1.dylib",
						"libquadmath.0.dylib",
						"libgfortran.5.dylib",
						"libopenblas64_.0.3.5.dylib",
						"libsuitesparseconfig.5.4.0.dylib",
						"libamd.2.4.6.dylib",
						"libccolamd.2.9.6.dylib",
						"libcamd.2.4.6.dylib",
						"libcolamd.2.9.6.dylib",
						"libcholmod.3.0.13.dylib",
						"libumfpack.5.7.8.dylib",
						"libolcar_withumf.dylib",
				};
			} else {
				return new String[] {
						"libgcc_s.1.dylib",
						"libquadmath.0.dylib",
						"libgfortran.5.dylib",
						"libopenblas64_.0.3.5.dylib",
						"libolcar.dylib",
				};
			}
		}
		return null;
	}

	/**
	 * Searches for the library which can be linked. When there are multiple link
	 * options it chooses the one with more functions.
	 */
	private static LinkOption linkOption(File dir) {
		if (dir == null || !dir.exists())
			return LinkOption.NONE;
		LinkOption opt = LinkOption.NONE;
		for (File f : dir.listFiles()) {
			if (!f.isFile())
				continue;
			if (f.getName().contains("olcar_withumf")) {
				return LinkOption.ALL;
			}
			if (f.getName().contains("olcar")) {
				opt = LinkOption.BLAS;
				continue;
			}
		}
		return opt;
	}

	// BLAS

	/**
	 * Matrix-matrix multiplication: C := A * B
	 *
	 * @param rowsA [in] number of rows of matrix A
	 * @param colsB [in] number of columns of matrix B
	 * @param k     [in] number of columns of matrix A and number of rows of matrix
	 *              B
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
	 * Solves a system of linear equations A * X = B for general matrices. It calls
	 * the LAPACK DGESV routine.
	 *
	 * @param n    [in] the dimension of the matrix A (n = rows = columns of A)
	 * @param nrhs [in] the number of columns of the matrix B
	 * @param a    [io] on entry the matrix A, on exit the LU factorization of A
	 *             (size = n * n)
	 * @param b    [io] on entry the matrix B, on exit the solution of the equation
	 *             (size = n * bColums)
	 * @return the LAPACK return code
	 */
	public static native int solve(int n, int nrhs, double[] a, double[] b);

	/**
	 * Inverts the given matrix.
	 *
	 * @param n [in] the dimension of the matrix (n = rows = columns)
	 * @param a [io] on entry: the matrix to be inverted, on exit: the inverse (size
	 *          = n * n)
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
