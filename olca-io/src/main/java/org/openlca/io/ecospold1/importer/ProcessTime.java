package org.openlca.io.ecospold1.importer;

import java.util.Date;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openlca.core.model.Process;
import org.openlca.core.model.Time;
import org.openlca.ecospold.ITimePeriod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ProcessTime {

	private ITimePeriod timePeriod;
	private Time time;
	private Logger log = LoggerFactory.getLogger(this.getClass());

	public ProcessTime(Process process, ITimePeriod timePeriod) {
		if (process == null)
			time = new Time();
		else
			time = new Time(process);
		this.timePeriod = timePeriod;
	}

	public Time map() {
		if (timePeriod != null) {
			time.setComment(timePeriod.getText());
			time.setStartDate(getStartDate());
			time.setEndDate(getEndDate());
		}
		return time;
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
