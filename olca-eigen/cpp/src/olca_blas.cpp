#include "olca_blas.h"

#ifdef __cplusplus
extern "C" {
#endif

    JNIEXPORT void JNICALL Java_org_openlca_eigen_Blas_dMmult(
            JNIEnv *env, jclass jclazz, jint rowsA, jint colsB, jint k,
            jdoubleArray a, jdoubleArray b, jdoubleArray c) {
        jdouble *aPtr = env->GetDoubleArrayElements(a, NULL);
        jdouble *bPtr = env->GetDoubleArrayElements(b, NULL);
        jdouble *cPtr = env->GetDoubleArrayElements(c, NULL);
        dMmult(rowsA, colsB, k, aPtr, bPtr, cPtr);
        env->ReleaseDoubleArrayElements(a, aPtr, 0);
        env->ReleaseDoubleArrayElements(b, bPtr, 0);
        env->ReleaseDoubleArrayElements(c, cPtr, 0);
    }

    JNIEXPORT void JNICALL Java_org_openlca_eigen_Blas_sMmult(
            JNIEnv *env, jclass jclazz, jint rowsA, jint colsB, jint k,
            jfloatArray a, jfloatArray b, jfloatArray c) {
        jfloat *aPtr = env->GetFloatArrayElements(a, NULL);
        jfloat *bPtr = env->GetFloatArrayElements(b, NULL);
        jfloat *cPtr = env->GetFloatArrayElements(c, NULL);
        sMmult(rowsA, colsB, k, aPtr, bPtr, cPtr);
        env->ReleaseFloatArrayElements(a, aPtr, 0);
        env->ReleaseFloatArrayElements(b, bPtr, 0);
        env->ReleaseFloatArrayElements(c, cPtr, 0);
    }

    JNIEXPORT void JNICALL Java_org_openlca_eigen_Blas_dMVmult(
            JNIEnv *env, jclass jclazz, jint rowsA, jint colsA, jdoubleArray a,
            jdoubleArray x, jdoubleArray y) {
        jdouble *aPtr = env->GetDoubleArrayElements(a, NULL);
        jdouble *xPtr = env->GetDoubleArrayElements(x, NULL);
        jdouble *yPtr = env->GetDoubleArrayElements(y, NULL);
        dMVmult(rowsA, colsA, aPtr, xPtr, yPtr);
        env->ReleaseDoubleArrayElements(a, aPtr, 0);
        env->ReleaseDoubleArrayElements(x, xPtr, 0);
        env->ReleaseDoubleArrayElements(y, yPtr, 0);
    }

    JNIEXPORT void JNICALL Java_org_openlca_eigen_Blas_sMVmult(
            JNIEnv *env, jclass jclazz, jint rowsA, jint colsA, jfloatArray a,
            jfloatArray x, jfloatArray y) {
        jfloat *aPtr = env->GetFloatArrayElements(a, NULL);
        jfloat *xPtr = env->GetFloatArrayElements(x, NULL);
        jfloat *yPtr = env->GetFloatArrayElements(y, NULL);
        sMVmult(rowsA, colsA, aPtr, xPtr, yPtr);
        env->ReleaseFloatArrayElements(a, aPtr, 0);
        env->ReleaseFloatArrayElements(x, xPtr, 0);
        env->ReleaseFloatArrayElements(y, yPtr, 0);
    }

#ifdef __cplusplus
}
#endif
