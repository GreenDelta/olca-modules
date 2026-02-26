package org.openlca.sd.model.cells;

import java.util.Objects;

import org.openlca.commons.Res;
import org.openlca.sd.eqn.Interpreter;
import org.openlca.sd.model.LookupFunc;

/// A cell that just contains a lookup function. Such a cell needs an outer
/// context that provides the input value of the lookup function. For example,
/// the elements of a tensor could contain a lookup function and an outer
/// equation provides the values passed into the respective elements.
public record LookupCell(LookupFunc func) implements Cell {

	public LookupCell {
		Objects.requireNonNull(func);
	}

	@Override
	public Res<Cell> eval(Interpreter interpreter) {
		return Res.ok(this);
	}

	@Override
	public String toString() {
		return "Lookup{f(x) -> y}";
	}
}
