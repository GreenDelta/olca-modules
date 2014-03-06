package org.openlca.simapro.csv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.simapro.csv.model.refdata.Quantity;
import org.openlca.simapro.csv.model.refdata.QuantityBlock;
import org.openlca.simapro.csv.reader.BlockReader;
import org.openlca.simapro.csv.reader.ModelReader;

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
		ModelReader modelReader = new ModelReader(reader,
				CsvConfig.getDefault(), QuantityBlock.class);
		QuantityBlock model = (QuantityBlock) modelReader.read();
		modelReader.close();
		List<Quantity> quantities = model.getQuantities();
		Assert.assertEquals(2, quantities.size());
		Assert.assertEquals("Mass", quantities.get(0).getName());
		Assert.assertEquals("Length", quantities.get(1).getName());
	}

	@Test
	public void testFromLine() {
		String line = "Mass;Yes";
		Quantity quantity = new Quantity();
		quantity.fill(line, CsvConfig.getDefault());
		assertEquals("Mass", quantity.getName());
		assertTrue(quantity.isWithDimension());
	}

	@Test
	public void testToLine() {
		Quantity quantity = new Quantity();
		quantity.setName("Mass");
		quantity.setWithDimension(true);
		assertEquals("Mass;Yes", quantity.toLine(";"));
	}

}
