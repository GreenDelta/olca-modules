package org.openlca.core.matrix.solvers.mkl;

import static java.lang.foreign.ValueLayout.JAVA_DOUBLE;
import static java.lang.foreign.ValueLayout.JAVA_INT;
import static java.lang.foreign.ValueLayout.JAVA_LONG;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import org.slf4j.LoggerFactory;

/// Wraps a PARDISO solver instance for a square, unsymmetric matrix in CSC
/// format. All data passed to PARDISO, including a copy of the matrix, is
/// allocated in an arena that is owned by this instance; it is released in
/// [#close()]. Instances are not thread-safe. See the documentation of
/// [PARDISO for details](https://www.intel.com/content/www/us/en/docs/onemkl/developer-reference-c/2023-0/pardiso.html).
final class Pardiso implements AutoCloseable {

	// Analysis, numerical factorization, solve, iterative refinement
	private static final int PHASE_ALL = 13;

	// Analysis, numerical factorization
	private static final int PHASE_FACT = 12;

	// Solve, iterative refinement
	private static final int PHASE_SOLVE = 33;

	// Release all internal memory for all matrices
	private static final int PHASE_CLEANUP = -1;

	// real and unsymmetric matrix
	private static final int MTYPE = 11;

	private final MklLib lib;
	private final Arena arena;
	private final int n;

	private final MemorySegment pt;
	private final MemorySegment iparm;
	private final MemorySegment perm;
	private final MemorySegment values;
	private final MemorySegment columnPointers;
	private final MemorySegment rowIndices;

	private final MemorySegment one;
	private final MemorySegment zero;
	private final MemorySegment mtype;
	private final MemorySegment size;
	private final MemorySegment phase;
	private final MemorySegment error;

	private boolean hasState;
	private boolean isClosed;

	private Pardiso(
		MklLib lib, int n, double[] values, int[] rowIndices, int[] columnPointers
	) {
		this.lib = lib;
		this.n = n;
		this.arena = Arena.ofShared();
		try {
			pt = arena.allocate(JAVA_LONG, 64);
			iparm = arena.allocate(JAVA_INT, 64);
			perm = arena.allocate(JAVA_INT, Math.max(n, 1));
			this.values = arena.allocateFrom(JAVA_DOUBLE, values);
			this.rowIndices = arena.allocateFrom(JAVA_INT, rowIndices);
			this.columnPointers = arena.allocateFrom(JAVA_INT, columnPointers);

			one = arena.allocateFrom(JAVA_INT, 1);
			zero = arena.allocateFrom(JAVA_INT, 0);
			mtype = arena.allocateFrom(JAVA_INT, MTYPE);
			size = arena.allocateFrom(JAVA_INT, n);
			phase = arena.allocate(JAVA_INT);
			error = arena.allocate(JAVA_INT);
		} catch (RuntimeException e) {
			arena.close();
			throw e;
		}

		param(0, 1); // no defaults
		param(1, 2); // nested dissection algorithm from METIS
		param(9, 13); // perturb pivots with 1e-13
		param(10, 1); // enable scaling
		param(11, 2); // CSC format: solve the transposed CSR system
		param(12, 1); // weighted matching
		param(34, 1); // zero-based indexing
	}

	/// Creates a new PARDISO instance for the given matrix in CSC format.
	/// The matrix data are copied into native memory.
	///
	/// @param n              the number of rows and columns of the matrix
	/// @param values         the non-zero values of the matrix
	/// @param rowIndices     the row indices of the non-zero values
	/// @param columnPointers the column pointers of the matrix
	static Pardiso of(
		int n, double[] values, int[] rowIndices, int[] columnPointers
	) {
		return new Pardiso(MKL.lib(), n, values, rowIndices, columnPointers);
	}

	int size() {
		return n;
	}

	/// Runs the analysis and numerical factorization. Returns 0 on success
	/// or an error code otherwise; see [InfoCode#checkPardiso(int)].
	int factorize() {
		int err = call(PHASE_FACT, MemorySegment.NULL, MemorySegment.NULL);
		return checkPerturbation(err);
	}

	/// Solves the system with a previous factorization, see
	/// [#factorize()]. Returns 0 on success or an error code otherwise.
	int solve(double[] b, double[] x) {
		return solveWith(PHASE_SOLVE, b, x);
	}

	/// Runs the analysis, numerical factorization, and solves the system in
	/// a single step. Returns 0 on success or an error code otherwise.
	int factorizeAndSolve(double[] b, double[] x) {
		int err = solveWith(PHASE_ALL, b, x);
		return checkPerturbation(err);
	}

	private int solveWith(int phase, double[] b, double[] x) {
		try (var local = Arena.ofConfined()) {
			var bSeg = local.allocateFrom(JAVA_DOUBLE, b);
			var xSeg = local.allocate(JAVA_DOUBLE, n);
			int err = call(phase, bSeg, xSeg);
			MemorySegment.copy(xSeg, JAVA_DOUBLE, 0, x, 0, n);
			return err;
		}
	}

	/// With pivot perturbation enabled, PARDISO does not report singular
	/// matrices but replaces zero pivots with small values. We treat such
	/// perturbations as singularity of the matrix.
	private int checkPerturbation(int err) {
		if (err != 0)
			return err;
		return iparm.getAtIndex(JAVA_INT, 13) > 0
			? InfoCode.PARDISO_PERTURBED_PIVOTS
			: 0;
	}

	private void param(int idx, int value) {
		iparm.setAtIndex(JAVA_INT, idx, value);
	}

	private int call(int phaseId, MemorySegment b, MemorySegment x) {
		if (isClosed)
			throw new IllegalStateException("PARDISO instance was closed");
		hasState = true;
		phase.set(JAVA_INT, 0, phaseId);
		error.set(JAVA_INT, 0, 0);
		try {
			lib.pardiso.invokeExact(
				pt,             // pt
				one,            // maxfct
				one,            // mnum
				mtype,          // mtype
				phase,          // phase
				size,           // n
				values,         // a
				columnPointers, // ia
				rowIndices,     // ja
				perm,           // perm
				one,            // nrhs
				iparm,          // iparm
				zero,           // msglvl
				b,              // b
				x,              // x
				error           // error
			);
		} catch (Throwable e) {
			throw new RuntimeException("failed to call PARDISO", e);
		}
		return error.get(JAVA_INT, 0);
	}

	/// Releases the internal memory of PARDISO and the native memory of
	/// this instance. Calling this method more than once has no effect.
	@Override
	public void close() {
		if (isClosed)
			return;
		try {
			if (hasState) {
				int err = call(PHASE_CLEANUP, MemorySegment.NULL, MemorySegment.NULL);
				if (err != 0) {
					LoggerFactory.getLogger(getClass())
						.error("failed to release PARDISO memory: error={}", err);
				}
			}
		} finally {
			isClosed = true;
			arena.close();
		}
	}
}
