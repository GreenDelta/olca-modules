package org.openlca.simapro.csv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.simapro.csv.model.Block;
import org.openlca.simapro.csv.model.SPQuantity;
import org.openlca.simapro.csv.reader.BlockReader;
import org.openlca.simapro.csv.reader.BlockUnmarshaller;

public class SPQuantityTest {

	//@formatter:off
	private String text = "" +
			"Quantities\n" +
			"Mass;Yes\n" +
			"Length;Yes\n" +
			"\n" +
			"End";
	//@formatter:on

	@Test
	public void testUnmarshallBlock() throws Exception {
		BlockReader reader = new BlockReader(new StringReader(text));
		Block block = reader.read();
		reader.close();
		List<SPQuantity> quantities = new BlockUnmarshaller().unmarshallRows(
				block, SPQuantity.class, ";");
		Assert.assertEquals(2, quantities.size());
		Assert.assertEquals("Mass", quantities.get(0).getName());
		Assert.assertEquals("Length", quantities.get(1).getName());
	}

	@Test
	public void testFromLine() {
		String line = "Mass;Yes";
		SPQuantity quantity = new SPQuantity();
		quantity.fill(line, ";");
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
