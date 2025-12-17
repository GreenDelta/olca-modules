package org.openlca.core.matrix.solvers.accelerate;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.foreign.ValueLayout.*;

/**
 * Java 21 FFI wrapper for Apple Accelerate framework on ARM64 macOS.
 * This replaces the JNI/Rust layer with direct FFI calls to Accelerate.
 * 
 * Requires Java 21+ with --enable-preview and --enable-native-access flags.
 */
public final class AccelerateFFI {
	
	private static final boolean IS_AVAILABLE;
	
	// BLAS function handles
	private static final MethodHandle dgemv;
	private static final MethodHandle dgemm;
	
	// LAPACK function handles
	private static final MethodHandle dgesv;
	private static final MethodHandle dgetrf;
	private static final MethodHandle dgetri;
	private static final MethodHandle dgetrs;
	
	// Track active factorizations: id -> [arena, matrix segment, ipiv segment, n]
	private static final ConcurrentHashMap<Long, FactorizationData> FACTORIZATION_DATA = new ConcurrentHashMap<>();
	private static final AtomicLong FACTORIZATION_COUNTER = new AtomicLong(1);
	
	// Internal structure to store factorization data
	private static class FactorizationData {
		final Arena arena;
		final MemorySegment matrix;
		final MemorySegment ipiv;
		final int n;
		
		FactorizationData(Arena arena, MemorySegment matrix, MemorySegment ipiv, int n) {
			this.arena = arena;
			this.matrix = matrix;
			this.ipiv = ipiv;
			this.n = n;
		}
	}
	
