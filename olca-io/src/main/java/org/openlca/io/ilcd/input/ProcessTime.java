package org.openlca.io.ilcd.input;

import org.openlca.core.model.doc.ProcessDoc;
import org.openlca.ilcd.commons.Time;
import org.openlca.ilcd.util.TimeExtension;

import java.util.Calendar;
import java.util.Date;

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
		TimeExtension extension = new TimeExtension(time);
		mapStartDate(extension, doc);
		mapEndDate(extension, doc);
		doc.time = imp.str(time.getDescription());
	}

	private void mapStartDate(TimeExtension extension, ProcessDoc doc) {
		Date startDate = extension.getStartDate();
		if (startDate == null)
			startDate = date(time.getReferenceYear());
		doc.validFrom = startDate;
	}

	private void mapEndDate(TimeExtension extension, ProcessDoc doc) {
		Date endDate = extension.getEndDate();
		if (endDate == null)
			endDate = date(time.getValidUntil());
		doc.validUntil = endDate;
	}

	private Date date(Integer bigInt) {
		if (bigInt == null)
			return null;
		Calendar calendar = Calendar.getInstance();
		calendar.clear();
		calendar.set(Calendar.YEAR, bigInt);
		return calendar.getTime();
	}

}
