#include <jni.h>

#ifdef __cplusplus
extern "C"
{
#endif

    // Declarations of external functions

    // BLAS

    // general matrix-vector multiplication
    void dgemv_(jchar *TRANS, jint *M, jint *N, jdouble *ALPHA, jdouble *A,
                jint *LDA, jdouble *X, jint *INCX, jdouble *BETA, jdouble *Y,
                jint *INCY);

    // general matrix-matrix multiplication
    void dgemm_(jchar *TRANSA, jchar *TRANSB, jint *M, jint *N, jint *K,
                jdouble *ALPHA, jdouble *A, jint *LDA, jdouble *B, jint *LDB,
                jdouble *BETA, jdouble *C, jint *LDC);

#ifdef __cplusplus
}
#endif
