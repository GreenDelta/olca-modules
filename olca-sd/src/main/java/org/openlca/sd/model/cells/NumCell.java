package org.openlca.sd.model.cells;

import org.openlca.commons.Res;
import org.openlca.sd.eqn.Interpreter;

public record NumCell(double value) implements Cell {

	@Override
	public Res<Cell> eval(Interpreter interpreter) {
		return Res.ok(new NumCell(value));
	}

	@Override
	public String toString() {
		return "{" + value + "}";
	}
}
