package org.openlca.core.matrix.solvers.mkl;

import org.apache.commons.math3.linear.SingularMatrixException;

class InfoCode {

	private InfoCode() {
	}

	static void checkPardiso(int code) {
		if (code == 0)
			return;
		throw (code == -7
			? new SingularMatrixException()
			: PardisoError.of(code));
	}

	static void checkBlas(int code) {
		if (code == 0)
			return;
		throw (code > 0
			? new SingularMatrixException()
			: new BlasError(code));
	}

	private static class BlasError extends RuntimeException {
		BlasError(int code) {
			super("BLAS/LAPACK error: illegal argument: code=" +code);
		}
	}

	private static class PardisoError extends RuntimeException {
		private PardisoError(String message) {
			super(message);
		}

		static PardisoError of(int code) {
			var message = switch (code) {
				case -1 -> "input inconsistent";
				case -2 -> "not enough memory";
				case -3 -> "reordering problem";
				case -4 ->
					"zero pivot, numerical factorization or iterative refinement problem";
				case -5 -> "unclassified (internal) error";
				case -6 -> "reordering failed (matrix types 11 and 13 only)";
				case -7 -> "diagonal matrix is singular";
				case -8 -> "32-bit integer overflow problem";
				case -9 -> "not enough memory for OOC";
				case -10 -> "error opening OOC files";
				case -11 -> "read/write error with OOC files";
				case -12 -> "(pardiso_64 only) pardiso_64 called from 32-bit library";
				case -13 -> "interrupted by the (user-defined) mkl_progress function";
				case -15 ->
					"internal error which can appear for iparm[23]=10 and iparm[12]=1.";
				default -> "undefined error code";
			};
			return new PardisoError("code = " + code + ": " + message);
		}
	}

}
