package org.openlca.sd.eqn.func;

import java.util.List;

import org.openlca.commons.Res;
import org.openlca.sd.model.Id;
import org.openlca.sd.model.cells.Cell;

public class Log10 implements Func {

	private final Id name = Id.of("LOG10");

	@Override
	public Id name() {
		return name;
	}

	@Override
	public Res<Cell> apply(List<Cell> args) {
		return Fn.withOneArg(args, arg -> {

			if (arg.isNumCell()) {
				double v = arg.asNum();
				if (v <= 0.0) {
					return Res.error(
						"LOG10 domain error: input must be positive, got " + v);
				}
				double result = Math.log10(v);
				return Res.ok(Cell.of(result));
			}

			return arg.isTensorCell()
				? Fn.each(this, arg.asTensorCell())
				: Res.error("LOG10 is not defined for: " + arg);
		});
	}

}
