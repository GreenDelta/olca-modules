/**
 * C wrapper for Apple Accelerate sparse matrix factorization.
 * This provides a simple interface for Java FFI to call Accelerate's
 * SparseFactor/SparseSolve functions.
 * 
 * This wrapper accepts matrices in CSC (Compressed Sparse Column) format,
 * which matches the format used by CSCMatrix in Java. The format
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
 * Compilation is handled by Maven (see pom.xml).
 * Debug symbols are included (-g -O0) for debugging.
 * To rebuild: mvn compile (or mvn clean compile)
 * 
 * Debug output is written to stderr and will appear in the Java console/logs.
 */

#include <vecLib/Sparse/Solve.h>
#include <vecLib/Sparse/Types.h>
#include <stdlib.h>
#include <stdint.h>
#include <stdio.h>
#include <string.h>

// Error codes for debugging
#define ERR_NULL_POINTER -1
#define ERR_INVALID_DIMENSION -2
#define ERR_INVALID_NNZ -3
#define ERR_MALLOC_FAILED -4
#define ERR_INVALID_CSC_FORMAT -5
#define ERR_INVALID_INDICES -6
#define ERR_MATRIX_CREATION_FAILED -7
#define ERR_FACTORIZATION_FAILED -8
#define ERR_FACTOR_ALLOC_FAILED -9

// Global error tracking (for debugging)
static int last_error_code = 0;
static const char* last_error_function = NULL;

/**
 * Validates input parameters for CSC format conversion.
 * Returns 0 on success, negative error code on failure.
 */
static int validate_csc_input(int n, int64_t* colPtr, int64_t* rowInd, double* values) {
    last_error_function = "validate_csc_input";
    
    if (n <= 0) {
        last_error_code = ERR_INVALID_DIMENSION;
        return ERR_INVALID_DIMENSION;
    }
    
    if (!colPtr || !rowInd || !values) {
        last_error_code = ERR_NULL_POINTER;
        return ERR_NULL_POINTER;
    }
    
    // Validate colPtr array
    if (colPtr[0] != 0) {
        last_error_code = ERR_INVALID_CSC_FORMAT;
        return ERR_INVALID_CSC_FORMAT;
    }
    
    int64_t nnz = colPtr[n] - colPtr[0];
    if (nnz <= 0) {
        last_error_code = ERR_INVALID_NNZ;
        return ERR_INVALID_NNZ;
    }
    
    // Validate that colPtr is non-decreasing
    for (int i = 0; i < n; i++) {
        if (colPtr[i] > colPtr[i + 1]) {
            last_error_code = ERR_INVALID_CSC_FORMAT;
            return ERR_INVALID_CSC_FORMAT;
        }
    }
    
    return 0;
}

/**
 * Validates row indices are within bounds [0, n-1].
 * Returns 0 on success, negative error code on failure.
 */
static int validate_row_indices(int n, int64_t nnz, int64_t* colPtr, int64_t* rowInd) {
    last_error_function = "validate_row_indices";
    
    for (int col = 0; col < n; col++) {
        int64_t start = colPtr[col];
        int64_t end = colPtr[col + 1];
        for (int64_t i = start; i < end; i++) {
            if (rowInd[i] < 0 || rowInd[i] >= n) {
                last_error_code = ERR_INVALID_INDICES;
                return ERR_INVALID_INDICES;
            }
        }
    }
    
    return 0;
}

/**
 * Converts CSC format to coordinate format (COO).
 * Allocates and returns rowIndices and colIndices arrays.
 * Returns 0 on success, negative error code on failure.
 */
static int convert_csc_to_coordinate(
    int n,
    int64_t nnz,
    int64_t* colPtr,
    int64_t* rowInd,
    int** out_rowIndices,
    int** out_colIndices
) {
    last_error_function = "convert_csc_to_coordinate";
    
    // Allocate coordinate arrays
    int* rowIndices = (int*)malloc(nnz * sizeof(int));
    int* colIndices = (int*)malloc(nnz * sizeof(int));
    
    if (!rowIndices || !colIndices) {
        free(rowIndices);
        free(colIndices);
        last_error_code = ERR_MALLOC_FAILED;
        return ERR_MALLOC_FAILED;
    }
    
    // Convert CSC to coordinate format
    int idx = 0;
    for (int col = 0; col < n; col++) {
        int64_t start = colPtr[col];
        int64_t end = colPtr[col + 1];
        
        if (end < start) {
            free(rowIndices);
            free(colIndices);
            last_error_code = ERR_INVALID_CSC_FORMAT;
            return ERR_INVALID_CSC_FORMAT;
        }
        
        for (int64_t i = start; i < end; i++) {
            if (idx >= nnz) {
                free(rowIndices);
                free(colIndices);
                last_error_code = ERR_INVALID_CSC_FORMAT;
                return ERR_INVALID_CSC_FORMAT;
            }
            
            int rowIdx = (int)rowInd[i];
            if (rowIdx < 0 || rowIdx >= n) {
                free(rowIndices);
                free(colIndices);
                last_error_code = ERR_INVALID_INDICES;
                return ERR_INVALID_INDICES;
            }
            
            rowIndices[idx] = rowIdx;
            colIndices[idx] = col;
            idx++;
        }
    }
    
    if (idx != nnz) {
        free(rowIndices);
        free(colIndices);
        last_error_code = ERR_INVALID_CSC_FORMAT;
        return ERR_INVALID_CSC_FORMAT;
    }
    
    *out_rowIndices = rowIndices;
    *out_colIndices = colIndices;
    return 0;
}

