package org.openlca.sd.model.cells;

import java.util.Objects;

import org.openlca.commons.Res;
import org.openlca.sd.eqn.Interpreter;
import org.openlca.sd.model.LookupFunc;
import org.openlca.sd.model.Tensor;

/// A cell with a lookup function and an equation where the equation
/// evaluates to a value that is then passed into the lookup function.
public record LookupEqnCell(
	String eqn, LookupFunc func
) implements Cell {

	public LookupEqnCell {
		Objects.requireNonNull(eqn);
		Objects.requireNonNull(func);
	}

	@Override
	public Res<Cell> eval(Interpreter interpreter) {
		var res = interpreter.eval(eqn);
		if (res.isError())
			return res;
		var val = res.value();

		if (val instanceof NumCell(double x)) {
			double y = func.get(x);
			return Res.ok(new NumCell(y));
		}

		// when the equation evaluates to a tensor, we apply the
		// lookup function on each element of that tensor
		if (val instanceof TensorCell(Tensor tensor)) {
			return applyOn(tensor);
		}

		return Res.error("Value cannot be applied on a lookup function: " + val);
	}

	private Res<Cell> applyOn(Tensor tensor) {
		var t = Tensor.of(tensor.dimensions());
		for (var i = 0; i < t.size(); i++) {
			var val = tensor.get(i);

			if (val instanceof NumCell(double x)) {
				var y = func.get(x);
				t.set(i, new NumCell(y));
				continue;
			}

			if (val instanceof TensorCell(Tensor row)) {
				var rowRes = applyOn(row);
				if (rowRes.isError())
					return rowRes;
				t.set(i, rowRes.value());
				continue;
			}

			return Res.error("Tensor cell value cannot be applied " +
				"on a lookup function: " + val);
		}
		return Res.ok(new TensorCell(t));
	}

	@Override
	public String toString() {
		return "lookup{'" + eqn + "'}";
	}
}
