package org.openlca.io.ilcd.input;

import org.openlca.core.model.ProcessDocumentation;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.util.TimeExtension;

import java.util.Calendar;
import java.util.Date;

/**
 * Converts an ILCD process time to an openLCA process time.
 */
class ProcessTime {

	private org.openlca.ilcd.commons.Time ilcdTime;
	private ImportConfig config;

	public ProcessTime(org.openlca.ilcd.commons.Time ilcdTime,
			ImportConfig config) {
		this.ilcdTime = ilcdTime;
		this.config = config;
	}

	public void map(ProcessDocumentation documentation) {
		if (ilcdTime != null) {
			mapValues(documentation);
		}
	}

	private void mapValues(ProcessDocumentation doc) {
		TimeExtension extension = new TimeExtension(ilcdTime);
		mapStartDate(extension, doc);
		mapEndDate(extension, doc);
		doc.time = LangString.getFirst(ilcdTime.description, config.langs);
	}

	private void mapStartDate(TimeExtension extension, ProcessDocumentation doc) {
		Date startDate = extension.getStartDate();
		if (startDate == null)
			startDate = date(ilcdTime.referenceYear);
		doc.validFrom = startDate;
	}

	private void mapEndDate(TimeExtension extension, ProcessDocumentation doc) {
		Date endDate = extension.getEndDate();
		if (endDate == null)
			endDate = date(ilcdTime.validUntil);
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
