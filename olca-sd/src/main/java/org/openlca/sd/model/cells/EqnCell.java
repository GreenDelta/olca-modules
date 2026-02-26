package org.openlca.sd.model.cells;

import java.util.Objects;

import org.openlca.commons.Res;
import org.openlca.sd.eqn.Interpreter;

public record EqnCell(String value) implements Cell {

	public EqnCell {
		Objects.requireNonNull(value);
	}

	@Override
	public Res<Cell> eval(Interpreter interpreter) {
		return interpreter.eval(value);
	}

	@Override
	public String toString() {
		return "{'" + value + "'}";
	}
}
