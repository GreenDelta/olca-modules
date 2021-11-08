package org.openlca.io.simapro.csv.input;

import org.openlca.core.model.Uncertainty;
import org.openlca.simapro.csv.UncertaintyRecord;
import org.openlca.simapro.csv.process.ElementaryExchangeRow;
import org.openlca.simapro.csv.process.ExchangeRow;
import org.openlca.simapro.csv.process.TechExchangeRow;

final class Uncertainties {

	private Uncertainties() {
	}

	static Uncertainty of(ExchangeRow row) {
		if (row == null || row.amount() == null)
			return null;
		UncertaintyRecord ur = null;
		if (row instanceof ElementaryExchangeRow e) {
			ur = e.uncertainty();
		} else if (row instanceof TechExchangeRow t) {
			ur = t.uncertainty();
		}
		if (ur == null)
			return null;
		return of(row.amount().value(), ur);
	}

	static Uncertainty of(double mean, UncertaintyRecord record) {
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
