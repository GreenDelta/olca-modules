package org.openlca.simapro.csv;

import java.io.StringReader;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.simapro.csv.io.BlockReader;
import org.openlca.simapro.csv.io.BlockUnmarshaller;
import org.openlca.simapro.csv.model.Block;
import org.openlca.simapro.csv.model.refdata.UnitBlock;
import org.openlca.simapro.csv.model.refdata.UnitRow;

public class UnitBlockTest {

	private CsvConfig config = CsvConfig.getDefault();

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
		UnitBlock model = new BlockUnmarshaller(config).unmarshall(block,
				UnitBlock.class);
		List<UnitRow> rows = model.getUnits();
		Assert.assertEquals(3, rows.size());
		Assert.assertEquals("kg", rows.get(0).getName());
		Assert.assertEquals(1000, rows.get(2).getConversionFactor(), 1e-16);
	}

	@Test
	public void testReadLine() {
		String line = "mile;Length;1609,35;m";
		UnitRow unit = new UnitRow();
		unit.fill(line, config);
		Assert.assertEquals("mile", unit.getName());
		Assert.assertEquals("Length", unit.getQuantity());
		Assert.assertEquals(1609.35, unit.getConversionFactor(), 1e-16);
		Assert.assertEquals("m", unit.getReferenceUnit());
	}

	@Test
	public void testWriteLine() {
		UnitRow unit = new UnitRow();
		unit.setName("mile");
		unit.setQuantity("Length");
		unit.setConversionFactor(1609.35);
		unit.setReferenceUnit("m");
		Assert.assertEquals("mile;Length;1609.35;m", unit.toLine(config));
	}

}
