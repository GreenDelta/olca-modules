package org.openlca.ilcd.util;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.List;

import org.junit.Test;
import org.openlca.ilcd.commons.Classification;

public class ClassListTest {

	@Test
	public void testRead() {
		InputStream is = getClass().getResourceAsStream(
				"/org/openlca/ilcd/io/sdk_sample_process.xml");
		List<Classification> list = ClassList.read(is);
		assertEquals(2, list.size());
		assertEquals("ILCD", list.get(0).name);
		assertEquals(2, list.get(0).categories.size());
		assertEquals("classId3", list.get(0).categories.get(1).classId);
		assertEquals("Custom", list.get(1).name);
		assertEquals(2, list.get(1).categories.size());
		assertEquals("classId7", list.get(1).categories.get(1).classId);
		assertEquals("class3", list.get(1).categories.get(1).value);
	}

}
