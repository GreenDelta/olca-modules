package org.openlca.simapro.csv;

import org.junit.Assert;
import org.junit.Test;

public class CsvUtilsTest {

	@Test
	public void testSplitLine() {
		Assert.assertArrayEquals(new String[0], CsvUtils.split(null, null));
		Assert.assertArrayEquals(new String[] { "test" },
				CsvUtils.split("test", null));
		Assert.assertArrayEquals(new String[] { "test,a" },
				CsvUtils.split("test,a", ";"));
		Assert.assertArrayEquals(new String[] { "a", "b", "", "c" },
				CsvUtils.split("a;b;;c", ";"));
	}
}
