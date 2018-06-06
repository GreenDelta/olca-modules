package org.openlca.julia;

import org.openlca.core.matrix.format.IMatrix;

import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;

public class UmfMatrix {

    int rowCount;
    int columnCount;
    double[] values;
    int[] rowIndices;
    int[] columnPointers;

    public static UmfMatrix from(IMatrix m) {
        UmfMatrix umf = new UmfMatrix();
        umf.rowCount = m.rows();
        umf.columnCount = m.columns();
        umf.columnPointers = new int[m.columns() + 1];
        TDoubleArrayList values = new TDoubleArrayList(m.rows());
        TIntArrayList rowIndices = new TIntArrayList(m.rows());
        int i = 0;
        for (int col = 0; col < umf.columnCount; col++) {
            boolean foundEntry = false;
            for (int row = 0; row < umf.rowCount; row++) {
                double val = m.get(row, col);
                if (val == 0)
                    continue;
                values.add(val);
                rowIndices.add(row);
                if (!foundEntry) {
                    umf.columnPointers[col] = i;
                    foundEntry = true;
                }
                i++;
            }
            if (!foundEntry) {
                umf.columnPointers[col] = i;
            }
        }
        umf.values = values.toArray();
        umf.rowIndices = rowIndices.toArray();
        umf.columnPointers[umf.columnCount] = umf.values.length;
        return umf;
    }
}
