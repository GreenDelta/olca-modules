/**
 * C wrapper for Apple Accelerate sparse matrix factorization.
 * This provides a simple interface for Java FFI to call Accelerate's
 * SparseFactor/SparseSolve functions.
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
 * Creates a sparse LU factorization from CSC (Compressed Sparse Column) format.
 * 
 * @param n Matrix dimension (n x n)
 * @param colPtr Column pointers array (CSC format, length n+1)
 * @param rowInd Row indices array (CSC format)
 * @param values Non-zero values array (CSC format)
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
    
    // Factorize using LU
    SparseOpaqueFactorization_Double factor = SparseFactor(
        SparseFactorizationLU,
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
 * @param factor Opaque pointer to factorization (from accelerate_sparse_factor_create)
 * @param b Right-hand side vector (input, length n)
 * @param x Solution vector (output, length n)
 * @return 0 on success, non-zero on error
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
 * @param factor Opaque pointer to factorization
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
