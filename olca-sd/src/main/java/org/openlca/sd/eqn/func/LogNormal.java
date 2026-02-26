package org.openlca.sd.eqn.func;

import java.util.List;

import org.apache.commons.math3.distribution.LogNormalDistribution;
import org.openlca.commons.Res;
import org.openlca.sd.model.Id;
import org.openlca.sd.model.cells.Cell;
import org.openlca.sd.model.cells.NumCell;

public class LogNormal implements Func {

	private final Id name = Id.of("LOGNORMAL");

	@Override
	public Id name() {
		return name;
	}

	@Override
	public Res<Cell> apply(List<Cell> args) {
		if (args == null || args.size() < 2)
			return Res.error("LOGNORMAL requires 2 arguments");
		if (!(args.getFirst() instanceof NumCell(double meanL)))
			return Res.error("first argument is not a number");
		if (!(args.get(1) instanceof NumCell(double sdL)))
			return Res.error("second argument is not a number");

		// given is the mean and standard deviation of the log-normal distribution
		// from this we calculate the standard deviation and mean of the underlying
		// normal distribution
		double sdN = Math.sqrt(Math.log(1 + Math.pow(sdL / meanL, 2)));
		double meanN = Math.log(meanL) - Math.pow(sdN, 2) / 2;

		var value = new LogNormalDistribution(meanN, sdN).sample();
		return Res.ok(Cell.of(value));
	}
}
