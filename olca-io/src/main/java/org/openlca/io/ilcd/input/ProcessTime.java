package org.openlca.io.ilcd.input;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;

import org.openlca.core.model.ProcessDocumentation;
import org.openlca.ilcd.util.LangString;
import org.openlca.ilcd.util.TimeExtension;

/**
 * Converts an ILCD process time to an openLCA process time.
 */
class ProcessTime {

	private org.openlca.ilcd.commons.Time ilcdTime;

	public ProcessTime(org.openlca.ilcd.commons.Time ilcdTime) {
		this.ilcdTime = ilcdTime;
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
		doc.setTime(LangString.get(ilcdTime.getDescription()));
	}

	private void mapStartDate(TimeExtension extension, ProcessDocumentation doc) {
		Date startDate = extension.getStartDate();
		if (startDate == null)
			startDate = date(ilcdTime.getReferenceYear());
		doc.setValidFrom(startDate);
	}

	private void mapEndDate(TimeExtension extension, ProcessDocumentation doc) {
		Date endDate = extension.getEndDate();
		if (endDate == null)
			endDate = date(ilcdTime.getValidUntil());
		doc.setValidUntil(endDate);
	}

	private Date date(BigInteger bigInt) {
		if (bigInt == null)
			return null;
		int year = bigInt.intValue();
		Calendar calendar = Calendar.getInstance();
		calendar.clear();
		calendar.set(Calendar.YEAR, year);
		return calendar.getTime();
	}

}
