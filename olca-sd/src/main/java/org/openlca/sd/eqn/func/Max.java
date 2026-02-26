package org.openlca.sd.eqn.func;

import java.util.List;

import org.openlca.commons.Res;
import org.openlca.sd.model.Id;
import org.openlca.sd.model.cells.Cell;
import org.openlca.sd.model.cells.TensorCell;

public class Max implements Func {

	private final Id name = Id.of("MAX");

	@Override
	public Id name() {
		return name;
	}

	@Override
	public Res<Cell> apply(List<Cell> args) {
		if (args == null || args.isEmpty())
			return Res.error("MAX function requires at least one argument");

		// handle single arg
		if (args.size() == 1) {
			var first = args.getFirst();
			if (first == null)
				return Res.error("MAX function argument cannot be null");
			if (first.isNumCell())
				return Res.ok(first);
			if (first.isTensorCell())
				return maxOf(first.asTensorCell());
			return Res.error("MAX is not defined for cell type: " + first);
		}

		double max = Double.NEGATIVE_INFINITY;
		for (var arg : args) {
			if (arg == null)
				return Res.error("MAX function arguments cannot be null");
			if (!arg.isNumCell())
				return Res.error("MAX with multiple arguments requires "
					+ "all arguments to be numbers, got: " + arg);
			max = Math.max(max, arg.asNum());
		}
		return Res.ok(Cell.of(max));
	}

	private Res<Cell> maxOf(TensorCell tensorCell) {
		var tensor = tensorCell.value();
		double max = Double.NEGATIVE_INFINITY;

		for (int i = 0; i < tensor.size(); i++) {
			var r = apply(List.of(tensor.get(i)));
			if (r.isError())
				return r.wrapError("failed to get max at row: " + i);
			var cell = r.value();
			if (!cell.isNumCell())
				return r.wrapError("max at row is not numeric: " + i);
			max = Math.max(max, cell.asNum());
		}
		return Res.ok(Cell.of(max));
	}
}
