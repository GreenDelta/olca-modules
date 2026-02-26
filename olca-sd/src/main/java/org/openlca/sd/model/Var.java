package org.openlca.sd.model;

import java.util.List;

import org.openlca.sd.model.cells.Cell;

public sealed interface Var permits Auxil, Rate, Stock {

	Id name();

	Cell def();

	String unit();

	List<Cell> values();

	/// Creates a fresh copy of the variable. This will not copy
	/// the values from the evaluation history.
	Var freshCopy();

	default void pushValue(Cell cell) {
		values().add(cell);
	}

	default Cell value() {
		return values().isEmpty()
				? def()
				: values().getLast();
	}

}
