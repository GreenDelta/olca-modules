package spold2;

import java.io.File;

import org.junit.Test;

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
		PersonList list = IO.read(IOTest.class
				.getResourceAsStream("Persons.xml"), PersonList.class);
		File file = new File(System.getProperty("java.io.tmpdir")
				+ "/_es2_test_persons_file.xml");
		System.out.println("written to file: " + file);
		IO.write(list, file);
	}

	@Test
	public void testSourceListIO() throws Exception {
		SourceList list = IO.read(IOTest.class
				.getResourceAsStream("Sources.xml"), SourceList.class);
		File file = new File(System.getProperty("java.io.tmpdir")
				+ "/_es2_test_sources_file.xml");
		System.out.println("written to file: " + file);
		IO.write(list, file);
	}

}
