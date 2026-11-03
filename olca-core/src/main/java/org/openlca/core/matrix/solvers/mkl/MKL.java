package org.openlca.core.matrix.solvers.mkl;

import static java.lang.foreign.ValueLayout.JAVA_LONG;

import java.io.File;
import java.lang.foreign.MemorySegment;

import org.openlca.core.DataDir;
import org.slf4j.LoggerFactory;

/// Bindings to the Intel oneAPI Math Kernel Library (MKL) via the foreign
/// function and memory API. The MKL runtime library is loaded from the olca-mkl
/// specific sub-folder of a directory, see [#loadFrom(File)]. Note that the JVM
/// should be started with the `--enable-native-access=ALL-UNNAMED` option to
/// allow native access without warnings.
public final class MKL {

	/// The version of the library folder layout. It is part of the name of the
	/// folder from which the libraries are loaded.
	public static final int VERSION = 1;

	private static final Object lock = new Object();
	private static volatile MklLib lib;

	/// Calculates `y := A * x`.
	///
	/// @param m the number of rows of matrix A.
	/// @param n the number of columns of matrix A.
	/// @param a the matrix A in column-major order.
	/// @param x the vector x of size n.
	/// @param y the vector y of size m.
	public static void denseMatrixVectorMul(
		int m, int n, double[] a, double[] x, double[] y
	) {
		try (var call = new NativeCall()) {
			lib().dgemv.invokeExact(
				call.of('N'),  // TRANS
				call.of(m),    // M
				call.of(n),    // N
				call.of(1.0),  // ALPHA
				call.in(a),    // A
				call.of(m),    // LDA
				call.in(x),    // X
				call.of(1),    // INCX
				call.of(0.0),  // BETA
				call.out(y),   // Y
				call.of(1)     // INCY
			);
			call.writeBack();
		} catch (Throwable e) {
			throw callError("dgemv", e);
		}
	}

	/// Calculates `C := A * B`.
	///
	/// @param m the number of rows of matrix A.
	/// @param n the number of columns of matrix B.
	/// @param k the number of rows (columns) of matrix A (B).
	/// @param a the matrix A.
	/// @param b the matrix B.
	/// @param c the matrix C.
	public static void denseMatrixMul(
		int m, int n, int k, double[] a, double[] b, double[] c
	) {
		try (var call = new NativeCall()) {
			lib().dgemm.invokeExact(
				call.of('N'),  // TRANSA
				call.of('N'),  // TRANSB
				call.of(m),    // M
				call.of(n),    // N
				call.of(k),    // K
				call.of(1.0),  // ALPHA
				call.in(a),    // A
				call.of(m),    // LDA
				call.in(b),    // B
				call.of(k),    // LDB
				call.of(0.0),  // BETA
				call.out(c),   // C
				call.of(m)     // LDC
			);
			call.writeBack();
		} catch (Throwable e) {
			throw callError("dgemm", e);
		}
	}

	/// Solves x in `A * x = b` where A is provided in CSC format.
	///
	/// @param n the number of rows and columns of A.
	/// @param a the non-zero values of A.
	/// @param ia the row indices of the non-zero values of A.
	/// @param ja the column pointers of A.
	/// @param b the right-hand side vector of size n.
	/// @param x the solution vector of size n.
	/// @return a possible error code or 0 if no error occurred.
	public static int solveSparse(
		int n, double[] a, int[] ia, int[] ja, double[] b, double[] x
	) {
		try (var pardiso = Pardiso.of(n, a, ia, ja)) {
			return pardiso.factorizeAndSolve(b, x);
		}
	}

	/// Solves x in `A * x = b`. Note that this method mutates the parameter A: on
	/// exit it will contain the LU-factorization of the matrix A.
	///
	/// @param n the number of rows and columns of A.
	/// @param nrhs the number of columns of x and b.
	/// @param a on entry, the matrix A, on exit the factorization of A.
	/// @param b on entry, the right-hand side, on exit the solution x.
	/// @return 0 on success or an error code otherwise.
	public static int solveDense(int n, int nrhs, double[] a, double[] b) {
		try (var call = new NativeCall()) {
			var info = call.info();
			lib().dgesv.invokeExact(
				call.of(n),               // N
				call.of(nrhs),            // NRHS
				call.inOut(a),            // A
				call.of(n),               // LDA
				call.out(new long[n]),    // IPIV
				call.inOut(b),            // B
				call.of(n),               // LDB
				info                      // INFO
			);
			call.writeBack();
			return infoOf(info);
		} catch (Throwable e) {
			throw callError("dgesv", e);
		}
	}

