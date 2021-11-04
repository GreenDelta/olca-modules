package org.openlca.io.simapro.csv.input;

import org.openlca.core.model.Uncertainty;
import org.openlca.simapro.csv.UncertaintyRecord;

final class Uncertainties {

	private Uncertainties() {
	}

	public static Uncertainty of(double mean, UncertaintyRecord record) {
		if (record == null || record.isUndefined())
			return null;

		if (record.isLogNormal()) {
			var log = record.getAsLogNormal();
			return Uncertainty.logNormal(mean, Math.sqrt(log.xsd()));
		}

		if (record.isNormal()) {
			var norm = record.getAsNormal();
			return Uncertainty.normal(mean, 0.5 * norm.xsd());
		}

		if (record.isTriangle()) {
			var tri = record.getAsTriangle();
			return Uncertainty.triangle(tri.min(), mean, tri.max());
		}

		if (record.isUniform()) {
			var uni = record.getAsUniform();
			return Uncertainty.uniform(uni.min(), uni.max());
		}

		return null;
	}

}
