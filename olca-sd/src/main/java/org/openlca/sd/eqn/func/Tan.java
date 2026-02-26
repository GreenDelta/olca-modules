package org.openlca.sd.eqn.func;

import java.util.List;

import org.openlca.commons.Res;
import org.openlca.sd.model.Id;
import org.openlca.sd.model.cells.Cell;

public class Tan implements Func {

	private final Id name = Id.of("TAN");

	@Override
	public Id name() {
		return name;
	}

	@Override
	public Res<Cell> apply(List<Cell> args) {
		return Fn.withOneArg(args, arg -> {
			if (arg.isNumCell()) {
				double result = Math.tan(arg.asNum());
				return Res.ok(Cell.of(result));
			}
			return arg.isTensorCell()
				? Fn.each(this, arg.asTensorCell())
				: Res.error("TAN is not defined for: " + arg);
		});
	}

}
