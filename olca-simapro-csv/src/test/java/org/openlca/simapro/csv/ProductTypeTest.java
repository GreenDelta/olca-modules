package org.openlca.simapro.csv;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.simapro.csv.model.enums.ProductFlowType;

public class ProductTypeTest {

	//@formatter:off
	private String[] headers = {
			"Products",
			"Avoided products",
			"Materials/fuels",
			"Electricity/heat",
			"Waste to treatment"
	};
	//@formatter:on

	@Test
	public void testEnumSize() {
		Assert.assertEquals(headers.length, ProductFlowType.values().length);
	}

	@Test
	public void testHeaders() {
		for (String header : headers) {
			ProductFlowType type = ProductFlowType.forHeader(header);
			Assert.assertNotNull(type);
			Assert.assertEquals(header, type.getHeader());
		}
	}

}
