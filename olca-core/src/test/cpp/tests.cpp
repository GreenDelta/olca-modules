#define CATCH_CONFIG_MAIN  // catch will generate a main method

#include <cstdlib>
#include "../src/olca_blas.h"
#include "../src/olca_lapack.h"
#include "../src/olca_eigen.h"
#include "../lib/catch.hpp"

/**
 * It is very imported that the JNI types in this test have the given sizes. 
 * Otherwise mapping of Java types to the respective Fortran routines will fail
 * or give wrong results.
 */
TEST_CASE("Check JNI data types") {
    REQUIRE(sizeof (jint) == 4);
    REQUIRE(sizeof (jfloat) == 4);
    REQUIRE(sizeof (jdouble) == 8);
}

void checkEqualArrays(jint n, jdouble *a, jdouble *b) {
    for (jint i = 0; i < n; i++) {
        REQUIRE(abs(a[i] - b[i]) < 1e-14);
    }
}

void checkEqualSingleArrays(jint n, jfloat *a, jfloat *b) {
    for (jint i = 0; i < n; i++) {
        REQUIRE(abs(a[i] - b[i]) < 1e-14);
    }
}

TEST_CASE("Invert a matrix", "[invert]") {
    jdouble a[4] = {1, -4, 0, 2};
    jint info = dInvert(2, a);
    REQUIRE(info == 0);
    jdouble expected[4] = {1, 2, 0, 0.5};
    checkEqualArrays(4, a, expected);
}

TEST_CASE("Invert a matrix (single precision)", "[invertSingle]") {
    jfloat a[4] = {1, -4, 0, 2};
    jint info = sInvert(2, a);
    REQUIRE(info == 0);
    jfloat expected[4] = {1, 2, 0, 0.5};
    checkEqualSingleArrays(4, a, expected);
}

TEST_CASE("Solve A * X = B", "[solve]") {
    jdouble a[4] = {1, -4, 0, 2};
    jdouble b[4] = {1, 0, 0, 1};
    jint info = dSolve(2, 2, a, b);
    REQUIRE(info == 0);
    jdouble expected[4] = {1, 2, 0, 0.5};
    checkEqualArrays(4, b, expected);
}

TEST_CASE("Solve A * X = B (single precision)", "[solveSingle]") {
    jfloat a[4] = {1, -4, 0, 2};
    jfloat b[4] = {1, 0, 0, 1};
    jint info = sSolve(2, 2, a, b);
    REQUIRE(info == 0);
    jfloat expected[4] = {1, 2, 0, 0.5};
    checkEqualSingleArrays(4, b, expected);
}

TEST_CASE("Solve A * X = B (iterative refinement)", "[solveIterRef]") {
    jdouble a[4] = {1, -4, 0, 2};
    jdouble b[4] = {1, 0, 0, 1};
    jdouble x[4];
    jint info = dsSolve(2, 2, a, b, x);
    REQUIRE(info == 0);
    jdouble expected[4] = {1, 2, 0, 0.5};
    checkEqualArrays(4, x, expected);
}

TEST_CASE("Solve A * X = B (separate LU fact.)", "[solveLu]") {
    jdouble a[4] = {1, -4, 0, 2};
    jint pivots[2];
    dLu(2, a, pivots);
    jdouble b[4] = {1, 0, 0, 1};
    jint info = dSolveLu(2, 2, a, pivots, b);
    REQUIRE(info == 0);
    jdouble expected[4] = {1, 2, 0, 0.5};
    checkEqualArrays(4, b, expected);
}

TEST_CASE("Solve A * X = B (separate LU fact.) (single precision)", "[solveLuSingle]") {
    jfloat a[4] = {1, -4, 0, 2};
    jint pivots[2];
    sLu(2, a, pivots);
    jfloat b[4] = {1, 0, 0, 1};
    jint info = sSolveLu(2, 2, a, pivots, b);
    REQUIRE(info == 0);
    jfloat expected[4] = {1, 2, 0, 0.5};
    checkEqualSingleArrays(4, b, expected);
}

TEST_CASE("General matrix-matrix multiplication", "[matrixMatrixMult]") {
    jdouble a[6] = {1, 4, 2, 5, 3, 6};
    jdouble b[6] = {7, 8, 9, 10, 11, 12};
    jdouble c[4];
    dMmult(2, 2, 3, a, b, c);
    jdouble expected[4] = {50, 122, 68, 167};
    checkEqualArrays(4, c, expected);
}

