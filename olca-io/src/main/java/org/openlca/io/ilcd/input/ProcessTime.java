package org.openlca.io.ilcd.input;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;

import org.openlca.core.model.Process;
import org.openlca.core.model.Time;
import org.openlca.ilcd.util.LangString;
import org.openlca.ilcd.util.TimeExtension;

/**
 * Converts an ILCD process time to an openLCA process time.
 * 
 * @author Michael Srocka
 * 
 */
class ProcessTime {

	private org.openlca.ilcd.commons.Time ilcdTime;

	public ProcessTime(org.openlca.ilcd.commons.Time ilcdTime) {
		this.ilcdTime = ilcdTime;
	}

	public Time create(Process process) {
		Time time = new Time();
		time.setId(process.getId());
		if (ilcdTime != null) {
			mapValues(time);
		}
		return time;
	}

	private void mapValues(Time time) {
		TimeExtension extension = new TimeExtension(ilcdTime);
		mapStartDate(extension, time);
		mapEndDate(extension, time);
		time.setComment(LangString.getFreeText(ilcdTime.getDescription()));
	}

	private void mapStartDate(TimeExtension extension, Time time) {
		Date startDate = extension.getStartDate();
		if (startDate == null)
			startDate = date(ilcdTime.getReferenceYear());
		time.setStartDate(startDate);
	}

	private void mapEndDate(TimeExtension extension, Time time) {
		Date endDate = extension.getEndDate();
		if (endDate == null)
			endDate = date(ilcdTime.getValidUntil());
		time.setEndDate(endDate);
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
