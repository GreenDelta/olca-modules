#include "olca_lapack.h"

#ifdef __cplusplus
extern "C" {
#endif

    JNIEXPORT jint JNICALL Java_org_openlca_eigen_Lapack_dSolve(
            JNIEnv *env, jclass jclazz, jint n, jint nrhs, jdoubleArray a,
            jdoubleArray b) {
        jdouble *aPtr = env->GetDoubleArrayElements(a, NULL);
        jdouble *bPtr = env->GetDoubleArrayElements(b, NULL);
        jint info = dSolve(n, nrhs, aPtr, bPtr);
        env->ReleaseDoubleArrayElements(a, aPtr, 0);
        env->ReleaseDoubleArrayElements(b, bPtr, 0);
        return info;
    }

    JNIEXPORT jint JNICALL Java_org_openlca_eigen_Lapack_sSolve(
            JNIEnv *env, jclass jclazz, jint n, jint nrhs, jfloatArray a,
            jfloatArray b) {
        jfloat *aPtr = env->GetFloatArrayElements(a, NULL);
        jfloat *bPtr = env->GetFloatArrayElements(b, NULL);
        jint info = sSolve(n, nrhs, aPtr, bPtr);
        env->ReleaseFloatArrayElements(a, aPtr, 0);
        env->ReleaseFloatArrayElements(b, bPtr, 0);
        return info;
    }

    JNIEXPORT jint JNICALL Java_org_openlca_eigen_Lapack_dInvert(
            JNIEnv *env, jclass jobj, jint n, jdoubleArray a) {
        jdouble *aPtr = env->GetDoubleArrayElements(a, NULL);
        jint info = dInvert(n, aPtr);
        env->ReleaseDoubleArrayElements(a, aPtr, 0);
        return info;
    }

    JNIEXPORT jint JNICALL Java_org_openlca_eigen_Lapack_sInvert(
            JNIEnv *env, jclass jobj, jint n, jfloatArray a) {
        jfloat *aPtr = env->GetFloatArrayElements(a, NULL);
        jint info = sInvert(n, aPtr);
        env->ReleaseFloatArrayElements(a, aPtr, 0);
        return info;
    }

    JNIEXPORT jint JNICALL Java_org_openlca_eigen_Lapack_dsSolve(
            JNIEnv *env, jclass jclazz, jint n, jint nrhs, jdoubleArray a,
            jdoubleArray b, jdoubleArray x) {
        jdouble *aPtr = env->GetDoubleArrayElements(a, NULL);
        jdouble *bPtr = env->GetDoubleArrayElements(b, NULL);
        jdouble *xPtr = env->GetDoubleArrayElements(x, NULL);
        jint info = dsSolve(n, nrhs, aPtr, bPtr, xPtr);
        env->ReleaseDoubleArrayElements(a, aPtr, 0);
        env->ReleaseDoubleArrayElements(b, bPtr, 0);
        env->ReleaseDoubleArrayElements(x, xPtr, 0);
        return info;
    }

    JNIEXPORT jint JNICALL Java_org_openlca_eigen_Lapack_dLu(
            JNIEnv *env, jclass jclazz, jint n, jdoubleArray a, jintArray pivots) {
        jdouble *aPtr = env->GetDoubleArrayElements(a, NULL);
        jint *pivotsPtr = env->GetIntArrayElements(pivots, NULL);
        jint info = dLu(n, aPtr, pivotsPtr);
        env->ReleaseDoubleArrayElements(a, aPtr, 0);
        env->ReleaseIntArrayElements(pivots, pivotsPtr, 0);
        return info;
    }

    JNIEXPORT jint JNICALL Java_org_openlca_eigen_Lapack_sLu(
            JNIEnv *env, jclass jclazz, jint n, jfloatArray a, jintArray pivots) {
        jfloat *aPtr = env->GetFloatArrayElements(a, NULL);
        jint *pivotsPtr = env->GetIntArrayElements(pivots, NULL);
        jint info = sLu(n, aPtr, pivotsPtr);
        env->ReleaseFloatArrayElements(a, aPtr, 0);
        env->ReleaseIntArrayElements(pivots, pivotsPtr, 0);
        return info;
    }

    JNIEXPORT jint JNICALL Java_org_openlca_eigen_Lapack_dSolveLu(
            JNIEnv *env, jclass jclazz, jint rows, jint bCols, jdoubleArray lu,
            jintArray pivots, jdoubleArray b) {
        jdouble *luPtr = env->GetDoubleArrayElements(lu, NULL);
        jint *pivotsPtr = env->GetIntArrayElements(pivots, NULL);
        jdouble *bPtr = env->GetDoubleArrayElements(b, NULL);
        jint info = dSolveLu(rows, bCols, luPtr, pivotsPtr, bPtr);
        env->ReleaseDoubleArrayElements(lu, luPtr, 0);
        env->ReleaseIntArrayElements(pivots, pivotsPtr, 0);
        env->ReleaseDoubleArrayElements(b, bPtr, 0);
        return info;
    }

    JNIEXPORT jint JNICALL Java_org_openlca_eigen_Lapack_sSolveLu(
            JNIEnv *env, jclass jclazz, jint rows, jint bCols, jfloatArray lu,
            jintArray pivots, jfloatArray b) {
        jfloat *luPtr = env->GetFloatArrayElements(lu, NULL);
        jint *pivotsPtr = env->GetIntArrayElements(pivots, NULL);
        jfloat *bPtr = env->GetFloatArrayElements(b, NULL);
        jint info = sSolveLu(rows, bCols, luPtr, pivotsPtr, bPtr);
        env->ReleaseFloatArrayElements(lu, luPtr, 0);
        env->ReleaseIntArrayElements(pivots, pivotsPtr, 0);
        env->ReleaseFloatArrayElements(b, bPtr, 0);
        return info;
    }

    JNIEXPORT jint JNICALL Java_org_openlca_eigen_Lapack_dEquilibrate(
            JNIEnv *env, jclass jclazz, jint rows, jint cols, jdoubleArray a,
            jdoubleArray r, jdoubleArray c) {
        jdouble *aPtr = env->GetDoubleArrayElements(a, NULL);
        jdouble *rPtr = env->GetDoubleArrayElements(r, NULL);
        jdouble *cPtr = env->GetDoubleArrayElements(c, NULL);
        jint info = dEquilibrate(rows, cols, aPtr, rPtr, cPtr);
        env->ReleaseDoubleArrayElements(a, aPtr, 0);
        env->ReleaseDoubleArrayElements(r, rPtr, 0);
        env->ReleaseDoubleArrayElements(c, cPtr, 0);
        return info;
    }

    JNIEXPORT jint JNICALL Java_org_openlca_eigen_Lapack_sEquilibrate(
            JNIEnv *env, jclass jclazz, jint rows, jint cols, jfloatArray a,
            jfloatArray r, jfloatArray c) {
        jfloat *aPtr = env->GetFloatArrayElements(a, NULL);
        jfloat *rPtr = env->GetFloatArrayElements(r, NULL);
        jfloat *cPtr = env->GetFloatArrayElements(c, NULL);
        jint info = sEquilibrate(rows, cols, aPtr, rPtr, cPtr);
        env->ReleaseFloatArrayElements(a, aPtr, 0);
        env->ReleaseFloatArrayElements(r, rPtr, 0);
        env->ReleaseFloatArrayElements(c, cPtr, 0);
        return info;
    }

#ifdef __cplusplus
}
#endif