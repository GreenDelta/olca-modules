package org.openlca.ecospold2;

import java.io.File;

import org.junit.Test;

public class IOTest {

	@Test
	public void testIO() throws Exception {
		DataSet dataSet = EcoSpold2.read(IOTest.class
				.getResourceAsStream("sample_ecospold2.xml"));
		File file = new File(System.getProperty("java.io.tmpdir")
				+ "/_es2_test_file.xml");
		System.out.println("written to file: " + file);
		EcoSpold2.write(dataSet, file);
	}

}
