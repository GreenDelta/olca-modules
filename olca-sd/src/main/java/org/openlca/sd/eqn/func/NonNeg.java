package org.openlca.sd.eqn.func;

import java.util.List;

import org.openlca.commons.Res;
import org.openlca.sd.model.Id;
import org.openlca.sd.model.cells.Cell;
import org.openlca.sd.model.cells.NumCell;

public class NonNeg implements Func {

	private final Id name = Id.of("NonNeg");

	public static Res<Cell> apply(Cell cell) {
		return new NonNeg().apply(List.of(cell));
	}

	@Override
	public Id name() {
		return name;
	}

	@Override
	public Res<Cell> apply(List<Cell> args) {
		return Fn.withOneArg(args, arg -> {
			if (arg.isEmpty())
				return Res.ok(arg);

			if (arg instanceof NumCell(double num)) {
				return num >= 0
					? Res.ok(arg)
					: Res.ok(new NumCell(0));
			}

			return arg.isTensorCell()
				? Fn.each(this, arg.asTensorCell())
				: Res.error("NonNeg is not defined for: " + arg);
		});
	}
}
