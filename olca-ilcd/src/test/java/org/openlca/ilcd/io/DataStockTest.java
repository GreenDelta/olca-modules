package org.openlca.ilcd.io;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXB;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.ilcd.descriptors.DataStock;
import org.openlca.ilcd.descriptors.DataStockList;

public class DataStockTest {

	private DataStockList list;

	@Before
	public void setUp() throws Exception {
		try (InputStream xml = getClass()
				.getResourceAsStream("sapi_sample_datastocks.xml")) {
			list = JAXB.unmarshal(xml, DataStockList.class);
		}
	}

	@Test
	public void testSize() {
		Assert.assertEquals(2, list.dataStocks.size());
	}

	@Test
	public void testAttributes() {
		for (DataStock stock : list.dataStocks) {
			if (stock.root) {
				Assert.assertEquals("8945a2f5-cdda-4ccd-b357-6debb3898ddd",
						stock.uuid);
				Assert.assertEquals("default", stock.shortName);
			} else {
				Assert.assertEquals("600695cc-24da-4f9d-b549-78d0c5e126d0",
						stock.uuid);
				Assert.assertEquals("other", stock.shortName);
			}
		}
	}

	@Test
	public void testIO() {
		StringWriter writer = new StringWriter();
		JAXB.marshal(list, writer);
		StringReader reader = new StringReader(writer.toString());
		list = JAXB.unmarshal(reader, DataStockList.class);
		testSize();
		testAttributes();
	}
}
