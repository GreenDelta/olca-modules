package org.openlca.sd.eqn.func;

import java.util.List;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.openlca.commons.Res;
import org.openlca.sd.model.Id;
import org.openlca.sd.model.cells.Cell;
import org.openlca.sd.model.cells.NumCell;

public class Normal implements Func {

	private final Id name = Id.of("NORMAL");

	@Override
	public Id name() {
		return name;
	}

	@Override
	public Res<Cell> apply(List<Cell> args) {
		if (args == null || args.size() < 2)
			return Res.error("NORMAL requires 2 arguments");
		if (!(args.getFirst() instanceof NumCell(double mean)))
			return Res.error("first argument is not a number");
		if (!(args.get(1) instanceof NumCell(double sd)))
			return Res.error("second argument is not a number");
		var value = new NormalDistribution(mean, sd).sample();
		return Res.ok(Cell.of(value));
	}
}
