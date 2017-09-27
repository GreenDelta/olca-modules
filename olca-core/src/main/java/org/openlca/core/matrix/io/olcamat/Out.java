package org.openlca.core.matrix.io.olcamat;

import org.openlca.core.matrix.format.IMatrix;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

class Out {

    static void denseColumns(IMatrix m, File file) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(file);
             BufferedOutputStream buffer = new BufferedOutputStream(fos)) {

            // byte buffers for int and double
            ByteBuffer i32 = ByteBuffer.allocate(4);
            ByteBuffer f64 = ByteBuffer.allocate(8);
            i32.order(ByteOrder.LITTLE_ENDIAN);
            f64.order(ByteOrder.LITTLE_ENDIAN);

            // format version -> 1
            i32.putInt(1);
            buffer.write(i32.array());
            i32.clear();

            // storage format -> 0 dense array in column major order
            i32.putInt(0);
            buffer.write(i32.array());
            i32.clear();

            // data type -> 0 64-bit floating point numbers
            i32.putInt(0);
            buffer.write(i32.array());
            i32.clear();

            // entry size -> 8 bytes
            i32.putInt(8);
            buffer.write(i32.array());
            i32.clear();

            // rows + columns
            i32.putInt(m.rows());
            buffer.write(i32.array());
            i32.clear();
            i32.putInt(m.columns());
            buffer.write(i32.array());

            // values
            for (int col = 0; col < m.columns(); col++) {
                for (int row = 0; row < m.rows(); row++) {
                    f64.putDouble(m.get(row, col));
                    buffer.write(f64.array());
                    f64.clear();
                }
            }
        }
    }
}
