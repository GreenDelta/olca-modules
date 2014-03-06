package org.openlca.simapro.csv;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.simapro.csv.model.Block;
import org.openlca.simapro.csv.model.refdata.SPUnit;
import org.openlca.simapro.csv.reader.BlockReader;
import org.openlca.simapro.csv.reader.BlockUnmarshaller;

import java.io.StringReader;
import java.util.List;

public class SPUnitTest {

	//@formatter:off
	private String text = "Units\n" +
			"kg;Mass;1;kg\n" +
			"g;Mass;0,001;kg\n" +
			"ton;Mass;1000;kg";
	//@formatter:on

	@Test
	public void testUnmarshallBlock() throws Exception {
		BlockReader reader = new BlockReader(new StringReader(text));
		Block block = reader.read();
		reader.close();
		List<SPUnit> rows = new BlockUnmarshaller().unmarshallRows(block,
				SPUnit.class, ";");
		Assert.assertEquals(3, rows.size());
		Assert.assertEquals("kg", rows.get(0).getName());
		Assert.assertEquals(1000, rows.get(2).getConversionFactor(), 1e-16);
	}

	@Test
	public void testReadLine() {
		String line = "mile;Length;1609,35;m";
		SPUnit unit =  new SPUnit();
		unit.fill(line, ";");
		Assert.assertEquals("mile", unit.getName());
		Assert.assertEquals("Length", unit.getQuantity());
		Assert.assertEquals(1609.35, unit.getConversionFactor(), 1e-16);
		Assert.assertEquals("m", unit.getReferenceUnit());
	}

	@Test
	public void testWriteLine() {
		SPUnit unit = new SPUnit();
		unit.setName("mile");
		unit.setQuantity("Length");
		unit.setConversionFactor(1609.35);
		unit.setReferenceUnit("m");
		Assert.assertEquals("mile;Length;1609.35;m", unit.toLine(";"));
	}

}