/**
 * Creates a SparseMatrix_Double from coordinate format.
 * Returns 0 on success, negative error code on failure.
 */
static int create_sparse_matrix(
    int n,
    int64_t nnz,
    int* rowIndices,
    int* colIndices,
    double* values,
    SparseMatrix_Double* out_matrix
) {
    last_error_function = "create_sparse_matrix";
    
    fprintf(stderr, "[DEBUG] create_sparse_matrix: n=%d, nnz=%lld\n", n, (long long)nnz);
    fprintf(stderr, "[DEBUG] Input arrays: rowIndices=%p, colIndices=%p, values=%p\n",
            (void*)rowIndices, (void*)colIndices, (void*)values);
    if (nnz > 0 && nnz <= 10) {
        fprintf(stderr, "[DEBUG] First few coordinates: ");
        for (int i = 0; i < (int)nnz && i < 5; i++) {
            fprintf(stderr, "(%d,%d)=%.2f ", rowIndices[i], colIndices[i], values[i]);
        }
        fprintf(stderr, "\n");
    }
    fflush(stderr);
    
    // Use zero-initialized attributes (defaults to ordinary, not symmetric)
    SparseAttributes_t attributes = {0};
    
    fprintf(stderr, "[DEBUG] Calling SparseConvertFromCoordinate...\n");
    fflush(stderr);
    
    // Create sparse matrix from coordinate format
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
    
    fprintf(stderr, "[DEBUG] SparseConvertFromCoordinate returned\n");
    fprintf(stderr, "[DEBUG] Matrix structure: rowCount=%d, columnCount=%d\n",
            matrix.structure.rowCount, matrix.structure.columnCount);
    fprintf(stderr, "[DEBUG] columnStarts=%p, _allocatedBySparse=%d\n",
            (void*)matrix.structure.columnStarts,
            matrix.structure.attributes._allocatedBySparse);
    fflush(stderr);
    
    // Check if matrix creation failed
    // According to Accelerate docs, a negative rowCount indicates failure
    if (matrix.structure.rowCount < 0 || matrix.structure.columnCount < 0) {
        fprintf(stderr, "[ERROR] Matrix creation failed: rowCount=%d, columnCount=%d\n",
                matrix.structure.rowCount, matrix.structure.columnCount);
        fflush(stderr);
        last_error_code = ERR_MATRIX_CREATION_FAILED;
        return ERR_MATRIX_CREATION_FAILED;
    }
    
    *out_matrix = matrix;
    return 0;
}

/**
 * Validates a sparse matrix structure before factorization.
 * Returns 0 on success, negative error code on failure.
 */
static int validate_matrix_before_factorization(SparseMatrix_Double matrix) {
    last_error_function = "validate_matrix_before_factorization";
    
    // Check basic structure validity
    if (matrix.structure.rowCount <= 0 || matrix.structure.columnCount <= 0) {
        last_error_code = ERR_MATRIX_CREATION_FAILED;
        return ERR_MATRIX_CREATION_FAILED;
    }
    
    // Check that columnStarts is valid
    if (!matrix.structure.columnStarts) {
        last_error_code = ERR_MATRIX_CREATION_FAILED;
        return ERR_MATRIX_CREATION_FAILED;
    }
    
    // Check that blockCount is reasonable
    int64_t blockCount = matrix.structure.columnStarts[matrix.structure.columnCount];
    if (blockCount < 0 || blockCount > (int64_t)matrix.structure.rowCount * (int64_t)matrix.structure.columnCount) {
        last_error_code = ERR_MATRIX_CREATION_FAILED;
        return ERR_MATRIX_CREATION_FAILED;
    }
    
    return 0;
}

/**
 * Performs QR factorization on a sparse matrix.
 * Returns 0 on success, negative error code on failure.
 */
