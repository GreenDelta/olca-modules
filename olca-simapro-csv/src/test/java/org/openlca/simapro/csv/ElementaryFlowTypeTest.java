package org.openlca.simapro.csv;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.simapro.csv.model.enums.ElementaryFlowType;

public class ElementaryFlowTypeTest {

	//@formatter:off
	private String[] values = {
			"Resources",
			"Emissions to air",
			"Emissions to water",
			"Emissions to soil",
			"Final waste flows",
			"Non material emissions",
			"Social issues",
			"Economic issues"			
	};
	//@formatter:on

	@Test
	public void testEnumSize() {
		Assert.assertEquals(values.length, ElementaryFlowType.values().length);
	}

	@Test
	public void testEnumContents() {
		for (String value : values) {
			ElementaryFlowType type = ElementaryFlowType.forValue(value);
			Assert.assertNotNull(type);
			Assert.assertEquals(value, type.getValue());
		}
	}
}