	static {
		boolean available = false;
		Linker linker = null;
		SymbolLookup lookup = null;
		MethodHandle dgemvHandle = null, dgemmHandle = null;
		MethodHandle dgesvHandle = null, dgetrfHandle = null;
		MethodHandle dgetriHandle = null, dgetrsHandle = null;
		
		try {
			// Check if we're on macOS ARM64
			if (AcceleratePlatform.isArm64MacOS()) {
				linker = Linker.nativeLinker();
				
				// Load Accelerate framework
				// On macOS, frameworks can be loaded via System.loadLibrary
				try {
					System.loadLibrary("Accelerate");
				} catch (UnsatisfiedLinkError e) {
					// Try alternative loading path
					try {
						System.load("/System/Library/Frameworks/Accelerate.framework/Accelerate");
					} catch (UnsatisfiedLinkError e2) {
						System.err.println("Failed to load Accelerate framework: " + e2.getMessage());
						// Leave everything as null/default - will skip initialization
						linker = null;
					}
				}
				
				if (linker != null) {
					lookup = SymbolLookup.loaderLookup();
			
			// BLAS function descriptors
			// Note: Fortran BLAS/LAPACK pass all integer parameters by reference (as pointers)
			FunctionDescriptor dgemvDesc = FunctionDescriptor.ofVoid(
				ADDRESS,  // TRANS (char*)
				ADDRESS,  // M (int*)
				ADDRESS,  // N (int*)
				ADDRESS,  // ALPHA (double*)
				ADDRESS,  // A (double*)
				ADDRESS,  // LDA (int*)
				ADDRESS,  // X (double*)
				ADDRESS,  // INCX (int*)
				ADDRESS,  // BETA (double*)
				ADDRESS,  // Y (double*)
				ADDRESS   // INCY (int*)
			);
			
			FunctionDescriptor dgemmDesc = FunctionDescriptor.ofVoid(
				ADDRESS,  // TRANSA (char*)
				ADDRESS,  // TRANSB (char*)
				ADDRESS,  // M (int*)
				ADDRESS,  // N (int*)
				ADDRESS,  // K (int*)
				ADDRESS,  // ALPHA (double*)
				ADDRESS,  // A (double*)
				ADDRESS,  // LDA (int*)
				ADDRESS,  // B (double*)
				ADDRESS,  // LDB (int*)
				ADDRESS,  // BETA (double*)
				ADDRESS,  // C (double*)
				ADDRESS   // LDC (int*)
			);
			
			// LAPACK function descriptors
			// Note: dgesv_ doesn't return a value - INFO is an output parameter
			// All integer parameters are passed by reference (as pointers) in Fortran calling convention
			FunctionDescriptor dgesvDesc = FunctionDescriptor.ofVoid(
				ADDRESS,  // N (int*)
				ADDRESS,  // NRHS (int*)
				ADDRESS,  // A (double*)
				ADDRESS,  // LDA (int*)
				ADDRESS,  // IPIV (int*)
				ADDRESS,  // B (double*)
				ADDRESS,  // LDB (int*)
				ADDRESS   // INFO (int*)
			);
			
			FunctionDescriptor dgetrfDesc = FunctionDescriptor.ofVoid(
				ADDRESS,  // M (int*)
				ADDRESS,  // N (int*)
				ADDRESS,  // A (double*)
				ADDRESS,  // LDA (int*)
				ADDRESS,  // IPIV (int*)
				ADDRESS   // INFO (int*)
			);
			
			FunctionDescriptor dgetriDesc = FunctionDescriptor.ofVoid(
				ADDRESS,  // N (int*)
				ADDRESS,  // A (double*)
				ADDRESS,  // LDA (int*)
				ADDRESS,  // IPIV (int*)
				ADDRESS,  // WORK (double*)
				ADDRESS,  // LWORK (int*)
				ADDRESS   // INFO (int*)
			);
			
			FunctionDescriptor dgetrsDesc = FunctionDescriptor.ofVoid(
				ADDRESS,  // TRANS (char*)
				ADDRESS,  // N (int*)
				ADDRESS,  // NRHS (int*)
				ADDRESS,  // A (double*)
				ADDRESS,  // LDA (int*)
				ADDRESS,  // IPIV (int*)
				ADDRESS,  // B (double*)
				ADDRESS,  // LDB (int*)
				ADDRESS   // INFO (int*)
			);
			
			// Find BLAS/LAPACK symbols (Accelerate uses standard BLAS/LAPACK naming with underscore)
			var dgemvSymbol = lookup.find("dgemv_");
			var dgemmSymbol = lookup.find("dgemm_");
			var dgesvSymbol = lookup.find("dgesv_");
			var dgetrfSymbol = lookup.find("dgetrf_");
			var dgetriSymbol = lookup.find("dgetri_");
			var dgetrsSymbol = lookup.find("dgetrs_");
			
			if (dgemvSymbol.isPresent() && dgemmSymbol.isPresent() && 
			    dgesvSymbol.isPresent() && dgetrfSymbol.isPresent()) {
				dgemvHandle = linker.downcallHandle(dgemvSymbol.get(), dgemvDesc);
				dgemmHandle = linker.downcallHandle(dgemmSymbol.get(), dgemmDesc);
				dgesvHandle = linker.downcallHandle(dgesvSymbol.get(), dgesvDesc);
				dgetrfHandle = linker.downcallHandle(dgetrfSymbol.get(), dgetrfDesc);
				dgetriHandle = dgetriSymbol.isPresent() 
					? linker.downcallHandle(dgetriSymbol.get(), dgetriDesc) : null;
				dgetrsHandle = dgetrsSymbol.isPresent()
					? linker.downcallHandle(dgetrsSymbol.get(), dgetrsDesc) : null;
				
				available = true;
			}
				} // end if (linker != null)
			} // end if (AcceleratePlatform.isArm64MacOS())
			
		} catch (Exception e) {
			System.err.println("Failed to initialize Accelerate FFI: " + e.getMessage());
			e.printStackTrace();
		}
		
		IS_AVAILABLE = available;
		dgemv = dgemvHandle;
		dgemm = dgemmHandle;
		dgesv = dgesvHandle;
		dgetrf = dgetrfHandle;
		dgetri = dgetriHandle;
		dgetrs = dgetrsHandle;
	}
	
	public static boolean isAvailable() {
		return IS_AVAILABLE;
	}
	
	// BLAS operations
	
