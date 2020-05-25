package org.openlca.jsonld;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

public class DatesTest {

	@Test
	public void testToString() {
		Date now = new Date();
		String s1 = Dates.toDateTime(now);
		String s2 = Dates.toString(now.getTime());
		Assert.assertTrue(s1.equals(s2));
	}

	@Test
	public void testTime() {
		var now = new Date();
		String s = Dates.toDateTime(now);
		Date now2 = Dates.parse(s);
		Assert.assertTrue(now2.getTime() == now.getTime());
		long time = Dates.parseTime(s);
		Assert.assertTrue(time == now.getTime());
	}
	
	@Test
	public void testDate() {
		var date = new Date();
		var d1 = LocalDate.ofInstant(
				date.toInstant(), ZoneId.systemDefault());
		
		var dateString = Dates.toDate(date);
		LocalDate.parse(dateString);
		
		/*
		var clone = Dates.parse(Dates.toDate(date));
		var d2 = LocalDate.ofInstant(
				clone.toInstant(), ZoneId.systemDefault());
		
		Assert.assertEquals(
				d1.get(ChronoField.DAY_OF_MONTH), 
				d2.get(ChronoField.DAY_OF_MONTH));
		*/
	}

}
