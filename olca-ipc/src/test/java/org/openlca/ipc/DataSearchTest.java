package org.openlca.ipc;

import org.junit.Test;

public class DataSearchTest {

	@Test
	public void testDataSearchMethod() {
		System.out.println("Testing data/search RPC method...");
		Tests.testDataSearch();
		System.out.println("✅ data/search method test completed successfully!");
	}
}