	/**
	 * Matrix-vector multiplication: y := alpha * A * x + beta * y
	 * where A is stored in column-major order
	 */
	public static void mvmult(int rowsA, int colsA, double[] a, double[] x, double[] y) {
		if (!IS_AVAILABLE) throw new UnsupportedOperationException("Accelerate not available");
		
		try (Arena arena = Arena.ofConfined()) {
			MemorySegment trans = arena.allocateUtf8String("N");
			MemorySegment alpha = arena.allocate(JAVA_DOUBLE);
			alpha.set(JAVA_DOUBLE, 0, 1.0);
			MemorySegment beta = arena.allocate(JAVA_DOUBLE);
			beta.set(JAVA_DOUBLE, 0, 0.0);
			
			// Allocate integer parameters (Fortran passes by reference)
			MemorySegment mSeg = arena.allocate(JAVA_INT);
			mSeg.set(JAVA_INT, 0, rowsA);
			MemorySegment nSeg = arena.allocate(JAVA_INT);
			nSeg.set(JAVA_INT, 0, colsA);
			MemorySegment ldaSeg = arena.allocate(JAVA_INT);
			ldaSeg.set(JAVA_INT, 0, rowsA);
			MemorySegment incxSeg = arena.allocate(JAVA_INT);
			incxSeg.set(JAVA_INT, 0, 1);
			MemorySegment incySeg = arena.allocate(JAVA_INT);
			incySeg.set(JAVA_INT, 0, 1);
			
			// Allocate and copy arrays
			MemorySegment aSeg = arena.allocateArray(JAVA_DOUBLE, a);
			MemorySegment xSeg = arena.allocateArray(JAVA_DOUBLE, x);
			MemorySegment ySeg = arena.allocateArray(JAVA_DOUBLE, y);
			
			dgemv.invokeExact(
				trans, mSeg, nSeg, alpha, aSeg, ldaSeg,
				xSeg, incxSeg, beta, ySeg, incySeg
			);
			
			// Copy result back
			for (int i = 0; i < y.length; i++) {
				y[i] = ySeg.getAtIndex(JAVA_DOUBLE, i);
			}
		} catch (Throwable e) {
			throw new RuntimeException("dgemv failed", e);
		}
	}
	
	/**
	 * Matrix-matrix multiplication: C := alpha * A * B + beta * C
	 * where matrices are stored in column-major order
	 */
	public static void mmult(int rowsA, int colsB, int k, double[] a, double[] b, double[] c) {
		if (!IS_AVAILABLE) throw new UnsupportedOperationException("Accelerate not available");
		
		try (Arena arena = Arena.ofConfined()) {
			MemorySegment trans = arena.allocateUtf8String("N");
			MemorySegment alpha = arena.allocate(JAVA_DOUBLE);
			alpha.set(JAVA_DOUBLE, 0, 1.0);
			MemorySegment beta = arena.allocate(JAVA_DOUBLE);
			beta.set(JAVA_DOUBLE, 0, 0.0);
			
			// Allocate integer parameters (Fortran passes by reference)
			MemorySegment mSeg = arena.allocate(JAVA_INT);
			mSeg.set(JAVA_INT, 0, rowsA);
			MemorySegment nSeg = arena.allocate(JAVA_INT);
			nSeg.set(JAVA_INT, 0, colsB);
			MemorySegment kSeg = arena.allocate(JAVA_INT);
			kSeg.set(JAVA_INT, 0, k);
			MemorySegment ldaSeg = arena.allocate(JAVA_INT);
			ldaSeg.set(JAVA_INT, 0, rowsA);
			MemorySegment ldbSeg = arena.allocate(JAVA_INT);
			ldbSeg.set(JAVA_INT, 0, k);
			MemorySegment ldcSeg = arena.allocate(JAVA_INT);
			ldcSeg.set(JAVA_INT, 0, rowsA);
			
			// Allocate and copy arrays
			MemorySegment aSeg = arena.allocateArray(JAVA_DOUBLE, a);
			MemorySegment bSeg = arena.allocateArray(JAVA_DOUBLE, b);
			MemorySegment cSeg = arena.allocateArray(JAVA_DOUBLE, c);
			
			dgemm.invokeExact(
				trans, trans, mSeg, nSeg, kSeg, alpha,
				aSeg, ldaSeg, bSeg, ldbSeg, beta, cSeg, ldcSeg
			);
			
			// Copy result back
			for (int i = 0; i < c.length; i++) {
				c[i] = cSeg.getAtIndex(JAVA_DOUBLE, i);
			}
		} catch (Throwable e) {
			throw new RuntimeException("dgemm failed", e);
		}
	}
	
	// LAPACK operations
	
