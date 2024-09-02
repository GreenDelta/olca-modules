package org.openlca.io.ilcd.input;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import org.openlca.core.model.doc.ProcessDoc;
import org.openlca.ilcd.processes.Time;
import org.openlca.ilcd.util.TimeExtension;

/**
 * Converts an ILCD process time to an openLCA process time.
 */
class ProcessTime {

	private final Time time;
	private final Import imp;

	public ProcessTime(Time time, Import imp) {
		this.time = time;
		this.imp = imp;
	}

	public void map(ProcessDoc documentation) {
		if (time != null) {
			mapValues(documentation);
		}
	}

	private void mapValues(ProcessDoc doc) {
		var ext = new TimeExtension(time);

		var startDate = ext.getStartDate();
		doc.validFrom = startDate != null
				? startDate
				: validFrom(time).orElse(null);

		var endDate = ext.getEndDate();
		doc.validUntil = endDate != null
				? endDate
				: validUntil(time).orElse(null);

		doc.time = imp.str(time.getDescription());
	}

	static Optional<Date> validFrom(Time time) {
		if (time == null)
			return Optional.empty();
		var startYear = time.getReferenceYear();
		if (startYear == null)
			return Optional.empty();
		var c = Calendar.getInstance();
		c.set(Calendar.YEAR, startYear);
		c.set(Calendar.MONTH, Calendar.JANUARY);
		c.set(Calendar.DAY_OF_MONTH, 1);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return Optional.of(c.getTime());
	}

	static Optional<Date> validUntil(Time time) {
		if (time == null)
			return Optional.empty();
		var endYear = time.getValidUntil();
		if (endYear == null)
			return Optional.empty();
		var c = Calendar.getInstance();
		c.set(Calendar.YEAR, endYear);
		c.set(Calendar.MONTH, Calendar.DECEMBER);
		c.set(Calendar.DAY_OF_MONTH, 31);
		c.set(Calendar.HOUR_OF_DAY, 23);
		c.set(Calendar.MINUTE, 59);
		c.set(Calendar.SECOND, 59);
		c.set(Calendar.MILLISECOND, 999);
		return Optional.of(c.getTime());
	}
}
