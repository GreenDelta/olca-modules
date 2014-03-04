package org.openlca.simapro.csv;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.simapro.csv.model.SPProductInput;
import org.openlca.simapro.csv.model.enums.DistributionType;

public class SPProductInputTest {

	@Test
	public void testFromCsv() {
		String line = "Transport, freight train {RoW}| market for | Alloc Def, U;"
				+ "tkm;0,124620979212666;Lognormal;2,281;0;0;(1,1,4,5,4,na)";
		SPProductInput input = SPProductInput.fromCsv(line, ";");
		Assert.assertEquals(
				"Transport, freight train {RoW}| market for | Alloc Def, U",
				input.getName());
		Assert.assertEquals("tkm", input.getUnit());
		Assert.assertEquals("0.124620979212666", input.getAmount());
		Assert.assertEquals(DistributionType.LOG_NORMAL, input
				.getUncertaintyDistribution().getType());
		Assert.assertEquals("(1,1,4,5,4,na)", input.getPedigreeUncertainty());
	}

	@Test
	public void testToCsv() {
		SPProductInput input = new SPProductInput();
		input.setName("test name");
		input.setUnit("kg");
		Assert.assertTrue(input.toCsv(";").startsWith("test name;kg"));
	}

}
