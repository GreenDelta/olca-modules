package org.openlca.ilcd.tests.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.openlca.ilcd.descriptors.CategorySystemList;
import org.openlca.ilcd.descriptors.DataStock;
import org.openlca.ilcd.descriptors.DataStockList;
import org.openlca.ilcd.io.SodaClient;
import org.openlca.ilcd.io.SodaConnection;
import org.openlca.ilcd.lists.CategoryList;
import org.openlca.ilcd.lists.CategorySystem;
import org.openlca.ilcd.lists.ContentType;

public class StocksAndCategoryListsTest {

	@Test
	@Ignore
	public void testGetDataStocks() throws Exception {
		SodaConnection con = new SodaConnection();
		con.url = "http://oekobaudat.online-now.de/OEKOBAU.DAT/resource";
		int numberOfStocks = 3;
		String shortName = "default";
		SodaClient client = new SodaClient(con);
		client.connect();
		DataStockList list = client.getDataStockList();
		assertEquals(numberOfStocks, list.dataStocks.size());
		boolean found = false;
		for (DataStock dataStock : list.dataStocks) {
			String sn = dataStock.shortName;
			if (shortName.equals(sn)) {
				found = true;
				break;
			}
		}
		assertTrue("Could not find data stock " + shortName, found);
		client.close();
	}

	@Test
	@Ignore
	public void testGetCategorySystems() throws Exception {
		SodaConnection con = new SodaConnection();
		con.url = "http://www.oekobaudat.de/OEKOBAU.DAT/resource";
		try (SodaClient client = new SodaClient(con)) {
			CategorySystemList list = client.getCategorySystemList();
			List<String> names = list.getNames();
			assertEquals(1, names.size());
			assertTrue(names.contains("OEKOBAU.DAT"));
		}
	}

	@Test
	@Ignore
	public void testGetCategorySystem() throws Exception {
		SodaConnection con = new SodaConnection();
		con.url = "http://www.oekobaudat.de/OEKOBAU.DAT/resource";
		try (SodaClient client = new SodaClient(con)) {
			CategorySystem system = client.getCategorySystem("OEKOBAU.DAT");
			boolean processTypeFound = false;
			for (CategoryList list : system.categories) {
				if (list.type == ContentType.PROCESS) {
					processTypeFound = true;
					assertTrue(list.categories.size() > 5);
					processTypeFound = true;
				}
			}
			assertTrue(processTypeFound);
		}
	}

}
