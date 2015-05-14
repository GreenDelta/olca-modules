package org.openlca.io.ecospold1.input;

import java.util.Date;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openlca.core.model.ProcessDocumentation;
import org.openlca.ecospold.ITimePeriod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ProcessTime {

	private ITimePeriod timePeriod;
	private Logger log = LoggerFactory.getLogger(this.getClass());

	public ProcessTime(ITimePeriod timePeriod) {
		this.timePeriod = timePeriod;
	}

	public void map(ProcessDocumentation doc) {
		if (timePeriod != null && doc != null) {
			doc.setTime(timePeriod.getText());
			doc.setValidFrom(getStartDate());
			doc.setValidUntil(getEndDate());
		}
	}

	private Date getStartDate() {
		if (timePeriod.getStartDate() != null)
			return convert(timePeriod.getStartDate());
		else if (timePeriod.getStartYear() != null)
			return convert(timePeriod.getStartYear());
		else
			return null;
	}

	private Date getEndDate() {
		if (timePeriod.getEndDate() != null)
			return convert(timePeriod.getEndDate());
		else if (timePeriod.getEndYear() != null)
			return convert(timePeriod.getEndYear());
		else
			return null;
	}

	private Date convert(XMLGregorianCalendar cal) {
		try {
			Date date = cal.toGregorianCalendar().getTime();
			return date;
		} catch (Exception e) {
			log.error("Cannot convert XML date: " + cal, e);
			return null;
		}
	}

}