static int perform_factorization(
    SparseMatrix_Double matrix,
    SparseOpaqueFactorization_Double* out_factor
) {
    last_error_function = "perform_factorization";
    
    fprintf(stderr, "[DEBUG] perform_factorization: Starting...\n");
    fflush(stderr);
    
    // Validate matrix structure before factorization
    int err = validate_matrix_before_factorization(matrix);
    if (err != 0) {
        fprintf(stderr, "[ERROR] Matrix validation failed: error code %d\n", err);
        fflush(stderr);
        return err;
    }
    
    fprintf(stderr, "[DEBUG] Matrix validation passed. About to call SparseFactor...\n");
    fprintf(stderr, "[DEBUG] Matrix details:\n");
    fprintf(stderr, "  rowCount=%d, columnCount=%d\n", matrix.structure.rowCount, matrix.structure.columnCount);
    fprintf(stderr, "  columnStarts pointer=%p\n", (void*)matrix.structure.columnStarts);
    if (matrix.structure.columnStarts) {
        fprintf(stderr, "  columnStarts[0]=%lld\n", (long long)matrix.structure.columnStarts[0]);
        if (matrix.structure.columnCount > 0) {
            fprintf(stderr, "  columnStarts[%d]=%lld\n",
                    matrix.structure.columnCount,
                    (long long)matrix.structure.columnStarts[matrix.structure.columnCount]);
        }
    }
    fprintf(stderr, "  blockSize=%d\n", matrix.structure.blockSize);
    fprintf(stderr, "  _allocatedBySparse=%d\n", matrix.structure.attributes._allocatedBySparse);
    fflush(stderr);
    
    // Factorize using QR
    // According to Apple documentation, SparseFactor takes only the factorization type and matrix
    // This is where the crash occurs - if it happens, we know the matrix structure
    // passed validation but Accelerate's internal code failed
    fprintf(stderr, "[DEBUG] Calling SparseFactor(SparseFactorizationQR, matrix)...\n");
    fflush(stderr);
    
    SparseOpaqueFactorization_Double factor = SparseFactor(
        SparseFactorizationQR,
        matrix
    );
    
    fprintf(stderr, "[DEBUG] SparseFactor returned, status=%d\n", factor.status);
    fflush(stderr);
    
    // Check factorization status - use 0 as success indicator
    if (factor.status != 0) {
        fprintf(stderr, "[ERROR] Factorization failed with status=%d\n", factor.status);
        fflush(stderr);
        last_error_code = ERR_FACTORIZATION_FAILED;
        return ERR_FACTORIZATION_FAILED;
    }
    
    fprintf(stderr, "[DEBUG] Factorization successful!\n");
    fflush(stderr);
    
    *out_factor = factor;
    return 0;
}

/**
 * Allocates and stores the factorization handle.
 * Returns 0 on success, negative error code on failure.
 */
static int store_factorization(
    SparseOpaqueFactorization_Double factor,
    void** out_factorPtr
) {
    last_error_function = "store_factorization";
    
    // Allocate memory to hold the factorization
    SparseOpaqueFactorization_Double* factorPtr = 
        (SparseOpaqueFactorization_Double*)malloc(sizeof(SparseOpaqueFactorization_Double));
    
    if (!factorPtr) {
        last_error_code = ERR_FACTOR_ALLOC_FAILED;
        return ERR_FACTOR_ALLOC_FAILED;
    }
    
    *factorPtr = factor;
    *out_factorPtr = (void*)factorPtr;
    return 0;
}

