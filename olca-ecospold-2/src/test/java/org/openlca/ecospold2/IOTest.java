package org.openlca.ecospold2;

import java.io.File;

import org.junit.Test;

public class IOTest {

	// TODO: these are currently no tests, we just check if the same
	// content is read and written

	@Test
	public void testDataSetIO() throws Exception {
		DataSet dataSet = EcoSpold2.readDataSet(IOTest.class
				.getResourceAsStream("sample_ecospold2.xml"));
		File file = new File(System.getProperty("java.io.tmpdir")
				+ "/_es2_test_file.xml");
		System.out.println("written to file: " + file);
		EcoSpold2.writeDataSet(dataSet, file);
	}

	@Test
	public void testPersonListIO() throws Exception {
		PersonList list = EcoSpold2.readPersons(IOTest.class
				.getResourceAsStream("Persons.xml"));
		File file = new File(System.getProperty("java.io.tmpdir")
				+ "/_es2_test_persons_file.xml");
		System.out.println("written to file: " + file);
		EcoSpold2.writePersons(list, file);
	}

	@Test
	public void testSourceListIO() throws Exception {
		SourceList list = EcoSpold2.readSources(IOTest.class
				.getResourceAsStream("Sources.xml"));
		File file = new File(System.getProperty("java.io.tmpdir")
				+ "/_es2_test_sources_file.xml");
		System.out.println("written to file: " + file);
		EcoSpold2.writeSources(list, file);
	}

}
