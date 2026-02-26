package org.openlca.sd.eqn.func;

import java.util.List;

import org.openlca.commons.Res;
import org.openlca.sd.model.Id;
import org.openlca.sd.model.Tensor;
import org.openlca.sd.model.cells.Cell;
import org.openlca.sd.model.cells.EmptyCell;
import org.openlca.sd.model.cells.NumCell;
import org.openlca.sd.model.cells.TensorCell;

public class Sum implements Func {

	private final Id name = Id.of("SUM");

	@Override
	public Id name() {
		return name;
	}

	public static Res<Cell> of(Tensor tensor) {
		if (tensor == null)
			return Res.error("Called SUM(tensor) with tensor = null");
		double sum = 0;
		for (int i = 0; i < tensor.size(); i++) {
			var cell = tensor.get(i);
			switch (cell) {
				case TensorCell(Tensor t) -> {
					var sub = of(t);
					if (sub.isError() || !(sub.value() instanceof NumCell(double num)))
						return sub;
					sum += num;
				}
				case NumCell(double num) -> sum += num;
				case EmptyCell ignored -> {}
				case null -> {}
				default -> {
					return Res.error(
						"Called SUM() on partially evaluated tensor: " + cell);
				}
			}
		}
		return Res.ok(Cell.of(sum));
	}

	@Override
	public Res<Cell> apply(List<Cell> args) {
		if (args == null || args.isEmpty())
			return Res.error("sum function requires at least one argument");
		if (args.size() == 1) {
			var cell = args.getFirst();
			return cell instanceof TensorCell(Tensor t)
				? of(t)
				: Res.ok(args.getFirst());
		}

		var acc = args.getFirst();
		var add = new Add();
		for (int i = 1; i < args.size(); i++) {
			var s = add.apply(List.of(acc, args.get(i)));
			if (s.isError())
				return s.wrapError("error in sum at argument " + (i + 1));
			acc = s.value();
		}
		return Res.ok(acc);
	}
}
