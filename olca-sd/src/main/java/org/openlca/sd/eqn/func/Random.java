package org.openlca.sd.eqn.func;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.openlca.commons.Res;
import org.openlca.sd.model.Id;
import org.openlca.sd.model.cells.Cell;
import org.openlca.sd.model.cells.NumCell;

public class Random implements Func {

	private final Id name = Id.of("RANDOM");

	@Override
	public Id name() {
		return name;
	}

	@Override
	public Res<Cell> apply(List<Cell> args) {
		if (args == null || args.size() < 2)
			return Res.error("RANDOM requires 2 arguments");
		if (!(args.getFirst() instanceof NumCell(double min)))
			return Res.error("first argument is not a number");
		if (!(args.get(1) instanceof NumCell(double max)))
			return Res.error("second argument is not a number");
		if (min >= max)
			return Res.error("first argument must be less than second argument");
		var value = ThreadLocalRandom.current().nextDouble(min, max);
		return Res.ok(Cell.of(value));
	}
}
