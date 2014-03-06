package org.openlca.simapro.csv;

import org.junit.Assert;
import org.junit.Test;

public class CsvUtilsTest {

	private CsvConfig config = CsvConfig.getDefault();

	@Test
	public void testSplitLine() {
		Assert.assertArrayEquals(new String[0], CsvUtils.split(null, null));
		Assert.assertArrayEquals(new String[] { "test" },
				CsvUtils.split("test", null));
		Assert.assertArrayEquals(new String[] { "test,a" },
				CsvUtils.split("test,a", config));
		Assert.assertArrayEquals(new String[] { "a", "b", "", "c" },
				CsvUtils.split("a;b;;c", config));
	}

	@Test
	public void testPedigreeUncertainty() {
		Assert.assertEquals(null, CsvUtils.getPedigreeUncertainty(null));
		Assert.assertEquals(null, CsvUtils.getPedigreeUncertainty("(1;58,34)"));
		Assert.assertEquals("(3,5,5,3,2,na)",
				CsvUtils.getPedigreeUncertainty("(3,5,5,3,2,na), Literature"));
		Assert.assertEquals(
				"(3, 5,	5 ,3,2,  na)",
				CsvUtils.getPedigreeUncertainty("uncertainty = (3, 5,	5 ,3,2,  na) , Literature"));
		Assert.assertEquals(
				"(na,na,na,na,na,na)",
				CsvUtils.getPedigreeUncertainty("uncertainty = (na,na,na,na,na,na) , Literature"));
	}
}
