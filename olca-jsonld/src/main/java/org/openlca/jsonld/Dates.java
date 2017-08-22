package org.openlca.jsonld;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.bind.DatatypeConverter;

public class Dates {

	private Dates() {
	}

	public static String toDateTime(Date date) {
		Calendar c = toCalendar(date);
		if (c == null)
			return null;
		return DatatypeConverter.printDateTime(c);
	}

	public static String toDate(Date date) {
		Calendar c = toCalendar(date);
		if (c == null)
			return null;
		return DatatypeConverter.printDate(c);
	}

	private static Calendar toCalendar(Date date) {
		if (date == null)
			return null;
		Calendar c = GregorianCalendar.getInstance();
		c.setTime(date);
		return c;
	}
	
	public static String toString(long time) {
		Calendar c = GregorianCalendar.getInstance();
		c.setTimeInMillis(time);
		return DatatypeConverter.printDateTime(c);
	}

	public static Date fromString(String date) {
		if (date == null || date.isEmpty())
			return null;
		Calendar cal = DatatypeConverter.parseDateTime(date);
		return cal == null ? null : cal.getTime();

	}

	public static long getTime(String date) {
		Date d = fromString(date);
		return d == null ? 0 : d.getTime();
	}

}
