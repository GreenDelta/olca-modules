package org.openlca.julia;

public class Umfpack {

    private static boolean loaded;

    public static native void solve(
        int n,
        int[] columnPointers,
        int[] rowIndices,
        double[] values,
        double[] demand,
        double[] result);

    public static double[] solve(UmfMatrix m, double[] demand) {
        double[] result = new double[demand.length];
        solve(m.rowCount,
            m.columnPointers,
            m.rowIndices,
            m.values,
            demand,
            result);
        return result;
    }

    public static native long factorize(
        int n,
        int[] columnPointers,
        int[] rowIndices,
        double[] values
    );

    public static UmfFactorizedMatrix factorize(UmfMatrix m) {
        long pointer = factorize(
            m.rowCount,
            m.columnPointers,
            m.rowIndices,
            m.values);
        return new UmfFactorizedMatrix(pointer);
    }

    public static native void dispose(long pointer);

    public static native long solveFactorized(
        long pointer, double[] demand, double[] result);

    public static double[] solve(UmfFactorizedMatrix m, double[] demand) {
        double[] result = new double[demand.length];
        solveFactorized(m.pointer, demand, result);
        return result;
    }
}