	/**
	 * Solves A * X = B using LU factorization
	 * @return LAPACK info code (0 = success, >0 = singular, <0 = invalid input)
	 */
	public static int solve(int n, int nrhs, double[] a, double[] b) {
		if (!IS_AVAILABLE) throw new UnsupportedOperationException("Accelerate not available");
		
		try (Arena arena = Arena.ofConfined()) {
			// Allocate integer parameters (Fortran passes by reference)
			MemorySegment nSeg = arena.allocate(JAVA_INT);
			nSeg.set(JAVA_INT, 0, n);
			MemorySegment nrhsSeg = arena.allocate(JAVA_INT);
			nrhsSeg.set(JAVA_INT, 0, nrhs);
			MemorySegment ldaSeg = arena.allocate(JAVA_INT);
			ldaSeg.set(JAVA_INT, 0, n);
			MemorySegment ldbSeg = arena.allocate(JAVA_INT);
			ldbSeg.set(JAVA_INT, 0, n);
			
			// Allocate and copy arrays
			MemorySegment aSeg = arena.allocateArray(JAVA_DOUBLE, a);
			MemorySegment bSeg = arena.allocateArray(JAVA_DOUBLE, b);
			MemorySegment ipivSeg = arena.allocateArray(JAVA_INT, new int[n]);
			MemorySegment infoSeg = arena.allocate(JAVA_INT);
			
			dgesv.invokeExact(nSeg, nrhsSeg, aSeg, ldaSeg, ipivSeg, bSeg, ldbSeg, infoSeg);
			
			int info = infoSeg.get(JAVA_INT, 0);
			
			// Copy results back
			for (int i = 0; i < a.length; i++) {
				a[i] = aSeg.getAtIndex(JAVA_DOUBLE, i);
			}
			for (int i = 0; i < b.length; i++) {
				b[i] = bSeg.getAtIndex(JAVA_DOUBLE, i);
			}
			
			return info;
		} catch (Throwable e) {
			throw new RuntimeException("dgesv failed", e);
		}
	}
	
	/**
	 * Inverts matrix A in place
	 * @return LAPACK info code (0 = success)
	 */
	public static int invert(int n, double[] a) {
		if (!IS_AVAILABLE) throw new UnsupportedOperationException("Accelerate not available");
		
		try (Arena arena = Arena.ofConfined()) {
			// Allocate integer parameters (Fortran passes by reference)
			MemorySegment nSeg = arena.allocate(JAVA_INT);
			nSeg.set(JAVA_INT, 0, n);
			MemorySegment ldaSeg = arena.allocate(JAVA_INT);
			ldaSeg.set(JAVA_INT, 0, n);
			MemorySegment lworkSeg = arena.allocate(JAVA_INT);
			
			// Allocate and copy array
			MemorySegment aSeg = arena.allocateArray(JAVA_DOUBLE, a);
			MemorySegment ipivSeg = arena.allocateArray(JAVA_INT, new int[n]);
			MemorySegment infoSeg = arena.allocate(JAVA_INT);
			
			// Factorize first
			dgetrf.invokeExact(nSeg, nSeg, aSeg, ldaSeg, ipivSeg, infoSeg);
			int info = infoSeg.get(JAVA_INT, 0);
			if (info != 0) {
				for (int i = 0; i < a.length; i++) {
					// Keep the a array in sync with its native variable before returning info
					a[i] = aSeg.getAtIndex(JAVA_DOUBLE, i);
				}
				return info;
			}
			
			// Query workspace size (use heuristic: 64*2*n) to match the rust implementation
			int lwork = 64 * 2 * n;
			lworkSeg.set(JAVA_INT, 0, lwork);
			// JAVA_DOUBLE is 8 bytes, so we allocate 8 * lwork double bytes to match the rust implementation
			MemorySegment workSeg = arena.allocateArray(JAVA_DOUBLE, new double[lwork]);
			
			// Invert
			dgetri.invokeExact(nSeg, aSeg, ldaSeg, ipivSeg, workSeg, lworkSeg, infoSeg);
			info = infoSeg.get(JAVA_INT, 0);
			
			// Copy result back
			for (int i = 0; i < a.length; i++) {
				a[i] = aSeg.getAtIndex(JAVA_DOUBLE, i);
			}
			
			return info;
		} catch (Throwable e) {
			throw new RuntimeException("dgetri failed", e);
		}
	}
	