TEST_CASE("General matrix-matrix multiplication (single precision)", "[matrixMatrixMultSingle]") {
    jfloat a[6] = {1, 4, 2, 5, 3, 6};
    jfloat b[6] = {7, 8, 9, 10, 11, 12};
    jfloat c[4];
    sMmult(2, 2, 3, a, b, c);
    jfloat expected[4] = {50, 122, 68, 167};
    checkEqualSingleArrays(4, c, expected);
}

TEST_CASE("General matrix-vector multiplication", "[matrixVectorMult]") {
    jdouble a[6] = {1, 4, 2, 5, 3, 6};
    jdouble x[3] = {2, 1, 0.5};
    jdouble y[2];
    dMVmult(2, 3, a, x, y);
    jdouble expected[2] = {5.5, 16};
    checkEqualArrays(2, y, expected);
}

TEST_CASE("General matrix-vector multiplication (single precision)", "[matrixVectorMultSingle]") {
    jfloat a[6] = {1, 4, 2, 5, 3, 6};
    jfloat x[3] = {2, 1, 0.5};
    jfloat y[2];
    sMVmult(2, 3, a, x, y);
    jfloat expected[2] = {5.5, 16};
    checkEqualSingleArrays(2, y, expected);
}

TEST_CASE("Equilibrate a matrix", "[equilibrate]") {
    jdouble a[4] = {1, -5, 0, 4};
    jdouble r[2];
    jdouble c[2];
    jint info = dEquilibrate(2, 2, a, r, c);
    jdouble expectedR[2] = {1.0, 0.2};
    jdouble expectedC[2] = {1.0, 1.25};
    checkEqualArrays(2, r, expectedR);
    checkEqualArrays(2, c, expectedC);
    REQUIRE(info == 0);
}

TEST_CASE("Equilibrate a matrix (single precision)", "[equilibrateSingle]") {
    jfloat a[4] = {1, -5, 0, 4};
    jfloat r[2];
    jfloat c[2];
    jint info = sEquilibrate(2, 2, a, r, c);
    jfloat expectedR[2] = {1.0, 0.2};
    jfloat expectedC[2] = {1.0, 1.25};
    checkEqualSingleArrays(2, r, expectedR);
    checkEqualSingleArrays(2, c, expectedC);
    REQUIRE(info == 0);
}

TEST_CASE("Test sparseLu", "[sparseLu]") {
    jint rows[3] = {0, 1, 1};
    jint cols[3] = {0, 0, 1};
    jdouble vals[3] = {1, -2, 1};
    jdouble b[2] = {1, 0};
    jdouble x[2];
    sparseLu(2, 3, rows, cols, vals, b, x);
    jdouble expected[2] = {1, 2};
    checkEqualArrays(2, x, expected);
}

// TODO: this test fails with the current version of Eigen?
//TEST_CASE("Test sparseLU with 1x1") {
//    jint rows[1] = {0};
//    jint cols[1] = {0};
//    jdouble vals[1] = {1};
//    jdouble b[1] = {1};
//    jdouble x[1];
//    sparseLu(1, 1, rows, cols, vals, b, x);
//    jdouble expected[1] = {1};
//    checkEqualArrays(1, x, expected);
//} 

TEST_CASE("Test bicgstab", "[bicgstab]") {
    jint rows[3] = {0, 1, 1};
    jint cols[3] = {0, 0, 1};
    jdouble vals[3] = {1, -2, 1};
    jdouble b[2] = {1, 0};
    jdouble x[2];
    bicgstab(2, 3, rows, cols, vals, b, x);
    jdouble expected[2] = {1, 2};
    checkEqualArrays(2, x, expected);
}

TEST_CASE("Test sparseLuInvert") {
    jint rows[3] = {0, 1, 1};
    jint cols[3] = {0, 0, 1};
    jdouble vals[3] = {1, -2, 1};
    jdouble result[4];
    sparseLuInvert(2, 3, rows, cols, vals, result);
    jdouble expected[4] = {1, 2, 0, 1};
    checkEqualArrays(4, expected, result);
}