	/// Inverts a matrix A in place.
	///
	/// @param n the number of rows and columns of A.
	/// @param a on entry the matrix A, on exit the inverse of A.
	/// @return 0 on success or an error code otherwise.
	public static int invertDense(int n, double[] a) {
		try (var call = new NativeCall()) {
			var aSeg = call.inOut(a);
			var pivots = call.out(new long[n]);
			var info = call.info();

			lib().dgetrf.invokeExact(
				call.of(n), // M
				call.of(n), // N
				aSeg,       // A
				call.of(n), // LDA
				pivots,     // IPIV
				info        // INFO
			);
			if (infoOf(info) != 0)
				return infoOf(info);

			long lwork = 64L * 2L * n;
			lib().dgetri.invokeExact(
				call.of(n),          // N
				aSeg,                // A
				call.of(n),          // LDA
				pivots,              // IPIV
				call.work(lwork),    // WORK
				call.of(lwork),      // LWORK
				info                 // INFO
			);
			call.writeBack();
			return infoOf(info);
		} catch (Throwable e) {
			throw callError("dgetrf/dgetri", e);
		}
	}

	/// Calculates the LU-factorization of the matrix A in place.
	///
	/// @param n the number of rows and columns of A.
	/// @param a on entry the matrix A, on exit its LU-factorization.
	/// @param pivots on exit, the pivot indices; an array of size n.
	/// @return 0 on success or an error code otherwise.
	static int luFactorization(int n, double[] a, long[] pivots) {
		try (var call = new NativeCall()) {
			var info = call.info();
			lib().dgetrf.invokeExact(
				call.of(n),        // M
				call.of(n),        // N
				call.inOut(a),     // A
				call.of(n),        // LDA
				call.out(pivots),  // IPIV
				info               // INFO
			);
			call.writeBack();
			return infoOf(info);
		} catch (Throwable e) {
			throw callError("dgetrf", e);
		}
	}

	/// Solves `A * X = B` with an LU-factorization of A, see
	/// [#luFactorization(int, double\[\], long\[\])].
	///
	/// @param n the number of rows and columns of A.
	/// @param nrhs the number of columns of B.
	/// @param lu the LU-factorization of A.
	/// @param pivots the pivot indices of the factorization.
	/// @param b on entry the matrix B, on exit the solution X.
	/// @return 0 on success or an error code otherwise.
	static int luSolve(
		int n, int nrhs, double[] lu, long[] pivots, double[] b
	) {
		try (var call = new NativeCall()) {
			var info = call.info();
			lib().dgetrs.invokeExact(
				call.of('N'),      // TRANS
				call.of(n),        // N
				call.of(nrhs),     // NRHS
				call.in(lu),       // A
				call.of(n),        // LDA
				call.in(pivots),   // IPIV
				call.inOut(b),     // B
				call.of(n),        // LDB
				info               // INFO
			);
			call.writeBack();
			return infoOf(info);
		} catch (Throwable e) {
			throw callError("dgetrs", e);
		}
	}

	public static boolean isLoaded() {
		return lib != null;
	}

	/// Returns the bound MKL functions or throws an exception if the library
	/// was not loaded yet.
	static MklLib lib() {
		var l = lib;
		if (l == null)
			throw new IllegalStateException("the MKL library is not loaded");
		return l;
	}

	/// Returns `true` if the default openLCA workspace is a directory with MKL
	/// library folder.
	public static boolean isDefaultLibraryDir() {
		return isLibraryDir(DataDir.get().root());
	}

	/// Returns `true` if the given directory contains the MKL library folder for
	/// the current platform.
	public static boolean isLibraryDir(File root) {
		var file = libraryFile(root);
		return file != null && file.exists();
	}

	/// Tries to load the native libraries from the default openLCA workspace
	/// location.
	public static boolean loadFromDefault() {
		return loadFrom(DataDir.get().root());
	}

	/// Tries to load the libraries from the olca-mkl specific sub-folder of the
	/// given directory. The name of the sub-folder has the following pattern:
	/// `olca-mkl-[arch]_v[version]`.
	public static boolean loadFrom(File root) {
		if (isLoaded())
			return true;
		var file = libraryFile(root);
		if (file == null)
			return false;

		synchronized (lock) {
			if (isLoaded())
				return true;
			var log = LoggerFactory.getLogger(MKL.class);
			if (!file.exists()) {
				log.warn("library {} missing; could not load MKL", file);
				return false;
			}
			try {
				lib = MklLib.load(file.toPath());
				log.info("loaded MKL library from {}", file);
				return true;
			} catch (Throwable e) {
				log.error("failed to load MKL library from {}", file, e);
				return false;
			}
		}
	}

	private static File libraryFile(File root) {
		if (root == null || !root.isDirectory())
			return null;
		var dirName = "olca-mkl-" + arch() + "_v" + VERSION;
		var libDir = new File(root, dirName);
		if (!libDir.isDirectory())
			return null;
		return new File(libDir, OS.detect().library());
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

	private static int infoOf(MemorySegment info) {
		return (int) info.get(JAVA_LONG, 0);
	}

	private static RuntimeException callError(String function, Throwable e) {
		return new RuntimeException("failed to call MKL function " + function, e);
	}
}
