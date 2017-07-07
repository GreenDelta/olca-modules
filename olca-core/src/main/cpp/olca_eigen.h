#ifndef OLCA_EIGEN_H
#define	OLCA_EIGEN_H

#define EIGEN_NO_DEBUG

#include <jni.h>
#include <Eigen/Dense>
#include <Eigen/SparseCore>
#include <Eigen/SparseLU>
#include <Eigen/IterativeLinearSolvers>

using Eigen::VectorXd;
using Eigen::SparseLU;
using Eigen::BiCGSTAB;
using Eigen::SparseMatrix;

typedef Eigen::Triplet<double> Triplet;
typedef Eigen::SparseMatrix<double> SMatrix;

inline void fillSparseMatrix(SMatrix *m, jint n, jint *rows, jint *cols,
        jdouble *data) {
    std::vector<Triplet> triplets;
    triplets.reserve(n);
    for (int i = 0; i < n; i++) {
        triplets.push_back(Triplet(rows[i], cols[i], data[i]));
    }
    m->setFromTriplets(triplets.begin(), triplets.end());
    m->makeCompressed();
}

/**
 * Solves the system of linear equations A * B = X using SuperLU.
 * 
 * @param dim
 *            the dimension of the sparse matrix A (A must be a square
 *            matrix).
 * @param n
 *            the number of non-zero entries in the sparse matrix A
 * @param rowsA
 *            the row indices with values of matrix A (size = n).
 * @param colsA
 *            the column indices with values of matrix A (size = n).
 * @param dataA
 *            the data values of the non-zero entries in A (size = n).
 * @param b
 *            the vector b (size = dim)
 * @param x
 *            the resulting vector x (size = dim).
 */
inline void sparseLu(jint dim, jint n, jint *rowsA, jint *colsA, jdouble *dataA,
        jdouble *b, jdouble *x) {

    SMatrix m(dim, dim);
    fillSparseMatrix(&m, n, rowsA, colsA, dataA);

    // initialize the vectors
    VectorXd xVec(dim), bVec(dim);
    for (int i = 0; i < dim; i++) {
        bVec(i) = b[i];
    }

    // solve the system
    SparseLU<SMatrix> solver;
    solver.analyzePattern(m);
    solver.factorize(m);
    xVec = solver.solve(bVec);

    // copy results to x
    for (int i = 0; i < dim; i++) {
        x[i] = xVec(i);
    }
}

inline void bicgstab(jint dim, jint n, jint *rowsA, jint *colsA, jdouble *dataA,
        jdouble *b, jdouble *x) {
    SMatrix m(dim, dim);
    fillSparseMatrix(&m, n, rowsA, colsA, dataA);

    // initialize the vectors
    VectorXd xVec(dim), bVec(dim);
    for (int i = 0; i < dim; i++) {
        bVec(i) = b[i];
    }

    BiCGSTAB<SMatrix> solver;
    xVec = solver.compute(m).solve(bVec);
    
    // copy results to x
    for (int i = 0; i < dim; i++) {
        x[i] = xVec(i);
    }
}

inline void sparseLuInvert(jint dim, jint n, jint *rowsA, jint *colsA,
        jdouble *dataA, jdouble *result) {

    // create the matrix
    SMatrix m(dim, dim);
    fillSparseMatrix(&m, n, rowsA, colsA, dataA);

    SMatrix eye(dim, dim);
    for (int i = 0; i < dim; i++) {
        eye.insert(i, i) = 1;
    }
    eye.makeCompressed();

    // solve the system
    SparseLU<SMatrix> solver;
    solver.analyzePattern(m);
    solver.factorize(m);
    SMatrix x = solver.solve(eye);

    // copy the results into th result vector
    for (int k = 0; k < x.outerSize(); ++k) {
        for (SMatrix::InnerIterator it(x, k); it; ++it) {
            result[it.row() + dim * it.col()] = it.value();
        }
    }
}

inline void sparseMmult(jint rowsA, jint k, jint colsB, jint entriesA,
        jint entriesB, jint *rowIndicesA, jint *colIndicesA, jdouble *valuesA,
        jint *rowIndicesB, jint *colIndicesB, jdouble *valuesB,
        jdouble *result) {
    SMatrix a(rowsA, k);
    fillSparseMatrix(&a, entriesA, rowIndicesA, colIndicesA, valuesA);
    SMatrix b(k, colsB);
    fillSparseMatrix(&b, entriesB, rowIndicesB, colIndicesB, valuesB);
    SMatrix r = a * b;
    for (int k = 0; k < r.outerSize(); ++k) {
        for (SMatrix::InnerIterator it(r, k); it; ++it) {
            result[it.row() + rowsA * it.col()] = it.value();
        }
    }
}

#endif	

