package org.openlca.ilcd.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Date;

import javax.xml.bind.JAXB;

import org.junit.Test;
import org.openlca.ilcd.commons.Time;
import org.openlca.ilcd.util.TimeExtension;

public class TimeExtensionTest {

	@Test
	public void testEmpty() {
		Time time = io(new Time());
		time.referenceYear = 1979;
		TimeExtension extension = new TimeExtension(time);
		assertFalse(extension.isValid());
		assertNull(extension.getStartDate());
		assertNull(extension.getEndDate());
	}

	@Test
	public void testGetStartDate() {
		Date date = new Date();
		Time time = io(createTime(date));
		TimeExtension extension = new TimeExtension(time);
		assertTrue(extension.isValid());
		assertEquals(date, extension.getStartDate());
	}

	@Test
	public void testGetEndDate() {
		Date date = new Date();
		Time time = io(createTime(date));
		TimeExtension extension = new TimeExtension(time);
		assertTrue(extension.isValid());
		assertEquals(date, extension.getEndDate());
	}

	private Time createTime(Date date) {
		Time time = io(new Time());
		time.referenceYear = 1979;
		TimeExtension extension = new TimeExtension(time);
		extension.setStartDate(date);
		extension.setEndDate(date);
		return time;
	}

	private Time io(Time time) {
		StringWriter writer = new StringWriter();
		JAXB.marshal(time, writer);
		writer.flush();
		String xml = writer.toString();
		StringReader reader = new StringReader(xml);
		return JAXB.unmarshal(reader, Time.class);
	}

}
