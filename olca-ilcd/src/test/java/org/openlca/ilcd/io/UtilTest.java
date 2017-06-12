package org.openlca.ilcd.io;

import static org.junit.Assert.assertTrue;

import java.nio.file.Paths;

import org.junit.Test;

public class UtilTest {

	@Test
	public void testIsXml() {
		String[] names = { "abc.xml", "abc.XML", "abc.Xml", "abc", "", "xml", "a/b/c.xml" };
		boolean[] expected = { true, true, true, false, false, false, true };
		for (int i = 0; i < names.length; i++) {
			assertTrue(names[i], expected[i] == Util.isXml(Paths.get(names[i])));
		}
	}

}
