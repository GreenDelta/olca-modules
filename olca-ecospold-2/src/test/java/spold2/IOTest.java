package spold2;

import java.io.File;

import org.junit.Test;

import spold2.DataSet;
import spold2.EcoSpold2;
import spold2.PersonList;
import spold2.SourceList;

public class IOTest {

	// TODO: these are currently no tests, we just check if the same
	// content is read and written

	@Test
	public void testDataSetIO() throws Exception {
		DataSet dataSet = EcoSpold2.read(IOTest.class
				.getResourceAsStream("sample_ecospold2.xml"));
		File file = new File(System.getProperty("java.io.tmpdir")
				+ "/_es2_test_file.xml");
		System.out.println("written to file: " + file);
		EcoSpold2.write(dataSet, file);
	}

	@Test
	public void testPersonListIO() throws Exception {
		PersonList list = PersonList.read(IOTest.class
				.getResourceAsStream("Persons.xml"));
		File file = new File(System.getProperty("java.io.tmpdir")
				+ "/_es2_test_persons_file.xml");
		System.out.println("written to file: " + file);
		PersonList.write(list, file);
	}

	@Test
	public void testSourceListIO() throws Exception {
		SourceList list = SourceList.read(IOTest.class
				.getResourceAsStream("Sources.xml"));
		File file = new File(System.getProperty("java.io.tmpdir")
				+ "/_es2_test_sources_file.xml");
		System.out.println("written to file: " + file);
		SourceList.write(list, file);
	}

}
