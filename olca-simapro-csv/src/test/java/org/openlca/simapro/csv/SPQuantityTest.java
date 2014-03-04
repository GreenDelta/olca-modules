package org.openlca.simapro.csv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openlca.simapro.csv.model.SPQuantity;

public class SPQuantityTest {

	@Test
	public void testFromLine() {
		String line = "Mass;Yes";
		SPQuantity quantity = SPQuantity.fromLine(line, ";");
		assertEquals("Mass", quantity.getName());
		assertTrue(quantity.isWithDimension());
	}

	@Test
	public void testToLine() {
		SPQuantity quantity = new SPQuantity();
		quantity.setName("Mass");
		quantity.setWithDimension(true);
		assertEquals("Mass;Yes", quantity.toLine(";"));
	}

}
