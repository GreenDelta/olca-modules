#include <stdlib.h>
#include <stdint.h>
#include <jni.h>

// BLAS

// mvmult -> dgemv64_
// general matrix-vector multiplication
void dgemv64_(
    jchar *TRANS,
    int64_t *M,
    int64_t *N,
    jdouble *ALPHA,
    jdouble *A,
    int64_t *LDA,
    jdouble *X,
    int64_t *INCX,
    jdouble *BETA,
    jdouble *Y,
    int64_t *INCY);

JNIEXPORT void JNICALL Java_org_openlca_julia_Julia_mvmult(
    JNIEnv *env, jclass jclazz, jint rowsA, jint colsA, jdoubleArray a,
    jdoubleArray x, jdoubleArray y)
{
    jdouble *aPtr = (*env)->GetDoubleArrayElements(env, a, NULL);
    jdouble *xPtr = (*env)->GetDoubleArrayElements(env, x, NULL);
    jdouble *yPtr = (*env)->GetDoubleArrayElements(env, y, NULL);

    jchar trans = 'N';
    jdouble alpha = 1;
    jdouble beta = 0;
    int64_t inc = 1;
    int64_t rowsA_64 = (int64_t)rowsA;
    int64_t colsA_64 = (int64_t)colsA;
    dgemv64_(&trans, &rowsA_64, &colsA_64, &alpha, aPtr, &rowsA_64, xPtr, &inc,
             &beta, yPtr, &inc);

    (*env)->ReleaseDoubleArrayElements(env, a, aPtr, 0);
    (*env)->ReleaseDoubleArrayElements(env, x, xPtr, 0);
    (*env)->ReleaseDoubleArrayElements(env, y, yPtr, 0);
}

// mmult -> dgemm64_
// general matrix-matrix multiplication
void dgemm64_(
    jchar *TRANSA,
    jchar *TRANSB,
    int64_t *M,
    int64_t *N,
    int64_t *K,
    jdouble *ALPHA,
    jdouble *A,
    int64_t *LDA,
    jdouble *B,
    int64_t *LDB,
    jdouble *BETA,
    jdouble *C,
    int64_t *LDC);

JNIEXPORT void JNICALL Java_org_openlca_julia_Julia_mmult(
    JNIEnv *env, jclass jclazz, jint rowsA, jint colsB, jint k,
    jdoubleArray a, jdoubleArray b, jdoubleArray c)
{

    jdouble *aPtr = (*env)->GetDoubleArrayElements(env, a, NULL);
    jdouble *bPtr = (*env)->GetDoubleArrayElements(env, b, NULL);
    jdouble *cPtr = (*env)->GetDoubleArrayElements(env, c, NULL);

    jchar trans = 'N';
    jdouble alpha = 1;
    jdouble beta = 0;
    int64_t rowsA_64 = (int64_t)rowsA;
    int64_t colsB_64 = (int64_t)colsB;
    int64_t k_64 = (int64_t)k;
    dgemm64_(&trans, &trans, &rowsA_64, &colsB_64, &k_64, &alpha, aPtr,
             &rowsA_64, bPtr, &k_64, &beta, cPtr, &rowsA_64);

    (*env)->ReleaseDoubleArrayElements(env, a, aPtr, 0);
    (*env)->ReleaseDoubleArrayElements(env, b, bPtr, 0);
    (*env)->ReleaseDoubleArrayElements(env, c, cPtr, 0);
}

// LAPACK

// solve -> dgesv64_
// DGESV computes the solution to a system of linear equations A * X = B
// see http://www.netlib.org/lapack/explore-html/d7/d3b/group__double_g_esolve_ga5ee879032a8365897c3ba91e3dc8d512.html#ga5ee879032a8365897c3ba91e3dc8d512
void dgesv64_(
    /* 0 */ int64_t *n,
    /* 1 */ int64_t *nrhs,
    /* 2 */ jdouble *A,
    /* 3 */ int64_t *lda,
    /* 4 */ int64_t *ipiv,
    /* 5 */ jdouble *B,
    /* 6 */ int64_t *ldb,
    /* 7 */ int64_t *info);

