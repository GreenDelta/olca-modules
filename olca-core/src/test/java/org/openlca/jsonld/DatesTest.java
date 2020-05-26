package org.openlca.jsonld;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.google.gson.JsonObject;
import org.junit.Assert;
import org.junit.Test;

public class DatesTest {

	@Test
	public void testWriteRead() {
		var now = Date.from(Instant.now());
		var obj = new JsonObject();
		Json.put(obj, "time", now);
		var date = Json.getDate(obj, "time");
		Assert.assertEquals(now, date);
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
		Assert.assertEquals(now, date);
	}

	@Test
	public void testReadInstant() {
		var now = new Date();
		var obj = new JsonObject();
		obj.addProperty("time", now.toInstant().toString());
		var date = Json.getDate(obj, "time");
		Assert.assertEquals(now, date);
	}

	@Test
	public void testReadDate() {
		var obj = new JsonObject();
		obj.addProperty("date", "2015-05-23");
		var date = Json.getDate(obj, "date");
		var calendar = new GregorianCalendar();
		Assert.assertNotNull(date);
		calendar.setTime(date);
		Assert.assertEquals(2015, calendar.get(Calendar.YEAR));
		Assert.assertEquals(4, calendar.get(Calendar.MONTH)); // starts with 0!
		Assert.assertEquals(23, calendar.get(Calendar.DAY_OF_MONTH));
	}

	@Test
	public void testReadDateWithOffset() {
		var obj = new JsonObject();
		obj.addProperty("date", "2015-05-23+02:00");
		var date = Json.getDate(obj, "date");
		var calendar = new GregorianCalendar();
		Assert.assertNotNull(date);
		calendar.setTime(date);
		Assert.assertEquals(2015, calendar.get(Calendar.YEAR));
		Assert.assertEquals(4, calendar.get(Calendar.MONTH)); // starts with 0!
		Assert.assertEquals(23, calendar.get(Calendar.DAY_OF_MONTH));
	}

}
