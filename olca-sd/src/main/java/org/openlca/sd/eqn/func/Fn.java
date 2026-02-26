package org.openlca.sd.eqn.func;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.openlca.commons.Res;
import org.openlca.sd.model.Tensor;
import org.openlca.sd.model.cells.Cell;
import org.openlca.sd.model.cells.TensorCell;

class Fn {

	private Fn() {
	}

	static Res<Cell> withOneArg(
		List<Cell> args, Function<Cell, Res<Cell>> fn
	) {
		if (args == null || args.size() != 1)
			return Res.error("function requires exactly 1 argument");
		var a = args.getFirst();
		if (a == null)
			return Res.error("function argument cannot be null");
		return fn.apply(a);
	}

	static Res<Cell> withTwoArgs(
		List<Cell> args, BiFunction<Cell, Cell, Res<Cell>> fn
	) {
		if (args == null || args.size() != 2)
			return Res.error("function requires exactly 2 arguments");
		var a = args.getFirst();
		var b = args.getLast();
		if (a == null || b == null)
			return Res.error("function arguments cannot be null");
		return fn.apply(a, b);
	}

	/// Applies the given function recursively on each cell of the given tensor.
	static Res<Cell> each(Func func, TensorCell cell) {
		if (func == null || cell == null)
			return Res.error("no function or tensor cell provided");
		var tensor = cell.value();
		var result = Tensor.of(tensor.dimensions());

		for (int i = 0; i < tensor.size(); i++) {
			var elem = tensor.get(i);
			var r = func.apply(List.of(elem));
			if (r.isError()) {
				return r.wrapError(
					"error computing " + func.name() + " at index " + i);
			}
			result.set(i, r.value());
		}
		return Res.ok(Cell.of(result));
	}

}
