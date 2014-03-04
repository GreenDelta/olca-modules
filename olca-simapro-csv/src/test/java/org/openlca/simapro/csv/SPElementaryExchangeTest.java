package org.openlca.simapro.csv;

import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.simapro.csv.model.SPElementaryExchange;
import org.openlca.simapro.csv.model.enums.DistributionType;

public class SPElementaryExchangeTest {

	@Test
	public void testFromCsv() {
		String line = "Methane, fossil;high. pop.;kg;0,00011855;Lognormal;"
				+ "2,3802;0;0;(4,5,5,5,5,na)(4,5,na,na,na,na), Estimation";
		SPElementaryExchange exchange = SPElementaryExchange.fromCsv(line, ";");
		assertEquals("Methane, fossil", exchange.getName());
		assertEquals("high. pop.", exchange.getSubCompartment());
		assertEquals("kg", exchange.getUnit());
		assertEquals("0.00011855", exchange.getAmount());
		assertEquals(DistributionType.LOG_NORMAL, exchange
				.getUncertaintyDistribution().getType());
		assertEquals("(4,5,5,5,5,na)", exchange.getPedigreeUncertainty());
	}

	@Test
	public void testToCsv() {
		SPElementaryExchange exchange = new SPElementaryExchange();
		exchange.setName("Methane, fossil");
		exchange.setSubCompartment("high. pop.");
		exchange.setUnit("kg");
		String line = exchange.toCsv(";");
		Assert.assertTrue(line.startsWith("Methane, fossil;high. pop.;kg;"));
	}

}
