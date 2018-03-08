package org.openlca.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openlca.core.TestProcess;
import org.openlca.core.Tests;

public class CloneProcessTest {

	@Test
	public void testInternalExchangeIDs() {
		Process p1 = TestProcess.refProduct("steel", 1.0, "kg")
				.elemOut("CO2", 2, "kg").get();
		assertEquals(2, p1.lastInternalId);
		for (Exchange e : p1.getExchanges()) {
			assertTrue(e.internalId > 0);
			assertEquals(e, p1.getExchange(e.internalId));
		}
		Process p2 = p1.clone();
		assertEquals(2, p2.lastInternalId);
		for (Exchange e : p2.getExchanges()) {
			assertTrue(e.internalId > 0);
			assertEquals(e, p2.getExchange(e.internalId));
		}
		Tests.delete(p1);
	}

}
