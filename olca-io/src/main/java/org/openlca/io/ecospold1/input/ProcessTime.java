package org.openlca.io.ecospold1.input;

import java.util.Calendar;
import java.util.Date;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openlca.core.model.doc.ProcessDoc;
import org.openlca.ecospold.model.ITimePeriod;
import org.slf4j.LoggerFactory;

class ProcessTime {

	private final ITimePeriod t;

	public ProcessTime(ITimePeriod t) {
		this.t = t;
	}

	public void map(ProcessDoc doc) {
		if (t != null && doc != null) {
			doc.time = t.getText();
			doc.validFrom = getStartDate();
			doc.validUntil = getEndDate();
		}
	}

	private Date getStartDate() {
		if (t.getStartDate() != null)
			return convert(t.getStartDate());
		if (t.getStartYear() != null)
			return convert(t.getStartYear());
		return t.getStartYearMonth() != null
			? convert(t.getStartYearMonth())
			: null;
	}

	private Date getEndDate() {
		if (t.getEndDate() != null)
			return convert(t.getEndDate());
		else if (t.getEndYear() != null)
			return convertEndYear(t.getEndYear());
		return t.getEndYearMonth() != null
			? convert(t.getEndYearMonth())
			: null;
	}

	private Date convertEndYear(XMLGregorianCalendar cal) {
		try {
			var date = cal.toGregorianCalendar();
			if (cal.getMonth() == DatatypeConstants.FIELD_UNDEFINED
				&& cal.getDay() == DatatypeConstants.FIELD_UNDEFINED) {
				date.set(Calendar.MONTH, Calendar.DECEMBER);
				date.set(Calendar.DAY_OF_MONTH, 31);
				date.set(Calendar.HOUR_OF_DAY, 23);
				date.set(Calendar.MINUTE, 59);
				date.set(Calendar.SECOND, 59);
				date.set(Calendar.MILLISECOND, 999);
			}
			return date.getTime();
		} catch (Exception e) {
			LoggerFactory.getLogger(this.getClass())
				.error("Cannot convert XML end year: {}", cal, e);
			return null;
		}
	}

	private Date convert(XMLGregorianCalendar cal) {
		try {
			return cal.toGregorianCalendar().getTime();
		} catch (Exception e) {
			LoggerFactory.getLogger(this.getClass())
				.error("Cannot convert XML date: {}", cal, e);
			return null;
		}
	}

}
