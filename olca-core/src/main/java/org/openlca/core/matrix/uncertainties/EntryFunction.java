package org.openlca.core.matrix.uncertainties;

@FunctionalInterface
public interface EntryFunction {

	void accept(int row, int col, UCell cell);

}
