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
	
	// Sparse factorization function handles
	// Note: Not final because they may be set later via loadSparseWrapperFrom()
	private static volatile MethodHandle sparseFactorCreateHandle;
	private static volatile MethodHandle sparseFactorSolveHandle;
	private static volatile MethodHandle sparseFactorDestroyHandle;
	
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
	
	// Internal structure to store sparse factorization data
	private static class SparseFactorizationData {
		final Arena arena;
		final long factorPtr;  // Opaque pointer from C
		final int n;
		
		SparseFactorizationData(Arena arena, long factorPtr, int n) {
			this.arena = arena;
			this.factorPtr = factorPtr;
			this.n = n;
		}
	}
	
	// Separate map for sparse factorizations
	private static final ConcurrentHashMap<Long, SparseFactorizationData> SPARSE_FACTORIZATION_DATA = new ConcurrentHashMap<>();
	
	// Linker and lookup for FFI operations
	private static Linker linker;
	private static SymbolLookup lookup;
	
	// Sparse factorization function descriptors (needed for loadSparseWrapperFrom)
	private static FunctionDescriptor sparseFactorCreateDesc;
	private static FunctionDescriptor sparseFactorSolveDesc;
	private static FunctionDescriptor sparseFactorDestroyDesc;
	
	static {
		boolean available = false;
		Linker staticLinker = null;
		SymbolLookup staticLookup = null;
		MethodHandle dgemvHandle = null, dgemmHandle = null;
		MethodHandle dgesvHandle = null, dgetrfHandle = null;
		MethodHandle dgetriHandle = null, dgetrsHandle = null;
		MethodHandle sparseCreateHandle = null;
		MethodHandle sparseSolveHandle = null;
		MethodHandle sparseDestroyHandle = null;
		
		try {
			// Check if we're on macOS ARM64
			if (AcceleratePlatform.isArm64MacOS()) {
				staticLinker = Linker.nativeLinker();
				
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
						staticLinker = null;
					}
				}
				
				if (staticLinker != null) {
					staticLookup = SymbolLookup.loaderLookup();
			
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
			var dgemvSymbol = staticLookup.find("dgemv_");
			var dgemmSymbol = staticLookup.find("dgemm_");
			var dgesvSymbol = staticLookup.find("dgesv_");
			var dgetrfSymbol = staticLookup.find("dgetrf_");
			var dgetriSymbol = staticLookup.find("dgetri_");
			var dgetrsSymbol = staticLookup.find("dgetrs_");
			
			if (dgemvSymbol.isPresent() && dgemmSymbol.isPresent() && 
			    dgesvSymbol.isPresent() && dgetrfSymbol.isPresent()) {
				dgemvHandle = staticLinker.downcallHandle(dgemvSymbol.get(), dgemvDesc);
				dgemmHandle = staticLinker.downcallHandle(dgemmSymbol.get(), dgemmDesc);
				dgesvHandle = staticLinker.downcallHandle(dgesvSymbol.get(), dgesvDesc);
				dgetrfHandle = staticLinker.downcallHandle(dgetrfSymbol.get(), dgetrfDesc);
				dgetriHandle = dgetriSymbol.isPresent() 
					? staticLinker.downcallHandle(dgetriSymbol.get(), dgetriDesc) : null;
				dgetrsHandle = dgetrsSymbol.isPresent()
					? staticLinker.downcallHandle(dgetrsSymbol.get(), dgetrsDesc) : null;
				
				available = true;
			}
			
			// Sparse wrapper library will be loaded separately via loadSparseWrapperFrom()
			
			// Sparse factorization function descriptors
			sparseFactorCreateDesc = FunctionDescriptor.of(
				ADDRESS,        // return: void* (opaque factorization pointer)
				JAVA_INT,       // param: int n
				ADDRESS,        // param: int64_t* colPtr
				ADDRESS,        // param: int64_t* rowInd
				ADDRESS         // param: double* values
			);
			
			sparseFactorSolveDesc = FunctionDescriptor.of(
				JAVA_INT,       // return: int (error code, 0 = success)
				ADDRESS,        // param: void* factor
				ADDRESS,        // param: double* b (input)
				ADDRESS         // param: double* x (output)
			);
			
			sparseFactorDestroyDesc = FunctionDescriptor.ofVoid(
				ADDRESS         // param: void* factor
			);
			
			// Sparse symbols will be looked up when library is loaded via loadSparseWrapperFrom()
				} // end if (staticLinker != null)
			} // end if (AcceleratePlatform.isArm64MacOS())
			
		} catch (Exception e) {
			System.err.println("Failed to initialize Accelerate FFI: " + e.getMessage());
			e.printStackTrace();
		}
		
		IS_AVAILABLE = available;
		linker = staticLinker;
		lookup = staticLookup;
		dgemv = dgemvHandle;
		dgemm = dgemmHandle;
		dgesv = dgesvHandle;
		dgetrf = dgetrfHandle;
		dgetri = dgetriHandle;
		dgetrs = dgetrsHandle;
		sparseFactorCreateHandle = sparseCreateHandle;
		sparseFactorSolveHandle = sparseSolveHandle;
		sparseFactorDestroyHandle = sparseDestroyHandle;
	}
	
	public static boolean isAvailable() {
		return IS_AVAILABLE;
	}
	
	/**
	 * Checks if sparse factorization is available
	 */
	public static boolean isSparseFactorizationAvailable() {
		return IS_AVAILABLE && sparseFactorCreateHandle != null;
	}
	
	/**
	 * Loads the sparse wrapper library from the given directory.
	 * Similar to NativeLib.loadFrom(), extracts from JAR if needed and loads the library.
	 * 
	 * @param root the root directory (workspace or install location)
	 * @return true if the library was successfully loaded
	 */
	public static boolean loadSparseWrapperFrom(java.io.File root) {
		// If already loaded, return true
		if (sparseFactorCreateHandle != null) {
			return true;
		}
		
		synchronized (AccelerateFFI.class) {
			// Double-check after acquiring lock
			if (sparseFactorCreateHandle != null) {
				return true;
			}
			
			if (root == null || !root.exists()) {
				System.err.println("AccelerateFFI.loadSparseWrapperFrom: root directory is null or does not exist: " + root);
				return false;
			}
			
			String osName = System.getProperty("os.name");
			String osArch = System.getProperty("os.arch");
			if (!osName.toLowerCase().contains("mac")) {
				return false;
			}
			
			// Determine the Maven classifier path based on architecture
			String classifier = (osArch.equals("aarch64") || osArch.equals("arm64")) 
				? "osx-aarch64" : "osx-x86_64";
			
			// Target directory: root/native/osx-aarch64/
			java.io.File libDir = new java.io.File(root, "native/" + classifier);
			java.io.File libFile = new java.io.File(libDir, "libaccelerate_sparse_wrapper.dylib");
			
			System.err.println("AccelerateFFI.loadSparseWrapperFrom: Target library path: " + libFile.getAbsolutePath());
			
			// If library doesn't exist, try to extract from JAR
			if (!libFile.exists()) {
				String resourcePath = "/native/" + classifier + "/libaccelerate_sparse_wrapper.dylib";
				
				// Debug: Check where the class is loaded from
				java.net.URL classUrl = AccelerateFFI.class.getResource("/" + AccelerateFFI.class.getName().replace('.', '/') + ".class");
				System.err.println("AccelerateFFI.loadSparseWrapperFrom: AccelerateFFI class loaded from: " + classUrl);
				
				// Try multiple ways to find the resource
				java.net.URL libUrl = AccelerateFFI.class.getResource(resourcePath);
				if (libUrl == null) {
					// Try using class loader directly
					java.lang.ClassLoader cl = AccelerateFFI.class.getClassLoader();
					if (cl != null) {
						libUrl = cl.getResource(resourcePath);
						System.err.println("AccelerateFFI.loadSparseWrapperFrom: Tried ClassLoader.getResource: " + libUrl);
					}
				}
				if (libUrl == null) {
					// Try with Thread context class loader
					java.lang.ClassLoader tcl = Thread.currentThread().getContextClassLoader();
					if (tcl != null) {
						libUrl = tcl.getResource(resourcePath);
						System.err.println("AccelerateFFI.loadSparseWrapperFrom: Tried Thread context ClassLoader.getResource: " + libUrl);
					}
				}
				
				if (libUrl == null) {
					System.err.println("AccelerateFFI.loadSparseWrapperFrom: Resource not found in JAR: " + resourcePath);
					System.err.println("AccelerateFFI.loadSparseWrapperFrom: Tried AccelerateFFI.class.getResource(), ClassLoader.getResource(), and Thread context ClassLoader.getResource()");
					return false;
				}
				
				System.err.println("AccelerateFFI.loadSparseWrapperFrom: Found resource URL: " + libUrl);
				
				try {
					if (!libDir.exists()) {
						libDir.mkdirs();
					}
					
					// Extract library from JAR
					try (java.io.InputStream in = libUrl.openStream();
						 java.io.FileOutputStream out = new java.io.FileOutputStream(libFile)) {
						byte[] buffer = new byte[8192];
						int bytesRead;
						while ((bytesRead = in.read(buffer)) != -1) {
							out.write(buffer, 0, bytesRead);
						}
					}
					
					// Make file executable (required for dylib on macOS)
					libFile.setExecutable(true, false);
					System.err.println("AccelerateFFI.loadSparseWrapperFrom: Extracted library to " + libFile);
					
				} catch (java.io.IOException e) {
					System.err.println("AccelerateFFI.loadSparseWrapperFrom: Failed to extract library: " + e.getMessage());
					return false;
				}
			}
			
			if (!libFile.exists() || linker == null) {
				return false;
			}
			
			try {
				// Use dlopen to get a handle to the library, then use dlsym with that handle
				// This is more reliable than RTLD_DEFAULT on macOS with two-level namespace
				SymbolLookup systemLookup = SymbolLookup.loaderLookup();
				var dlopenSymbol = systemLookup.find("dlopen");
				var dlsymSymbol = systemLookup.find("dlsym");
				var dlcloseSymbol = systemLookup.find("dlclose");
				
				if (!dlopenSymbol.isPresent() || !dlsymSymbol.isPresent()) {
					SymbolLookup defaultLookup = linker.defaultLookup();
					if (!dlopenSymbol.isPresent()) dlopenSymbol = defaultLookup.find("dlopen");
					if (!dlsymSymbol.isPresent()) dlsymSymbol = defaultLookup.find("dlsym");
					if (!dlcloseSymbol.isPresent()) dlcloseSymbol = defaultLookup.find("dlclose");
				}
				
				if (dlopenSymbol.isPresent() && dlsymSymbol.isPresent()) {
					System.err.println("AccelerateFFI.loadSparseWrapperFrom: Found dlopen/dlsym, using them to get symbol addresses");
					
					// dlopen signature: void* dlopen(const char* path, int mode)
					// RTLD_LAZY = 1, RTLD_NOW = 2, RTLD_GLOBAL = 0x8
					FunctionDescriptor dlopenDesc = FunctionDescriptor.of(
						ADDRESS,  // return: void* handle
						ADDRESS,  // param: const char* path
						JAVA_INT  // param: int mode
					);
					
					// dlsym signature: void* dlsym(void* handle, const char* symbol)
					FunctionDescriptor dlsymDesc = FunctionDescriptor.of(
						ADDRESS,  // return: void*
						ADDRESS,  // param: void* handle
						ADDRESS   // param: const char* symbol name
					);
					
					MethodHandle dlopenHandle = linker.downcallHandle(dlopenSymbol.get(), dlopenDesc);
					MethodHandle dlsymHandle = linker.downcallHandle(dlsymSymbol.get(), dlsymDesc);
					
					try (Arena arena = Arena.ofConfined()) {
						// Open the library with RTLD_NOW | RTLD_GLOBAL (0xA = 2 | 8)
						// RTLD_NOW = 2, RTLD_GLOBAL = 8
						MemorySegment libPath = arena.allocateUtf8String(libFile.getAbsolutePath());
						MemorySegment libHandle = (MemorySegment) dlopenHandle.invokeExact(libPath, 0xA);
						
						if (libHandle.address() == 0) {
							System.err.println("AccelerateFFI.loadSparseWrapperFrom: dlopen failed");
						} else {
							System.err.println("AccelerateFFI.loadSparseWrapperFrom: dlopen succeeded, handle: 0x" + Long.toHexString(libHandle.address()));
							
							// Try both with and without underscore prefix
							// macOS sometimes strips the underscore in dlsym lookups
							MemorySegment createNameUnderscore = arena.allocateUtf8String("_accelerate_sparse_factor_create");
							MemorySegment solveNameUnderscore = arena.allocateUtf8String("_accelerate_sparse_factor_solve");
							MemorySegment destroyNameUnderscore = arena.allocateUtf8String("_accelerate_sparse_factor_destroy");
							
							MemorySegment createNameNoUnderscore = arena.allocateUtf8String("accelerate_sparse_factor_create");
							MemorySegment solveNameNoUnderscore = arena.allocateUtf8String("accelerate_sparse_factor_solve");
							MemorySegment destroyNameNoUnderscore = arena.allocateUtf8String("accelerate_sparse_factor_destroy");
							
							// Try with underscore first
							MemorySegment createAddr = (MemorySegment) dlsymHandle.invokeExact(libHandle, createNameUnderscore);
							MemorySegment solveAddr = (MemorySegment) dlsymHandle.invokeExact(libHandle, solveNameUnderscore);
							MemorySegment destroyAddr = (MemorySegment) dlsymHandle.invokeExact(libHandle, destroyNameUnderscore);
							
							// If not found, try without underscore
							if (createAddr.address() == 0) {
								createAddr = (MemorySegment) dlsymHandle.invokeExact(libHandle, createNameNoUnderscore);
							}
							if (solveAddr.address() == 0) {
								solveAddr = (MemorySegment) dlsymHandle.invokeExact(libHandle, solveNameNoUnderscore);
							}
							if (destroyAddr.address() == 0) {
								destroyAddr = (MemorySegment) dlsymHandle.invokeExact(libHandle, destroyNameNoUnderscore);
							}
							
							System.err.println("AccelerateFFI.loadSparseWrapperFrom: dlsym results:");
							System.err.println("  _accelerate_sparse_factor_create: " + (createAddr.address() != 0 ? "found at 0x" + Long.toHexString(createAddr.address()) : "not found"));
							System.err.println("  _accelerate_sparse_factor_solve: " + (solveAddr.address() != 0 ? "found at 0x" + Long.toHexString(solveAddr.address()) : "not found"));
							System.err.println("  _accelerate_sparse_factor_destroy: " + (destroyAddr.address() != 0 ? "found at 0x" + Long.toHexString(destroyAddr.address()) : "not found"));
							
							if (createAddr.address() != 0 && solveAddr.address() != 0 && destroyAddr.address() != 0) {
								// Create method handles from the addresses
								sparseFactorCreateHandle = linker.downcallHandle(createAddr, sparseFactorCreateDesc);
								sparseFactorSolveHandle = linker.downcallHandle(solveAddr, sparseFactorSolveDesc);
								sparseFactorDestroyHandle = linker.downcallHandle(destroyAddr, sparseFactorDestroyDesc);
								
								System.err.println("AccelerateFFI.loadSparseWrapperFrom: Successfully created method handles via dlopen/dlsym");
								// Note: We don't close the library handle - it needs to stay loaded
								return true;
							} else {
								System.err.println("AccelerateFFI.loadSparseWrapperFrom: dlsym could not find all symbols");
							}
						}
					} catch (Throwable e) {
						System.err.println("AccelerateFFI.loadSparseWrapperFrom: Exception calling dlopen/dlsym: " + e.getMessage());
						e.printStackTrace();
					}
				} else {
					System.err.println("AccelerateFFI.loadSparseWrapperFrom: dlopen/dlsym not found, falling back to System.load()");
					System.load(libFile.getAbsolutePath());
				}
				
				// Fallback: Try SymbolLookup methods
				SymbolLookup loaderLookup = SymbolLookup.loaderLookup();
				var sparseCreateSymbol = loaderLookup.find("_accelerate_sparse_factor_create");
				var sparseSolveSymbol = loaderLookup.find("_accelerate_sparse_factor_solve");
				var sparseDestroySymbol = loaderLookup.find("_accelerate_sparse_factor_destroy");
				
				if (!sparseCreateSymbol.isPresent()) {
					SymbolLookup libraryLookup = SymbolLookup.libraryLookup(libFile.toPath(), Arena.global());
					sparseCreateSymbol = libraryLookup.find("_accelerate_sparse_factor_create");
					sparseSolveSymbol = libraryLookup.find("_accelerate_sparse_factor_solve");
					sparseDestroySymbol = libraryLookup.find("_accelerate_sparse_factor_destroy");
				}
				
				System.err.println("AccelerateFFI.loadSparseWrapperFrom: SymbolLookup fallback results:");
				System.err.println("  _accelerate_sparse_factor_create: " + sparseCreateSymbol.isPresent());
				System.err.println("  _accelerate_sparse_factor_solve: " + sparseSolveSymbol.isPresent());
				System.err.println("  _accelerate_sparse_factor_destroy: " + sparseDestroySymbol.isPresent());
				
				if (sparseCreateSymbol.isPresent() && 
					sparseSolveSymbol.isPresent() && 
					sparseDestroySymbol.isPresent()) {
					
					sparseFactorCreateHandle = linker.downcallHandle(
						sparseCreateSymbol.get(), 
						sparseFactorCreateDesc
					);
					sparseFactorSolveHandle = linker.downcallHandle(
						sparseSolveSymbol.get(), 
						sparseFactorSolveDesc
					);
					sparseFactorDestroyHandle = linker.downcallHandle(
						sparseDestroySymbol.get(), 
						sparseFactorDestroyDesc
					);
					
					System.err.println("AccelerateFFI.loadSparseWrapperFrom: Successfully loaded sparse wrapper library");
					return true;
				} else {
					System.err.println("AccelerateFFI.loadSparseWrapperFrom: Symbols not found in library");
					return false;
				}
				
			} catch (Exception e) {
				System.err.println("AccelerateFFI.loadSparseWrapperFrom: Exception: " + e.getMessage());
				e.printStackTrace();
				return false;
			}
		}
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

	/* Sparse factorization */

	/**
	 * Creates a sparse factorization from CSC format
	 * @return factorization ID (long) - must be disposed with destroySparseFactorization
	 */
	public static long createSparseFactorization(
		int n,
		int[] columnPointers,
		int[] rowIndices,
		double[] values) {
		if (!IS_AVAILABLE || sparseFactorCreateHandle == null) {
			throw new UnsupportedOperationException("Sparse factorization not available");
		}
		
		Arena arena = Arena.ofShared();
		
		try {
			// Convert int[] to int64_t* for C
			// Note: C expects int64_t, but Java int[] is int32_t
			// We need to convert or the C code needs to handle both
			MemorySegment colPtrSeg = arena.allocateArray(JAVA_LONG, 
				java.util.Arrays.stream(columnPointers).mapToLong(i -> i).toArray());
			MemorySegment rowIndSeg = arena.allocateArray(JAVA_LONG,
				java.util.Arrays.stream(rowIndices).mapToLong(i -> i).toArray());
			MemorySegment valuesSeg = arena.allocateArray(JAVA_DOUBLE, values);
			
			// Call C function - n is passed by value as int
			MemorySegment factorPtr = (MemorySegment) sparseFactorCreateHandle.invokeExact(
				n,
				colPtrSeg,
				rowIndSeg,
				valuesSeg
			);
			
			if (factorPtr == null || factorPtr.address() == 0) {
				arena.close();
				throw new RuntimeException("Sparse factorization creation failed");
			}
			
			long id = FACTORIZATION_COUNTER.getAndIncrement();
			SPARSE_FACTORIZATION_DATA.put(id, new SparseFactorizationData(arena, factorPtr.address(), n));
			
			return id;
		} catch (Throwable e) {
			arena.close();
			throw new RuntimeException("createSparseFactorization failed", e);
		}
	}

	/**
	 * Solves using a sparse factorization
	 */
	public static void solveSparseFactorization(long factorization, double[] b, double[] x) {
		if (!IS_AVAILABLE || sparseFactorSolveHandle == null) {
			throw new UnsupportedOperationException("Sparse factorization not available");
		}
		
		SparseFactorizationData data = SPARSE_FACTORIZATION_DATA.get(factorization);
		if (data == null) {
			throw new IllegalArgumentException("Invalid sparse factorization: " + factorization);
		}
		
		try (Arena arena = Arena.ofConfined()) {
			MemorySegment factorPtr = MemorySegment.ofAddress(data.factorPtr);
			MemorySegment bSeg = arena.allocateArray(JAVA_DOUBLE, b);
			MemorySegment xSeg = arena.allocateArray(JAVA_DOUBLE, x);
			
			int result = (int) sparseFactorSolveHandle.invokeExact(
				factorPtr,
				bSeg,
				xSeg
			);
			
			if (result != 0) {
				throw new RuntimeException("Sparse solve failed with code: " + result);
			}
			
			// Copy result back
			for (int i = 0; i < x.length; i++) {
				x[i] = xSeg.getAtIndex(JAVA_DOUBLE, i);
			}
		} catch (Throwable e) {
			throw new RuntimeException("solveSparseFactorization failed", e);
		}
	}

	/**
	 * Destroys a sparse factorization and frees its memory
	 */
	public static void destroySparseFactorization(long factorization) {
		if (sparseFactorDestroyHandle == null) {
			return;
		}
		
		SparseFactorizationData data = SPARSE_FACTORIZATION_DATA.remove(factorization);
		if (data != null) {
			try (Arena arena = Arena.ofConfined()) {
				MemorySegment factorPtr = MemorySegment.ofAddress(data.factorPtr);
				sparseFactorDestroyHandle.invokeExact(factorPtr);
			} catch (Throwable e) {
				System.err.println("Error destroying sparse factorization: " + e.getMessage());
			} finally {
				data.arena.close();
			}
		}
	}
}
