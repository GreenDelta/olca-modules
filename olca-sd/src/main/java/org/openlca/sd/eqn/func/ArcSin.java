package org.openlca.sd.eqn.func;

import java.util.List;

import org.openlca.commons.Res;
import org.openlca.sd.model.Id;
import org.openlca.sd.model.cells.Cell;

public class ArcSin implements Func {

	private final Id name = Id.of("ARCSIN");

	@Override
	public Id name() {
		return name;
	}

	@Override
	public Res<Cell> apply(List<Cell> args) {
		return Fn.withOneArg(args, arg -> {

			if (arg.isNumCell()) {
				double v = arg.asNum();
				if (v < -1.0 || v > 1.0) {
					return Res.error(
						"ARCSIN domain error: input must be in range [-1, 1], got " + v);
				}
				double result = Math.asin(v);
				return Res.ok(Cell.of(result));
			}

			return arg.isTensorCell()
				? Fn.each(this, arg.asTensorCell())
				: Res.error("ARCSIN is not defined for: " + arg);
		});
	}

}
