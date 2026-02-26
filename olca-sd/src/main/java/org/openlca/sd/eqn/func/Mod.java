package org.openlca.sd.eqn.func;

import java.util.List;

import org.openlca.commons.Res;
import org.openlca.sd.model.Id;
import org.openlca.sd.model.Tensor;
import org.openlca.sd.model.cells.Cell;
import org.openlca.sd.model.cells.TensorCell;

public class Mod implements Func {

	private final Id name = Id.of("MOD");

	public static Res<Cell> apply(Cell a, Cell b) {
		return new Mod().apply(List.of(a, b));
	}

	@Override
	public Id name() {
		return name;
	}

	@Override
	public Res<Cell> apply(List<Cell> args) {
		return Fn.withTwoArgs(args, (a, b) -> {

			// modulo of numbers
			if (a.isNumCell() && b.isNumCell()) {
				double divisor = b.asNum();
				if (divisor == 0.0)
					return Res.error("modulo by zero");
				double result = a.asNum() % divisor;
				return Res.ok(Cell.of(result));
			}

			// modulo: tensor % number
			if (a.isTensorCell() && b.isNumCell()) {
				double divisor = b.asNum();
				if (divisor == 0.0)
					return Res.error("modulo by zero");
				return scalar(a.asTensorCell(), divisor);
			}

			return Res.error("modulo is not defined for: " + a + " % " + b);
		});
	}

	private Res<Cell> scalar(TensorCell cell, double divisor) {
		var tensor = cell.value();
		var result = Tensor.of(tensor.dimensions());
		var shape = tensor.shape();
		for (int i = 0; i < shape[0]; i++) {
			var element = tensor.get(i);
			var mi = apply(List.of(element, Cell.of(divisor)));
			if (mi.isError())
				return mi.wrapError("error in scalar modulo at index " + i);
			result.set(i, mi.value());
		}
		return Res.ok(Cell.of(result));
	}

}
