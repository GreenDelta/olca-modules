#include "olca_eigen.h"

#ifdef __cplusplus
extern "C" {
#endif

    JNIEXPORT void JNICALL Java_org_openlca_eigen_Eigen_sparseLu(
            JNIEnv *env, jclass jclazz, jint dim, jint n, jintArray rowsA,
            jintArray colsA, jdoubleArray dataA, jdoubleArray b,
            jdoubleArray x) {
        jint *rowsAPtr = env->GetIntArrayElements(rowsA, NULL);
        jint *colsAPtr = env->GetIntArrayElements(colsA, NULL);
        jdouble *dataAPtr = env->GetDoubleArrayElements(dataA, NULL);
        jdouble *bPtr = env->GetDoubleArrayElements(b, NULL);
        jdouble *xPtr = env->GetDoubleArrayElements(x, NULL);
        sparseLu(dim, n, rowsAPtr, colsAPtr, dataAPtr, bPtr, xPtr);
        env->ReleaseIntArrayElements(rowsA, rowsAPtr, 0);
        env->ReleaseIntArrayElements(colsA, colsAPtr, 0);
        env->ReleaseDoubleArrayElements(dataA, dataAPtr, 0);
        env->ReleaseDoubleArrayElements(b, bPtr, 0);
        env->ReleaseDoubleArrayElements(x, xPtr, 0);
    }

    JNIEXPORT void JNICALL Java_org_openlca_eigen_Eigen_bicgstab(
            JNIEnv *env, jclass jclazz, jint dim, jint n, jintArray rowsA,
            jintArray colsA, jdoubleArray dataA, jdoubleArray b,
            jdoubleArray x) {
        jint *rowsAPtr = env->GetIntArrayElements(rowsA, NULL);
        jint *colsAPtr = env->GetIntArrayElements(colsA, NULL);
        jdouble *dataAPtr = env->GetDoubleArrayElements(dataA, NULL);
        jdouble *bPtr = env->GetDoubleArrayElements(b, NULL);
        jdouble *xPtr = env->GetDoubleArrayElements(x, NULL);
        bicgstab(dim, n, rowsAPtr, colsAPtr, dataAPtr, bPtr, xPtr);
        env->ReleaseIntArrayElements(rowsA, rowsAPtr, 0);
        env->ReleaseIntArrayElements(colsA, colsAPtr, 0);
        env->ReleaseDoubleArrayElements(dataA, dataAPtr, 0);
        env->ReleaseDoubleArrayElements(b, bPtr, 0);
        env->ReleaseDoubleArrayElements(x, xPtr, 0);
    }

    JNIEXPORT void JNICALL Java_org_openlca_eigen_Eigen_bicgstabInvert(
            JNIEnv *env, jclass jclazz, jobject matrix, jdoubleArray inverseArray) {

        // get the values from the Java matrix object
        jclass matrixClass = env->GetObjectClass(matrix);

        jfieldID rowsFieldId = env->GetFieldID(matrixClass, "rows", "I");
        jint n = env->GetIntField(matrix, rowsFieldId);

        jfieldID valuesFieldId = env->GetFieldID(matrixClass, "values", "[D");
        jobject valuesObject = env->GetObjectField(matrix, valuesFieldId);
        jdoubleArray *valuesArray = reinterpret_cast<jdoubleArray*> (&valuesObject);
        jdouble *values = env->GetDoubleArrayElements(*valuesArray, NULL);

        jint entryCount = env->GetArrayLength(*valuesArray);

        jfieldID rowPointersFieldId = env->GetFieldID(matrixClass, "rowPointers", "[I");
        jobject rowPointersObject = env->GetObjectField(matrix, rowPointersFieldId);
        jintArray *rowPointersArray = reinterpret_cast<jintArray*> (&rowPointersObject);
        jint *rowPointers = env->GetIntArrayElements(*rowPointersArray, NULL);

        jfieldID columnIndexFieldId = env->GetFieldID(matrixClass, "columnIndices", "[I");
        jobject columnIndexObject = env->GetObjectField(matrix, columnIndexFieldId);
        jintArray *columnIndexArray = reinterpret_cast<jintArray*> (&columnIndexObject);
        jint *columnIndex = env->GetIntArrayElements(*columnIndexArray, NULL);

        jdouble *inverse = env->GetDoubleArrayElements(inverseArray, NULL);


        // create the Eigen sparse matrix
        std::vector<Triplet> triplets;
        triplets.reserve(entryCount);
        for (jint row = 0; row < n; row++) {
            jint idxStart = rowPointers[row];
            jint idxEnd = row == (n - 1) ? entryCount : rowPointers[row + 1];
            for (jint idx = idxStart; idx < idxEnd; idx++) {
                jint col = columnIndex[idx];
                jdouble val = values[idx];
                triplets.push_back(Triplet(row, col, val));
            }
        }
        SMatrix a(n, n);
        a.setFromTriplets(triplets.begin(), triplets.end());
        a.makeCompressed();

        // solve the inverse
        BiCGSTAB<SMatrix> solver;
        VectorXd xVec(n), bVec(n);
        solver.compute(a);
        for (jint col = 0; col < n; col++) {
            bVec(col) = 1;
            xVec = solver.solve(bVec);
            for (jint row = 0; row < n; row++) {
                jint idx = row + n * col;
                inverse[idx] = xVec(row);
            }
            bVec(col) = 0;
        }

        // release the Java resources
        env->ReleaseDoubleArrayElements(*valuesArray, values, 0);
        env->ReleaseIntArrayElements(*rowPointersArray, rowPointers, 0);
        env->ReleaseIntArrayElements(*columnIndexArray, columnIndex, 0);
        env->ReleaseDoubleArrayElements(inverseArray, inverse, 0);
    }

    JNIEXPORT void JNICALL Java_org_openlca_eigen_Eigen_sparseLuInvert(
            JNIEnv *env, jclass jclazz, jint dim, jint n, jintArray rowsA,
            jintArray colsA, jdoubleArray dataA, jdoubleArray inverse) {
        jint *rowsAPtr = env->GetIntArrayElements(rowsA, NULL);
        jint *colsAPtr = env->GetIntArrayElements(colsA, NULL);
        jdouble *dataAPtr = env->GetDoubleArrayElements(dataA, NULL);
        jdouble *inversePtr = env->GetDoubleArrayElements(inverse, NULL);
        sparseLuInvert(dim, n, rowsAPtr, colsAPtr, dataAPtr, inversePtr);
        env->ReleaseIntArrayElements(rowsA, rowsAPtr, 0);
        env->ReleaseIntArrayElements(colsA, colsAPtr, 0);
        env->ReleaseDoubleArrayElements(dataA, dataAPtr, 0);
        env->ReleaseDoubleArrayElements(inverse, inversePtr, 0);
    }

    JNIEXPORT void JNICALL Java_org_openlca_eigen_Eigen_sparseLuInvert2(
            JNIEnv *env, jclass jclazz, jobject matrix, jdoubleArray inverseArray) {

        // get the values from the Java matrix object
        jclass matrixClass = env->GetObjectClass(matrix);

        jfieldID rowsFieldId = env->GetFieldID(matrixClass, "rows", "I");
        jint n = env->GetIntField(matrix, rowsFieldId);

        jfieldID valuesFieldId = env->GetFieldID(matrixClass, "values", "[D");
        jobject valuesObject = env->GetObjectField(matrix, valuesFieldId);
        jdoubleArray *valuesArray = reinterpret_cast<jdoubleArray*> (&valuesObject);
        jdouble *values = env->GetDoubleArrayElements(*valuesArray, NULL);

        jint entryCount = env->GetArrayLength(*valuesArray);

        jfieldID rowPointersFieldId = env->GetFieldID(matrixClass, "rowPointers", "[I");
        jobject rowPointersObject = env->GetObjectField(matrix, rowPointersFieldId);
        jintArray *rowPointersArray = reinterpret_cast<jintArray*> (&rowPointersObject);
        jint *rowPointers = env->GetIntArrayElements(*rowPointersArray, NULL);

        jfieldID columnIndexFieldId = env->GetFieldID(matrixClass, "columnIndices", "[I");
        jobject columnIndexObject = env->GetObjectField(matrix, columnIndexFieldId);
        jintArray *columnIndexArray = reinterpret_cast<jintArray*> (&columnIndexObject);
        jint *columnIndex = env->GetIntArrayElements(*columnIndexArray, NULL);

        jdouble *inverse = env->GetDoubleArrayElements(inverseArray, NULL);


        // create the Eigen sparse matrix
        std::vector<Triplet> triplets;
        triplets.reserve(entryCount);
        for (jint row = 0; row < n; row++) {
            jint idxStart = rowPointers[row];
            jint idxEnd = row == (n - 1) ? entryCount : rowPointers[row + 1];
            for (jint idx = idxStart; idx < idxEnd; idx++) {
                jint col = columnIndex[idx];
                jdouble val = values[idx];
                triplets.push_back(Triplet(row, col, val));
            }
        }
        SMatrix a(n, n);
        a.setFromTriplets(triplets.begin(), triplets.end());
        a.makeCompressed();

        // solve the inverse
        SparseLU<SMatrix> solver;
        solver.analyzePattern(a);
        solver.factorize(a);
        VectorXd xVec(n), bVec(n);
        for (jint col = 0; col < n; col++) {
            bVec(col) = 1;
            xVec = solver.solve(bVec);
            for (jint row = 0; row < n; row++) {
                jint idx = row + n * col;
                inverse[idx] = xVec(row);
            }
            bVec(col) = 0;
        }

        // release the Java resources
        env->ReleaseDoubleArrayElements(*valuesArray, values, 0);
        env->ReleaseIntArrayElements(*rowPointersArray, rowPointers, 0);
        env->ReleaseIntArrayElements(*columnIndexArray, columnIndex, 0);
        env->ReleaseDoubleArrayElements(inverseArray, inverse, 0);
    }

    JNIEXPORT void JNICALL Java_org_openlca_eigen_Eigen_sparseMmult(
            JNIEnv *env, jclass jclazz, jint rowsA, jint k, jint colsB,
            jint entriesA, jint entriesB, jintArray rowIndicesA,
            jintArray colIndicesA, jdoubleArray valuesA, jintArray rowIndicesB,
            jintArray colIndicesB, jdoubleArray valuesB, jdoubleArray result) {
        jint *rowIdxA = env->GetIntArrayElements(rowIndicesA, NULL);
        jint *colIdxA = env->GetIntArrayElements(colIndicesA, NULL);
        jdouble *valsA = env->GetDoubleArrayElements(valuesA, NULL);
        jint *rowIdxB = env->GetIntArrayElements(rowIndicesB, NULL);
        jint *colIdxB = env->GetIntArrayElements(colIndicesB, NULL);
        jdouble *valsB = env->GetDoubleArrayElements(valuesB, NULL);
        jdouble *c = env->GetDoubleArrayElements(result, NULL);
        sparseMmult(rowsA, k, colsB, entriesA, entriesB, rowIdxA, colIdxA, valsA,
                rowIdxB, colIdxB, valsB, c);
        env->ReleaseIntArrayElements(rowIndicesA, rowIdxA, 0);
        env->ReleaseIntArrayElements(colIndicesA, colIdxA, 0);
        env->ReleaseDoubleArrayElements(valuesA, valsA, 0);
        env->ReleaseIntArrayElements(rowIndicesB, rowIdxB, 0);
        env->ReleaseIntArrayElements(colIndicesB, colIdxB, 0);
        env->ReleaseDoubleArrayElements(valuesB, valsB, 0);
        env->ReleaseDoubleArrayElements(result, c, 0);
    }

#ifdef __cplusplus
}
#endif

