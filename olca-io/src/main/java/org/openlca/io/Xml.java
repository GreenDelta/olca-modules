package org.openlca.io;

import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Xml {

	/**
	 * We hold the DatatypeFactory instance in a static variable because the
	 * initialization is terribly slow. see: https://stackoverflow.com/q/7346508
	 */
	public static DatatypeFactory datatypeFactory;

	public static XMLGregorianCalendar calendar(long time) {
		return calendar(new Date(time));
	}

	public static XMLGregorianCalendar calendar(Date date) {
		var cal = new GregorianCalendar();
		cal.setTime(date == null ? new Date() : date);
		try {
			if (datatypeFactory == null) {
				datatypeFactory = DatatypeFactory.newInstance();
			}
			return datatypeFactory.newXMLGregorianCalendar(cal);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Xml.class);
			log.warn("Could not create XML Gregorian Calender", e);
			return null;
		}
	}
}
