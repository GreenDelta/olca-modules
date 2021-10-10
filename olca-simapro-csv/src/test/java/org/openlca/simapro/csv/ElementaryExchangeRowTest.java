package org.openlca.simapro.csv;

import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.simapro.csv.model.enums.DistributionType;
import org.openlca.simapro.csv.model.process.ElementaryExchangeRow;

public class ElementaryExchangeRowTest {

	private final CsvConfig config = CsvConfig.getDefault();

	@Test
	public void testFromCsv() {
		String line = "Methane, fossil;high. pop.;kg;0,00011855;Lognormal;"
				+ "2,3802;0;0;(4,5,5,5,5,na)(4,5,na,na,na,na), Estimation";
		ElementaryExchangeRow exchange = new ElementaryExchangeRow();
		exchange.fill(line, config);
		assertEquals("Methane, fossil", exchange.name);
		assertEquals("high. pop.", exchange.subCompartment);
		assertEquals("kg", exchange.unit);
		assertEquals("0.00011855", exchange.amount);
		assertEquals(DistributionType.LOG_NORMAL, exchange.uncertaintyDistribution.getType());
		assertEquals("(4,5,5,5,5,na)", exchange.pedigreeUncertainty);
	}

	@Test
	public void testToCsv() {
		ElementaryExchangeRow exchange = new ElementaryExchangeRow();
		exchange.name = "Methane, fossil";
		exchange.subCompartment = "high. pop.";
		exchange.unit = "kg";
		String line = exchange.toCsv(config);
		Assert.assertTrue(line.startsWith("Methane, fossil;high. pop.;kg;"));
	}

}
