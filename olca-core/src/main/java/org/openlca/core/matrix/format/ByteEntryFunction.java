package org.openlca.core.matrix.format;

@FunctionalInterface
public interface ByteEntryFunction {

	void value(int row, int col, byte value);

}
