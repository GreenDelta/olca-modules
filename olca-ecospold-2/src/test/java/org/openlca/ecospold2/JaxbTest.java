package org.openlca.ecospold2;

import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.bind.JAXB;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

@RunWith(Theories.class)
public class JaxbTest {

	@DataPoints
	public static String[] dataSets = { "sample_ecospold2.xml",
			"sample_child_ecospold2.xml" };

	@Test
	public void test() {
		IntermediateExchange e = new IntermediateExchange();
		e.amount = 42d;
		e.outputGroup = 5;
		StringWriter xml = new StringWriter();
		JAXB.marshal(e, xml);
		System.out.println(xml);
	}

	@Theory
	public void testDataSetNotNull(String file) throws Exception {
		DataSet dataSet = read(file);
		Assert.assertNotNull(dataSet);
	}

	private DataSet read(String file) throws Exception {
		InputStream stream = getClass().getResourceAsStream(file);
		EcoSpold2 spold = JAXB.unmarshal(stream, EcoSpold2.class);
		return spold.dataSet != null ? spold.dataSet : spold.childDataSet;
	}
}
