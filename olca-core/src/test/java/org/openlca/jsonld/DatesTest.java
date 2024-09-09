package org.openlca.jsonld;

import static org.junit.Assert.*;

import java.time.Instant;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.junit.Test;

import com.google.gson.JsonObject;

public class DatesTest {

	@Test
	public void testWriteRead() {
		var now = Date.from(Instant.now());
		var obj = new JsonObject();
		Json.put(obj, "time", now);
		var date = Json.getDate(obj, "time");
		assertEquals(now, date);
	}

	@Test
	public void testReadOffset() {
		var now = Date.from(Instant.now());
		var obj = new JsonObject();
		var time = OffsetDateTime
				.ofInstant(now.toInstant(), ZoneId.systemDefault())
				.toString();
		obj.addProperty("time", time);
		var date = Json.getDate(obj, "time");
		assertEquals(now, date);
	}

	@Test
	public void testReadInstant() {
		var now = new Date();
		var obj = new JsonObject();
		obj.addProperty("time", now.toInstant().toString());
		var date = Json.getDate(obj, "time");
		assertEquals(now, date);
	}

	@Test
	public void testReadDate() {
		var obj = new JsonObject();
		obj.addProperty("date", "2015-05-23");
		var date = Json.getDate(obj, "date");
		var calendar = new GregorianCalendar();
		assertNotNull(date);
		calendar.setTime(date);
		assertEquals(2015, calendar.get(Calendar.YEAR));
		assertEquals(4, calendar.get(Calendar.MONTH)); // starts with 0!
		assertEquals(23, calendar.get(Calendar.DAY_OF_MONTH));
	}

	@Test
	public void testAsDate() {
		var date = new GregorianCalendar(2022, Calendar.SEPTEMBER, 16).getTime();
		assertEquals("2022-09-16", Json.asDate(date));
	}

	@Test
	public void testAsDateTime() {
		var cal = new GregorianCalendar(2022, Calendar.SEPTEMBER, 16, 15, 50, 20);
		cal.setTimeZone(TimeZone.getTimeZone("UTC"));
		assertEquals("2022-09-16T15:50:20Z", Json.asDateTime(cal.getTime()));
	}

	@Test
	public void testReadDateWithOffset() {
		var obj = new JsonObject();
		obj.addProperty("date", "2015-05-23+02:00");
		var date = Json.getDate(obj, "date");
		var calendar = new GregorianCalendar();
		assertNotNull(date);
		calendar.setTime(date);
		assertEquals(2015, calendar.get(Calendar.YEAR));
		assertEquals(4, calendar.get(Calendar.MONTH)); // starts with 0!
		assertEquals(23, calendar.get(Calendar.DAY_OF_MONTH));
	}

	@Test
	public void testParseDate() {
		var dates = new String[] {
				"2011-12-03",
				"2011-12-03+01:00",
				"2011-12-03T10:15:30",
				"2011-12-03T10:15:30.073876",
				"2011-12-03T10:15:30+01:00",
				"2011-12-03T10:15:30+01:00[Europe/Paris]",
				"2011-12-03T10:15:30Z",
		};
		for (var date : dates) {
			var d = Json.parseDate(date);
			assertNotNull(d);
			var time = d.toInstant()
					.atZone(ZoneId.systemDefault());
			assertEquals(2011, time.getYear());
			assertEquals(Month.DECEMBER, time.getMonth());
			assertEquals(3, time.getDayOfMonth());
		}
	}

	@Test
	public void testParseDateTime() {
		var dates = new String[] {
				"2011-12-03T10:15:30+01:00",
				"2011-12-03T10:15:30.073876+01:00",
				"2011-12-03T10:15:30+01:00[Europe/Paris]",
				"2011-12-03T09:15:30Z",
		};
		for (var date : dates) {
			var d = Json.parseDate(date);
			assertNotNull(d);
			var time = d.toInstant().atZone(ZoneId.of("UTC"));
			assertEquals(9, time.getHour());
			assertEquals(15, time.getMinute());
			assertEquals(30, time.getSecond());
		}
	}

}
