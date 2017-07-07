#ifndef OLCA_LAPACK_H
#define	OLCA_LAPACK_H

#include <jni.h>
#include <stdlib.h>

#ifdef	__cplusplus
extern "C" {
#endif

    // solves A * X = B
    void dgesv_(jint *N, jint *NHS, jdouble *A, jint *LDA, jint *IPIV,
            jdouble *B, jint *LDB, jint *INFO);

    void sgesv_(jint *N, jint *NHS, jfloat *A, jint *LDA, jint *IPIV,
            jfloat *B, jint *LDB, jint *INFO);

    // LU decomposition of a general matrix
    void dgetrf_(jint* M, jint *N, jdouble* A, jint* lda, jint* IPIV, jint*
            INFO);

    void sgetrf_(jint* M, jint *N, jfloat* A, jint* lda, jint* IPIV, jint*
            INFO);

    // generate inverse of a matrix given its LU decomposition
    void dgetri_(jint* N, jdouble* A, jint* lda, jint* IPIV, jdouble* WORK,
            jint* lwork, jint* INFO);

    void sgetri_(jint* N, jfloat* A, jint* lda, jint* IPIV, jfloat* WORK,
            jint* lwork, jint* INFO);

    // solves A * X = B in mixed precision with iterative refinement
    void dsgesv_(jint *N, jint *NRHS, jdouble *A, jint *LDA, jint *IPIV,
            jdouble *B, jint *LDB, jdouble *X, jint *LDX, jdouble *WORK,
            jfloat *SWORK, jint *ITER, jint *INFO);

    // solves A * X = B where A contains the LU factorization
    void dgetrs_(jchar *TRANS, jint *N, jint *NRHS, jdouble *A, jint *LDA,
            jint *IPIV, jdouble *B, jint *LDB, jint *INFO);

    void sgetrs_(jchar *TRANS, jint *N, jint *NRHS, jfloat *A, jint *LDA,
            jint *IPIV, jfloat *B, jint *LDB, jint *INFO);

    // calculates factors for matrix equilibration
    void dgeequ_(jint *M, jint *N, jdouble *A, jint *LDA, jdouble *R, jdouble *C,
            jdouble *ROWCOND, jdouble *COLCOND, jdouble *AMAX, jint *INFO);

    void sgeequ_(jint *M, jint *N, jfloat *A, jint *LDA, jfloat *R, jfloat *C,
            jfloat *ROWCOND, jfloat *COLCOND, jfloat *AMAX, jint *INFO);


    // more user friendly functions

    /**
     * Inverts the given matrix.
     * 
     * @param n[in] the dimension of the matrix (n = rows = columns)
     * @param a[io] on entry: the matrix to be inverted, on exit: the inverse 
     *        (size = n * n)
     * @return the LAPACK return code
     */
    inline jint dInvert(jint n, jdouble *a) {
        jint *ipiv = (jint *) malloc(n * sizeof (jint));
        jint lwork = 64 * 2 * n;
        jdouble *work = (jdouble *) malloc(lwork * sizeof (jdouble));
        jint info;
        dgetrf_(&n, &n, a, &n, ipiv, &info);
        if (info)
            return info;
        dgetri_(&n, a, &n, ipiv, work, &lwork, &info);
        free(ipiv);
        free(work);
        return info;
    }

    /**
     * Inverts the given matrix. (single precision)
     * 
     * @param n[in] the dimension of the matrix (n = rows = columns)
     * @param a[io] on entry: the matrix to be inverted, on exit: the inverse 
     *        (size = n * n)
     * @return the LAPACK return code
     */
    inline jint sInvert(jint n, jfloat *a) {
        jint *ipiv = (jint *) malloc(n * sizeof (jint));
        jint lwork = 64 * 2 * n;
        jfloat *work = (jfloat *) malloc(lwork * sizeof (jfloat));
        jint info;
        sgetrf_(&n, &n, a, &n, ipiv, &info);
        if (info)
            return info;
        sgetri_(&n, a, &n, ipiv, work, &lwork, &info);
        free(ipiv);
        free(work);
        return info;
    }

    /**
     * Solves a system of linear equations A * X = B for general matrices. It 
     * calls the LAPACK DGESV routine. 
     * 
     * @param n[in] the dimension of the matrix A (n = rows = columns of A)
     * @param bColums[in] the number of columns of the matrix B 
     * @param a[io] on entry the matrix A, on exit the LU factorization of A 
     *       (size = n * n)
     * @param b[io] on entry the matrix B, on exit the solution of the equation
     *       (size = n * bColums) 
     * @return the LAPACK return code
     */
    inline jint dSolve(jint rows, jint bColums, jdouble *a, jdouble *b) {
        jint *ipiv = (jint *) malloc(rows * sizeof (jint));
        jint info;
        dgesv_(&rows, &bColums, a, &rows, ipiv, b, &rows, &info);
        free(ipiv);
        return info;
    }

    /**
     * Solves a system of linear equations A * X = B for general matrices. It 
     * calls the LAPACK DGESV routine. (single precision)
     * 
     * @param n[in] the dimension of the matrix A (n = rows = columns of A)
     * @param bColums[in] the number of columns of the matrix B 
     * @param a[io] on entry the matrix A, on exit the LU factorization of A 
     *       (size = n * n)
     * @param b[io] on entry the matrix B, on exit the solution of the equation
     *       (size = n * bColums) 
     * @return the LAPACK return code
     */
    inline jint sSolve(jint rows, jint bColums, jfloat *a, jfloat *b) {
        jint *ipiv = (jint *) malloc(rows * sizeof (jint));
        jint info;
        sgesv_(&rows, &bColums, a, &rows, ipiv, b, &rows, &info);
        free(ipiv);
        return info;
    }

    /**
     * Solves the system of linear equations A * X = B for general matrices in
     * single precision with iterative refinement.
     * 
     * @TODO: add parameter doc
     */
    inline jint dsSolve(jint rows, jint bColumns, jdouble *a, jdouble *b,
            jdouble *x) {
        jint *ipiv = (jint *) malloc(rows * sizeof (jint));
        jdouble *work = (jdouble *) malloc(rows * bColumns * sizeof (jdouble));
        jfloat *swork = (jfloat *) malloc(rows * (rows + bColumns) * sizeof (jfloat));
        jint iter;
        jint info;
        dsgesv_(&rows, &bColumns, a, &rows, ipiv, b, &rows, x, &rows, work, swork,
                &iter, &info);
        free(ipiv);
        free(work);
        free(swork);
        return info;
    }

    /**
     * Computes the LU factorization of a square matrix A.
     * 
     * @param n[in] the dimension of a (n = rows = columns)
     * @param a[io] on entry the matrix to be factorized, on exit the LU 
     *        factorization of A (size = n*n).
     * @param pivots[out] the pivot indices of the factorization (size = n)
     * @return the LAPACK return code (0=success)
     */
    inline jint dLu(jint n, jdouble *a, jint *pivots) {
        jint info;
        dgetrf_(&n, &n, a, &n, pivots, &info);
        return info;
    }

    /**
     * Computes the LU factorization of a square matrix A. (single precision)
     * 
     * @param n[in] the dimension of a (n = rows = columns)
     * @param a[io] on entry the matrix to be factorized, on exit the LU 
     *        factorization of A (size = n*n).
     * @param pivots[out] the pivot indices of the factorization (size = n)
     * @return the LAPACK return code (0=success)
     */
    inline jint sLu(jint n, jfloat *a, jint *pivots) {
        jint info;
        sgetrf_(&n, &n, a, &n, pivots, &info);
        return info;
    }

    /**
     * Solves A * X = B where A is already factorized.
     * 
     * @param rows[in] the number of rows in matrix A
     * @param bCols[in] the number of columns of the right side
     * @param lu[in] the LU factorization of matrix A (size = rows * rows)
     * @param pivots[in] the pivot indices (see method lu)
     * @param b[io] on entry the right hand side of the equation, on exit the
     *        solution of the equation (size = rows * bCols)
     * @return the LAPACK return code (0=success)
     */
    inline jint dSolveLu(jint rows, jint bCols, jdouble *lu, jint *pivots,
            jdouble *b) {
        jchar trans = 'N';
        jint info;
        dgetrs_(&trans, &rows, &bCols, lu, &rows, pivots, b, &rows, &info);
        return info;
    }

    /**
     * Solves A * X = B where A is already factorized. (single precision)
     * 
     * @param rows[in] the number of rows in matrix A
     * @param bCols[in] the number of columns of the right side
     * @param lu[in] the LU factorization of matrix A (size = rows * rows)
     * @param pivots[in] the pivot indices (see method lu)
     * @param b[io] on entry the right hand side of the equation, on exit the
     *        solution of the equation (size = rows * bCols)
     * @return the LAPACK return code (0=success)
     */
    inline jint sSolveLu(jint rows, jint bCols, jfloat *lu, jint *pivots,
            jfloat *b) {
        jchar trans = 'N';
        jint info;
        sgetrs_(&trans, &rows, &bCols, lu, &rows, pivots, b, &rows, &info);
        return info;
    }

    inline jint dEquilibrate(jint rows, jint cols, jdouble *a, jdouble *r,
            jdouble *c) {
        jdouble rowCond;
        jdouble colCond;
        jdouble amax;
        jint info;
        dgeequ_(&rows, &cols, a, &rows, r, c, &rowCond, &colCond, &amax, &info);
        return info;
    }

    inline jint sEquilibrate(jint rows, jint cols, jfloat *a, jfloat *r,
            jfloat *c) {
        jfloat rowCond;
        jfloat colCond;
        jfloat amax;
        jint info;
        sgeequ_(&rows, &cols, a, &rows, r, c, &rowCond, &colCond, &amax, &info);
        return info;
    }

#ifdef	__cplusplus
}
#endif

#endif	/* OLCA_LAPACK_H */