/**
 * Creates a sparse QR factorization from CSC (Compressed Sparse Column) format.
 * 
 * This function accepts a matrix in CSC format (matching CSCMatrix format
 * in Java) and creates a sparse QR factorization using Apple Accelerate framework.
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
    last_error_code = 0;
    last_error_function = "accelerate_sparse_factor_create";
    
    fprintf(stderr, "[DEBUG] accelerate_sparse_factor_create: n=%d\n", n);
    fflush(stderr);
    
    // Step 1: Validate input parameters
    fprintf(stderr, "[DEBUG] Step 1: Validating CSC input...\n");
    fflush(stderr);
    int err = validate_csc_input(n, colPtr, rowInd, values);
    if (err != 0) {
        fprintf(stderr, "[ERROR] Step 1 failed: error code %d\n", err);
        fflush(stderr);
        return NULL;
    }
    
    // Count non-zeros
    int64_t nnz = colPtr[n] - colPtr[0];
    fprintf(stderr, "[DEBUG] Matrix: %dx%d, nnz=%lld\n", n, n, (long long)nnz);
    fflush(stderr);
    
    // Step 2: Validate row indices are within bounds
    fprintf(stderr, "[DEBUG] Step 2: Validating row indices...\n");
    fflush(stderr);
    err = validate_row_indices(n, nnz, colPtr, rowInd);
    if (err != 0) {
        fprintf(stderr, "[ERROR] Step 2 failed: error code %d\n", err);
        fflush(stderr);
        return NULL;
    }
    
    // Step 3: Convert CSC to coordinate format
    fprintf(stderr, "[DEBUG] Step 3: Converting CSC to coordinate format...\n");
    fflush(stderr);
    int* rowIndices = NULL;
    int* colIndices = NULL;
    err = convert_csc_to_coordinate(n, nnz, colPtr, rowInd, &rowIndices, &colIndices);
    if (err != 0) {
        fprintf(stderr, "[ERROR] Step 3 failed: error code %d\n", err);
        fflush(stderr);
        return NULL;
    }
    fprintf(stderr, "[DEBUG] Step 3: Conversion complete, allocated %lld elements\n", (long long)nnz);
    fflush(stderr);
    
    // Step 4: Create sparse matrix from coordinate format
    fprintf(stderr, "[DEBUG] Step 4: Creating sparse matrix from coordinate format...\n");
    fflush(stderr);
    SparseMatrix_Double matrix;
    err = create_sparse_matrix(n, nnz, rowIndices, colIndices, values, &matrix);
    
    if (err != 0) {
        fprintf(stderr, "[ERROR] Step 4 failed: error code %d\n", err);
        fflush(stderr);
        free(rowIndices);
        free(colIndices);
        return NULL;
    }
    
    fprintf(stderr, "[DEBUG] Step 4: Matrix created - rows=%d, cols=%d, allocatedBySparse=%d\n",
            matrix.structure.rowCount, matrix.structure.columnCount,
            matrix.structure.attributes._allocatedBySparse);
    fflush(stderr);
    
    // Free coordinate arrays (no longer needed after matrix creation)
    fprintf(stderr, "[DEBUG] Freeing coordinate arrays (rowIndices=%p, colIndices=%p)...\n",
            (void*)rowIndices, (void*)colIndices);
    fflush(stderr);
    free(rowIndices);
    free(colIndices);
    
    // Step 5: Perform QR factorization
    fprintf(stderr, "[DEBUG] Step 5: Performing QR factorization...\n");
    fprintf(stderr, "[DEBUG] Matrix structure before factorization:\n");
    fprintf(stderr, "  rowCount=%d, columnCount=%d\n", matrix.structure.rowCount, matrix.structure.columnCount);
    fprintf(stderr, "  columnStarts=%p\n", (void*)matrix.structure.columnStarts);
    if (matrix.structure.columnStarts) {
        fprintf(stderr, "  columnStarts[0]=%lld, columnStarts[%d]=%lld\n",
                (long long)matrix.structure.columnStarts[0],
                matrix.structure.columnCount,
                (long long)matrix.structure.columnStarts[matrix.structure.columnCount]);
    }
    fprintf(stderr, "  blockSize=%d\n", matrix.structure.blockSize);
    fprintf(stderr, "  _allocatedBySparse=%d\n", matrix.structure.attributes._allocatedBySparse);
    fflush(stderr);
    
    SparseOpaqueFactorization_Double factor;
    err = perform_factorization(matrix, &factor);
    
    fprintf(stderr, "[DEBUG] Step 5: Factorization call returned, status=%d\n", factor.status);
    fflush(stderr);
    
    // Clean up matrix if it was allocated by SparseConvertFromCoordinate
    if (matrix.structure.attributes._allocatedBySparse) {
        fprintf(stderr, "[DEBUG] Cleaning up matrix columnStarts...\n");
        fflush(stderr);
        free(matrix.structure.columnStarts);
    }
    
    if (err != 0) {
        fprintf(stderr, "[ERROR] Step 5 failed: error code %d\n", err);
        fflush(stderr);
        return NULL;
    }
    
    // Step 6: Store factorization handle
    fprintf(stderr, "[DEBUG] Step 6: Storing factorization handle...\n");
    fflush(stderr);
    void* factorPtr = NULL;
    err = store_factorization(factor, &factorPtr);
    if (err != 0) {
        fprintf(stderr, "[ERROR] Step 6 failed: error code %d\n", err);
        fflush(stderr);
        SparseCleanup(factor);
        return NULL;
    }
    
    fprintf(stderr, "[DEBUG] accelerate_sparse_factor_create: SUCCESS, returning factorPtr=%p\n", factorPtr);
    fflush(stderr);
    
    return factorPtr;
}

/**
 * Gets the last error code for debugging purposes.
 * Returns the error code from the last operation, or 0 if no error.
 */
int accelerate_sparse_get_last_error(void) {
    return last_error_code;
}

/**
 * Gets the name of the function where the last error occurred.
 * Returns a pointer to a static string, or NULL if no error.
 */
const char* accelerate_sparse_get_last_error_function(void) {
    return last_error_function;
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
