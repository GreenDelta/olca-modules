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
/// The BLAS and LAPACK handles are linked as critical functions with heap
/// access, so that Java arrays can be passed directly as heap segments without
/// copying. PARDISO is linked as a normal function because it can run for a
/// long time and keeps references to its inputs between calls; thus, all memory
/// passed to PARDISO must be allocated off-heap.
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
		var heap = Linker.Option.critical(true);
		dgemv = bind(linker, lookup, "dgemv_64", 11, heap);
		dgemm = bind(linker, lookup, "dgemm_64", 13, heap);
		dgesv = bind(linker, lookup, "dgesv_64", 8, heap);
		dgetrf = bind(linker, lookup, "dgetrf_64", 6, heap);
		dgetri = bind(linker, lookup, "dgetri_64", 7, heap);
		dgetrs = bind(linker, lookup, "dgetrs_64", 9, heap);
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
		Linker linker, SymbolLookup lookup, String name, int args,
		Linker.Option... options
	) {
		var symbol = lookup.find(name).orElseThrow(
			() -> new IllegalStateException("MKL function not found: " + name));
		var params = new MemoryLayout[args];
		Arrays.fill(params, ADDRESS);
		var descriptor = FunctionDescriptor.ofVoid(params);
		return linker.downcallHandle(symbol, descriptor, options);
	}
}
