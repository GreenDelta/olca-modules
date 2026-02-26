package org.openlca.sd.model.cells;

import java.util.List;
import java.util.Objects;

import org.openlca.commons.Res;
import org.openlca.sd.eqn.Interpreter;
import org.openlca.sd.eqn.func.NonNeg;

public record NonNegativeCell(Cell value) implements Cell {

	public NonNegativeCell {
		Objects.requireNonNull(value);
	}

	@Override
	public Res<Cell> eval(Interpreter interpreter) {
		var res = value.eval(interpreter);
		return res.isError()
			? res
			: new NonNeg().apply(List.of(res.value()));
	}

	@Override
	public String toString() {
		return "nonNeg{" + value + "}";
	}

}
