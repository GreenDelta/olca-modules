package org.openlca.core.matrix.solvers.mkl;

import static java.lang.foreign.ValueLayout.ADDRESS;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.SymbolLookup;
import java.lang.invoke.MethodHandle;
import java.nio.file.Path;
import java.util.Arrays;

/// The method handles of the MKL functions that we call via the foreign
/// function API. The BLAS and LAPACK functions are bound to their ILP64
/// variants (64-bit integers), PARDISO to its LP64 variant (32-bit integers).
/// All parameters are passed by reference.
///
/// All functions are linked as normal (non-critical) downcalls. A critical
/// downcall would allow passing Java arrays directly as heap segments, but the
/// calling thread would then stay in the Java state during the call, and any
/// safepoint operation (like a garbage collection) would have to wait until the
/// call returns, which would stall all Java threads. Critical downcalls are
/// meant for functions with an extremely short running time, which is not the
/// case for the MKL functions with larger matrices. Thus, all memory that is
/// passed to these functions must be allocated off-heap.
final class MklLib {

	final MethodHandle dgemv;
	final MethodHandle dgemm;
	final MethodHandle dgesv;
	final MethodHandle dgetrf;
	final MethodHandle dgetri;
	final MethodHandle dgetrs;
	final MethodHandle pardiso;

	private MklLib(SymbolLookup lookup) {
		var linker = Linker.nativeLinker();
		dgemv = bind(linker, lookup, "dgemv_64", 11);
		dgemm = bind(linker, lookup, "dgemm_64", 13);
		dgesv = bind(linker, lookup, "dgesv_64", 8);
		dgetrf = bind(linker, lookup, "dgetrf_64", 6);
		dgetri = bind(linker, lookup, "dgetri_64", 7);
		dgetrs = bind(linker, lookup, "dgetrs_64", 9);
		pardiso = bind(linker, lookup, "pardiso", 16);
	}

	/// Loads the MKL runtime library from the given path and binds the
	/// required functions. Throws an exception if the library cannot be
	/// loaded or a function is missing.
	static MklLib load(Path path) {
		var lookup = SymbolLookup.libraryLookup(path, Arena.global());
		return new MklLib(lookup);
	}

	private static MethodHandle bind(
		Linker linker, SymbolLookup lookup, String name, int args
	) {
		var symbol = lookup.find(name).orElseThrow(
			() -> new IllegalStateException("MKL function not found: " + name));
		var params = new MemoryLayout[args];
		Arrays.fill(params, ADDRESS);
		var descriptor = FunctionDescriptor.ofVoid(params);
		return linker.downcallHandle(symbol, descriptor);
	}
}
