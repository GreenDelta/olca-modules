package org.openlca.jsonld;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

public class DatesTest {

	@Test
	public void testToString() {
		Date now = new Date();
		String s1 = Dates.toString(now);
		String s2 = Dates.toString(now.getTime());
		Assert.assertTrue(s1.equals(s2));
	}

	@Test
	public void testFromString() {
		Date now = new Date();
		String s = Dates.toString(now);
		Date now2 = Dates.fromString(s);
		Assert.assertTrue(now2.getTime() == now.getTime());
		long time = Dates.getTime(s);
		Assert.assertTrue(time == now.getTime());
	}

}
