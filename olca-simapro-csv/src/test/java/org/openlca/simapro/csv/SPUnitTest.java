package org.openlca.simapro.csv;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.simapro.csv.model.SPUnit;

public class SPUnitTest {

	@Test
	public void testReadLine() {
		String line = "mile;Length;1609,35;m";
		SPUnit unit = SPUnit.fromLine(line, ";");
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