JNIEXPORT jint JNICALL Java_org_openlca_julia_Julia_solve(
    JNIEnv *env,
    jclass jclazz,
    jint n32,
    jint nrhs32,
    jdoubleArray a,
    jdoubleArray b)
{

    jdouble *A = (*env)->GetDoubleArrayElements(env, a, NULL);
    jdouble *B = (*env)->GetDoubleArrayElements(env, b, NULL);
    int64_t n = (int64_t)n32;
    int64_t nrhs = (int64_t)nrhs32;
    int64_t *ipiv = malloc(sizeof(int64_t) * n32);
    int64_t info;

    dgesv64_(
        /* 0 */ &n,
        /* 1 */ &nrhs,
        /* 2 */ A,
        /* 3 */ &n,
        /* 4 */ ipiv,
        /* 5 */ B,
        /* 6 */ &n,
        /* 7 */ &info);

    free(ipiv);
    (*env)->ReleaseDoubleArrayElements(env, a, A, 0);
    (*env)->ReleaseDoubleArrayElements(env, b, B, 0);

    return (jint)info;
}

// DGETRF computes an LU factorization of a general matrix
// see http://www.netlib.org/lapack/explore-html/dd/d9a/group__double_g_ecomputational_ga0019443faea08275ca60a734d0593e60.html#ga0019443faea08275ca60a734d0593e60
void dgetrf64_(
    /* 0 */ int64_t *M,
    /* 1 */ int64_t *N,
    /* 2 */ jdouble *A,
    /* 3 */ int64_t *LDA,
    /* 4 */ int64_t *IPIV,
    /* 5 */ int64_t *INFO);

// invert
// DGETRI computes the inverse of a matrix using the LU factorization computed by DGETRF
// see http://www.netlib.org/lapack/explore-html/dd/d9a/group__double_g_ecomputational_ga56d9c860ce4ce42ded7f914fdb0683ff.html#ga56d9c860ce4ce42ded7f914fdb0683ff
void dgetri64_(
    /* 0 */ int64_t *N,
    /* 1 */ jdouble *A,
    /* 2 */ int64_t *LDA,
    /* 3 */ int64_t *IPIV,
    /* 4 */ jdouble *WORK,
    /* 5 */ int64_t *LWORK,
    /* 6 */ int64_t *INFO);

JNIEXPORT jint JNICALL Java_org_openlca_julia_Julia_invert(
    JNIEnv *env, jclass jobj, jint n32, jdoubleArray a)
{
    int64_t n = (int64_t)n32;
    jdouble *A = (*env)->GetDoubleArrayElements(env, a, NULL);
    int64_t *ipiv = malloc(sizeof(int64_t) * n32);
    int64_t lwork = 64 * 2 * n;
    jdouble *work = malloc(sizeof(jdouble) * lwork);
    int64_t info;

    // calculate the factorization
    dgetrf64_(
        /* 0 */ &n,
        /* 1 */ &n,
        /* 2 */ A,
        /* 3 */ &n,
        /* 4 */ ipiv,
        /* 5 */ &info);

    if (info != 0)
    {
        // factorization error
        free(ipiv);
        free(work);
        (*env)->ReleaseDoubleArrayElements(env, a, A, 0);
        return (jint)info;
    }

    // invert it
    dgetri64_(
        /* 0 */ &n,
        /* 1 */ A,
        /* 2 */ &n,
        /* 3 */ ipiv,
        /* 4 */ work,
        /* 5 */ &lwork,
        /* 6 */ &info);

    free(ipiv);
    free(work);
    (*env)->ReleaseDoubleArrayElements(env, a, A, 0);
    return (jint)info;
}
