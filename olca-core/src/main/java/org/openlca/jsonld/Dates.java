package org.openlca.jsonld;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import org.openlca.util.Strings;

public class Dates {

	private Dates() {
	}

	public static String toString(Date date) {
		return date.toInstant().toString();
	}

	public static String toDate(Date date) {
		var time = date.toInstant();
        var locDate = LocalDate.ofInstant(
        		time, ZoneId.systemDefault());
		return locDate.toString();
	}

	public static String toString(long time) {
		return Instant.ofEpochMilli(time).toString();
	}

	public static Date parse(String date) {
		if (Strings.nullOrEmpty(date))
			return null;
		var time = Instant.parse(date);
		return new Date(time.toEpochMilli());
	}

	public static long parseTime(String date) {
		Date d = parse(date);
		return d == null ? 0 : d.getTime();
	}

}
