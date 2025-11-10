package org.openlca.core.matrix.solvers.mkl;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

import org.openlca.core.DataDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MKL {

	/**
	 * The version of this JNI interface. This version and the version
	 * returned by the native library have to match exactly.
	 */
	public static final int VERSION = 1;

	private static final AtomicBoolean _loaded = new AtomicBoolean(false);

	/**
	 * The JNI version returned from the native library.
	 */
	public static native int version();

	/**
	 * Calculates {@code y := A * x}.
	 *
	 * @param m the number of rows of matrix A.
	 * @param n the number of columns of matrix A.
	 * @param a the matrix A in column-major order.
	 * @param x the vector x of size n.
	 * @param y the vector y of size m.
	 */
	public static native void denseMatrixVectorMul(
		int m, int n, double[] a, double[] x, double[] y
	);

	/**
	 * Calculates {@code C := A * B}.
	 *
	 * @param m the number of rows of matrix A.
	 * @param n the number of columns of matrix B.
	 * @param k the number of rows (columns) of matrix A (B).
	 * @param a the matrix A.
	 * @param b the matrix B.
	 * @param c the matrix C.
	 */
	public static native void denseMatrixMul(
		int m, int n, int k, double[] a, double[] b, double[] c
	);

	/**
	 * Solves x in {@code A * x = b} where A is provided in CSC format.
	 *
	 * @param n  the number of rows and columns of A.
	 * @param a  the non-zero values of A.
	 * @param ia the row indices of the non-zero values of A.
	 * @param ja the column pointers of A.
	 * @param b  the right-hand side vector of size n.
	 * @param x  the solution vector of size n.
	 * @return a possible error code or 0 if no error occurred.
	 */
	public static native int solveSparse(
		int n, double[] a, int[] ia, int[] ja, double[] b, double[] x
	);

	public static native int sparseFactorization(
		int n, double[] a, int[] ia, int[] ja, long[] ptr
	);

	public static native int solveSparseFactorization(
		long ptr, double[] b, double[] x
	);

	public static native void disposeSparseFactorization(long ptr);

	public static native int denseFactorization(
		int n, double[] a, long[] ptr
	);

	public static native int solveDenseFactorization(
		long ptr, int nrhs, double[] b
	);

	public static native void disposeDenseFactorization(long ptr);

	/**
	 * Solves x in {@code A * x = b}. Note that this method mutates
	 * the parameter A: on exit it will contain the LU-factorization
	 * of the matrix A.
	 *
	 * @param n    the number of rows and columns of A.
	 * @param nrhs the number of columns of x and b.
	 * @param a    on entry, the matrix A, on exit the factorization of A.
	 * @param b    on entry, the right-hand side, on exit the solution x.
	 */
	public static native int solveDense(
		int n, int nrhs, double[] a, double[] b
	);

	/**
	 * Inverts a matrix A in place.
	 *
	 * @param n the number of rows and columns of A.
	 * @param a on entry the matrix A, on exit the inverse of A.
	 * @return 0 on success or an error code otherwise.
	 */
	public static native int invertDense(int n, double[] a);

	public static boolean isLoaded() {
		return _loaded.get();
	}

	/**
	 * Returns {@code true} if the default openLCA workspace is
	 * a directory with MKL library folder.
	 */
	public static boolean isDefaultLibraryDir() {
		return isLibraryDir(DataDir.get().root());
	}

	/**
	 * Returns {@code true} if the given directory contains the
	 * MKL library folder for the current platform.
	 */
	public static boolean isLibraryDir(File root) {
		if (root == null || !root.isDirectory())
			return false;
		var dirName = "olca-mkl-" + arch() + "_v" + VERSION;
		var libDir = new File(root, dirName);
		if (!libDir.isDirectory())
			return false;
		for (var lib : OS.detect().libraries()) {
			var dll = new File(libDir, lib);
			if (!dll.exists())
				return false;
		}
		return true;
	}

	/**
	 * Tries to load the native libraries from the default
	 * openLCA workspace location.
	 */
	public static boolean loadFromDefault() {
		return loadFrom(DataDir.get().root());
	}

	/**
	 * Tries to load the libraries from the olca-mkl specific
	 * sub-folder of the given directory. The name of the
	 * sub-folder has the following pattern:
	 * {@code olca-mkl-[arch]_v[version]}.
	 */
	public static boolean loadFrom(File root) {
		if (_loaded.get())
			return true;
		if (root == null || !root.exists())
			return false;

		var dirName = "olca-mkl-" + arch() + "_v" + VERSION;
		var libDir = new File(root, dirName);
		if (!libDir.isDirectory())
			return false;

		synchronized (_loaded) {
			if (_loaded.get())
				return true;
			var log = LoggerFactory.getLogger(MKL.class);
			log.debug("try to load MKL libraries from {}", libDir);
			try {

				// load the DLLs
				var libs = OS.detect().libraries();
				for (var lib : libs) {
					var dll = new File(libDir, lib);
					if (!tryLoad(dll, log))
						return false;
				}

				// check that the version matches
				int v = MKL.version();
				if (v != VERSION) {
					log.warn(
						"loaded MKL libraries from {} but versions " +
							"do not match: Java = {}, native = {}",
						libDir, VERSION, v);
					return false;
				}

				_loaded.set(true);
				log.info("loaded MKL libraries v{} from {}", v, libDir);
				return true;
			} catch (Throwable e) {
				log.error("failed to load MKL libraries from " + libDir, e);
				_loaded.set(false);
				return false;
			}
		}
	}

	private static boolean tryLoad(File dll, Logger log) {
		if (!dll.exists()) {
			log.warn("DLL {} missing; could not load MKL libraries", dll);
			return false;
		}
		try {
			System.load(dll.getAbsolutePath());
			log.info("loaded MKL DLL {}", dll);
			return true;
		} catch (Throwable e) {
			log.error("failed to load MKL DLL " + dll, e);
			return false;
		}
	}

	private static String arch() {
		var arch = System.getProperty("os.arch");
		if (arch == null)
			return "x64";
		var lower = arch.trim().toLowerCase();
		return lower.startsWith("aarch") || lower.startsWith("arm")
			? "arm64"
			: "x64";
	}
}
