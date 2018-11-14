package org.openlca.core.matrix.format;

@FunctionalInterface
public interface EntryFunction {

	void value(int row, int col, double value);

}
