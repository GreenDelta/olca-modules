package org.openlca.sd.eqn.func;

import java.util.List;

import org.openlca.commons.Res;
import org.openlca.sd.model.Id;
import org.openlca.sd.model.cells.Cell;

public class Pow implements Func {

	private final Id name = Id.of("POW");

	public static Res<Cell> apply(Cell a, Cell b) {
		return new Pow().apply(List.of(a, b));
	}

	@Override
	public Id name() {
		return name;
	}

	@Override
	public Res<Cell> apply(List<Cell> args) {
		return Fn.withTwoArgs(args, (a, b) -> {
			if (a.isNumCell() && b.isNumCell()) {
				double r = Math.pow(a.asNum(), b.asNum());
				return Res.ok(Cell.of(r));
			}
			return Res.error("pow is not defined for: " + a + "^" + b);
		});
	}
}
