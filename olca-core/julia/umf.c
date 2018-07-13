#include <stdlib.h>
#include <stdint.h>
#include <string.h>
#include <jni.h>

// from https://github.com/PetterS/SuiteSparse/blob/master/UMFPACK/Include/umfpack.h
#define UMFPACK_A (0) /* Ax=b    */

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

typedef struct
{
    jint *columnPointers;
    jint *rowIndices;
    jdouble *values;
    void *Numeric;
} UmfFactorizedMatrix;

// umfFactorize
JNIEXPORT jlong JNICALL Java_org_openlca_julia_Julia_umfFactorize(
    JNIEnv *env, jclass jclazz,
    jint n,
    jintArray columnPointers,
    jintArray rowIndices,
    jdoubleArray values)
{

    UmfFactorizedMatrix *fm = malloc(sizeof(UmfFactorizedMatrix));

    jsize numElems = 0;
    size_t numBytes = 0;

    // copy column pointers
    numElems = (*env)->GetArrayLength(env, columnPointers);
    numBytes = numElems * sizeof(jint);
    jint *rawColPointers = (*env)->GetIntArrayElements(env, columnPointers, NULL);
    fm->columnPointers = malloc(numBytes);
    memcpy(fm->columnPointers, rawColPointers, numBytes);
    (*env)->ReleaseIntArrayElements(env, columnPointers, rawColPointers, 0);

    // copy row indices
    numElems = (*env)->GetArrayLength(env, rowIndices);
    numBytes = numElems * sizeof(jint);
    jint *rawRowIndices = (*env)->GetIntArrayElements(env, rowIndices, NULL);
    fm->rowIndices = malloc(numBytes);
    memcpy(fm->rowIndices, rawRowIndices, numBytes);
    (*env)->ReleaseIntArrayElements(env, rowIndices, rawRowIndices, 0);

    // copy values
    numElems = (*env)->GetArrayLength(env, values);
    numBytes = numElems * sizeof(jdouble);
    jdouble *rawValues = (*env)->GetDoubleArrayElements(env, values, NULL);
    fm->values = malloc(numBytes);
    memcpy(fm->values, rawValues, numBytes);
    (*env)->ReleaseDoubleArrayElements(env, values, rawValues, 0);

    double *null = (double *)NULL;
    void *Symbolic, *Numeric;

    umfpack_di_symbolic(
        n,
        n,
        fm->columnPointers,
        fm->rowIndices,
        fm->values,
        &Symbolic,
        null, null);

    umfpack_di_numeric(
        fm->columnPointers,
        fm->rowIndices,
        fm->values,
        Symbolic,
        &Numeric,
        null, null);
    umfpack_di_free_symbolic(&Symbolic);

    fm->Numeric = Numeric;

    return (jlong)fm;
}

// umfSolveFactorized
JNIEXPORT void JNICALL Java_org_openlca_julia_Julia_umfSolveFactorized(
    JNIEnv *env, jclass jclazz, jlong pointer,
    jdoubleArray demand, jdoubleArray result)
{

    jdouble *demandPtr = (*env)->GetDoubleArrayElements(env, demand, NULL);
    jdouble *resultPtr = (*env)->GetDoubleArrayElements(env, result, NULL);

    UmfFactorizedMatrix *fm = (void *)pointer;

    double *null = (double *)NULL;
    umfpack_di_solve(
        UMFPACK_A,
        fm->columnPointers,
        fm->rowIndices,
        fm->values,
        resultPtr,
        demandPtr,
        fm->Numeric,
        null, null);

    (*env)->ReleaseDoubleArrayElements(env, demand, demandPtr, 0);
    (*env)->ReleaseDoubleArrayElements(env, result, resultPtr, 0);
}

// umfDispose
JNIEXPORT void JNICALL Java_org_openlca_julia_Julia_umfDispose(
    JNIEnv *env, jclass jclazz, jlong pointer)
{
    UmfFactorizedMatrix *fm = (void *)pointer;
    free(fm->columnPointers);
    free(fm->rowIndices);
    free(fm->values);
    umfpack_di_free_numeric(&(fm->Numeric));
    free(fm);
}
