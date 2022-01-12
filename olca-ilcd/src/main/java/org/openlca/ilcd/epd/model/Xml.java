package org.openlca.ilcd.epd.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Xml {

	private Xml() {
	}

	public static XMLGregorianCalendar now() {
		try {
			Date date = new Date();
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTime(date);
			return DatatypeFactory.newInstance()
				.newXMLGregorianCalendar(cal);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Xml.class);
			log.error("failed to create XMLGregorianCalendar", e);
			return null;
		}
	}

	public static String toString(XMLGregorianCalendar cal) {
		if (cal == null)
			return "";
		try {
			Date time = cal.toGregorianCalendar().getTime();
			SimpleDateFormat format = new SimpleDateFormat(
					"yyyy-MM-dd'T'HH:mm:ssZ");
			return format.format(time);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Xml.class);
			log.error("failed to convert " + cal, e);
			return "";
		}
	}
}
