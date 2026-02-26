package org.openlca.sd.eqn.func;

import java.util.List;

import org.openlca.commons.Res;
import org.openlca.sd.model.Id;
import org.openlca.sd.model.cells.Cell;
import org.openlca.sd.model.cells.TensorCell;

public class Min implements Func {

	private final Id name = Id.of("MIN");

	@Override
	public Id name() {
		return name;
	}

	@Override
	public Res<Cell> apply(List<Cell> args) {
		if (args == null || args.isEmpty())
			return Res.error("MIN function requires at least one argument");

		// handle single arg
		if (args.size() == 1) {
			var first = args.getFirst();
			if (first == null)
				return Res.error("MIN function argument cannot be null");
			if (first.isNumCell())
				return Res.ok(first);
			if (first.isTensorCell())
				return minOf(first.asTensorCell());
			return Res.error("MIN is not defined for cell type: " +	first);
		}

		double min = Double.POSITIVE_INFINITY;
		for (var arg : args) {
			if (arg == null)
				return Res.error("MIN function arguments cannot be null");
			if (!arg.isNumCell())
				return Res.error("MIN with multiple arguments requires "
					+ "all arguments to be numbers, got: " + arg);
			min = Math.min(min, arg.asNum());
		}
		return Res.ok(Cell.of(min));
	}

	private Res<Cell> minOf(TensorCell tensorCell) {
		var tensor = tensorCell.value();
		double min = Double.POSITIVE_INFINITY;
		for (int i = 0; i < tensor.size(); i++) {
			var r = apply(List.of(tensor.get(i)));
			if (r.isError())
				return r.wrapError("failed to get min at row: " + i);
			var cell = r.value();
			if (!cell.isNumCell())
				return r.wrapError("min at row is not numeric: " + i);
			min = Math.min(min, cell.asNum());
		}
		return Res.ok(Cell.of(min));
	}
}
