#include <stdlib.h>
#include <stdint.h>
#include <jni.h>
#include <stdio.h> // currently just for testing

// from https://github.com/PetterS/SuiteSparse/blob/master/UMFPACK/Include/umfpack.h
#define UMFPACK_A (0) /* Ax=b    */

// BLAS

// general matrix-vector multiplication
void dgemv64_(jchar *TRANS, int64_t *M, int64_t *N, jdouble *ALPHA, jdouble *A,
              int64_t *LDA, jdouble *X, int64_t *INCX, jdouble *BETA, jdouble *Y,
              int64_t *INCY);

// general matrix-matrix multiplication
void dgemm64_(jchar *TRANSA, jchar *TRANSB, int64_t *M, int64_t *N, int64_t *K,
              jdouble *ALPHA, jdouble *A, int64_t *LDA, jdouble *B, int64_t *LDB,
              jdouble *BETA, jdouble *C, int64_t *LDC);

JNIEXPORT void JNICALL Java_org_openlca_julia_Julia_dgemm(
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

JNIEXPORT void JNICALL Java_org_openlca_julia_Julia_dgemv(
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

// UMFPACK

extern int umfpack_di_symbolic(
    jint n_row,
    jint n_col,
    jint *Ap,
    jint *Ai,
    jdouble *Ax,
    void **Symbolic,
    double *Control,
    double *Info);

extern int umfpack_di_numeric(
    jint *Ap,
    jint *Ai,
    jdouble *Ax,
    void *Symbolic,
    void **Numeric,
    double *Control,
    double *Info);

int umfpack_di_solve(
    int sys,
    jint *Ap,
    jint *Ai,
    jdouble *Ax,
    jdouble *X,
    jdouble *B,
    void *Numeric,
    double *Control,
    double *Info);

extern void umfpack_di_free_symbolic(void **Symbolic);

extern void umfpack_di_free_numeric(void **Numeric);

// umfSolve
JNIEXPORT void JNICALL Java_org_openlca_julia_Julia_umfSolve(
    JNIEnv *env, jclass jclazz,
    jint n,
    jintArray columnPointers,
    jintArray rowIndices,
    jdoubleArray values,
    jdoubleArray demand,
    jdoubleArray result)
{
    jint *columnPointersPtr = (*env)->GetIntArrayElements(env, columnPointers, NULL);
    jint *rowIndicesPtr = (*env)->GetIntArrayElements(env, rowIndices, NULL);
    jdouble *valuesPtr = (*env)->GetDoubleArrayElements(env, values, NULL);
    jdouble *demandPtr = (*env)->GetDoubleArrayElements(env, demand, NULL);
    jdouble *resultPtr = (*env)->GetDoubleArrayElements(env, result, NULL);

    double *null = (double *)NULL;
    void *Symbolic, *Numeric;

    umfpack_di_symbolic(n, n, columnPointersPtr, rowIndicesPtr, valuesPtr, &Symbolic, null, null);
    umfpack_di_numeric(columnPointersPtr, rowIndicesPtr, valuesPtr, Symbolic, &Numeric, null, null);
    umfpack_di_free_symbolic(&Symbolic);
    umfpack_di_solve(UMFPACK_A, columnPointersPtr, rowIndicesPtr, valuesPtr, resultPtr, demandPtr, Numeric, null, null);
    umfpack_di_free_numeric(&Numeric);

    (*env)->ReleaseIntArrayElements(env, columnPointers, columnPointersPtr, 0);
    (*env)->ReleaseIntArrayElements(env, rowIndices, rowIndicesPtr, 0);
    (*env)->ReleaseDoubleArrayElements(env, values, valuesPtr, 0);
    (*env)->ReleaseDoubleArrayElements(env, demand, demandPtr, 0);
    (*env)->ReleaseDoubleArrayElements(env, result, resultPtr, 0);
}

struct UmfFactorizedMatrix
{

    jintArray *columnPointers;
    jint *columnPointersPtr;

    jintArray *rowIndices;
    jint *rowIndicesPtr;

    jdoubleArray *values;
    jdouble *valuesPtr;

    void *Numeric;
};

JNIEXPORT jlong JNICALL Java_org_openlca_julia_Julia_umfFactorize(
    JNIEnv *env, jclass jclazz,
    jint n,
    jintArray columnPointers,
    jintArray rowIndices,
    jdoubleArray values)
{

    struct UmfFactorizedMatrix *fm = malloc(sizeof(struct UmfFactorizedMatrix));
    printf("factorized: %p\n", fm);
    fm->columnPointers = &columnPointers;
    fm->columnPointersPtr = (*env)->GetIntArrayElements(env, columnPointers, NULL);

    fm->rowIndices = &rowIndices;
    fm->rowIndicesPtr = (*env)->GetIntArrayElements(env, rowIndices, NULL);

    fm->values = &values;
    fm->valuesPtr = (*env)->GetDoubleArrayElements(env, values, NULL);

    double *null = (double *)NULL;
    void *Symbolic, *Numeric;

    umfpack_di_symbolic(
        n,
        n,
        fm->columnPointersPtr,
        fm->rowIndicesPtr,
        fm->valuesPtr,
        &Symbolic,
        null, null);

    umfpack_di_numeric(
        fm->columnPointersPtr,
        fm->rowIndicesPtr,
        fm->valuesPtr,
        Symbolic,
        &Numeric,
        null, null);
    umfpack_di_free_symbolic(&Symbolic);

    fm->Numeric = Numeric;

    return (jlong)fm;
}

JNIEXPORT void JNICALL Java_org_openlca_julia_Julia_umfSolveFactorized(
    JNIEnv *env, jclass jclazz, jlong pointer,
    jdoubleArray demand, jdoubleArray result)
{

    jdouble *demandPtr = (*env)->GetDoubleArrayElements(env, demand, NULL);
    jdouble *resultPtr = (*env)->GetDoubleArrayElements(env, result, NULL);

    struct UmfFactorizedMatrix *fm = (void *)pointer;
    printf("solve factorized: %p\n", fm);

    double *null = (double *)NULL;
    umfpack_di_solve(
        UMFPACK_A,
        fm->columnPointersPtr,
        fm->rowIndicesPtr,
        fm->valuesPtr,
        resultPtr,
        demandPtr,
        fm->Numeric,
        null, null);

    (*env)->ReleaseDoubleArrayElements(env, demand, demandPtr, 0);
    (*env)->ReleaseDoubleArrayElements(env, result, resultPtr, 0);
}

JNIEXPORT void JNICALL Java_org_openlca_julia_Julia_umfDispose(
    JNIEnv *env, jclass jclazz, jlong pointer)
{
    struct UmfFactorizedMatrix *fm = (void *)pointer;
    printf("dispose factorized: %p\n", fm);
    (*env)->ReleaseIntArrayElements(env, *(fm->columnPointers), fm->columnPointersPtr, 0);
    (*env)->ReleaseIntArrayElements(env, *(fm->rowIndices), fm->rowIndicesPtr, 0);
    (*env)->ReleaseDoubleArrayElements(env, *(fm->values), fm->valuesPtr, 0);
    umfpack_di_free_numeric(&(fm->Numeric));
    free(fm);
}
