/**
 * C wrapper for Apple Accelerate sparse matrix factorization.
 * This provides a simple interface for Java FFI to call Accelerate's
 * SparseFactor/SparseSolve functions.
 * 
 * This wrapper accepts matrices in CSC (Compressed Sparse Column) format,
 * which matches the format used by AccelerateSparseMatrix in Java. The format
 * consists of:
 * - columnStarts (columnPointers): array of length (n+1) indicating where
 *   each column starts in the rowIndices/values arrays
 * - rowIndices: array of row indices for each non-zero value
 * - values: array of non-zero values
 * 
 * The wrapper converts the CSC format to Accelerate's coordinate format,
 * creates a SparseMatrix_Double structure, and performs QR factorization
 * using SparseFactor.
 * 
 * Compile with:
 * gcc -shared -fPIC -o libaccelerate_sparse_wrapper.dylib \
 *     accelerate_sparse_wrapper.c -framework Accelerate -std=c11
 */

#include <vecLib/Sparse/Solve.h>
#include <vecLib/Sparse/Types.h>
#include <stdlib.h>
#include <stdint.h>

/**
 * Creates a sparse QR factorization from CSC (Compressed Sparse Column) format.
 * 
 * This function accepts a matrix in CSC format (matching AccelerateSparseMatrix
 * format) and creates a sparse QR factorization using Apple Accelerate framework.
 * 
 * The input format is:
 * - colPtr: Column start indices (length n+1). colPtr[i] indicates where
 *   column i starts in rowInd/values. colPtr[n] equals the total number of
 *   non-zeros. colPtr[0] must be 0.
 * - rowInd: Row indices for each non-zero value (length = nnz = colPtr[n])
 * - values: Non-zero values (length = nnz)
 * 
 * The function converts CSC to coordinate format, creates a SparseMatrix_Double,
 * and performs QR factorization. The factorization handle is returned as an
 * opaque pointer that must be freed with accelerate_sparse_factor_destroy().
 * 
 * @param n Matrix dimension (n x n, square matrix required)
 * @param colPtr Column pointers array (CSC format, length n+1)
 * @param rowInd Row indices array (CSC format, length = nnz)
 * @param values Non-zero values array (CSC format, length = nnz)
 * @return Opaque pointer to factorization, or NULL on error
 */
void* accelerate_sparse_factor_create(int n, int64_t* colPtr, int64_t* rowInd, double* values) {
    if (n <= 0 || !colPtr || !rowInd || !values) {
        return NULL;
    }
    
    // Count non-zeros
    int64_t nnz = colPtr[n] - colPtr[0];
    if (nnz <= 0) {
        return NULL;
    }
    
    // Convert to int arrays (Accelerate uses int, not int64_t)
    int* rowIndices = (int*)malloc(nnz * sizeof(int));
    int* colIndices = (int*)malloc(nnz * sizeof(int));
    
    if (!rowIndices || !colIndices) {
        free(rowIndices);
        free(colIndices);
        return NULL;
    }
    
    // Convert CSC to coordinate format
    int idx = 0;
    for (int col = 0; col < n; col++) {
        int64_t start = colPtr[col];
        int64_t end = colPtr[col + 1];
        for (int64_t i = start; i < end; i++) {
            rowIndices[idx] = (int)rowInd[i];
            colIndices[idx] = col;
            idx++;
        }
    }
    
    // Create sparse matrix from coordinate format
    // Use zero-initialized attributes (defaults to ordinary, not symmetric)
    SparseAttributes_t attributes = {0};
    
    SparseMatrix_Double matrix = SparseConvertFromCoordinate(
        n,           // rowCount
        n,           // columnCount
        nnz,         // blockCount (number of non-zeros)
        1,           // blockSize (point-wise, not block-wise)
        attributes,  // attributes
        rowIndices,  // row indices
        colIndices,  // column indices
        values       // data values
    );
    
    free(rowIndices);
    free(colIndices);
    
    // Check if matrix creation failed
    if (matrix.structure.rowCount < 0) {
        return NULL;
    }
    
    // Default factorization options (zero-initialized uses defaults)
    SparseSymbolicFactorOptions sfoptions = {0};
    SparseNumericFactorOptions nfoptions = {0};
    
    // Factorize using QR
    SparseOpaqueFactorization_Double factor = SparseFactor(
        SparseFactorizationQR,
        matrix,
        sfoptions,
        nfoptions
    );
    
    // Check factorization status - use 0 as success indicator
    if (factor.status != 0) {
        // Clean up matrix if it was allocated
        if (matrix.structure.attributes._allocatedBySparse) {
            free(matrix.structure.columnStarts);
        }
        return NULL;
    }
    
    // Allocate memory to hold the factorization
    SparseOpaqueFactorization_Double* factorPtr = (SparseOpaqueFactorization_Double*)malloc(sizeof(SparseOpaqueFactorization_Double));
    if (!factorPtr) {
        SparseCleanup(factor);
        if (matrix.structure.attributes._allocatedBySparse) {
            free(matrix.structure.columnStarts);
        }
        return NULL;
    }
    
    *factorPtr = factor;
    
    // Clean up matrix (factorization has its own copy)
    if (matrix.structure.attributes._allocatedBySparse) {
        free(matrix.structure.columnStarts);
    }
    
    return (void*)factorPtr;
}

/**
 * Solves A * x = b using a pre-computed sparse factorization.
 * 
 * This function solves the linear system A * x = b where A is the matrix
 * that was factorized by accelerate_sparse_factor_create(). The factorization
 * uses QR decomposition, so this solves the least-squares problem if A is
 * not square or overdetermined.
 * 
 * @param factor Opaque pointer to factorization (from accelerate_sparse_factor_create)
 * @param b Right-hand side vector (input, length n, must be pre-allocated)
 * @param x Solution vector (output, length n, must be pre-allocated)
 * @return 0 on success, non-zero on error (-1: null pointer, -2: invalid factor, -3: solve failed)
 */
int accelerate_sparse_factor_solve(void* factor, double* b, double* x) {
    if (!factor || !b || !x) {
        return -1;
    }
    
    SparseOpaqueFactorization_Double* factorPtr = (SparseOpaqueFactorization_Double*)factor;
    
    // Check status - use 0 as success
    if (factorPtr->status != 0) {
        return -2;
    }
    
    // Create dense vector structures (DenseVector_Double doesn't have stride field)
    DenseVector_Double rhs = {
        .count = factorPtr->symbolicFactorization.rowCount,
        .data = b
    };
    
    DenseVector_Double sol = {
        .count = factorPtr->symbolicFactorization.rowCount,
        .data = x
    };
    
    // Solve
    SparseSolve(*factorPtr, rhs, sol);
    
    // Check solve status - if count becomes negative, there was an error
    if (sol.count < 0) {
        return -3;
    }
    
    return 0;
}

/**
 * Destroys a sparse factorization and frees its memory.
 * 
 * This function cleans up the factorization created by
 * accelerate_sparse_factor_create(). It calls Accelerate's SparseCleanup
 * and frees the wrapper structure. After calling this, the factor pointer
 * is invalid and must not be used again.
 * 
 * @param factor Opaque pointer to factorization (from accelerate_sparse_factor_create)
 */
void accelerate_sparse_factor_destroy(void* factor) {
    if (!factor) {
        return;
    }
    
    SparseOpaqueFactorization_Double* factorPtr = (SparseOpaqueFactorization_Double*)factor;
    
    // Clean up factorization
    SparseCleanup(*factorPtr);
    
    // Free the wrapper structure
    free(factorPtr);
}
