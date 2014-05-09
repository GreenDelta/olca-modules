#ifndef OLCA_BLAS_H
#define	OLCA_BLAS_H

#include <jni.h>
#ifdef	__cplusplus
extern "C" {
#endif

    // general matrix-vector multiplication
    void dgemv_(jchar *TRANS, jint *M, jint *N, jdouble *ALPHA, jdouble *A,
            jint *LDA, jdouble *X, jint *INCX, jdouble *BETA, jdouble *Y,
            jint *INCY);

    void sgemv_(jchar *TRANS, jint *M, jint *N, jfloat *ALPHA, jfloat *A,
            jint *LDA, jfloat *X, jint *INCX, jfloat *BETA, jfloat *Y,
            jint *INCY);

    // general matrix-matrix multiplication
    void dgemm_(jchar *TRANSA, jchar *TRANSB, jint *M, jint *N, jint *K,
            jdouble *ALPHA, jdouble *A, jint *LDA, jdouble *B, jint *LDB,
            jdouble *BETA, jdouble *C, jint *LDC);

    void sgemm_(jchar *TRANSA, jchar *TRANSB, jint *M, jint *N, jint *K,
            jfloat *ALPHA, jfloat *A, jint *LDA, jfloat *B, jint *LDB,
            jfloat *BETA, jfloat *C, jint *LDC);

    /**
     * Matrix-matrix multiplication: C := A * B
     * 
     * @param rowsA[in] number of rows of matrix A
     * @param colsB[in] number of columns of matrix B
     * @param k[in] number of columns of matrix A and number of rows of matrix B
     * @param a[in] matrix A (size = rowsA*k)
     * @param b[in] matrix B (size = k * colsB)
     * @param c[out] matrix C (size = rowsA * colsB)
     */
    inline void dMmult(jint rowsA, jint colsB, jint k, jdouble *a,
            jdouble *b, jdouble *c) {
        jchar trans = 'N';
        jdouble alpha = 1;
        jdouble beta = 0;
        dgemm_(&trans, &trans, &rowsA, &colsB, &k, &alpha, a, &rowsA, b, &k,
                &beta, c, &rowsA);
    }

    /**
     * Matrix-matrix multiplication: C := A * B (single precision)
     * 
     * @param rowsA[in] number of rows of matrix A
     * @param colsB[in] number of columns of matrix B
     * @param k[in] number of columns of matrix A and number of rows of matrix B
     * @param a[in] matrix A (size = rowsA*k)
     * @param b[in] matrix B (size = k * colsB)
     * @param c[out] matrix C (size = rowsA * colsB)
     */
    inline void sMmult(jint rowsA, jint colsB, jint k, jfloat *a,
            jfloat *b, jfloat *c) {
        jchar trans = 'N';
        jfloat alpha = 1;
        jfloat beta = 0;
        sgemm_(&trans, &trans, &rowsA, &colsB, &k, &alpha, a, &rowsA, b, &k,
                &beta, c, &rowsA);
    }

    /**
     * Matrix-vector multiplication: y:= A * x
     * 
     * @param rowsA[in] rows of matrix A 
     * @param colsA[in] columns of matrix A
     * @param a[in] the matrix A
     * @param x[in] the vector x
     * @param y[out] the resulting vector y
     */
    inline void dMVmult(jint rowsA, jint colsA, jdouble *a, jdouble *x,
            jdouble *y) {
        jchar trans = 'N';
        jdouble alpha = 1;
        jint incx = 1;
        jdouble beta = 0;
        jint incy = 1;
        dgemv_(&trans, &rowsA, &colsA, &alpha, a, &rowsA, x, &incx, &beta, y,
                &incy);
    }

    /**
     * Matrix-vector multiplication: y:= A * x (single precision)
     * 
     * @param rowsA[in] rows of matrix A 
     * @param colsA[in] columns of matrix A
     * @param a[in] the matrix A
     * @param x[in] the vector x
     * @param y[out] the resulting vector y
     */
    inline void sMVmult(jint rowsA, jint colsA, jfloat *a, jfloat *x,
            jfloat *y) {
        jchar trans = 'N';
        jfloat alpha = 1;
        jint incx = 1;
        jfloat beta = 0;
        jint incy = 1;
        sgemv_(&trans, &rowsA, &colsA, &alpha, a, &rowsA, x, &incx, &beta, y,
                &incy);
    }

#ifdef	__cplusplus
}
#endif

#endif	/* OLCA_BLAS_H */

