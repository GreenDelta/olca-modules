package org.openlca.ilcd.util;

import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.ilcd.commons.DataSetType;

public class SchemaValidatorTest {

	private SchemaValidator validator = new SchemaValidator();

	@Test
	public void testPass() {
		DataSetType[] types = {
				DataSetType.LCIA_METHOD,
				DataSetType.PROCESS,
				DataSetType.FLOW,
				DataSetType.FLOW_PROPERTY,
				DataSetType.SOURCE,
				DataSetType.CONTACT,
				DataSetType.UNIT_GROUP };
		String[] dataSets = {
				"sdk_sample_lciamethod.xml",
				"sdk_sample_process.xml",
				"sdk_sample_flow.xml",
				"sdk_sample_flowproperty.xml",
				"sdk_sample_source.xml",
				"sdk_sample_contact.xml",
				"sdk_sample_unitgroup.xml" };
		for (int i = 0; i < types.length; i++) {
			String url = "/org/openlca/ilcd/io/" + dataSets[i];
			InputStream stream = getClass().getResourceAsStream(url);
			boolean valid = validator.isValid(stream, types[i]);
			Assert.assertTrue("Validation failed: " + url, valid);
		}
	}

	@Test
	public void testFail() {
		String url = "/org/openlca/ilcd/io/sdk_sample_flow.xml";
		InputStream stream = getClass().getResourceAsStream(url);
		boolean valid = validator.isValid(stream, DataSetType.PROCESS);
		Assert.assertFalse(valid);
	}

}