	/**
	 * Creates a dense LU factorization
	 * @return pointer to factorization (as long) - must be disposed with destroyDenseFactorization
	 */
	public static long createDenseFactorization(int n, double[] matrix) {
		if (!IS_AVAILABLE) throw new UnsupportedOperationException("Accelerate not available");
		
		// Allocate persistent memory for factorization
		// Arena.ofShared() has automatic cleanup of the memory when the arena is closed,
		// so we don't need to manually free the memory like in the rust implementation
		Arena arena = Arena.ofShared();
		
		try {
			// Allocate integer parameters (Fortran passes by reference)
			MemorySegment mSeg = arena.allocate(JAVA_INT);
			mSeg.set(JAVA_INT, 0, n);
			MemorySegment nSeg = arena.allocate(JAVA_INT);
			nSeg.set(JAVA_INT, 0, n);
			MemorySegment ldaSeg = arena.allocate(JAVA_INT);
			ldaSeg.set(JAVA_INT, 0, n);
			
			// Allocate matrix copy and pivot indices
			MemorySegment matrixSeg = arena.allocateArray(JAVA_DOUBLE, matrix);
			// Allocate 32-bit int compared to rust 64 bit implementation
			MemorySegment ipivSeg = arena.allocateArray(JAVA_INT, new int[n]);
			MemorySegment infoSeg = arena.allocate(JAVA_INT);
			
			// Factorize
			dgetrf.invokeExact(mSeg, nSeg, matrixSeg, ldaSeg, ipivSeg, infoSeg);
			int info = infoSeg.get(JAVA_INT, 0);
			if (info != 0) {
				arena.close();
				throw new RuntimeException("dgetrf failed with info=" + info);
			}
			
			// Store factorization data
			long id = FACTORIZATION_COUNTER.getAndIncrement();
			FACTORIZATION_DATA.put(id, new FactorizationData(arena, matrixSeg, ipivSeg, n));
			
			return id;
		} catch (Throwable e) {
			arena.close();
			throw new RuntimeException("createDenseFactorization failed", e);
		}
	}
	
	/**
	 * Solves using a dense factorization
	 */
	public static void solveDenseFactorization(long factorization, int columns, double[] b) {
		if (!IS_AVAILABLE) throw new UnsupportedOperationException("Accelerate not available");
		
		FactorizationData data = FACTORIZATION_DATA.get(factorization);
		if (data == null) {
			throw new IllegalArgumentException("Invalid factorization pointer: " + factorization);
		}
		
		if (dgetrs == null) {
			throw new UnsupportedOperationException("dgetrs not available");
		}
		
		try (Arena arena = Arena.ofConfined()) {
			MemorySegment trans = arena.allocateUtf8String("N");
			
			// Allocate integer parameters (Fortran passes by reference)
			MemorySegment nSeg = arena.allocate(JAVA_INT);
			nSeg.set(JAVA_INT, 0, data.n);
			MemorySegment nrhsSeg = arena.allocate(JAVA_INT);
			nrhsSeg.set(JAVA_INT, 0, columns);
			MemorySegment ldaSeg = arena.allocate(JAVA_INT);
			ldaSeg.set(JAVA_INT, 0, data.n);
			MemorySegment ldbSeg = arena.allocate(JAVA_INT);
			ldbSeg.set(JAVA_INT, 0, data.n);
			
			// Allocate and copy array
			MemorySegment bSeg = arena.allocateArray(JAVA_DOUBLE, b);
			
			// Solve using stored factorization
			MemorySegment infoSeg = arena.allocate(JAVA_INT);
			dgetrs.invokeExact(
				trans, nSeg, nrhsSeg, data.matrix, ldaSeg, data.ipiv, bSeg, ldbSeg, infoSeg
			);
			
			int info = infoSeg.get(JAVA_INT, 0);
			if (info != 0) {
				throw new RuntimeException("dgetrs failed with info=" + info);
			}
			
			// Copy result back
			for (int i = 0; i < b.length; i++) {
				b[i] = bSeg.getAtIndex(JAVA_DOUBLE, i);
			}
		} catch (Throwable e) {
			throw new RuntimeException("solveDenseFactorization failed", e);
		}
	}
	
	/**
	 * Destroys a dense factorization and frees its memory
	 */
	public static void destroyDenseFactorization(long factorization) {
		FactorizationData data = FACTORIZATION_DATA.remove(factorization);
		if (data != null) {
			data.arena.close();
		}
	}
}
