package org.openlca.simapro.csv;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.simapro.csv.model.enums.DistributionType;
import org.openlca.simapro.csv.model.process.ProductExchangeRow;

public class ProductExchangeRowTest {

	private CsvConfig config = CsvConfig.getDefault();

	@Test
	public void testFromCsv() {
		String line = "Transport, freight train {RoW}| market for | Alloc Def, U;"
				+ "tkm;0,124620979212666;Lognormal;2,281;0;0;(1,1,4,5,4,na)";
		ProductExchangeRow input = new ProductExchangeRow();
		input.fill(line, config);
		Assert.assertEquals(
				"Transport, freight train {RoW}| market for | Alloc Def, U",
				input.name);
		Assert.assertEquals("tkm", input.unit);
		Assert.assertEquals("0.124620979212666", input.amount);
		Assert.assertEquals(DistributionType.LOG_NORMAL, input.uncertaintyDistribution.getType());
		Assert.assertEquals("(1,1,4,5,4,na)", input.pedigreeUncertainty);
	}

	@Test
	public void testToCsv() {
		ProductExchangeRow input = new ProductExchangeRow();
		input.name = "test name";
		input.unit = "kg";
		Assert.assertTrue(input.toCsv(config).startsWith("test name;kg"));
	}

}
