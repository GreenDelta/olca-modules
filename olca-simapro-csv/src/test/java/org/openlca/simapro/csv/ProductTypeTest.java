package org.openlca.simapro.csv;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.simapro.csv.model.enums.ProductType;

public class ProductTypeTest {

	//@formatter:off
	private String[] headers = {
			"Avoided products",
			"Materials/fuels",
			"Electricity/heat",
			"Waste to treatment"
	};
	//@formatter:on

	@Test
	public void testEnumSize() {
		Assert.assertEquals(headers.length, ProductType.values().length);
	}

	@Test
	public void testHeaders() {
		for (String header : headers) {
			ProductType type = ProductType.forHeader(header);
			Assert.assertNotNull(type);
			Assert.assertEquals(header, type.getHeader());
		}
	}

}
