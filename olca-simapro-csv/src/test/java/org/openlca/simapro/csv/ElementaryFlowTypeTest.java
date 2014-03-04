package org.openlca.simapro.csv;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.simapro.csv.model.enums.ElementaryFlowType;

public class ElementaryFlowTypeTest {

	//@formatter:off
	private String[] exchangeHeaders = {
			"Resources",
			"Emissions to air",
			"Emissions to water",
			"Emissions to soil",
			"Final waste flows",
			"Non material emissions",
			"Social issues",
			"Economic issues"			
	};
	
	private String[] referenceHeaders = {
			"Raw materials",
			"Airborne emissions",
			"Waterborne emissions",
			"Final waste flows",
			"Emissions to soil",
			"Non material emissions",
			"Social issues",
			"Economic issues"			
	};
	//@formatter:on

	@Test
	public void testEnumSize() {
		Assert.assertEquals(exchangeHeaders.length,
				ElementaryFlowType.values().length);
		Assert.assertEquals(referenceHeaders.length,
				ElementaryFlowType.values().length);
	}

	@Test
	public void testExchangeHeaders() {
		for (String header : exchangeHeaders) {
			ElementaryFlowType type = ElementaryFlowType
					.forExchangeHeader(header);
			Assert.assertNotNull(type);
			Assert.assertEquals(header, type.getExchangeHeader());
		}
	}

	@Test
	public void testReferenceHeaders() {
		for (String header : referenceHeaders) {
			ElementaryFlowType type = ElementaryFlowType
					.forReferenceHeader(header);
			Assert.assertNotNull(type);
			Assert.assertEquals(header, type.getReferenceHeader());
		}
	}
}
