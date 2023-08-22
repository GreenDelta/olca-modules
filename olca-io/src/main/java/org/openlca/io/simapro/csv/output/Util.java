package org.openlca.io.simapro.csv.output;

import org.openlca.core.model.Uncertainty;

class Util {
	private Util() {
	}

	/**
	 * Converts the given uncertainty into a SimaPro entry. Passing
	 * {@code null} into this function is fine. Note that SimaPro does some
	 * validation checks in the import (e.g. {@code min <= mean <= max}),
	 * so that we have to pass also the mean value into this function.
	 */
	static Object[] uncertainty(double mean, Uncertainty u, double factor) {
		var row = new Object[]{"Undefined", 0, 0, 0};
		if (u == null || u.distributionType == null)
			return row;
		switch (u.distributionType) {
			case LOG_NORMAL -> {
				row[0] = "Lognormal";
				row[1] = u.parameter2 != null
					? Math.pow(u.parameter2, 2)
					: 1;
				return row;
			}
			case NORMAL -> {
				row[0] = "Normal";
				row[1] = u.parameter2 != null
					? 2 * factor * u.parameter2
					: 0;
				return row;
			}
			case TRIANGLE -> {
				var tmin = u.parameter1 == null ? 0 : factor * u.parameter1;
				var tmax = u.parameter3 == null ? 0 : factor * u.parameter3;
				if (tmin > mean || tmax < mean)
					return row;
				row[0] = "Triangle";
				row[2] = tmin;
				row[3] = tmax;
				return row;
			}
			case UNIFORM -> {
				var umin = u.parameter1 == null ? 0 : factor * u.parameter1;
				var umax = u.parameter2 == null ? 0 : factor * u.parameter2;
				if (umin > mean || umax < mean)
					return row;
				row[0] = "Uniform";
				row[2] = umin;
				row[3] = umax;
				return row;
			}
			default -> {
				return row;
			}
		}
	}
}
