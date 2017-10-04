package org.openlca.core.matrix.io.olcamat;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

public class TestCsv {

	@Test
	public void testReadLine() throws Exception {
		assertArrayEquals(new String[] { "a", "b", "c" }, Csv.readLine("a,b,c"));
		assertArrayEquals(new String[] { "a", "b", "c" }, Csv.readLine("\"a\",\"b\",\"c\""));
		assertArrayEquals(new String[] { "a", "b", "c" }, Csv.readLine("\"a\",b,\"c\""));
		assertArrayEquals(new String[] { "a", "b", "c" }, Csv.readLine("a,\"b\",c"));
		assertArrayEquals(new String[] { "", "", "" }, Csv.readLine(",,"));
		assertArrayEquals(new String[] { "", "", "" }, Csv.readLine("\"\",\"\",\"\""));
		assertArrayEquals(new String[] { ",", ",", "," }, Csv.readLine("\",\",\",\",\",\""));
	}
}
