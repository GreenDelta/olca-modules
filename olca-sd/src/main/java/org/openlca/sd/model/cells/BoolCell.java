package org.openlca.sd.model.cells;

import org.openlca.commons.Res;
import org.openlca.sd.eqn.Interpreter;

public record BoolCell(boolean value) implements Cell {

	@Override
	public Res<Cell> eval(Interpreter interpreter) {
		return Res.ok(this);
	}

	@Override
	public String toString() {
		return "{" + value + "}";
	}
}
